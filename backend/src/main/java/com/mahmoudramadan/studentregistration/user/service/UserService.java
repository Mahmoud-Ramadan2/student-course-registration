package com.mahmoudramadan.studentregistration.user.service;

import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import com.mahmoudramadan.studentregistration.user.dto.UserResponse;
import com.mahmoudramadan.studentregistration.user.entity.User;
import com.mahmoudramadan.studentregistration.user.mapper.UserMapper;
import com.mahmoudramadan.studentregistration.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        log.debug("Loading current user username={}", username);
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

}
