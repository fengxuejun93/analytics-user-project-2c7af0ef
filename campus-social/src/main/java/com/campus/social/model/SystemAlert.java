package com.campus.social.model;

import java.time.LocalDateTime;

public class SystemAlert {

    public enum AlertType {
        FRIEND_REQUEST("好友申请"),
        POST_COMMENT("动态评论"),
        COMMENT_REPLY("评论回复"),
        SYSTEM("系统通知");

        private final String label;
        AlertType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum AlertStatus {
        PENDING("待处理"),
        DISPATCHED("已派发"),
        RESOLVED("已关闭");

        private final String label;
        AlertStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private Long id;
    private AlertType type;
    private String content;
    private Long sourceUserId;
    private Long targetUserId;
    private Long relatedId;
    private AlertStatus status;
    private Long assigneeId;
    private String resolveNote;
    private LocalDateTime createdAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime resolvedAt;

    public SystemAlert() {}

    public SystemAlert(Long id, AlertType type, String content, Long sourceUserId,
                       Long targetUserId, Long relatedId, AlertStatus status,
                       LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.relatedId = relatedId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getSourceUserId() { return sourceUserId; }
    public void setSourceUserId(Long sourceUserId) { this.sourceUserId = sourceUserId; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getResolveNote() { return resolveNote; }
    public void setResolveNote(String resolveNote) { this.resolveNote = resolveNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
