package com.identityservice.controller;

import com.identityservice.dto.response.UserAdminResponse;
import com.identityservice.dto.response.UserBasicResponse;
import com.identityservice.dto.response.UserPrivateResponse;
import com.identityservice.service.UserService;
import com.sharekernel.response.ApiResponse;
import com.sharekernel.web.RequestContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/public/{publicId}")
    public ResponseEntity<ApiResponse<UserBasicResponse>> getBasicUserDetailByPublicId(
            @PathVariable UUID publicId, HttpServletRequest httpServletRequest
    ){
        UserBasicResponse userBasicResponse = userService.getBasicUserDetailByPublicId(publicId);
        return ResponseEntity.ok(ApiResponse.success(userBasicResponse, "Lấy chi tiết thông tin người dùng thành công", RequestContextUtils.getRequestId(httpServletRequest)));
    }

    @GetMapping("/public/by-email")
    public ResponseEntity<ApiResponse<UserBasicResponse>> getBasicUserDetailByEmail(
            @RequestParam String email, HttpServletRequest httpServletRequest
    ) {
        UserBasicResponse userBasicResponse = userService.getBasicUserDetailByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(userBasicResponse, "Lấy chi tiết thông tin người dùng thành công", RequestContextUtils.getRequestId(httpServletRequest)));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPrivateResponse>> getMyUserDetailByPublicId(
            HttpServletRequest httpServletRequest
    ){
        UserPrivateResponse userPrivateResponse = userService.getMyUserDetail();
        return ResponseEntity.ok(ApiResponse.success(userPrivateResponse, "Lấy chi tiết thông tin người dùng thành công", RequestContextUtils.getRequestId(httpServletRequest)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{publicId}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getAdminUserDetailByPublicId(
            @PathVariable UUID publicId, HttpServletRequest httpServletRequest
    ){
        UserAdminResponse userAdminResponse = userService.getAdminUserDetailByPublicId(publicId);
        return ResponseEntity.ok(ApiResponse.success(userAdminResponse, "Lấy chi tiết thông tin người dùng thành công", RequestContextUtils.getRequestId(httpServletRequest)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/by-email")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getAdminUserDetailByEmail(
            @RequestParam String email, HttpServletRequest httpServletRequest
    ) {
        UserAdminResponse userAdminResponse = userService.getAdminUserDetailByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(userAdminResponse, "Lấy chi tiết thông tin người dùng thành công", RequestContextUtils.getRequestId(httpServletRequest)));
    }
}
