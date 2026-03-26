package com.loopers.interfaces.consumer;

import com.loopers.application.metrics.MetricsEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

import static org.mockito.Mockito.*;

class CatalogEventConsumerTest {

    private MetricsEventService metricsEventService;
    private Acknowledgment acknowledgment;
    private CatalogEventConsumer consumer;

    @BeforeEach
    void setUp() {
        metricsEventService = mock(MetricsEventService.class);
        acknowledgment = mock(Acknowledgment.class);
        consumer = new CatalogEventConsumer(metricsEventService);
    }

    @DisplayName("PRODUCT_VIEWED 메시지를 수신하면 MetricsEventService에 위임한다")
    @Test
    void consume_productViewed() {
        String payload = "{\"eventId\":\"uuid-1\",\"eventType\":\"PRODUCT_VIEWED\",\"productId\":10}";
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>(
            "catalog.events.topic-v1", 0, 0, "10", payload.getBytes()
        );

        consumer.consume(List.of(record), acknowledgment);

        verify(metricsEventService).process("catalog.events.topic-v1", "uuid-1", "PRODUCT_VIEWED", 10L, 0);
        verify(acknowledgment).acknowledge();
    }

    @DisplayName("PRODUCT_LIKED 메시지를 수신하면 MetricsEventService에 위임한다")
    @Test
    void consume_productLiked() {
        String payload = "{\"eventId\":\"uuid-2\",\"eventType\":\"PRODUCT_LIKED\",\"productId\":10}";
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>(
            "catalog.events.topic-v1", 0, 0, "10", payload.getBytes()
        );

        consumer.consume(List.of(record), acknowledgment);

        verify(metricsEventService).process("catalog.events.topic-v1", "uuid-2", "PRODUCT_LIKED", 10L, 0);
        verify(acknowledgment).acknowledge();
    }

    @DisplayName("ORDER_PLACED 메시지를 수신하면 quantity와 함께 MetricsEventService에 위임한다")
    @Test
    void consume_orderPlaced() {
        String payload = "{\"eventId\":\"uuid-3\",\"eventType\":\"ORDER_PLACED\",\"productId\":10,\"quantity\":2}";
        ConsumerRecord<String, byte[]> record = new ConsumerRecord<>(
            "catalog.events.topic-v1", 0, 0, "10", payload.getBytes()
        );

        consumer.consume(List.of(record), acknowledgment);

        verify(metricsEventService).process("catalog.events.topic-v1", "uuid-3", "ORDER_PLACED", 10L, 2);
        verify(acknowledgment).acknowledge();
    }

    @DisplayName("배치 메시지를 순차적으로 처리한다")
    @Test
    void consume_batchMessages() {
        String payload1 = "{\"eventId\":\"uuid-1\",\"eventType\":\"PRODUCT_VIEWED\",\"productId\":10}";
        String payload2 = "{\"eventId\":\"uuid-2\",\"eventType\":\"PRODUCT_LIKED\",\"productId\":20}";
        ConsumerRecord<String, byte[]> record1 = new ConsumerRecord<>(
            "catalog.events.topic-v1", 0, 0, "10", payload1.getBytes()
        );
        ConsumerRecord<String, byte[]> record2 = new ConsumerRecord<>(
            "catalog.events.topic-v1", 0, 1, "20", payload2.getBytes()
        );

        consumer.consume(List.of(record1, record2), acknowledgment);

        verify(metricsEventService).process("catalog.events.topic-v1", "uuid-1", "PRODUCT_VIEWED", 10L, 0);
        verify(metricsEventService).process("catalog.events.topic-v1", "uuid-2", "PRODUCT_LIKED", 20L, 0);
        verify(acknowledgment).acknowledge();
    }

    @DisplayName("개별 메시지 처리 실패 시 나머지 메시지는 계속 처리하고 ack한다")
    @Test
    void consume_individualFailure_continuesProcessing() {
        String payload1 = "{\"eventId\":\"uuid-1\",\"eventType\":\"PRODUCT_VIEWED\",\"productId\":10}";
        String payload2 = "{\"eventId\":\"uuid-2\",\"eventType\":\"PRODUCT_LIKED\",\"productId\":20}";
        ConsumerRecord<String, byte[]> record1 = new ConsumerRecord<>(
            "catalog.events.topic-v1", 0, 0, "10", payload1.getBytes()
        );
        ConsumerRecord<String, byte[]> record2 = new ConsumerRecord<>(
            "catalog.events.topic-v1", 0, 1, "20", payload2.getBytes()
        );

        doThrow(new RuntimeException("DB 장애")).when(metricsEventService)
            .process(anyString(), eq("uuid-1"), anyString(), anyLong(), anyInt());

        consumer.consume(List.of(record1, record2), acknowledgment);

        verify(metricsEventService).process("catalog.events.topic-v1", "uuid-2", "PRODUCT_LIKED", 20L, 0);
        verify(acknowledgment).acknowledge();
    }
}
