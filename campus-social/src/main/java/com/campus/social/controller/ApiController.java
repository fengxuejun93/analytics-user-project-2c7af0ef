package com.campus.social.controller;

import com.campus.social.model.*;
import com.campus.social.model.SystemAlert.AlertStatus;
import com.campus.social.model.SystemAlert.AlertType;
import com.campus.social.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final MonitorService monitorService;
    private final PostService postService;
    private final UserService userService;
    private final FriendService friendService;

    public ApiController(MonitorService monitorService, PostService postService,
                         UserService userService, FriendService friendService) {
        this.monitorService = monitorService;
        this.postService = postService;
        this.userService = userService;
        this.friendService = friendService;
    }

    // ===== 看板统计 =====

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", userService.findAll().size());
        data.put("totalPosts", postService.getPhotoPostCount());
        data.put("totalAlerts", monitorService.getTotalAlertCount());
        data.put("pendingAlerts", monitorService.getAlertCountByStatus(AlertStatus.PENDING));
        data.put("dispatchedAlerts", monitorService.getAlertCountByStatus(AlertStatus.DISPATCHED));
        data.put("resolvedAlerts", monitorService.getAlertCountByStatus(AlertStatus.RESOLVED));
        data.put("totalLogs", monitorService.getTotalLogCount());
        data.put("totalFriendRelations", friendService.getFriendCount(1L) // 近似值
                + friendService.getPendingRequestCount(1L));

        // 各类告警数量
        Map<String, Long> alertsByType = new LinkedHashMap<>();
        for (AlertType type : AlertType.values()) {
            alertsByType.put(type.name(), monitorService.getAlertsByType(type).size());
        }
        data.put("alertsByType", alertsByType);

        // 最近5条告警
        List<SystemAlert> recentAlerts = monitorService.getAllAlerts().stream()
                .limit(5)
                .collect(Collectors.toList());
        data.put("recentAlerts", enrichAlerts(recentAlerts));

        return data;
    }

    // ===== 告警接口 =====

    @GetMapping("/alerts")
    public List<Map<String, Object>> listAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        List<SystemAlert> alerts;
        if (status != null && !status.isEmpty()) {
            alerts = monitorService.getAlertsByStatus(AlertStatus.valueOf(status));
        } else if (type != null && !type.isEmpty()) {
            alerts = monitorService.getAlertsByType(AlertType.valueOf(type));
        } else {
            alerts = monitorService.getAllAlerts();
        }
        return enrichAlerts(alerts);
    }

    @GetMapping("/alerts/{id}")
    public Map<String, Object> getAlert(@PathVariable Long id) {
        SystemAlert alert = monitorService.getAlertById(id);
        if (alert == null) return Map.of("error", "告警不存在");
        return enrichAlert(alert);
    }

    @PostMapping("/alerts/{id}/dispatch")
    public Map<String, Object> dispatchAlert(@PathVariable Long id,
                                             @RequestBody Map<String, Long> body) {
        Long assigneeId = body.get("assigneeId");
        if (assigneeId == null) return Map.of("ok", false, "msg", "请指定派发对象");
        boolean ok = monitorService.dispatchAlert(id, assigneeId);
        if (ok) {
            monitorService.recordAction(assigneeId, "DISPATCH_ALERT", "ALERT", id, "派发告警 #" + id);
        }
        return Map.of("ok", ok, "msg", ok ? "已派发" : "派发失败：告警不存在或非待处理状态");
    }

    @PostMapping("/alerts/{id}/resolve")
    public Map<String, Object> resolveAlert(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        String note = body.getOrDefault("note", "");
        if (note.trim().isEmpty()) return Map.of("ok", false, "msg", "请填写关闭说明");
        boolean ok = monitorService.resolveAlert(id, note);
        if (ok) {
            SystemAlert alert = monitorService.getAlertById(id);
            Long resolverId = alert != null && alert.getAssigneeId() != null ? alert.getAssigneeId() : 0L;
            monitorService.recordAction(resolverId, "RESOLVE_ALERT", "ALERT", id, "关闭告警 #" + id + "：" + note);
        }
        return Map.of("ok", ok, "msg", ok ? "已关闭" : "关闭失败：告警不存在或已关闭");
    }

    // ===== 审计日志接口 =====

    @GetMapping("/audit-logs")
    public List<Map<String, Object>> listLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action) {
        List<AuditLog> logs;
        if (userId != null) {
            logs = monitorService.getLogsByUserId(userId);
        } else if (action != null && !action.isEmpty()) {
            logs = monitorService.getLogsByAction(action);
        } else {
            logs = monitorService.getAllLogs();
        }
        return logs.stream().map(this::enrichLog).collect(Collectors.toList());
    }

    // ===== 用户列表（给派发下拉用） =====

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        return userService.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("realName", u.getRealName());
                    m.put("avatar", u.getAvatar());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ===== 辅助方法 =====

    private List<Map<String, Object>> enrichAlerts(List<SystemAlert> alerts) {
        return alerts.stream().map(this::enrichAlert).collect(Collectors.toList());
    }

    private Map<String, Object> enrichAlert(SystemAlert alert) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", alert.getId());
        m.put("type", alert.getType().name());
        m.put("typeLabel", alert.getType().getLabel());
        m.put("content", alert.getContent());
        m.put("sourceUserId", alert.getSourceUserId());
        m.put("targetUserId", alert.getTargetUserId());
        m.put("relatedId", alert.getRelatedId());
        m.put("status", alert.getStatus().name());
        m.put("statusLabel", alert.getStatus().getLabel());
        m.put("assigneeId", alert.getAssigneeId());
        m.put("resolveNote", alert.getResolveNote());
        m.put("createdAt", alert.getCreatedAt().toString());
        m.put("dispatchedAt", alert.getDispatchedAt() != null ? alert.getDispatchedAt().toString() : null);
        m.put("resolvedAt", alert.getResolvedAt() != null ? alert.getResolvedAt().toString() : null);
        // 关联用户名
        if (alert.getSourceUserId() != null) {
            User src = userService.getById(alert.getSourceUserId());
            m.put("sourceUserName", src != null ? src.getRealName() : "未知");
        }
        if (alert.getTargetUserId() != null) {
            User tgt = userService.getById(alert.getTargetUserId());
            m.put("targetUserName", tgt != null ? tgt.getRealName() : "未知");
        }
        if (alert.getAssigneeId() != null) {
            User asn = userService.getById(alert.getAssigneeId());
            m.put("assigneeName", asn != null ? asn.getRealName() : "未知");
        }
        return m;
    }

    private Map<String, Object> enrichLog(AuditLog log) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", log.getId());
        m.put("userId", log.getUserId());
        m.put("action", log.getAction());
        m.put("targetType", log.getTargetType());
        m.put("targetId", log.getTargetId());
        m.put("detail", log.getDetail());
        m.put("createdAt", log.getCreatedAt().toString());
        User u = userService.getById(log.getUserId());
        m.put("userName", u != null ? u.getRealName() : "系统");
        return m;
    }
}
