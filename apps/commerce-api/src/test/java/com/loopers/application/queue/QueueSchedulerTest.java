package com.loopers.application.queue;

import com.loopers.domain.queue.QueueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QueueSchedulerTest {

    private QueueRepository queueRepository;
    private QueueScheduler queueScheduler;

    @BeforeEach
    void setUp() {
        queueRepository = mock(QueueRepository.class);
        queueScheduler = new QueueScheduler(queueRepository);
    }

    @DisplayName("대기열에 유저가 있으면 꺼내서 토큰을 발급한다")
    @Test
    void activateUsers_success() {
        when(queueRepository.popFromWaitQueue(18)).thenReturn(List.of(1L, 2L, 3L));

        queueScheduler.activateUsers();

        verify(queueRepository).issueToken(eq(1L), anyString(), eq(300L));
        verify(queueRepository).issueToken(eq(2L), anyString(), eq(300L));
        verify(queueRepository).issueToken(eq(3L), anyString(), eq(300L));
    }

    @DisplayName("대기열이 비어있으면 토큰을 발급하지 않는다")
    @Test
    void activateUsers_emptyQueue() {
        when(queueRepository.popFromWaitQueue(18)).thenReturn(List.of());

        queueScheduler.activateUsers();

        verify(queueRepository, never()).issueToken(anyLong(), anyString(), anyLong());
    }
}
