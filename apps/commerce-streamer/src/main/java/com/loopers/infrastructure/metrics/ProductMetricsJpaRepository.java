package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long> {

    @Modifying
    @Query(value = "INSERT INTO product_metrics (product_id, view_count, like_count, sales_count) " +
        "VALUES (:productId, :delta, 0, 0) " +
        "ON DUPLICATE KEY UPDATE view_count = view_count + :delta", nativeQuery = true)
    void upsertViewCount(@Param("productId") Long productId, @Param("delta") int delta);

    @Modifying
    @Query(value = "INSERT INTO product_metrics (product_id, view_count, like_count, sales_count) " +
        "VALUES (:productId, 0, :delta, 0) " +
        "ON DUPLICATE KEY UPDATE like_count = like_count + :delta", nativeQuery = true)
    void upsertLikeCount(@Param("productId") Long productId, @Param("delta") int delta);

    @Modifying
    @Query(value = "INSERT INTO product_metrics (product_id, view_count, like_count, sales_count) " +
        "VALUES (:productId, 0, 0, :delta) " +
        "ON DUPLICATE KEY UPDATE sales_count = sales_count + :delta", nativeQuery = true)
    void upsertSalesCount(@Param("productId") Long productId, @Param("delta") int delta);
}
