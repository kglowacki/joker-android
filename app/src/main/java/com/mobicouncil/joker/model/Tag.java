package com.mobicouncil.joker.model;

import java.util.Map;

/**
 * Created by kris on 29/12/2017.
 */
public class Tag {

    private final String id;
    private final Map<String,String> labels;

    public Tag(String id, Map<String, String> labels) {
        this.id = id;
        this.labels = labels;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id='" + id + '\'' +
                '}';
    }

}
