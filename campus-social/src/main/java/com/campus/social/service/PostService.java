package com.campus.social.service;

import com.campus.social.model.*;
import com.campus.social.repository.CommentRepository;
import com.campus.social.repository.PhotoPostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PhotoPostRepository photoPostRepository;
    private final CommentRepository commentRepository;
    private final FriendService friendService;

    public PostService(PhotoPostRepository photoPostRepository,
                       CommentRepository commentRepository,
                       FriendService friendService) {
        this.photoPostRepository = photoPostRepository;
        this.commentRepository = commentRepository;
        this.friendService = friendService;
    }

    public PhotoPost findById(Long id) {
        return photoPostRepository.findById(id).orElse(null);
    }

    public List<PhotoPost> getVisiblePosts(Long currentUserId) {
        return photoPostRepository.findAllOrderByCreatedAtDesc().stream()
                .filter(post -> isPostVisible(post, currentUserId))
                .collect(Collectors.toList());
    }

    public boolean isPostVisible(PhotoPost post, Long currentUserId) {
        if (post.getVisibility() == Visibility.PUBLIC) return true;
        if (post.getVisibility() == Visibility.PRIVATE) {
            return post.getUserId().equals(currentUserId);
        }
        if (post.getVisibility() == Visibility.FRIENDS_ONLY) {
            return post.getUserId().equals(currentUserId) ||
                    friendService.areFriends(post.getUserId(), currentUserId);
        }
        return false;
    }

    public int getVisiblePostCount(Long currentUserId) {
        return (int) getVisiblePosts(currentUserId).size();
    }

    public long getPhotoPostCount() {
        return photoPostRepository.findAll().size();
    }

    public List<PhotoPost> getPostsByUser(Long userId, Long currentUserId) {
        return photoPostRepository.findByUserId(userId).stream()
                .filter(post -> isPostVisible(post, currentUserId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public PhotoPost createPost(Long userId, String content, String imageUrl, Visibility visibility) {
        PhotoPost post = new PhotoPost(null, userId, content, imageUrl, visibility, 0, LocalDateTime.now());
        return photoPostRepository.save(post);
    }

    public void toggleLike(Long postId, Long userId) {
        photoPostRepository.findById(postId).ifPresent(post -> post.toggleLike(userId));
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public int getCommentCount(Long postId) {
        return (int) commentRepository.countByPostIdIncludingReplies(postId);
    }

    public Comment addComment(Long postId, Long userId, String content) {
        Comment comment = new Comment(null, postId, userId, content, LocalDateTime.now());
        return commentRepository.saveComment(comment);
    }

    public Reply addReply(Long commentId, Long userId, String content) {
        Reply reply = new Reply(null, commentId, userId, content, LocalDateTime.now());
        return commentRepository.saveReply(reply);
    }

    public boolean updatePost(Long postId, Long currentUserId, String content, String imageUrl, Visibility visibility) {
        PhotoPost post = findById(postId);
        if (post == null || !post.getUserId().equals(currentUserId)) return false;
        if (content == null || content.trim().isEmpty()) return false;
        if (content.length() > 500) return false;
        if (imageUrl == null || imageUrl.trim().isEmpty()) return false;
        if (visibility == null) return false;
        post.setContent(content.trim());
        post.setImageUrl(imageUrl.trim());
        post.setVisibility(visibility);
        post.setEdited(true);
        post.setLastEditedAt(LocalDateTime.now());
        photoPostRepository.save(post);
        return true;
    }

    public boolean deletePost(Long postId, Long currentUserId) {
        PhotoPost post = findById(postId);
        if (post == null || !post.getUserId().equals(currentUserId)) return false;
        commentRepository.deleteByPostId(postId);
        photoPostRepository.deleteById(postId);
        return true;
    }

    public boolean togglePin(Long postId, Long currentUserId) {
        PhotoPost post = findById(postId);
        if (post == null || !post.getUserId().equals(currentUserId)) return false;
        post.setPinned(!post.isPinned());
        photoPostRepository.save(post);
        return true;
    }

    public List<PhotoPost> getPinnedPosts(Long currentUserId) {
        return photoPostRepository.findByPinnedTrue().stream()
                .filter(post -> isPostVisible(post, currentUserId))
                .collect(Collectors.toList());
    }

    public boolean deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findCommentById(commentId).orElse(null);
        if (comment == null) return false;
        if (!comment.getUserId().equals(currentUserId)) return false;
        commentRepository.deleteCommentById(commentId);
        return true;
    }

    public void toggleBookmark(Long postId, Long userId) {
        photoPostRepository.findById(postId).ifPresent(post -> post.toggleBookmark(userId));
    }

    public int getBookmarkCount(Long postId) {
        return photoPostRepository.findById(postId)
                .map(PhotoPost::getBookmarkCount)
                .orElse(0);
    }
}
