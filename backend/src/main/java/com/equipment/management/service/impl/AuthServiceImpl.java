package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.context.UserContext;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.util.JwtUtils;
import com.equipment.management.common.util.LoginLockManager;
import com.equipment.management.common.util.RequestUtils;
import com.equipment.management.dto.request.LoginRequest;
import com.equipment.management.dto.request.PasswordChangeRequest;
import com.equipment.management.dto.response.LoginResponse;
import com.equipment.management.dto.response.UserInfoResponse;
import com.equipment.management.entity.SysDepartment;
import com.equipment.management.entity.SysLoginLog;
import com.equipment.management.entity.SysRole;
import com.equipment.management.entity.SysUser;
import com.equipment.management.entity.SysUserRole;
import com.equipment.management.mapper.SysDepartmentMapper;
import com.equipment.management.mapper.SysLoginLogMapper;
import com.equipment.management.mapper.SysRoleMapper;
import com.equipment.management.mapper.SysUserMapper;
import com.equipment.management.mapper.SysUserRoleMapper;
import com.equipment.management.service.AuthService;
import com.equipment.management.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginLockManager loginLockManager;
    private final PermissionService permissionService;

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        loginLockManager.checkLocked(username);

        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username));
        boolean loginSuccess = false;
        try {
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                loginLockManager.recordFailure(username);
                throw new BusinessException(ErrorCode.LOGIN_FAILED);
            }
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new BusinessException(ErrorCode.LOGIN_FAILED, "账号已禁用");
            }

            loginLockManager.clearFailures(username);
            loginSuccess = true;

            UserContext.LoginUser loginUser = buildLoginUser(user);
            String token = jwtUtils.generateToken(loginUser);
            UserInfoResponse userInfo = buildUserInfo(user);

            return LoginResponse.builder().token(token).user(userInfo).build();
        } finally {
            saveLoginLog(username, httpRequest, loginSuccess ? 1 : 0);
        }
    }

    @Override
    public LoginResponse refreshToken() {
        UserContext.LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String token = jwtUtils.generateToken(loginUser);
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return LoginResponse.builder()
                .token(token)
                .user(buildUserInfo(user))
                .build();
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        UserContext.LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return buildUserInfo(user);
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

    private UserInfoResponse buildUserInfo(SysUser user) {
        Set<String> roleCodes = loadRoleCodes(user.getId());
        UserContext.DataScope dataScope = resolveDataScope(roleCodes);
        return UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getRealName())
                .username(user.getUsername())
                .departmentId(user.getDepartmentId())
                .department(resolveDepartmentName(user.getDepartmentId()))
                .roles(roleCodes.stream().sorted().toList())
                .permissions(permissionService.getUserPermissionCodes(user.getId()))
                .menus(permissionService.getUserMenus(user.getId()))
                .dataScope(dataScope.name())
                .build();
    }

    private UserContext.LoginUser buildLoginUser(SysUser user) {
        UserContext.LoginUser loginUser = new UserContext.LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setDepartmentId(user.getDepartmentId());
        Set<String> roleCodes = loadRoleCodes(user.getId());
        loginUser.setRoleCodes(roleCodes);
        loginUser.setDataScope(resolveDataScope(roleCodes));
        return loginUser;
    }

    private void saveLoginLog(String username, HttpServletRequest httpRequest, int result) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginIp(RequestUtils.resolveClientIp(httpRequest));
        loginLog.setBrowser(RequestUtils.resolveBrowser(httpRequest));
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setResult(result);
        sysLoginLogMapper.insert(loginLog);
    }

    private String resolveDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        SysDepartment department = sysDepartmentMapper.selectById(departmentId);
        return department != null ? department.getDepartmentName() : null;
    }

    private Set<String> loadRoleCodes(Long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return sysRoleMapper.selectBatchIds(roleIds).stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());
    }

    private UserContext.DataScope resolveDataScope(Set<String> roleCodes) {
        if (containsAny(roleCodes, "SUPER_ADMIN", "SYS_ADMIN", "SYSTEM_ADMIN", "ADMIN")) {
            return UserContext.DataScope.ALL;
        }
        if (containsAny(roleCodes, "DEPT_ADMIN", "DEPARTMENT_ADMIN")) {
            return UserContext.DataScope.DEPARTMENT;
        }
        if (containsAny(roleCodes, "DEVICE_OWNER", "USER", "NORMAL_USER")) {
            return UserContext.DataScope.SELF;
        }
        return UserContext.DataScope.SELF;
    }

    private boolean containsAny(Set<String> roleCodes, String... candidates) {
        for (String candidate : candidates) {
            if (roleCodes.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
