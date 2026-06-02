package com.identityservice.controller;

import com.identityservice.dto.response.UserResponse;
import com.identityservice.service.UserService;
import com.sharekernel.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller cung cấp các API cơ bản liên quan đến người dùng.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByPublicId(
            @PathVariable UUID publicId
    ) {
        UserResponse userResponse = userService.getUserByPublicId(publicId);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Lay thong tin nguoi dung thanh cong", null));
    }

    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @RequestParam String email
    ) {
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Lay thong tin nguoi dung thanh cong", null));
    }
}
