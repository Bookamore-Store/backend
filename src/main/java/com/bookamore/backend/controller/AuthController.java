package com.bookamore.backend.controller;

import com.bookamore.backend.annotation.No404Swgr;
import com.bookamore.backend.dto.error.ErrorResponse;
import com.bookamore.backend.dto.password.ForgotPasswordRequest;
import com.bookamore.backend.dto.password.ResetPasswordRequest;
import com.bookamore.backend.dto.signin.SignInRequest;
import com.bookamore.backend.dto.signin.SignInResponse;
import com.bookamore.backend.dto.singup.SignUpRequest;
import com.bookamore.backend.dto.singup.SignUpResponse;
import com.bookamore.backend.service.AuthService;
import com.bookamore.backend.service.EmailSenderService;
import com.bookamore.backend.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@No404Swgr
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final ObjectProvider<EmailSenderService> emailSenderService;

    @PostMapping("signup")
    @Operation(summary = "User Registration", description = "Registers a new user account with a unique email.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successful signup",
                            content = @Content(
                                    schema = @Schema(implementation = SignUpResponse.class),
                                    examples = @ExampleObject(
                                            name = "Sign in example",
                                            value = "{" +
                                                    "  \"email\": \"john@example.com\"," +
                                                    "  \"message\": \"Sign up is successful!\"" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict: The requested resource already exists (e.g., email already taken)",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            name = "Email Conflict Example",
                                            value = "{" +
                                                    "  \"timestamp\": \"2025-08-13T10:00:00.000Z\"," +
                                                    "  \"status\": 409, \"error\": \"Conflict\"," +
                                                    "  \"message\": \"Email already exists\"," +
                                                    "  \"path\": \"/api/v1/signup\"" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<SignUpResponse> signUp(@Validated @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    @PostMapping("signin")
    @Operation(summary = "User Authentication", description = "Authenticates a user with their credentials and returns a JWT token.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful authentication",
                            content = @Content(
                                    schema = @Schema(implementation = SignInResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SignInResponse> signIn(@Validated @RequestBody SignInRequest request) {
        SignInResponse response = authService.signIn(request);
        return response.isStatus() ? ResponseEntity.ok(response) : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("forgot-password")
    @Operation(summary = "Send password reset request",
            description = "Accepts an email and starts a password reset. The secret code is sent asynchronously when the account exists.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "202", description = "Password reset request accepted"),
                    @ApiResponse(responseCode = "503", description = "SMTP is not configured")
            }
    )
    public ResponseEntity<Void> forgotPassword(@Validated @RequestBody ForgotPasswordRequest request) {
        if (isSmtpUnavailable(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("reset-password")
    @Operation(summary = "Setting a new password",
            description = "Sets a new password when the email, secret code, and code lifetime are valid.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "New password was set"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or expired reset code",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            name = "Invalid reset code",
                                            value = "{" +
                                                    "  \"timestamp\": \"2025-08-13T10:00:00.000Z\"," +
                                                    "  \"status\": 400, \"error\": \"Bad Request\"," +
                                                    "  \"message\": \"Invalid or expired reset code.\"," +
                                                    "  \"path\": \"/api/v1/auth/reset-password\"" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    private boolean isSmtpUnavailable(String email) {
        if (emailSenderService.getIfAvailable() != null) {
            return false;
        }
        log.warn("SMTP is not configured. Password reset requested by user with email {}", email);
        return true;
    }
}
