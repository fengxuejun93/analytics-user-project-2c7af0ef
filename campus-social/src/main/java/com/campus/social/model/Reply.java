package com.campus.social.model;

import java.time.LocalDateTime;

public class Reply {
    private Long id;
    private Long commentId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

    public Reply() {}

    public Reply(Long id, Long commentId, Long userId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
