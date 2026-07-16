package com.equipment.management.common.util;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class IpUtils {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );

    private IpUtils() {
    }

    public static void validateIpv4(String ip, String fieldName) {
        if (!StringUtils.hasText(ip)) {
            return;
        }
        if (!IPV4_PATTERN.matcher(ip.trim()).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + "格式不正确，请输入有效的 IPv4 地址");
        }
    }
}
