package com.mobicouncil.joker.adapter;

import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ContextThemeWrapper;

import com.cunoraz.tagview.TagView;
import com.mobicouncil.joker.R;
import com.mobicouncil.joker.model.Tag;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by kris on 03/01/2018.
 */

public class TagViewAdapter {

    private final ContextThemeWrapper context;
    private List<Tag> tags;

    public interface OnTagClickListener {
        void onClick(String tagId);
    }

    private final TagView tagView;

    public TagViewAdapter(ContextThemeWrapper context, TagView tagView, OnTagClickListener tagClickListener) {
        this.context = context;
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
            com.cunoraz.tagview.Tag tagItem = new com.cunoraz.tagview.Tag(tags.get(i).getId());
            //tagItem.background = new ColorDrawable(Color.BLUE);
            boolean s = selected.contains(tags.get(i).getId());
            tagItem.layoutColor = context.getResources().getColor(s ? R.color.colorAccent : R.color.colorPrimary, context.getTheme());
            tagItem.tagTextColor = context.getResources().getColor(R.color.greyLight, context.getTheme());// selected.contains(tags.get(i).getId()) ? Color.WHITE : Color.WHITE;
            tagViews.add(tagItem);
        }
        tagView.addTags(tagViews);
    }
}
