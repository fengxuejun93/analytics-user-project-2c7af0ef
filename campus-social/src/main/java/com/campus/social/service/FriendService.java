package com.campus.social.service;

import com.campus.social.model.FriendRelation;
import com.campus.social.model.FriendRelation.FriendStatus;
import com.campus.social.model.SystemAlert;
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
    private final MonitorService monitorService;

    public FriendService(FriendRelationRepository friendRelationRepository,
                         UserRepository userRepository,
                         MonitorService monitorService) {
        this.friendRelationRepository = friendRelationRepository;
        this.userRepository = userRepository;
        this.monitorService = monitorService;
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
            // 被拒绝后可重新申请：把 REJECTED 改回 PENDING，并翻转发起人/接收人
            if (existing.get().getStatus() == FriendStatus.REJECTED) {
                existing.get().setUserId(fromUserId);
                existing.get().setFriendId(toUserId);
                existing.get().setStatus(FriendStatus.PENDING);
                existing.get().setCreatedAt(java.time.LocalDateTime.now());
                friendRelationRepository.save(existing.get());
                monitorService.recordAction(fromUserId, "SEND_REQUEST", "FRIEND_RELATION",
                        existing.get().getId(), "重新发送好友申请(已翻转)");
                monitorService.createAlert(SystemAlert.AlertType.FRIEND_REQUEST,
                        "用户重新向你发送了好友申请", fromUserId, toUserId, existing.get().getId());
            }
            // 已有 PENDING 或 ACCEPTED 记录则不重复创建
            return;
        }
        FriendRelation rel = friendRelationRepository.save(new FriendRelation(null, fromUserId, toUserId,
                FriendStatus.PENDING, java.time.LocalDateTime.now()));
        monitorService.recordAction(fromUserId, "SEND_REQUEST", "FRIEND_RELATION",
                rel.getId(), "发送好友申请");
        monitorService.createAlert(SystemAlert.AlertType.FRIEND_REQUEST,
                "用户向你发送了好友申请", fromUserId, toUserId, rel.getId());
    }

    public void acceptFriendRequest(Long relationId) {
        friendRelationRepository.findById(relationId).ifPresent(r -> {
            r.setStatus(FriendStatus.ACCEPTED);
            friendRelationRepository.save(r);
            monitorService.recordAction(r.getFriendId(), "ACCEPT_REQUEST", "FRIEND_RELATION",
                    relationId, "通过好友申请");
        });
    }

    public void rejectFriendRequest(Long relationId) {
        friendRelationRepository.findById(relationId).ifPresent(r -> {
            r.setStatus(FriendStatus.REJECTED);
            friendRelationRepository.save(r);
            monitorService.recordAction(r.getFriendId(), "REJECT_REQUEST", "FRIEND_RELATION",
                    relationId, "拒绝好友申请");
        });
    }

    public FriendRelation getRelationBetween(Long userId1, Long userId2) {
        return friendRelationRepository.findBetweenUsers(userId1, userId2).orElse(null);
    }
}
