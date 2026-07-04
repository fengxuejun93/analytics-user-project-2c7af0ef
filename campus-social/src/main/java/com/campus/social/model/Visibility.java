package com.campus.social.model;

public enum Visibility {
    PUBLIC("公开"),
    FRIENDS_ONLY("仅好友"),
    PRIVATE("仅自己");

    private final String label;

    Visibility(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
