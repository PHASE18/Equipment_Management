package com.equipment.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.dto.request.UserQuery;
import com.equipment.management.entity.SysUser;
import com.equipment.management.mapper.SysUserMapper;
import com.equipment.management.common.service.impl.BaseCrudServiceImpl;
import com.equipment.management.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
/** 系统用户服务实现，负责用户资料、角色和密码处理。 */
public class SysUserServiceImpl extends BaseCrudServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysUser> list(UserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getDepartmentId() != null, SysUser::getDepartmentId, query.getDepartmentId());
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or().like(SysUser::getRealName, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }

    @Override
    public void createEntity(SysUser entity) {
        if (StringUtils.hasText(entity.getPassword())) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        super.createEntity(entity);
    }

    @Override
    public void updateEntity(SysUser entity) {
        SysUser existing = getDetail(entity.getId());
        if (StringUtils.hasText(entity.getPassword())) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        } else {
            entity.setPassword(existing.getPassword());
        }
        super.updateEntity(entity);
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = getDetail(id);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        updateById(user);
    }
}
