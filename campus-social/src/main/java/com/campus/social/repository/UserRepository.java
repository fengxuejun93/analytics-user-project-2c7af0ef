package com.campus.social.repository;

import com.campus.social.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new LinkedHashMap<>();
    private long nextId = 1;

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public List<User> findByIdIn(List<Long> ids) {
        List<User> result = new ArrayList<>();
        for (Long id : ids) {
            if (users.containsKey(id)) {
                result.add(users.get(id));
            }
        }
        return result;
    }
}
