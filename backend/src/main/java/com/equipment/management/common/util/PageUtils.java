package com.equipment.management.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.equipment.management.common.query.PageQuery;
import com.equipment.management.common.result.PageResult;
import org.springframework.util.StringUtils;

/**
 * 统一分页工具类
 */
public final class PageUtils {

    private PageUtils() {
    }

    public static <T> Page<T> buildPage(PageQuery query) {
        Page<T> page = new Page<>(query.getPageNum(), query.getPageSize());
        if (StringUtils.hasText(query.getSortField())) {
            if ("asc".equalsIgnoreCase(query.getSortOrder())) {
                page.addOrder(OrderItem.asc(camelToUnderline(query.getSortField())));
            } else {
                page.addOrder(OrderItem.desc(camelToUnderline(query.getSortField())));
            }
        }
        return page;
    }

    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        return PageResult.of(page);
    }

    private static String camelToUnderline(String field) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
