package com.bookamore.backend.controller;

import com.bookamore.backend.annotation.No404Swgr;
import com.bookamore.backend.dto.password.ChangePasswordRequest;
import com.bookamore.backend.dto.signin.SignInResponse;
import com.bookamore.backend.dto.user.UserProfileUpdateRequest;
import com.bookamore.backend.dto.user.UserResponse;
import com.bookamore.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("current-user")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @No404Swgr
    @PatchMapping("profile")
    @Operation(summary = "Update current user profile",
            description = "Partially updates the authenticated user's profile. All fields are optional, "
                    + "but at least one field must be provided. Only the supplied fields are updated.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Profile updated"),
                    @ApiResponse(responseCode = "400", description = "No fields provided or a field is invalid")
            }
    )
    public ResponseEntity<UserResponse> updateProfile(@Validated @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @No404Swgr
    @PatchMapping("password")
    @Operation(summary = "Change current user password",
            description = "Sets or changes the password of the authenticated user. "
                    + "Send currentPassword as an empty string when the account does not have a password yet.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password changed",
                            content = @Content(schema = @Schema(implementation = SignInResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid payload or current password is incorrect"),
                    @ApiResponse(responseCode = "422", description = "New password is the same as the current one")
            }
    )
    public ResponseEntity<SignInResponse> changePassword(@Validated @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(request));
    }

    @GetMapping("{uuid}")
    public ResponseEntity<UserResponse> getUserByUuid(@PathVariable("uuid") UUID uuid) {
        return ResponseEntity.ok(userService.findById(uuid));
    }
}
