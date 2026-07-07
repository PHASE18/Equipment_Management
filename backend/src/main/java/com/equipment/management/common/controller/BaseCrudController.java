package com.equipment.management.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import com.equipment.management.common.result.Result;
import com.equipment.management.common.service.BaseCrudService;
import com.equipment.management.entity.BaseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通用 CRUD Controller 基类
 */
public abstract class BaseCrudController<S extends BaseCrudService<T>, T extends BaseEntity> {

    protected final S baseService;

    protected BaseCrudController(S baseService) {
        this.baseService = baseService;
    }

    @GetMapping("/page")
    public Result<PageResult<T>> page(@Valid PageQuery query) {
        return Result.success(baseService.pageQuery(query, buildQueryWrapper(query)));
    }

    @GetMapping("/{id}")
    public Result<T> getById(@PathVariable Long id) {
        return Result.success(baseService.getDetail(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody T entity) {
        baseService.createEntity(entity);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody T entity) {
        baseService.updateEntity(entity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        baseService.removeEntity(id);
        return Result.success();
    }

    protected QueryWrapper<T> buildQueryWrapper(PageQuery query) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return wrapper;
    }
}
