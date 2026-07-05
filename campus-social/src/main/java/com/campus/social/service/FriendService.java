package com.campus.social.service;

import com.campus.social.model.FriendRelation;
import com.campus.social.model.FriendRelation.FriendStatus;
import com.campus.social.model.User;
import com.campus.social.repository.FriendRelationRepository;
import com.campus.social.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final FriendRelationRepository friendRelationRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRelationRepository friendRelationRepository,
                         UserRepository userRepository) {
        this.friendRelationRepository = friendRelationRepository;
        this.userRepository = userRepository;
    }

    public boolean areFriends(Long userId1, Long userId2) {
        return friendRelationRepository.areFriends(userId1, userId2);
    }

    public int getFriendCount(Long userId) {
        return (int) friendRelationRepository.findAcceptedRelations(userId).size();
    }

    public List<User> getFriendList(Long userId) {
        List<FriendRelation> relations = friendRelationRepository.findAcceptedRelations(userId);
        List<Long> friendIds = relations.stream()
                .map(r -> r.getUserId().equals(userId) ? r.getFriendId() : r.getUserId())
                .collect(Collectors.toList());
        return userRepository.findByIdIn(friendIds);
    }

    public List<FriendRelation> getPendingRequestsReceived(Long userId) {
        return friendRelationRepository.findByFriendIdAndStatus(userId, FriendStatus.PENDING);
    }

    public int getPendingRequestCount(Long userId) {
        return getPendingRequestsReceived(userId).size();
    }

    public List<FriendRelation> getPendingRequestsSent(Long userId) {
        return friendRelationRepository.findByUserIdAndStatus(userId, FriendStatus.PENDING);
    }

    public void sendFriendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) return;
        var existing = friendRelationRepository.findBetweenUsers(fromUserId, toUserId);
        if (existing.isPresent()) {
            // 被拒绝后可重新申请：把 REJECTED 改回 PENDING
            if (existing.get().getStatus() == FriendStatus.REJECTED) {
                existing.get().setStatus(FriendStatus.PENDING);
                existing.get().setCreatedAt(java.time.LocalDateTime.now());
                friendRelationRepository.save(existing.get());
            }
            // 已有 PENDING 或 ACCEPTED 记录则不重复创建
            return;
        }
        friendRelationRepository.save(new FriendRelation(null, fromUserId, toUserId,
                FriendStatus.PENDING, java.time.LocalDateTime.now()));
    }

    public void acceptFriendRequest(Long relationId) {
        friendRelationRepository.findById(relationId).ifPresent(r -> {
            r.setStatus(FriendStatus.ACCEPTED);
            friendRelationRepository.save(r);
        });
    }

    public void rejectFriendRequest(Long relationId) {
        friendRelationRepository.findById(relationId).ifPresent(r -> {
            r.setStatus(FriendStatus.REJECTED);
            friendRelationRepository.save(r);
        });
    }

    public FriendRelation getRelationBetween(Long userId1, Long userId2) {
        return friendRelationRepository.findBetweenUsers(userId1, userId2).orElse(null);
    }
}
