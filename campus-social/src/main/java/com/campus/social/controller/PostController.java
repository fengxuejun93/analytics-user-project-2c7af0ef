package com.campus.social.controller;

import com.campus.social.model.*;
import com.campus.social.service.FriendService;
import com.campus.social.service.PostService;
import com.campus.social.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final FriendService friendService;

    public PostController(PostService postService, UserService userService, FriendService friendService) {
        this.postService = postService;
        this.userService = userService;
        this.friendService = friendService;
    }

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("currentUserId");
        return userId != null ? userId : 1L;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        PhotoPost post = postService.findById(id);
        if (post == null) return "redirect:/";

        if (!postService.isPostVisible(post, currentUserId)) {
            return "redirect:/";
        }

        User author = userService.getById(post.getUserId());
        List<Comment> comments = postService.getComments(id);

        Map<Long, User> userMap = new HashMap<>();
        for (User u : userService.findAll()) {
            userMap.put(u.getId(), u);
        }

        model.addAttribute("post", post);
        model.addAttribute("author", author);
        model.addAttribute("comments", comments);
        model.addAttribute("userMap", userMap);
        model.addAttribute("currentUser", userService.getById(currentUserId));
        return "detail";
    }

    @GetMapping("/publish")
    public String publishForm(Model model, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        model.addAttribute("currentUser", userService.getById(currentUserId));
        model.addAttribute("visibilities", Visibility.values());
        model.addAttribute("presetImages", List.of(
                "/images/photo1.svg", "/images/photo2.svg", "/images/photo3.svg",
                "/images/photo4.svg", "/images/photo5.svg", "/images/photo6.svg",
                "/images/photo7.svg", "/images/photo8.svg"
        ));
        return "publish";
    }

    @PostMapping("/publish")
    public String publish(@RequestParam String content,
                          @RequestParam String imageUrl,
                          @RequestParam Visibility visibility,
                          HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        postService.createPost(currentUserId, content, imageUrl, visibility);
        return "redirect:/";
    }

    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        postService.toggleLike(id, currentUserId);
        return "redirect:/post/" + id;
    }

    @PostMapping("/{id}/comment")
    public String comment(@PathVariable Long id,
                          @RequestParam String content,
                          HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        postService.addComment(id, currentUserId, content);
        return "redirect:/post/" + id;
    }

    @PostMapping("/comment/{commentId}/reply")
    public String reply(@PathVariable Long commentId,
                        @RequestParam String content,
                        @RequestParam Long postId,
                        HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        postService.addReply(commentId, currentUserId, content);
        return "redirect:/post/" + postId;
    }
}
