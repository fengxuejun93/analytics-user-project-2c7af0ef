package com.campus.social.repository;

import com.campus.social.model.Comment;
import com.campus.social.model.Reply;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CommentRepository {
    private final Map<Long, Comment> comments = new LinkedHashMap<>();
    private long nextCommentId = 1;
    private long nextReplyId = 1;

    public Comment saveComment(Comment comment) {
        if (comment.getId() == null) {
            comment.setId(nextCommentId++);
        }
        comments.put(comment.getId(), comment);
        return comment;
    }

    public Reply saveReply(Reply reply) {
        if (reply.getId() == null) {
            reply.setId(nextReplyId++);
        }
        Comment comment = comments.get(reply.getCommentId());
        if (comment != null) {
            comment.getReplies().add(reply);
        }
        return reply;
    }

    public List<Comment> findByPostId(Long postId) {
        return comments.values().stream()
                .filter(c -> c.getPostId().equals(postId))
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public Optional<Comment> findCommentById(Long id) {
        return Optional.ofNullable(comments.get(id));
    }

    public long countByPostId(Long postId) {
        return comments.values().stream()
                .filter(c -> c.getPostId().equals(postId))
                .count();
    }

    /** 统计评论+回复总数 */
    public long countByPostIdIncludingReplies(Long postId) {
        return comments.values().stream()
                .filter(c -> c.getPostId().equals(postId))
                .mapToLong(c -> 1 + c.getReplies().size())
                .sum();
    }

    public void deleteCommentById(Long id) {
        comments.remove(id);
    }

    public void deleteByPostId(Long postId) {
        comments.entrySet().removeIf(e -> e.getValue().getPostId().equals(postId));
    }
}
