package com.loopers.domain.ranking;

import lombok.Getter;

@Getter
public class ProductMetricsAggregation {

    private static final double WEIGHT_VIEW = 0.1;
    private static final double WEIGHT_LIKE = 0.2;
    private static final double WEIGHT_SALES = 0.7;

    private final Long productId;
    private final long viewCount;
    private final long likeCount;
    private final long salesCount;

    public ProductMetricsAggregation(Long productId, long viewCount, long likeCount, long salesCount) {
        this.productId = productId;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.salesCount = salesCount;
    }

    public double calculateScore() {
        return viewCount * WEIGHT_VIEW + likeCount * WEIGHT_LIKE + salesCount * WEIGHT_SALES;
    }
}
