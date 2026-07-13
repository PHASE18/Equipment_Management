package com.equipment.management.common.util;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginLockManager {

    @Value("${auth.login.max-failures:5}")
    private int maxFailures;

    @Value("${auth.login.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void checkLocked(String username) {
        LoginAttempt attempt = attempts.get(username);
        if (attempt == null) {
            return;
        }
        if (attempt.isLocked()) {
            long remainingMinutes = Math.max(1, Duration.between(LocalDateTime.now(), attempt.lockUntil).toMinutes());
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED,
                    "账号已锁定，请" + remainingMinutes + "分钟后重试");
        }
        attempts.remove(username, attempt);
    }

    public void recordFailure(String username) {
        attempts.compute(username, (key, current) -> {
            if (current == null) {
                current = new LoginAttempt();
            }
            if (current.isLocked()) {
                return current;
            }
            current.failCount++;
            if (current.failCount >= maxFailures) {
                current.lockUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
            }
            return current;
        });
    }

    public void clearFailures(String username) {
        attempts.remove(username);
    }

    private static class LoginAttempt {
        private int failCount;
        private LocalDateTime lockUntil;

        private boolean isLocked() {
            return lockUntil != null && LocalDateTime.now().isBefore(lockUntil);
        }
    }
}
