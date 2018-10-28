package com.example.appengine;

import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.org.joda.time.Instant;

import javax.annotation.Nullable;

public class AutoValue_Greeting extends Greeting {
    @Nullable
    @Override
    public User getUser() {
        return null;
    }

    public AutoValue_Greeting(User user, Instant date, String content) {
        super();
    }

    @Override
    public Instant getDate() {
        return null;
    }

    @Override
    public String getContent() {
        return null;
    }
}
