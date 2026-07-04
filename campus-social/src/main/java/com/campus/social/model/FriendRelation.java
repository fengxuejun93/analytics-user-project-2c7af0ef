package com.campus.social.model;

import java.time.LocalDateTime;

public class FriendRelation {
    private Long id;
    private Long userId;
    private Long friendId;
    private FriendStatus status;
    private LocalDateTime createdAt;

    public enum FriendStatus {
        PENDING, ACCEPTED, REJECTED
    }

    public FriendRelation() {}

    public FriendRelation(Long id, Long userId, Long friendId, FriendStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFriendId() { return friendId; }
    public void setFriendId(Long friendId) { this.friendId = friendId; }
    public FriendStatus getStatus() { return status; }
    public void setStatus(FriendStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
