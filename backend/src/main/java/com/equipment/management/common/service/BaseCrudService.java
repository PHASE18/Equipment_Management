package com.equipment.management.common.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.entity.BaseEntity;

/** 领域 CRUD 服务基接口，约束实体服务统一继承 MyBatis-Plus 能力。 */
public interface BaseCrudService<T extends BaseEntity> extends IService<T> {

    PageResult<T> pageQuery(PageQuery query, Wrapper<T> wrapper);

    T getDetail(Long id);

    void createEntity(T entity);

    void updateEntity(T entity);

    void removeEntity(Long id);
}
