package com.CompraVenta.Backend.Modules.auth.Service.Impl;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LoginRateLimitService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15))))
                .build();
    }

    public boolean tryConsume(String email) {
        return buckets.computeIfAbsent(email, k -> createNewBucket()).tryConsume(1);
    }

    public boolean checkLoginAttempts(String email) {
        return tryConsume(email);
    }

    public void incrementAttempts(String email) {
        // In Bucket4j, tryConsume already increments the attempt.
        // If this is called separately, we just try to consume one token.
        tryConsume(email);
    }

    public void resetAttempts(String email) {
        buckets.remove(email);
    }
}
