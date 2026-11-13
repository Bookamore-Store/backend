package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.password.ChangePasswordRequest;
import com.bookamore.backend.dto.signin.SignInResponse;
import com.bookamore.backend.dto.user.UserProfileUpdateRequest;
import com.bookamore.backend.dto.user.UserResponse;
import com.bookamore.backend.entity.User;
import com.bookamore.backend.exception.EmailAlreadyExistsException;
import com.bookamore.backend.exception.InvalidPasswordException;
import com.bookamore.backend.exception.ResourceNotFoundException;
import com.bookamore.backend.exception.UnprocessableRequestException;
import com.bookamore.backend.jwt.JwtTokenService;
import com.bookamore.backend.mapper.user.UserMapper;
import com.bookamore.backend.repository.UserRepository;
import com.bookamore.backend.service.UserService;
import com.bookamore.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenService tokenService;

    private final UserMapper userMapper;

    @Override
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("User with email " + user.getEmail() + " already exists.");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmailForAuth(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return userMapper.userToUserResponse(getAuthenticatedUser());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found User with uuid = " + id));

        return userMapper.userToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UserProfileUpdateRequest request) {
        User user = getAuthenticatedUser();
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        return userMapper.userToUserResponse(user);
    }

    @Override
    @Transactional
    public SignInResponse changePassword(ChangePasswordRequest request) {
        User user = getAuthenticatedUser();
        if (StringUtils.hasText(user.getPassword())) {
            if (!StringUtils.hasText(request.getCurrentPassword())
                    || !encoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new InvalidPasswordException("Current password is incorrect.");
            }
            if (encoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new UnprocessableRequestException("New password must be different from the current password.");
            }
        }
        user.setPassword(encoder.encode(request.getNewPassword()));
        return SignInResponse.ok(tokenService.generateToken(user.getId(), user.getPassword()));
    }

    private User getAuthenticatedUser() {
        UUID userId = SecurityUtils.requireAuthenticatedUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found User with uuid = " + userId));
    }
}
