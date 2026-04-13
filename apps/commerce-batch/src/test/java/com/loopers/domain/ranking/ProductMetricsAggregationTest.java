package com.loopers.domain.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMetricsAggregationTest {

    @DisplayName("가중치 점수를 계산한다: view×0.1 + like×0.2 + sales×0.7")
    @Test
    void calculateScore() {
        var aggregation = new ProductMetricsAggregation(1L, 100, 50, 10);

        // 100×0.1 + 50×0.2 + 10×0.7 = 10 + 10 + 7 = 27.0
        assertThat(aggregation.calculateScore()).isEqualTo(27.0);
    }

    @DisplayName("모든 카운트가 0이면 점수도 0이다")
    @Test
    void calculateScore_allZero() {
        var aggregation = new ProductMetricsAggregation(1L, 0, 0, 0);

        assertThat(aggregation.calculateScore()).isEqualTo(0.0);
    }

    @DisplayName("음수 카운트가 있으면 점수가 음수가 될 수 있다")
    @Test
    void calculateScore_negativeCount() {
        var aggregation = new ProductMetricsAggregation(1L, 0, -5, 0);

        // 0×0.1 + (-5)×0.2 + 0×0.7 = -1.0
        assertThat(aggregation.calculateScore()).isEqualTo(-1.0);
    }
}
