package com.equipment.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
/** 登录审计记录，保存账号、IP、结果和失败原因。 */
public class SysLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String loginIp;
    private String browser;
    private LocalDateTime loginTime;
    private Integer result;
}
