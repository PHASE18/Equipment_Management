package com.equipment.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.controller.BaseCrudController;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.entity.SysDict;
import com.equipment.management.service.DictService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireAuth
@RequestMapping("/api/dict/crud")
public class DictCrudController extends BaseCrudController<DictService, SysDict> {

    public DictCrudController(DictService dictService) {
        super(dictService);
    }

    @Override
    protected QueryWrapper<SysDict> buildQueryWrapper(PageQuery query) {
        QueryWrapper<SysDict> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("dict_name", query.getKeyword())
                    .or().like("dict_code", query.getKeyword())
                    .or().like("dict_type", query.getKeyword()));
        }
        wrapper.orderByAsc("dict_type").orderByAsc("sort");
        return wrapper;
    }
}
