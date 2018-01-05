package com.mobicouncil.joker.adapter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mobicouncil.joker.model.Auth;
import com.mobicouncil.joker.model.Joke;
import com.mobicouncil.joker.model.Tag;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.subjects.BehaviorSubject;


/**
 * Created by kris on 30/12/2017.
 */
public class JokeService {

    public static class JokesLoadedEvent {
        private final List<Joke> jokes;
        private final boolean append;

        public JokesLoadedEvent(List<Joke> jokes, boolean append) {
            this.jokes = jokes;
            this.append = append;
        }

        public List<Joke> getJokes() {
            return jokes;
        }

        public boolean isAppend() {
            return append;
        }
    }

    public static class JokesErrorEvent {

        private final String message;

        public JokesErrorEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private static final String TAG = "Service";

    public final BehaviorSubject<List<Tag>> tags = BehaviorSubject.createDefault((List<Tag>)new LinkedList<Tag>());
    public final BehaviorSubject<JokesLoadedEvent> jokes = BehaviorSubject.create();
    private long lastJokeTs;
    public final BehaviorSubject<Auth> auth = BehaviorSubject.createDefault(Auth.signedOut());
    public final BehaviorSubject<Boolean> loading = BehaviorSubject.createDefault(false);
    public final BehaviorSubject<JokesErrorEvent> errors = BehaviorSubject.create();

    private final FirebaseAuth.AuthStateListener authStateListener = this::onAuthChanged;
    private ListenerRegistration userProfileSub;

    public JokeService() {
        FirebaseFirestore.setLoggingEnabled(true);
    }

    public void start() {
        //loadJokes(false);
        loadTags();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        auth.subscribe(v->loadJokes(false)); //TODO unsub on stop
    }

    private void onAuthChanged(final FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getUid() != null) {
            DocumentReference profileRef = FirebaseFirestore.getInstance().collection("users").document(firebaseAuth.getUid());
            profileRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Auth.Profile profile = documentSnapshot.toObject(Auth.Profile.class);
                        auth.onNext(Auth.create(firebaseAuth.getCurrentUser(), profile));
                    } else {
                        profileRef.set(auth.getValue().getProfile());
                    }
                    updateTokenIfNeeded(); //TODO how to handle re-auth (two users on the same device)?
                } else {
                    onError("cannot load user profile", task.getException());
                }
            });
        } else {
            auth.onNext(auth.getValue().withUser(null));
        }
    }

    private void updateTokenIfNeeded() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && !auth.getValue().getProfile().getNotificationTokens().getOrDefault(token, false)) {
            Log.d(TAG, "persist ID token: " + token);
            auth.getValue().getProfile().getNotificationTokens().put(token, true);
            persistProfile();
        }
    }

    private void unsubUserProfile() {
        if (userProfileSub != null) {
            userProfileSub.remove();
            userProfileSub = null;
        }
    }

    public void stop() {
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        unsubUserProfile();
    }

    public void loadMore() {
        this.loadJokes(true);
    }

    private void loadJokes(final boolean more) {
        Query query = FirebaseFirestore.getInstance().collection("jokes");
        for (String tag : this.auth.getValue().getProfile().getFilter().selectedTags()) {
            query = query.whereEqualTo("tags."+tag, true);
        }
        query = query.orderBy("ts", Query.Direction.DESCENDING)
                .limit(10);

        if (more) {
            query = query.startAfter(lastJokeTs);
        }
        loading.onNext(true);
        query.get().addOnCompleteListener(task -> {
            loading.onNext(false);
            if (task.isSuccessful()) {
                List<Joke> result = task.getResult().getDocuments().stream()
                        .map(documentSnapshot -> {
                            Joke joke = documentSnapshot.toObject(Joke.class);
                            joke.setText(joke.getText().trim());
                            joke.setId(documentSnapshot.getId());
                            return joke;
                        })
                        .collect(Collectors.toList());
                lastJokeTs = result.isEmpty() ? -1 : result.get(result.size()-1).getTs();
                jokes.onNext(new JokesLoadedEvent(result, more));
            } else {
                onError("cannot load jokes", task.getException());
            }
        });
    }

    private void onError(String msg, Exception e) {
        Log.e(TAG, msg, e);
        this.errors.onNext(new JokesErrorEvent(msg));
    }

    private void loadTags() {
        FirebaseFirestore.getInstance().collection("tags")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tags.onNext(task.getResult().getDocuments().stream().map(documentSnapshot -> {

                    //documentSnapshot.getData().entrySet().stream().filter(e->e.getValue() instanceof String).collect(Collectors.toMap());
                    Map<String,String> labels = documentSnapshot.getData().entrySet().stream()
                            .filter(e->e.getValue() instanceof String).collect(Collectors.toMap(Map.Entry::getKey, e->(String)e.getValue()));
                    if (!labels.containsKey("en")) labels.put("en", documentSnapshot.getId());
                    Tag tag = new Tag(documentSnapshot.getId(), labels);
                    return tag;
                }).collect(Collectors.toList()));
            } else {
                onError("cannot load tags", task.getException());
            }
        });
    }


    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private void persistProfile() {
        Auth auth = this.auth.getValue();
        if (auth.isSignedIn()) {
            DocumentReference profileRef = FirebaseFirestore.getInstance().collection("users").document(auth.getUser().getUid());
            profileRef.set(auth.getProfile()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "profile persisted");
                } else {
                    onError("can't store profile", task.getException());
                }
            });
        }
    }

    public void toggleTag(String tagId) {
        Auth auth = this.auth.getValue();
        this.auth.onNext(auth.withProfile(auth.getProfile().withFilter(auth.getProfile().getFilter().withToggledTag(tagId))));
        persistProfile();
    }
}
