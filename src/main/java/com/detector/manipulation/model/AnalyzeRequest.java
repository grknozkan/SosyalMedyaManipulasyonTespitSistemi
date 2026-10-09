package com.detector.manipulation.model;

import java.util.List;

public class AnalyzeRequest {
    private String text;
    private String username;
    private Integer followerCount;
    private Integer followingCount;
    private Integer accountAgeDays;
    private Boolean defaultAvatar;

    public AnalyzeRequest() {}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }

    public Integer getAccountAgeDays() {
        return accountAgeDays;
    }

    public void setAccountAgeDays(Integer accountAgeDays) {
        this.accountAgeDays = accountAgeDays;
    }

    public Boolean getDefaultAvatar() {
        return defaultAvatar;
    }

    public void setDefaultAvatar(Boolean defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }
}
