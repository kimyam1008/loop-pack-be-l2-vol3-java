package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductMetricsRepositoryAdapter implements ProductMetricsRepository {

    private final ProductMetricsJpaRepository jpaRepository;

    @Override
    public void upsertViewCount(Long productId, int delta) {
        jpaRepository.upsertViewCount(productId, delta);
    }

    @Override
    public void upsertLikeCount(Long productId, int delta) {
        jpaRepository.upsertLikeCount(productId, delta);
    }

    @Override
    public void upsertSalesCount(Long productId, int delta) {
        jpaRepository.upsertSalesCount(productId, delta);
    }
}
