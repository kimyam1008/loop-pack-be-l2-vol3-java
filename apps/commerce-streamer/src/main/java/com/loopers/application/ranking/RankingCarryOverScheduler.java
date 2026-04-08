package com.loopers.application.ranking;

import com.loopers.domain.ranking.ProductRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingCarryOverScheduler {

    private static final String RANKING_KEY_PREFIX = "ranking:all:";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final double CARRY_OVER_WEIGHT = 0.1;
    private static final long TTL_SECONDS = 172_800; // 2일

    private final ProductRankingRepository productRankingRepository;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void carryOver() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate yesterday = today.minusDays(1);

        String yesterdayKey = RANKING_KEY_PREFIX + yesterday.format(DATE_FORMAT);
        String todayKey = RANKING_KEY_PREFIX + today.format(DATE_FORMAT);

        if (!productRankingRepository.exists(yesterdayKey)) {
            log.info("어제 랭킹 데이터 없음 - carry-over 생략: {}", yesterdayKey);
            return;
        }

        if (productRankingRepository.exists(todayKey)) {
            log.info("오늘 랭킹 키 이미 존재 - carry-over 생략 (멱등성): {}", todayKey);
            return;
        }

        productRankingRepository.unionStoreWithWeight(todayKey, yesterdayKey, CARRY_OVER_WEIGHT);
        productRankingRepository.setTtlIfAbsent(todayKey, TTL_SECONDS);

        log.info("Score carry-over 완료: {} → {} (weight: {})", yesterdayKey, todayKey, CARRY_OVER_WEIGHT);
    }
}
