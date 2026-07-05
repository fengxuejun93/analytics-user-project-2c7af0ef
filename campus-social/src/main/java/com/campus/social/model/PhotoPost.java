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
    private List<Long> bookmarkedByUsers = new ArrayList<>();
    private boolean pinned;
    private boolean edited;
    private LocalDateTime lastEditedAt;

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

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
    public LocalDateTime getLastEditedAt() { return lastEditedAt; }
    public void setLastEditedAt(LocalDateTime lastEditedAt) { this.lastEditedAt = lastEditedAt; }

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

    public List<Long> getBookmarkedByUsers() { return bookmarkedByUsers; }
    public void setBookmarkedByUsers(List<Long> bookmarkedByUsers) { this.bookmarkedByUsers = bookmarkedByUsers; }

    public int getBookmarkCount() { return bookmarkedByUsers.size(); }

    public boolean isBookmarkedBy(Long userId) {
        return bookmarkedByUsers.contains(userId);
    }

    public void toggleBookmark(Long userId) {
        if (bookmarkedByUsers.contains(userId)) {
            bookmarkedByUsers.remove(userId);
        } else {
            bookmarkedByUsers.add(userId);
        }
    }
}
