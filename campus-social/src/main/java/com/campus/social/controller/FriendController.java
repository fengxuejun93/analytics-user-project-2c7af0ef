package com.campus.social.controller;

import com.campus.social.service.FriendService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/friend")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        return userId != null ? userId : 1L;
    }

    @PostMapping("/request/{toUserId}")
    public String sendRequest(@PathVariable Long toUserId, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        friendService.sendFriendRequest(currentUserId, toUserId);
        return "redirect:/user/" + toUserId;
    }

    @PostMapping("/accept/{relationId}")
    public String acceptRequest(@PathVariable Long relationId,
                                @RequestParam(required = false) String redirect) {
        friendService.acceptFriendRequest(relationId);
        if (redirect != null && !redirect.isEmpty()) {
            return "redirect:" + redirect;
        }
        return "redirect:/";
    }

    @PostMapping("/reject/{relationId}")
    public String rejectRequest(@PathVariable Long relationId,
                                @RequestParam(required = false) String redirect) {
        friendService.rejectFriendRequest(relationId);
        if (redirect != null && !redirect.isEmpty()) {
            return "redirect:" + redirect;
        }
        return "redirect:/";
    }
}
