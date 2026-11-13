package com.bookamore.backend.service;

import com.bookamore.backend.dto.password.ChangePasswordRequest;
import com.bookamore.backend.dto.signin.SignInResponse;
import com.bookamore.backend.dto.user.UserProfileUpdateRequest;
import com.bookamore.backend.dto.user.UserResponse;
import com.bookamore.backend.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User create(User user);

    Optional<User> findByEmailForAuth(String email);

    UserResponse getCurrentUser();

    UserResponse findById(UUID id);

    UserResponse updateProfile(UserProfileUpdateRequest request);

    SignInResponse changePassword(ChangePasswordRequest request);
}
