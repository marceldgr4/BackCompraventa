package com.CompraVenta.Backend.Modules.auth.Service.Impl;

import com.CompraVenta.Backend.Exception.custom.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginRateLimitService {
    private static final String LOGIN_ATTEMPTS_PREFIX = "auth:attempts:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_MINUTES = 15L;

    private final RedisTemplate<String, Object> redisTemplate;

    public void checkLoginAttempts(String email) {
        try{
        String key = LOGIN_ATTEMPTS_PREFIX + email;
        Object attempts = redisTemplate.opsForValue().get(key);

        if (attempts != null && Integer.parseInt(attempts.toString()) >= MAX_LOGIN_ATTEMPTS) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new BusinessException(String.format(
                    "Cuenta bloqueada temporalmente por exceso de intentos. Intente en %d segundos.", ttl));
        }
    }catch (BusinessException e){
            throw e;
        }catch (Exception e){
        log.warn("Redis no disponible para rate limitado, omitiendi verificacion: {}", e.getMessage());
        }
    }

    public void incrementAttempts(String email) {
        String key = LOGIN_ATTEMPTS_PREFIX + email;
        Long current = redisTemplate.opsForValue().increment(key);

        if (current != null && current == 1L) {
            redisTemplate.expire(key, LOCKOUT_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void resetAttempts(String email) {
        redisTemplate.delete(LOGIN_ATTEMPTS_PREFIX + email);
    }
}
