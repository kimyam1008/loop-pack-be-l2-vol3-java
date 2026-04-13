package com.loopers.batch.job.ranking;

import com.loopers.batch.job.ranking.step.MonthlyRankingTasklet;
import com.loopers.batch.job.ranking.step.WeeklyRankingTasklet;
import com.loopers.batch.listener.JobListener;
import com.loopers.batch.listener.StepMonitorListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = RankingAggregationJobConfig.JOB_NAME)
@RequiredArgsConstructor
@Configuration
public class RankingAggregationJobConfig {

    public static final String JOB_NAME = "rankingAggregationJob";
    private static final String STEP_WEEKLY = "weeklyRankingStep";
    private static final String STEP_MONTHLY = "monthlyRankingStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobListener jobListener;
    private final StepMonitorListener stepMonitorListener;
    private final WeeklyRankingTasklet weeklyRankingTasklet;
    private final MonthlyRankingTasklet monthlyRankingTasklet;

    @Bean(JOB_NAME)
    public Job rankingAggregationJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(weeklyRankingStep())
            .next(monthlyRankingStep())
            .listener(jobListener)
            .build();
    }

    @Bean(STEP_WEEKLY)
    public Step weeklyRankingStep() {
        return new StepBuilder(STEP_WEEKLY, jobRepository)
            .tasklet(weeklyRankingTasklet, transactionManager)
            .listener(stepMonitorListener)
            .build();
    }

    @Bean(STEP_MONTHLY)
    public Step monthlyRankingStep() {
        return new StepBuilder(STEP_MONTHLY, jobRepository)
            .tasklet(monthlyRankingTasklet, transactionManager)
            .listener(stepMonitorListener)
            .build();
    }
}
