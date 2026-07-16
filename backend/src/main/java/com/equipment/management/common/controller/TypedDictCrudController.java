package com.equipment.management.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.Result;
import com.equipment.management.entity.SysDict;
import com.equipment.management.service.DictService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 按 dict_type 隔离的字典 CRUD 基类（设备品牌、设备类型等）
 */
public abstract class TypedDictCrudController extends BaseCrudController<DictService, SysDict> {

    protected TypedDictCrudController(DictService dictService) {
        super(dictService);
    }

    protected abstract String dictType();

    @Override
    public Result<Void> create(@Valid @RequestBody SysDict entity) {
        entity.setDictType(dictType());
        baseService.createEntity(entity);
        return Result.success();
    }

    @Override
    public Result<Void> update(@Valid @RequestBody SysDict entity) {
        entity.setDictType(dictType());
        baseService.updateEntity(entity);
        return Result.success();
    }

    @Override
    protected QueryWrapper<SysDict> buildQueryWrapper(PageQuery query) {
        QueryWrapper<SysDict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type", dictType());
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("dict_name", query.getKeyword())
                    .or().like("dict_code", query.getKeyword()));
        }
        wrapper.orderByAsc("sort").orderByDesc("create_time");
        return wrapper;
    }
}
