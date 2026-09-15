package com.bookamore.backend.util;

import com.bookamore.backend.exception.UnauthorizedAccessException;
import com.bookamore.backend.jwt.JwtUserDetails;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    @Nullable
    public static UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof JwtUserDetails) {
                return ((JwtUserDetails) principal).getId();
            }
        }
        return null;
    }

    @Nonnull
    public static UUID requireAuthenticatedUserId() {
        UUID userId = getAuthenticatedUserId();
        if (userId == null) {
            throw new UnauthorizedAccessException("Authenticated user is required");
        }
        return userId;
    }

}
