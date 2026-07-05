package com.campus.social.controller;

import com.campus.social.service.FriendService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String sendRequest(@PathVariable Long toUserId, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        friendService.sendFriendRequest(currentUserId, toUserId);
        redirectAttributes.addFlashAttribute("toastMsg", "好友申请已发送");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/user/" + toUserId;
    }

    @PostMapping("/accept/{relationId}")
    public String acceptRequest(@PathVariable Long relationId,
                                @RequestParam(required = false) String redirect,
                                RedirectAttributes redirectAttributes) {
        friendService.acceptFriendRequest(relationId);
        redirectAttributes.addFlashAttribute("toastMsg", "已通过好友申请");
        redirectAttributes.addFlashAttribute("toastType", "success");
        if (redirect != null && !redirect.isEmpty()) {
            return "redirect:" + redirect;
        }
        return "redirect:/";
    }

    @PostMapping("/reject/{relationId}")
    public String rejectRequest(@PathVariable Long relationId,
                                @RequestParam(required = false) String redirect,
                                RedirectAttributes redirectAttributes) {
        friendService.rejectFriendRequest(relationId);
        redirectAttributes.addFlashAttribute("toastMsg", "已拒绝好友申请");
        redirectAttributes.addFlashAttribute("toastType", "success");
        if (redirect != null && !redirect.isEmpty()) {
            return "redirect:" + redirect;
        }
        return "redirect:/";
    }
}
