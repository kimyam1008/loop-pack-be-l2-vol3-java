package com.loopers.domain.ranking;

public interface ProductRankingRepository {

    void incrementScore(String key, Long productId, double score);

    boolean exists(String key);

    void setTtlIfAbsent(String key, long ttlSeconds);
}
