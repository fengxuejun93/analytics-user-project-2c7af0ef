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
    public String detail(@PathVariable Long id, Model model, HttpSession session,
                         @ModelAttribute("toastMsg") String toastMsg,
                         @ModelAttribute("toastType") String toastType,
                         RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        PhotoPost post = postService.findById(id);
        if (post == null) {
            model.addAttribute("toastMsg", "该动态不存在或已被删除");
            model.addAttribute("toastType", "error");
            return "redirect:/";
        }

        if (!postService.isPostVisible(post, currentUserId)) {
            redirectAttributes.addFlashAttribute("toastMsg", "该动态对当前身份不可见");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        User author = userService.getById(post.getUserId());
        List<Comment> comments = postService.getComments(id);

        Map<Long, User> userMap = new HashMap<>();
        for (User u : userService.findAll()) {
            userMap.put(u.getId(), u);
        }

        boolean isOwner = post.getUserId().equals(currentUserId);

        model.addAttribute("post", post);
        model.addAttribute("author", author);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", postService.getCommentCount(id));
        model.addAttribute("userMap", userMap);
        model.addAttribute("currentUser", userService.getById(currentUserId));
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("visibilities", Visibility.values());
        model.addAttribute("presetImages", List.of(
                "/images/photo1.svg", "/images/photo2.svg", "/images/photo3.svg",
                "/images/photo4.svg", "/images/photo5.svg", "/images/photo6.svg",
                "/images/photo7.svg", "/images/photo8.svg"
        ));
        model.addAttribute("toastMsg", toastMsg);
        model.addAttribute("toastType", toastType);
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
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMsg", "内容不能为空");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/post/publish";
        }
        if (content.length() > 500) {
            redirectAttributes.addFlashAttribute("toastMsg", "内容不能超过500字");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/post/publish";
        }
        postService.createPost(currentUserId, content.trim(), imageUrl, visibility);
        redirectAttributes.addFlashAttribute("toastMsg", "发布成功");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/";
    }

    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        postService.toggleLike(id, currentUserId);
        return "redirect:/post/" + id;
    }

    @PostMapping("/{id}/bookmark")
    public String bookmark(@PathVariable Long id, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        postService.toggleBookmark(id, currentUserId);
        PhotoPost post = postService.findById(id);
        if (post != null) {
            String msg = post.isBookmarkedBy(currentUserId) ? "已收藏" : "已取消收藏";
            redirectAttributes.addFlashAttribute("toastMsg", msg);
            redirectAttributes.addFlashAttribute("toastType", "success");
        }
        return "redirect:/post/" + id;
    }

    @PostMapping("/{id}/comment")
    public String comment(@PathVariable Long id,
                          @RequestParam String content,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMsg", "评论内容不能为空");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/post/" + id;
        }
        postService.addComment(id, currentUserId, content.trim());
        redirectAttributes.addFlashAttribute("toastMsg", "评论成功");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/post/" + id;
    }

    @PostMapping("/comment/{commentId}/reply")
    public String reply(@PathVariable Long commentId,
                        @RequestParam String content,
                        @RequestParam Long postId,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMsg", "回复内容不能为空");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/post/" + postId;
        }
        postService.addReply(commentId, currentUserId, content.trim());
        redirectAttributes.addFlashAttribute("toastMsg", "回复成功");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/post/" + postId;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        PhotoPost post = postService.findById(id);
        if (post == null) {
            redirectAttributes.addFlashAttribute("toastMsg", "该动态不存在或已被删除");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }
        if (!post.getUserId().equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("toastMsg", "只有动态发布者才能编辑");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/post/" + id;
        }
        model.addAttribute("post", post);
        model.addAttribute("currentUser", userService.getById(currentUserId));
        model.addAttribute("visibilities", Visibility.values());
        model.addAttribute("presetImages", List.of(
                "/images/photo1.svg", "/images/photo2.svg", "/images/photo3.svg",
                "/images/photo4.svg", "/images/photo5.svg", "/images/photo6.svg",
                "/images/photo7.svg", "/images/photo8.svg"
        ));
        return "edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String content,
                       @RequestParam String imageUrl,
                       @RequestParam Visibility visibility,
                       @RequestParam(required = false) String redirect,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);

        // 服务端校验
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMsg", "编辑失败：内容不能为空");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/post/" + id + "/edit";
        }
        if (content.length() > 500) {
            redirectAttributes.addFlashAttribute("toastMsg", "编辑失败：内容不能超过500字");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/post/" + id + "/edit";
        }
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMsg", "编辑失败：请选择一张图片");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/post/" + id + "/edit";
        }

        boolean ok = postService.updatePost(id, currentUserId, content, imageUrl, visibility);
        if (ok) {
            redirectAttributes.addFlashAttribute("toastMsg", "编辑成功");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } else {
            redirectAttributes.addFlashAttribute("toastMsg", "编辑失败：无权操作");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }
        if (redirect != null && !redirect.isEmpty()) {
            return "redirect:" + redirect;
        }
        return "redirect:/post/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        boolean ok = postService.deletePost(id, currentUserId);
        if (ok) {
            redirectAttributes.addFlashAttribute("toastMsg", "动态已删除");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } else {
            redirectAttributes.addFlashAttribute("toastMsg", "删除失败：无权操作");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }
        return "redirect:/";
    }

    @PostMapping("/{id}/pin")
    public String pin(@PathVariable Long id,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        boolean ok = postService.togglePin(id, currentUserId);
        PhotoPost post = postService.findById(id);
        if (ok && post != null) {
            String msg = post.isPinned() ? "已置顶" : "已取消置顶";
            redirectAttributes.addFlashAttribute("toastMsg", msg);
            redirectAttributes.addFlashAttribute("toastType", "success");
        } else {
            redirectAttributes.addFlashAttribute("toastMsg", "操作失败：无权操作");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }
        return "redirect:/post/" + id;
    }

    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam Long postId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long currentUserId = getCurrentUserId(session);
        boolean ok = postService.deleteComment(commentId, currentUserId);
        if (ok) {
            redirectAttributes.addFlashAttribute("toastMsg", "评论已删除");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } else {
            redirectAttributes.addFlashAttribute("toastMsg", "删除失败：无权操作");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }
        return "redirect:/post/" + postId;
    }
}
