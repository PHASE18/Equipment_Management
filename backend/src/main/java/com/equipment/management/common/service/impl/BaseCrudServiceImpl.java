package com.equipment.management.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.common.util.PageUtils;
import com.equipment.management.entity.BaseEntity;

/** 通用 CRUD 服务实现，承载实体保存、更新、删除和分页等基础逻辑。 */
public abstract class BaseCrudServiceImpl<M extends BaseMapper<T>, T extends BaseEntity>
        extends ServiceImpl<M, T> implements BaseCrudService<T> {

    @Override
    public PageResult<T> pageQuery(PageQuery query, Wrapper<T> wrapper) {
        return PageUtils.toPageResult(page(PageUtils.buildPage(query), wrapper));
    }

    @Override
    public T getDetail(Long id) {
        T entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return entity;
    }

    @Override
    public void createEntity(T entity) {
        // 忽略前端残留的主键，始终由数据库自增生成，避免 Duplicate entry
        entity.setId(null);
        save(entity);
    }

    @Override
    public void updateEntity(T entity) {
        if (entity.getId() == null || getById(entity.getId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        updateById(entity);
    }

    @Override
    public void removeEntity(Long id) {
        if (!removeById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
    }
}
