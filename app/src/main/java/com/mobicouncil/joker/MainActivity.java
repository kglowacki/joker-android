package com.mobicouncil.joker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cunoraz.tagview.TagView;
import com.firebase.ui.auth.AuthUI;
import com.mobicouncil.joker.adapter.JokeAdapter;
import com.mobicouncil.joker.adapter.JokeService;
import com.mobicouncil.joker.model.Joke;
import com.mobicouncil.joker.adapter.TagViewAdapter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements
        JokeAdapter.OnJokeSelectedListener {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 9001;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recycler_jokes)
    RecyclerView mJokesRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    @BindView(R.id.user_avatar)
    ImageView mUserAvatarView;

    @BindView(R.id.user_name)
    TextView mUserNameView;

    @BindView(R.id.user_email)
    TextView mUserEmailView;

    @BindView(R.id.user_info_panel)
    ViewGroup mUserInfoPanelView;

    @BindView(R.id.user_signin_panel)
    ViewGroup mUserSignInPanelView;

    @BindView(R.id.progress_loading)
    ProgressBar mProgressBarView;

    @BindView(R.id.tag_group)
    TagView tagView;

    private JokeAdapter mAdapter;

    private JokeService jokeService;
    private List<Disposable> disposables = new LinkedList<>();
    private TagViewAdapter mTagViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jokeService = new JokeService();

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        //jokes
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        final EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                jokeService.loadMore();
            }
        };

        // RecyclerView
        mAdapter = new JokeAdapter(this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    scrollListener.resetState();
                    mJokesRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mJokesRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };


        mJokesRecycler.setLayoutManager(linearLayoutManager);
        mJokesRecycler.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        mJokesRecycler.addOnScrollListener(scrollListener);
        mJokesRecycler.setAdapter(mAdapter);

        //tags
        mTagViewAdapter = new TagViewAdapter(tagView, tagId -> jokeService.toggleTag(tagId));
    }

    @Override
    public void onStart() {
        super.onStart();

        jokeService.start(); //?

        disposables.add(jokeService.loading.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isLoading-> mProgressBarView.setVisibility(isLoading ? View.VISIBLE : View.GONE)));

        disposables.add(jokeService.errors.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(err-> Snackbar.make(findViewById(android.R.id.content), err.getMessage(), Snackbar.LENGTH_LONG).show()));

        disposables.add(jokeService.jokes.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event->{
                    if (event.isAppend()) {
                        mAdapter.addAllItems(event.getJokes());
                    } else {
                        mAdapter.setItems(event.getJokes());
                    }
                }));

        disposables.add(jokeService.tags.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tags->{
                    updateTags();
                }));

        disposables.add(jokeService.auth.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(auth->{
                    Log.i(TAG, "AUTH="+auth.toString());
                    updateTags();
                    if (auth.isSignedIn()) {
                        if (auth.getUser().getPhotoUrl() != null) {
                            Glide.with(mUserAvatarView.getContext())
                                    .load(auth.getUser().getPhotoUrl())
                                    .into(mUserAvatarView);
                        }
                        mUserNameView.setText(auth.getUser().getDisplayName());
                        mUserEmailView.setText(auth.getUser().getEmail());
                        mUserInfoPanelView.setVisibility(View.VISIBLE);
                        mUserSignInPanelView.setVisibility(View.GONE);
                    } else {
                        mUserInfoPanelView.setVisibility(View.GONE);
                        mUserSignInPanelView.setVisibility(View.VISIBLE);
                    }
                }));
    }

    private void updateTags() {


        mTagViewAdapter.setTags(jokeService.tags.getValue(),
                jokeService.auth.getValue().getProfile().getFilter().selectedTags());


//
//        tagView.removeAll();
//        tagView.addTags(jokeService.tags.getValue().stream()
//                .map(tag -> {
//                    com.cunoraz.tagview.Tag tagView = new com.cunoraz.tagview.Tag(tag.getId());
//                    tagView.tagTextColor = jokeService.auth.getValue().getProfile().getFilter().isTagSelected(tag.getId())? Color.BLACK:Color.RED;
//                    return tagView;
//                })
//                .collect(Collectors.toList()));
    }

    @Override
    public void onStop() {
        super.onStop();

        disposables.forEach(Disposable::dispose);
        disposables.clear();

        if (jokeService != null) {
            jokeService.stop();
        }

    }

    @Override
    public void onJokeSelected(Joke joke) {
        // Go to the details page for the selected restaurant
        Intent intent = new Intent(this, JokeDetailActivity.class);
        intent.putExtra(JokeDetailActivity.KEY_JOKE_ID, joke.getId());

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @OnClick(R.id.user_sign_out)
    public void signOut() {
        jokeService.signOut();
    }

    @OnClick(R.id.user_sign_in)
    public void startSignIn() {
        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                        ,new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()
                ))
                .setIsSmartLockEnabled(false)
                //TODO set logo and theme
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
    }

}

