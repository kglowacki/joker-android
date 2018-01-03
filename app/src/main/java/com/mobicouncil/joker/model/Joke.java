package com.mobicouncil.joker.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Map;

/**
 * Created by kris on 29/12/2017.
 */

@IgnoreExtraProperties
public class Joke {

    private String id;
    private String text;
    private String lang;
    private Map<String,Boolean> tags;
    private long ts;

    public Joke() {
    }

    public Joke(String id, String text, String lang, Map<String, Boolean> tags, long ts) {
        this.id = id;
        this.text = text;
        this.lang = lang;
        this.tags = tags;
        this.ts = ts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Map<String, Boolean> getTags() {
        return tags;
    }

    public void setTags(Map<String, Boolean> tags) {
        this.tags = tags;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
}
