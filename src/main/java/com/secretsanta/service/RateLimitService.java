package com.secretsanta.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean allow(String action, String key, int maxAttempts, long windowMillis) {
        String bucketKey = action + ":" + key;
        long now = Instant.now().toEpochMilli();
        Bucket bucket = buckets.compute(bucketKey, (ignored, existing) -> {
            if (existing == null || now >= existing.resetAt) {
                return new Bucket(1, now + windowMillis);
            }
            existing.count++;
            return existing;
        });
        return bucket.count <= maxAttempts;
    }

    private static class Bucket {
        private int count;
        private final long resetAt;

        private Bucket(int count, long resetAt) {
            this.count = count;
            this.resetAt = resetAt;
        }
    }
}
