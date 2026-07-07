package com.equipment.management.common.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.entity.BaseEntity;

public interface BaseCrudService<T extends BaseEntity> extends IService<T> {

    PageResult<T> pageQuery(PageQuery query, Wrapper<T> wrapper);

    T getDetail(Long id);

    void createEntity(T entity);

    void updateEntity(T entity);

    void removeEntity(Long id);
}
