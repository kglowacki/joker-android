package com.mobicouncil.joker.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobicouncil.joker.R;
import com.mobicouncil.joker.model.Joke;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kris on 31/12/2017.
 */
public class JokeAdapter extends RecyclerView.Adapter<JokeAdapter.ViewHolder> {

    private List<Joke> jokes = new LinkedList<>();

    public interface OnJokeSelectedListener {

        void onJokeSelected(Joke joke);

    }

    private final OnJokeSelectedListener mListener;


    public JokeAdapter(OnJokeSelectedListener mListener) {
        this.mListener = mListener;
        onDataChanged();
    }

    public void addAllItems(List<Joke> jokes) {
        int start = this.jokes.size();
        this.jokes.addAll(jokes);
        notifyItemRangeInserted(start, jokes.size());
        onDataChanged();
    }

    public void clearItems() {
        this.setItems(Collections.emptyList());
    }

    public void setItems(List<Joke> jokes) {
        this.jokes.clear();
        this.jokes.addAll(jokes);
        notifyDataSetChanged();
        onDataChanged();
    }

    protected void onDataChanged() {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new JokeAdapter.ViewHolder(inflater.inflate(R.layout.item_joke, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(jokes.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return jokes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.joke_text)
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final Joke joke,
                         final OnJokeSelectedListener listener) {
            textView.setText(joke.getText());

            // Click listener
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onJokeSelected(joke);
                }
            });
        }

    }
}
