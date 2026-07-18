package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
/** 通用字典项，按字典类型维护前端下拉选项。 */
public class SysDict extends BaseEntity {

    private String dictType;
    private String dictCode;
    private String dictName;
    private Integer sort;
    private Integer status;
}
