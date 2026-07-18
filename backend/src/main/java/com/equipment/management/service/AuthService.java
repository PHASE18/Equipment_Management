package com.equipment.management.service;

import com.equipment.management.dto.request.LoginRequest;
import com.equipment.management.dto.request.PasswordChangeRequest;
import com.equipment.management.dto.response.LoginResponse;
import com.equipment.management.dto.response.UserInfoResponse;
import jakarta.servlet.http.HttpServletRequest;

/** 认证领域服务：登录、刷新令牌、当前用户信息和密码修改。 */
public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    LoginResponse refreshToken();

    UserInfoResponse getCurrentUser();

    void changePassword(PasswordChangeRequest request);

    void logout();
}
