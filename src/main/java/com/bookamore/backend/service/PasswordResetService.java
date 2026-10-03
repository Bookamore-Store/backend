package com.bookamore.backend.service;

import com.bookamore.backend.dto.password.ResetPasswordRequest;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;

public interface PasswordResetService {

    @Async
    void requestReset(String email);

    void resetPassword(ResetPasswordRequest request);

    void clearResetCode(UUID userId);
}
