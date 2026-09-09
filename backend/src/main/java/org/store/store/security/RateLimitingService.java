package org.store.store.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucketForPath(String path) {
        Bandwidth limit;

        if (path.contains("/store/login")) {
            limit = Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(5))
                    .build();
        } else if (path.contains("/store/forgot-password")) {
            limit = Bandwidth.builder()
                    .capacity(3)
                    .refillIntervally(3, Duration.ofMinutes(10))
                    .build();
        } else if (path.contains("/store/register/verify")) {
            limit = Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(10))
                    .build();
        } else {
            limit = Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, Duration.ofMinutes(1))
                    .build();
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket resolveBucket(String clientIp, String path) {
        String key = clientIp + ":" + path;
        return buckets.computeIfAbsent(key, _ -> createBucketForPath(path));
    }

    public void reset() {
        buckets.clear();
    }
}