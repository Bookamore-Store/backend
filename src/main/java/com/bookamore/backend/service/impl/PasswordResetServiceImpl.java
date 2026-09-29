package com.bookamore.backend.service.impl;

import com.bookamore.backend.dto.password.ResetPasswordRequest;
import com.bookamore.backend.entity.User;
import com.bookamore.backend.exception.InvalidResetCodeException;
import com.bookamore.backend.repository.UserRepository;
import com.bookamore.backend.service.EmailSenderService;
import com.bookamore.backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    static final String INVALID_RESET_CODE = "Invalid or expired reset code.";
    static final String RESET_EMAIL_TITLE = "Код для відновлення пароля на Bookamore.Store";
    private static final int CODE_TTL_SECONDS = 180;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String RESET_EMAIL_TEMPLATE = readResetEmailTemplate();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<EmailSenderService> emailSenderService;
    private final PlatformTransactionManager transactionManager;

    @Async
    @Override
    public void requestReset(String email) {
        User user = userRepository.findUserByEmail(email).orElse(null);
        if (user == null) {
            log.info("Password reset requested for a non-existing email: {}", email);
            return;
        }

        EmailSenderService sender = emailSenderService.getIfAvailable();
        if (sender == null) {
            log.warn("SMTP is not configured. Password reset requested by user with email {}", email);
            return;
        }

        UUID userId = user.getId();
        String code = generateCode();
        try {
            sender.sendHtml(user.getEmail(), RESET_EMAIL_TITLE, compileResetPasswordEmailHtml(code));
        } catch (MailException ex) {
            log.error("Failed to send password reset code to {}", email, ex);
            return;
        }

        String codeHash = passwordEncoder.encode(code);
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            User current = userRepository.findById(userId).orElse(null);
            if (current == null) {
                return;
            }
            current.setPasswordResetCode(codeHash);
            current.setPasswordResetCodeExpirationTime(LocalDateTime.now().plusSeconds(CODE_TTL_SECONDS));
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        userRepository.findUserByEmail(request.getEmail())
            .filter(found -> isResetCodeValid(found, request.getCode()))
            .ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setPasswordResetCode(null);
                    user.setPasswordResetCodeExpirationTime(null);
            },
                () -> {
                    throw new InvalidResetCodeException(INVALID_RESET_CODE);
                }
            );
    }

    @Override
    @Transactional
    public void clearResetCode(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getPasswordResetCode() == null && user.getPasswordResetCodeExpirationTime() == null) {
                return;
            }
            user.setPasswordResetCode(null);
            user.setPasswordResetCodeExpirationTime(null);
        });
    }

    private boolean isResetCodeValid(User user, String code) {
        if (!StringUtils.hasText(user.getPasswordResetCode()) || user.getPasswordResetCodeExpirationTime() == null) {
            return false;
        }
        if (!LocalDateTime.now().isBefore(user.getPasswordResetCodeExpirationTime())) {
            return false;
        }
        return passwordEncoder.matches(code, user.getPasswordResetCode());
    }

    private String generateCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private static String compileResetPasswordEmailHtml(String code) {
        return RESET_EMAIL_TEMPLATE
                .replace("{{title}}", RESET_EMAIL_TITLE)
                .replace("{{code}}", code);
    }

    private static String readResetEmailTemplate() {
        try (InputStream in = PasswordResetServiceImpl.class.getResourceAsStream("/mail/password-reset.html")) {
            if (in == null) {
                throw new IllegalStateException("Password reset email template is missing");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read password reset email template", ex);
        }
    }
}
