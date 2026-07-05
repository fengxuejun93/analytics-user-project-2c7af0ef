package com.campus.social.repository;

import com.campus.social.model.AuditLog;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class AuditLogRepository {

    private final Map<Long, AuditLog> logs = new LinkedHashMap<>();
    private long nextId = 1;

    public AuditLog save(AuditLog log) {
        if (log.getId() == null) {
            log.setId(nextId++);
        }
        logs.put(log.getId(), log);
        return log;
    }

    public List<AuditLog> findAll() {
        return logs.values().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<AuditLog> findByUserId(Long userId) {
        return logs.values().stream()
                .filter(l -> l.getUserId().equals(userId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<AuditLog> findByAction(String action) {
        return logs.values().stream()
                .filter(l -> l.getAction().equals(action))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public long count() {
        return logs.size();
    }
}
