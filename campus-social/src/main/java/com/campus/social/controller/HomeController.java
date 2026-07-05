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
public class HomeController {

    private final PostService postService;
    private final UserService userService;
    private final FriendService friendService;

    public HomeController(PostService postService, UserService userService, FriendService friendService) {
        this.postService = postService;
        this.userService = userService;
        this.friendService = friendService;
    }

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        if (userId == null) {
            userId = 1L;
            session.setAttribute("currentUserId", userId);
        }
        return userId;
    }

    @GetMapping("/")
    public String feed(Model model, HttpSession session,
                       @ModelAttribute("toastMsg") String toastMsg,
                       @ModelAttribute("toastType") String toastType) {
        Long currentUserId = getCurrentUserId(session);
        User currentUser = userService.getById(currentUserId);

        List<PhotoPost> posts = postService.getVisiblePosts(currentUserId);
        List<PhotoPost> pinnedPosts = postService.getPinnedPosts(currentUserId);
        List<User> allUsers = userService.findAll();

        Map<Long, User> userMap = new HashMap<>();
        for (User u : allUsers) {
            userMap.put(u.getId(), u);
        }

        Map<Long, Integer> commentCountMap = new HashMap<>();
        Map<Long, Integer> bookmarkCountMap = new HashMap<>();
        for (PhotoPost post : posts) {
            commentCountMap.put(post.getId(), postService.getCommentCount(post.getId()));
            bookmarkCountMap.put(post.getId(), post.getBookmarkCount());
        }
        for (PhotoPost post : pinnedPosts) {
            commentCountMap.put(post.getId(), postService.getCommentCount(post.getId()));
            bookmarkCountMap.put(post.getId(), post.getBookmarkCount());
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("posts", posts);
        model.addAttribute("pinnedPosts", pinnedPosts);
        model.addAttribute("userMap", userMap);
        model.addAttribute("commentCountMap", commentCountMap);
        model.addAttribute("bookmarkCountMap", bookmarkCountMap);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("friendCount", friendService.getFriendCount(currentUserId));
        model.addAttribute("pendingRequestCount", friendService.getPendingRequestCount(currentUserId));
        model.addAttribute("pendingRequests", friendService.getPendingRequestsReceived(currentUserId));
        model.addAttribute("visiblePostCount", postService.getVisiblePostCount(currentUserId));
        model.addAttribute("photoPostCount", postService.getPhotoPostCount());
        model.addAttribute("toastMsg", toastMsg);
        model.addAttribute("toastType", toastType);

        return "feed";
    }

    @PostMapping("/switch-user")
    public String switchUser(@RequestParam Long userId, HttpSession session) {
        session.setAttribute("currentUserId", userId);
        return "redirect:/";
    }
}
