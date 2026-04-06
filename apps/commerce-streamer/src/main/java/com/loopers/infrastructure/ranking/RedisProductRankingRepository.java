package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;

@Repository
@RequiredArgsConstructor
public class RedisProductRankingRepository implements ProductRankingRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void incrementScore(String key, Long productId, double score) {
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), score);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void setTtlIfAbsent(String key, long ttlSeconds) {
        Long ttl = redisTemplate.getExpire(key);
        if (ttl == null || ttl == -1) {
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        }
    }

    @Override
    public void unionStoreWithWeight(String destKey, String sourceKey, double weight) {
        redisTemplate.opsForZSet().unionAndStore(
            sourceKey,
            Collections.emptyList(),
            destKey,
            Aggregate.SUM,
            Weights.of(weight)
        );
    }
}
