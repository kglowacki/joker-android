package com.mobicouncil.joker.adapter;

import android.graphics.Color;

import com.cunoraz.tagview.TagView;
import com.mobicouncil.joker.model.Tag;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by kris on 03/01/2018.
 */

public class TagViewAdapter {

    private List<Tag> tags;

    public interface OnTagClickListener {
        void onClick(String tagId);
    }

    private final TagView tagView;

    public TagViewAdapter(TagView tagView, OnTagClickListener tagClickListener) {
        this.tagView = tagView;
        tagView.setOnTagClickListener((tag, i) -> {
            if (tags != null && tags.size() > i) {
                tagClickListener.onClick(tags.get(i).getId());
            }
        });
    }

    public void setTags(List<Tag> tags, Set<String> selected) {
        this.tags = tags;
        tagView.removeAll();
        List<com.cunoraz.tagview.Tag> tagViews = new LinkedList<>();
        for (int i = 0; i < tags.size(); i++) {
            com.cunoraz.tagview.Tag tagView = new com.cunoraz.tagview.Tag(tags.get(i).getId());
            tagView.id = i;
            tagView.tagTextColor = selected.contains(tags.get(i).getId()) ? Color.WHITE : Color.BLACK;
            tagViews.add(tagView);
        }
        tagView.addTags(tagViews);
    }
}
