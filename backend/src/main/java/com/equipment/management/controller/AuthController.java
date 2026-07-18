package com.equipment.management.controller;

import com.equipment.management.annotation.AllowExpiredToken;
import com.equipment.management.annotation.Anonymous;
import com.equipment.management.annotation.IgnoreAudit;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.LoginRequest;
import com.equipment.management.dto.request.PasswordChangeRequest;
import com.equipment.management.dto.response.LoginResponse;
import com.equipment.management.dto.response.UserInfoResponse;
import com.equipment.management.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequiredArgsConstructor
/** 登录、令牌刷新、当前用户和密码维护接口。 */
public class AuthController {

    private final AuthService authService;

    @Anonymous
    @PostMapping("/api/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.success(authService.login(request, httpRequest));
    }

    @IgnoreAudit
    @AllowExpiredToken
    @PostMapping("/api/refresh")
    public Result<LoginResponse> refreshToken() {
        return Result.success(authService.refreshToken());
    }

    @GetMapping("/api/user/info")
    public Result<UserInfoResponse> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }

    @PutMapping("/api/user/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(request);
        return Result.success();
    }

    @PostMapping("/api/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
