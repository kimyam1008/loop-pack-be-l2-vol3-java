package com.loopers.domain.order.exception;

public class OrderNotFoundException extends OrderException {

    private final Long orderId;

    public OrderNotFoundException(Long orderId) {
        super("주문을 찾을 수 없습니다: " + orderId);
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
