package com.campus.social.repository;

import com.campus.social.model.PhotoPost;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PhotoPostRepository {
    private final Map<Long, PhotoPost> posts = new LinkedHashMap<>();
    private long nextId = 1;

    public PhotoPost save(PhotoPost post) {
        if (post.getId() == null) {
            post.setId(nextId++);
        }
        posts.put(post.getId(), post);
        return post;
    }

    public Optional<PhotoPost> findById(Long id) {
        return Optional.ofNullable(posts.get(id));
    }

    public List<PhotoPost> findAll() {
        return new ArrayList<>(posts.values());
    }

    public List<PhotoPost> findByUserId(Long userId) {
        return posts.values().stream()
                .filter(p -> p.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<PhotoPost> findAllOrderByCreatedAtDesc() {
        return posts.values().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public long countByUserId(Long userId) {
        return posts.values().stream()
                .filter(p -> p.getUserId().equals(userId))
                .count();
    }
}
