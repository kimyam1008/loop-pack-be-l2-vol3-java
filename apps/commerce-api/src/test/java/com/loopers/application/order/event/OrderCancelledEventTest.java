package com.loopers.application.order.event;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCancelledEventTest {

    @DisplayName("Order로부터 OrderCancelledEvent를 생성하면 orderId와 userId가 올바르게 매핑된다")
    @Test
    void from_mapsOrderFieldsCorrectly() {
        OrderItem item = OrderItem.createSnapshot(10L, "상품A", BigDecimal.valueOf(1000), 1);
        Order order = Order.create(100L, List.of(item));

        OrderCancelledEvent event = OrderCancelledEvent.from(order);

        assertThat(event.userId()).isEqualTo(100L);
    }
}
