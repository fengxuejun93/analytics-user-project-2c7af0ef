package com.campus.social.repository;

import com.campus.social.model.FriendRelation;
import com.campus.social.model.FriendRelation.FriendStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FriendRelationRepository {
    private final Map<Long, FriendRelation> relations = new LinkedHashMap<>();
    private long nextId = 1;

    public FriendRelation save(FriendRelation relation) {
        if (relation.getId() == null) {
            relation.setId(nextId++);
        }
        relations.put(relation.getId(), relation);
        return relation;
    }

    public Optional<FriendRelation> findById(Long id) {
        return Optional.ofNullable(relations.get(id));
    }

    public List<FriendRelation> findAll() {
        return new ArrayList<>(relations.values());
    }

    public List<FriendRelation> findByFriendIdAndStatus(Long friendId, FriendStatus status) {
        return relations.values().stream()
                .filter(r -> r.getFriendId().equals(friendId) && r.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<FriendRelation> findByUserIdAndStatus(Long userId, FriendStatus status) {
        return relations.values().stream()
                .filter(r -> r.getUserId().equals(userId) && r.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<FriendRelation> findAcceptedRelations(Long userId) {
        return relations.values().stream()
                .filter(r -> r.getStatus() == FriendStatus.ACCEPTED &&
                        (r.getUserId().equals(userId) || r.getFriendId().equals(userId)))
                .collect(Collectors.toList());
    }

    public Optional<FriendRelation> findBetweenUsers(Long userId1, Long userId2) {
        return relations.values().stream()
                .filter(r -> (r.getUserId().equals(userId1) && r.getFriendId().equals(userId2)) ||
                        (r.getUserId().equals(userId2) && r.getFriendId().equals(userId1)))
                .findFirst();
    }

    public boolean areFriends(Long userId1, Long userId2) {
        return relations.values().stream()
                .anyMatch(r -> r.getStatus() == FriendStatus.ACCEPTED &&
                        ((r.getUserId().equals(userId1) && r.getFriendId().equals(userId2)) ||
                         (r.getUserId().equals(userId2) && r.getFriendId().equals(userId1))));
    }
}
