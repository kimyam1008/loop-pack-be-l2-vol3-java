package com.loopers.domain.metrics;

public interface ProductMetricsRepository {

    void upsertViewCount(Long productId, int delta);

    void upsertLikeCount(Long productId, int delta);

    void upsertSalesCount(Long productId, int delta);
}
