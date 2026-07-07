package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.Result;
import com.equipment.management.entity.SysConfig;
import com.equipment.management.service.SysConfigService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequireAuth
@RequestMapping("/api/config")
public class SysConfigController extends BaseCrudController<SysConfigService, SysConfig> {

    public SysConfigController(SysConfigService sysConfigService) {
        super(sysConfigService);
    }

    @GetMapping
    public Result<List<SysConfig>> listAll() {
        return Result.success(baseService.listAll());
    }

    @PutMapping("/batch")
    public Result<Void> batchUpdate(@Valid @RequestBody List<SysConfig> configs) {
        baseService.batchUpdate(configs);
        return Result.success();
    }

    @Override
    protected QueryWrapper<SysConfig> buildQueryWrapper(PageQuery query) {
        QueryWrapper<SysConfig> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("config_key", query.getKeyword())
                    .or().like("config_value", query.getKeyword()));
        }
        wrapper.orderByAsc("config_key");
        return wrapper;
    }
}
