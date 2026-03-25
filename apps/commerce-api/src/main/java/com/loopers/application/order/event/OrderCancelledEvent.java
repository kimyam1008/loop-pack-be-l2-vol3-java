package com.loopers.application.order.event;

import com.loopers.domain.order.Order;

public record OrderCancelledEvent(
    Long orderId,
    Long userId
) {

    public static OrderCancelledEvent from(Order order) {
        return new OrderCancelledEvent(order.getId(), order.getUserId());
    }
}
