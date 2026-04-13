package com.loopers.batch.job.ranking.step;

import com.loopers.domain.ranking.MvProductRankMonthly;
import com.loopers.domain.ranking.ProductMetricsAggregation;
import com.loopers.infrastructure.metrics.ProductMetricsBatchJpaRepository;
import com.loopers.infrastructure.ranking.MvProductRankMonthlyJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyRankingTasklet implements Tasklet {

    private static final int TOP_N = 100;
    private static final int MONTHLY_DAYS = 30;

    private final ProductMetricsBatchJpaRepository productMetricsRepository;
    private final MvProductRankMonthlyJpaRepository mvMonthlyRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate baseDate = (LocalDate) chunkContext.getStepContext()
            .getJobParameters().get("baseDate");

        if (baseDate == null) {
            throw new IllegalArgumentException("baseDate 파라미터가 필요합니다");
        }

        LocalDate fromDate = baseDate.minusDays(MONTHLY_DAYS - 1);

        List<ProductMetricsAggregation> aggregations =
            productMetricsRepository.aggregateByDateRange(fromDate, baseDate);

        log.info("월간 랭킹 집계 - baseDate: {}, 대상 상품 수: {}", baseDate, aggregations.size());

        mvMonthlyRepository.deleteByAggregatedAt(baseDate);

        AtomicInteger rankCounter = new AtomicInteger(1);
        List<MvProductRankMonthly> ranks = aggregations.stream()
            .sorted(Comparator.comparingDouble(ProductMetricsAggregation::calculateScore).reversed())
            .limit(TOP_N)
            .map(agg -> MvProductRankMonthly.of(agg, rankCounter.getAndIncrement(), baseDate))
            .toList();

        mvMonthlyRepository.saveAll(ranks);

        log.info("월간 랭킹 적재 완료 - {}건", ranks.size());
        return RepeatStatus.FINISHED;
    }
}
