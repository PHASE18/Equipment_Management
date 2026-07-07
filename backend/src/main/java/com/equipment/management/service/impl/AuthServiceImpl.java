package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.util.JwtUtils;
import com.equipment.management.dto.request.LoginRequest;
import com.equipment.management.dto.request.PasswordChangeRequest;
import com.equipment.management.dto.response.LoginResponse;
import com.equipment.management.dto.response.UserInfoResponse;
import com.equipment.management.entity.SysDepartment;
import com.equipment.management.entity.SysUser;
import com.equipment.management.mapper.SysDepartmentMapper;
import com.equipment.management.mapper.SysUserMapper;
import com.equipment.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "账号已禁用");
        }

        UserContext.LoginUser loginUser = new UserContext.LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setDepartmentId(user.getDepartmentId());

        String token = jwtUtils.generateToken(loginUser);
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getRealName())
                .username(user.getUsername())
                .departmentId(user.getDepartmentId())
                .department(resolveDepartmentName(user.getDepartmentId()))
                .build();

        return LoginResponse.builder().token(token).user(userInfo).build();
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        UserContext.LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return UserInfoResponse.builder()
                .id(loginUser.getUserId())
                .name(loginUser.getRealName())
                .username(loginUser.getUsername())
                .departmentId(loginUser.getDepartmentId())
                .department(resolveDepartmentName(loginUser.getDepartmentId()))
                .build();
    }

    @Override
    public void changePassword(PasswordChangeRequest request) {
        Long userId = UserContext.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    public void logout() {
        log.info("用户 {} 退出登录", UserContext.getUsername());
    }

    private String resolveDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        SysDepartment department = sysDepartmentMapper.selectById(departmentId);
        return department != null ? department.getDepartmentName() : null;
    }
}
