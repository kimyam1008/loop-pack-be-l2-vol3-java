package com.loopers.application.queue;

import com.loopers.domain.queue.QueueRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QueueServiceTest {

    private QueueRepository queueRepository;
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        queueRepository = mock(QueueRepository.class);
        queueService = new QueueService(queueRepository);
    }

    @DisplayName("대기열 진입")
    @Nested
    class Enter {

        @DisplayName("신규 유저가 대기열에 진입하면 순번을 반환한다")
        @Test
        void enter_success() {
            Long userId = 1L;
            when(queueRepository.enterWaitQueue(eq(userId), anyDouble())).thenReturn(true);
            when(queueRepository.getWaitPosition(userId)).thenReturn(10L);
            when(queueRepository.getWaitTotalCount()).thenReturn(100L);

            QueueDto.QueueEntryResult result = queueService.enter(userId);

            assertThat(result.position()).isEqualTo(10L);
            assertThat(result.totalWaiting()).isEqualTo(100L);
            assertThat(result.estimatedWaitSeconds()).isGreaterThanOrEqualTo(0L);
            verify(queueRepository).enterWaitQueue(eq(userId), anyDouble());
        }

        @DisplayName("이미 진입한 유저가 다시 요청하면 기존 순번을 반환한다")
        @Test
        void enter_alreadyEntered_returnsExistingPosition() {
            Long userId = 1L;
            when(queueRepository.enterWaitQueue(eq(userId), anyDouble())).thenReturn(false);
            when(queueRepository.getWaitPosition(userId)).thenReturn(5L);
            when(queueRepository.getWaitTotalCount()).thenReturn(100L);

            QueueDto.QueueEntryResult result = queueService.enter(userId);

            assertThat(result.position()).isEqualTo(5L);
        }
    }

    @DisplayName("순번 조회")
    @Nested
    class GetPosition {

        @DisplayName("대기 중인 유저의 순번과 예상 대기 시간을 반환한다")
        @Test
        void getPosition_waiting() {
            Long userId = 1L;
            when(queueRepository.getToken(userId)).thenReturn(null);
            when(queueRepository.getWaitPosition(userId)).thenReturn(350L);
            when(queueRepository.getWaitTotalCount()).thenReturn(1000L);

            QueueDto.QueuePositionResult result = queueService.getPosition(userId);

            assertThat(result.position()).isEqualTo(350L);
            assertThat(result.totalWaiting()).isEqualTo(1000L);
            assertThat(result.estimatedWaitSeconds()).isEqualTo(2L);
            assertThat(result.token()).isNull();
        }

        @DisplayName("토큰이 발급된 유저는 position 0과 토큰을 반환한다")
        @Test
        void getPosition_tokenIssued() {
            Long userId = 1L;
            String token = "abc-123-def";
            when(queueRepository.getToken(userId)).thenReturn(token);

            QueueDto.QueuePositionResult result = queueService.getPosition(userId);

            assertThat(result.position()).isEqualTo(0L);
            assertThat(result.token()).isEqualTo(token);
        }

        @DisplayName("대기열에 없는 유저가 조회하면 예외가 발생한다")
        @Test
        void getPosition_notEntered() {
            Long userId = 1L;
            when(queueRepository.getToken(userId)).thenReturn(null);
            when(queueRepository.getWaitPosition(userId)).thenReturn(null);

            assertThatThrownBy(() -> queueService.getPosition(userId))
                    .isInstanceOf(CoreException.class)
                    .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                            .isEqualTo(ErrorType.QUEUE_NOT_ENTERED));
        }
    }
}
