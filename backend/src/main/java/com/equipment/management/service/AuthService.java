package com.equipment.management.service;

import com.equipment.management.dto.request.LoginRequest;
import com.equipment.management.dto.request.PasswordChangeRequest;
import com.equipment.management.dto.response.LoginResponse;
import com.equipment.management.dto.response.UserInfoResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserInfoResponse getCurrentUser();

    void changePassword(PasswordChangeRequest request);

    void logout();
}
