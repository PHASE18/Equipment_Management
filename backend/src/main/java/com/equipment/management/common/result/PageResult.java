package com.equipment.management.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** 统一分页响应，包含当前页记录、总数和分页元数据。 */
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private long pageNum;
    private long pageSize;
    private long pages;

    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return PageResult.<T>builder()
                .records(Collections.emptyList())
                .total(0)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(0)
                .build();
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return PageResult.<T>builder()
                .records(page.getRecords())
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .pages(page.getPages())
                .build();
    }

    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        long pages = pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
        return PageResult.<T>builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(pages)
                .build();
    }
}
