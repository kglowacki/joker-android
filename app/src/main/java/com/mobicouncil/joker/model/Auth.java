package com.mobicouncil.joker.model;

import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by kris on 30/12/2017.
 */

public class Auth {

    public static Auth signedOut() {
        return new Auth(null, new Profile(new Filter()));
    };

    public Auth withUser(FirebaseUser user) {
        return new Auth(user, this.profile);
    }

    public Auth withProfile(Profile profile) {
        return new Auth(user, profile);
    }

    public static class Filter {
        private final String lang;
        private final Map<String,Object> tags;

        public Filter() {
            this("pl", new HashMap<>());
        }

        public Filter(String lang, Map<String, Object> tags) {
            this.lang = lang;
            this.tags = tags;
        }

        public String getLang() {
            return lang;
        }

        public Map<String, Object> getTags() {
            return tags;
        }

        public Filter withLang(String lang) {
            return new Filter(lang, new HashMap<>(this.tags));
        }

        public Filter withToggledTag(String tagId) {
            //Map<String,Object> tags = new HashMap<>(this.tags);
            //tags.put(tagId, !isTagSelected(tagId));
            return new Filter(this.lang, Collections.singletonMap(tagId, !isTagSelected(tagId)));
        }

        public boolean isTagSelected(String tagId) {
            return Boolean.TRUE.equals(tags.get(tagId));
        }

        public Set<String> selectedTags() {
            return tags.entrySet().stream()
                    .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                    .map(entry -> entry.getKey()).collect(Collectors.toSet());
        }
    }

    public static class Profile {
        private final Filter filter;
        private final Map<String,Boolean> notificationTokens = new HashMap();

        public Profile() {
            this(new Filter());
        }

        public Profile(Filter filter) {
            this.filter = filter;
        }

        public Filter getFilter() {
            return filter;
        }

        public Profile withFilter(Filter filter) {
            return new Profile(filter);
        }

        public Map<String, Boolean> getNotificationTokens() {
            return notificationTokens;
        }
    }

    private final FirebaseUser user;
    private final Profile profile;

    private Auth(FirebaseUser user, Profile profile) {
        this.user = user;
        this.profile = profile;
    }

    public static Auth create(FirebaseUser user, Profile profile) {
        return user == null ? signedOut() : new Auth(user, profile);
    }

    public boolean isSignedIn() {
        return user != null;
    }

    public FirebaseUser getUser() {
        return user;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "user=" + user +
                ", profile=" + profile +
                '}';
    }
}
