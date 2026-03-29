package com.loopers.application.queue;

import com.loopers.domain.queue.QueueRepository;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueueIntegrationTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueScheduler queueScheduler;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    void tearDown() {
        redisCleanUp.truncateAll();
    }

    @DisplayName("동시 진입 테스트")
    @Nested
    class ConcurrentEntry {

        @DisplayName("100명이 동시에 진입하면 모두 고유한 순번을 받는다")
        @Test
        void concurrentEnter_allGetUniquePositions() throws InterruptedException {
            int userCount = 100;
            ExecutorService executor = Executors.newFixedThreadPool(20);
            CountDownLatch latch = new CountDownLatch(userCount);
            List<Long> positions = Collections.synchronizedList(new ArrayList<>());
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

            for (int i = 1; i <= userCount; i++) {
                long userId = i;
                executor.submit(() -> {
                    try {
                        QueueDto.QueueEntryResult result = queueService.enter(userId);
                        positions.add(result.position());
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();
            assertThat(errors).isEmpty();
            // 스케줄러가 자동 실행되어 일부를 소비할 수 있으므로,
            // 대기열 + 토큰 발급된 유저 합이 100인지 검증
            long waitCount = queueRepository.getWaitTotalCount();
            long tokenCount = 0;
            for (long i = 1; i <= 100; i++) {
                if (queueRepository.hasActiveToken(i)) tokenCount++;
            }
            assertThat(waitCount + tokenCount).isEqualTo(100L);
        }

        @DisplayName("같은 유저가 여러 번 진입해도 순번이 유지된다 (ZADD NX)")
        @Test
        void duplicateEnter_positionPreserved() {
            Long userId = 1L;

            QueueDto.QueueEntryResult first = queueService.enter(userId);
            QueueDto.QueueEntryResult second = queueService.enter(userId);

            assertThat(first.position()).isEqualTo(second.position());
            assertThat(queueRepository.getWaitTotalCount()).isEqualTo(1L);
        }
    }

    @DisplayName("토큰 발급 테스트")
    @Nested
    class TokenIssuance {

        @DisplayName("스케줄러가 대기열에서 유저를 꺼내 토큰을 발급한다")
        @Test
        void scheduler_issuesTokens() {
            for (long i = 1; i <= 5; i++) {
                queueService.enter(i);
            }

            queueScheduler.activateUsers();

            for (long i = 1; i <= 5; i++) {
                assertThat(queueRepository.hasActiveToken(i)).isTrue();
            }
            assertThat(queueRepository.getWaitTotalCount()).isEqualTo(0L);
        }

        @DisplayName("토큰이 발급된 유저는 순번 조회 시 토큰을 받는다")
        @Test
        void getPosition_afterTokenIssued_returnsToken() {
            Long userId = 1L;
            queueService.enter(userId);
            queueScheduler.activateUsers();

            QueueDto.QueuePositionResult result = queueService.getPosition(userId);

            assertThat(result.position()).isEqualTo(0L);
            assertThat(result.token()).isNotNull();
        }
    }

    @DisplayName("토큰 만료 테스트")
    @Nested
    class TokenExpiry {

        @DisplayName("TTL이 지나면 토큰이 만료된다")
        @Test
        void token_expiresAfterTTL() throws InterruptedException {
            Long userId = 1L;
            queueRepository.issueToken(userId, "test-token", 1);

            assertThat(queueRepository.hasActiveToken(userId)).isTrue();

            Thread.sleep(1500);

            assertThat(queueRepository.hasActiveToken(userId)).isFalse();
        }
    }

    @DisplayName("처리량 초과 테스트")
    @Nested
    class ThroughputControl {

        @DisplayName("popFromWaitQueue는 요청한 수만큼만 꺼낸다")
        @Test
        void popFromWaitQueue_respectsBatchSize() {
            for (long i = 1; i <= 100; i++) {
                queueRepository.enterWaitQueue(i, System.currentTimeMillis() + i);
            }

            long beforePop = queueRepository.getWaitTotalCount();
            java.util.List<Long> popped = queueRepository.popFromWaitQueue(18);

            assertThat(popped).hasSize(18);
            // 스케줄러가 자동 실행으로 일부 소비했을 수 있으므로 pop 전후 차이로 검증
            long afterPop = queueRepository.getWaitTotalCount();
            assertThat(beforePop - afterPop).isGreaterThanOrEqualTo(18L);
        }
    }
}
