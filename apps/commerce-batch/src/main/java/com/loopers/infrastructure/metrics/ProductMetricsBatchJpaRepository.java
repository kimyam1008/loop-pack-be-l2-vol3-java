package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.ranking.ProductMetricsAggregation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductMetricsBatchJpaRepository extends JpaRepository<ProductMetrics, Long> {

    @Query("SELECT new com.loopers.domain.ranking.ProductMetricsAggregation(" +
        "pm.productId, SUM(pm.viewCount), SUM(pm.likeCount), SUM(pm.salesCount)) " +
        "FROM ProductMetrics pm " +
        "WHERE pm.metricDate BETWEEN :fromDate AND :toDate " +
        "GROUP BY pm.productId")
    List<ProductMetricsAggregation> aggregateByDateRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate);
}
