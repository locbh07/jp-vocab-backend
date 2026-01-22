package com.jpvocab.vocabsite.controller;

import com.jpvocab.vocabsite.mapper.UserMapper;
import com.jpvocab.vocabsite.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AdminGuard {

    @Autowired
    private UserMapper userMapper;

    public UserAccount requireAdmin(String username, Long userId) {
        if ((username == null || username.trim().isEmpty()) && userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing admin identity");
        }

        UserAccount user = null;
        if (userId != null) {
            user = userMapper.findById(userId);
        } else {
            user = userMapper.findByUsername(username);
        }

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not found");
        }

        String role = user.getRole();
        if (role == null || !"ADMIN".equalsIgnoreCase(role.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return user;
    }
}
