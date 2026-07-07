package com.equipment.management.controller;

import com.equipment.management.annotation.Anonymous;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.request.LoginRequest;
import com.equipment.management.dto.request.PasswordChangeRequest;
import com.equipment.management.dto.response.LoginResponse;
import com.equipment.management.dto.response.UserInfoResponse;
import com.equipment.management.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    @Anonymous
    @PostMapping("/api/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
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
