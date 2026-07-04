package com.campus.social.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhotoPost {
    private Long id;
    private Long userId;
    private String content;
    private String imageUrl;
    private Visibility visibility;
    private int likeCount;
    private LocalDateTime createdAt;
    private List<Long> likedByUsers = new ArrayList<>();

    public PhotoPost() {}

    public PhotoPost(Long id, Long userId, String content, String imageUrl,
                     Visibility visibility, int likeCount, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.visibility = visibility;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Long> getLikedByUsers() { return likedByUsers; }
    public void setLikedByUsers(List<Long> likedByUsers) { this.likedByUsers = likedByUsers; }

    public boolean isLikedBy(Long userId) {
        return likedByUsers.contains(userId);
    }

    public void toggleLike(Long userId) {
        if (likedByUsers.contains(userId)) {
            likedByUsers.remove(userId);
            likeCount--;
        } else {
            likedByUsers.add(userId);
            likeCount++;
        }
    }
}
