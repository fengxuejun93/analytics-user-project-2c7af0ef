package com.campus.social.controller;

import com.campus.social.model.*;
import com.campus.social.service.FriendService;
import com.campus.social.service.PostService;
import com.campus.social.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final FriendService friendService;

    public UserController(UserService userService, PostService postService, FriendService friendService) {
        this.userService = userService;
        this.postService = postService;
        this.friendService = friendService;
    }

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        return userId != null ? userId : 1L;
    }

    @GetMapping("/{id}")
    public String profile(@PathVariable Long id, Model model, HttpSession session,
                          @ModelAttribute("toastMsg") String toastMsg,
                          @ModelAttribute("toastType") String toastType) {
        Long currentUserId = getCurrentUserId(session);
        User profileUser = userService.getById(id);
        if (profileUser == null) return "redirect:/";

        List<PhotoPost> userPosts = postService.getPostsByUser(id, currentUserId);
        List<User> friends = friendService.getFriendList(id);
        FriendRelation relation = friendService.getRelationBetween(currentUserId, id);

        boolean isSelf = id.equals(currentUserId);
        boolean isFriend = friendService.areFriends(currentUserId, id);

        String friendStatus = "none";
        Long pendingRelationId = null;
        if (isSelf) {
            friendStatus = "self";
        } else if (isFriend) {
            friendStatus = "friend";
        } else if (relation != null) {
            if (relation.getStatus() == FriendRelation.FriendStatus.PENDING) {
                pendingRelationId = relation.getId();
                if (relation.getUserId().equals(currentUserId)) {
                    friendStatus = "sent";
                } else {
                    friendStatus = "received";
                }
            } else if (relation.getStatus() == FriendRelation.FriendStatus.REJECTED) {
                friendStatus = "rejected";
            }
        }

        Map<Long, User> userMap = new HashMap<>();
        for (User u : userService.findAll()) {
            userMap.put(u.getId(), u);
        }

        Map<Long, Integer> commentCountMap = new HashMap<>();
        Map<Long, Integer> bookmarkCountMap = new HashMap<>();
        for (PhotoPost post : userPosts) {
            commentCountMap.put(post.getId(), postService.getCommentCount(post.getId()));
            bookmarkCountMap.put(post.getId(), post.getBookmarkCount());
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("userPosts", userPosts);
        model.addAttribute("friends", friends);
        model.addAttribute("friendStatus", friendStatus);
        model.addAttribute("relation", relation);
        model.addAttribute("pendingRelationId", pendingRelationId);
        model.addAttribute("currentUser", userService.getById(currentUserId));
        model.addAttribute("userMap", userMap);
        model.addAttribute("commentCountMap", commentCountMap);
        model.addAttribute("bookmarkCountMap", bookmarkCountMap);
        model.addAttribute("friendCount", friendService.getFriendCount(currentUserId));
        model.addAttribute("pendingRequestCount", friendService.getPendingRequestCount(currentUserId));
        model.addAttribute("visiblePostCount", postService.getVisiblePostCount(currentUserId));
        model.addAttribute("photoPostCount", postService.getPhotoPostCount());
        model.addAttribute("toastMsg", toastMsg);
        model.addAttribute("toastType", toastType);
        return "profile";
    }
}
