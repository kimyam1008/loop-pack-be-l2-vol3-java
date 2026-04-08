package com.loopers.application.ranking;

import com.loopers.domain.ranking.ProductRankingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.*;

class RankingCarryOverSchedulerTest {

    private ProductRankingRepository productRankingRepository;
    private RankingCarryOverScheduler scheduler;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final String todayKey = "ranking:all:" +
        LocalDate.now(ZoneId.of("Asia/Seoul")).format(DATE_FORMAT);
    private final String yesterdayKey = "ranking:all:" +
        LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).format(DATE_FORMAT);

    @BeforeEach
    void setUp() {
        productRankingRepository = mock(ProductRankingRepository.class);
        scheduler = new RankingCarryOverScheduler(productRankingRepository);
    }

    @DisplayName("어제 랭킹 데이터가 있고 오늘 키가 없으면 10% 가중치로 복사한다")
    @Test
    void carryOver_copiesWithWeight() {
        when(productRankingRepository.exists(yesterdayKey)).thenReturn(true);
        when(productRankingRepository.exists(todayKey)).thenReturn(false);

        scheduler.carryOver();

        verify(productRankingRepository).unionStoreWithWeight(todayKey, yesterdayKey, 0.1);
        verify(productRankingRepository).setTtlIfAbsent(todayKey, 172_800);
    }

    @DisplayName("어제 랭킹 데이터가 없으면 carry-over를 생략한다")
    @Test
    void carryOver_noYesterdayData_skips() {
        when(productRankingRepository.exists(yesterdayKey)).thenReturn(false);

        scheduler.carryOver();

        verify(productRankingRepository, never()).unionStoreWithWeight(anyString(), anyString(), anyDouble());
    }

    @DisplayName("오늘 키가 이미 존재하면 carry-over를 생략한다 (멱등성)")
    @Test
    void carryOver_todayKeyExists_skips() {
        when(productRankingRepository.exists(yesterdayKey)).thenReturn(true);
        when(productRankingRepository.exists(todayKey)).thenReturn(true);

        scheduler.carryOver();

        verify(productRankingRepository, never()).unionStoreWithWeight(anyString(), anyString(), anyDouble());
    }
}
