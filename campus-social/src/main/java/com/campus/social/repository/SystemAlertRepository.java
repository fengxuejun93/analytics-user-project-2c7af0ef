package com.campus.social.repository;

import com.campus.social.model.SystemAlert;
import com.campus.social.model.SystemAlert.AlertStatus;
import com.campus.social.model.SystemAlert.AlertType;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class SystemAlertRepository {

    private final Map<Long, SystemAlert> alerts = new LinkedHashMap<>();
    private long nextId = 1;

    public SystemAlert save(SystemAlert alert) {
        if (alert.getId() == null) {
            alert.setId(nextId++);
        }
        alerts.put(alert.getId(), alert);
        return alert;
    }

    public Optional<SystemAlert> findById(Long id) {
        return Optional.ofNullable(alerts.get(id));
    }

    public List<SystemAlert> findAll() {
        return new ArrayList<>(alerts.values());
    }

    public List<SystemAlert> findByStatus(AlertStatus status) {
        return alerts.values().stream()
                .filter(a -> a.getStatus() == status)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<SystemAlert> findByType(AlertType type) {
        return alerts.values().stream()
                .filter(a -> a.getType() == type)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public long countByStatus(AlertStatus status) {
        return alerts.values().stream()
                .filter(a -> a.getStatus() == status)
                .count();
    }

    public long count() {
        return alerts.size();
    }
}
