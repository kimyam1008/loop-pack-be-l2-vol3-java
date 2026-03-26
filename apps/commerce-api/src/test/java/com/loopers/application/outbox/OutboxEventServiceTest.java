package com.loopers.application.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxEventServiceTest {

    private OutboxEventRepository outboxEventRepository;
    private OutboxEventService outboxEventService;

    @BeforeEach
    void setUp() {
        outboxEventRepository = mock(OutboxEventRepository.class);
        outboxEventService = new OutboxEventService(outboxEventRepository, new ObjectMapper());
    }

    @DisplayName("save: Outbox 이벤트를 JSON 직렬화하여 저장한다")
    @Test
    void save_serializesAndSaves() {
        Map<String, Object> payload = Map.of("productId", 1L, "quantity", 2);

        outboxEventService.save("PRODUCT", 1L, "ORDER_PLACED", payload);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertThat(saved.getAggregateType()).isEqualTo("PRODUCT");
        assertThat(saved.getAggregateId()).isEqualTo(1L);
        assertThat(saved.getEventType()).isEqualTo("ORDER_PLACED");
        assertThat(saved.getPayload()).contains("productId");
        assertThat(saved.getPayload()).contains("eventId");
        assertThat(saved.isPublished()).isFalse();
    }

    @DisplayName("save: payload에 eventId(UUID)가 자동으로 포함된다")
    @Test
    void save_includesEventId() {
        Map<String, Object> payload = Map.of("productId", 10L);

        outboxEventService.save("PRODUCT", 10L, "PRODUCT_LIKED", payload);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        String savedPayload = captor.getValue().getPayload();
        assertThat(savedPayload).contains("eventId");
        // eventId는 UUID 형식이어야 한다
        assertThat(savedPayload).containsPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
}
