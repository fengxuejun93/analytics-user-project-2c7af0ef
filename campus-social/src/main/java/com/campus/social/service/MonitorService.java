package com.campus.social.service;

import com.campus.social.model.AuditLog;
import com.campus.social.model.SystemAlert;
import com.campus.social.model.SystemAlert.AlertStatus;
import com.campus.social.model.SystemAlert.AlertType;
import com.campus.social.repository.AuditLogRepository;
import com.campus.social.repository.SystemAlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MonitorService {

    private final SystemAlertRepository alertRepository;
    private final AuditLogRepository auditLogRepository;

    public MonitorService(SystemAlertRepository alertRepository,
                          AuditLogRepository auditLogRepository) {
        this.alertRepository = alertRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ===== 告警管理 =====

    public SystemAlert createAlert(AlertType type, String content, Long sourceUserId,
                                   Long targetUserId, Long relatedId) {
        SystemAlert alert = new SystemAlert(null, type, content, sourceUserId,
                targetUserId, relatedId, AlertStatus.PENDING, LocalDateTime.now());
        return alertRepository.save(alert);
    }

    public List<SystemAlert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public List<SystemAlert> getAlertsByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status);
    }

    public List<SystemAlert> getAlertsByType(AlertType type) {
        return alertRepository.findByType(type);
    }

    public SystemAlert getAlertById(Long id) {
        return alertRepository.findById(id).orElse(null);
    }

    public boolean dispatchAlert(Long alertId, Long assigneeId) {
        var opt = alertRepository.findById(alertId);
        if (opt.isEmpty()) return false;
        if (opt.get().getStatus() != AlertStatus.PENDING) return false;
        SystemAlert alert = opt.get();
        alert.setStatus(AlertStatus.DISPATCHED);
        alert.setAssigneeId(assigneeId);
        alert.setDispatchedAt(LocalDateTime.now());
        alertRepository.save(alert);
        return true;
    }

    public boolean resolveAlert(Long alertId, String note) {
        var opt = alertRepository.findById(alertId);
        if (opt.isEmpty()) return false;
        if (opt.get().getStatus() == AlertStatus.RESOLVED) return false;
        SystemAlert alert = opt.get();
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolveNote(note);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);
        return true;
    }

    public long getAlertCountByStatus(AlertStatus status) {
        return alertRepository.countByStatus(status);
    }

    public long getTotalAlertCount() {
        return alertRepository.count();
    }

    // ===== 审计日志 =====

    public AuditLog recordAction(Long userId, String action, String targetType,
                                 Long targetId, String detail) {
        AuditLog log = new AuditLog(null, userId, action, targetType,
                targetId, detail, LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public long getTotalLogCount() {
        return auditLogRepository.count();
    }
}
