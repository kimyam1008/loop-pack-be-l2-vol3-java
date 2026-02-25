package com.loopers.domain.order;

import com.loopers.domain.order.exception.EmptyOrderItemException;
import com.loopers.domain.product.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Order 도메인 서비스
 * 주문 엔티티와 상품 엔티티 협력 규칙을 담당한다.
 */
@Component
public class OrderDomainService {

    public Order createOrder(Long userId, List<ProductOrderLine> orderLines) {
        if (orderLines == null || orderLines.isEmpty()) {
            throw new EmptyOrderItemException();
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (ProductOrderLine line : orderLines) {
            if (line == null || line.product() == null) {
                throw new IllegalArgumentException("상품 정보는 필수입니다");
            }

            Product product = line.product();
            Integer quantity = line.quantity();

            product.decreaseStock(quantity);
            orderItems.add(OrderItem.createSnapshot(product, quantity));
        }

        return Order.create(userId, orderItems);
    }

    public boolean cancelOrder(Order order, List<ProductOrderLine> restoreLines) {
        if (order == null) {
            throw new IllegalArgumentException("주문 정보는 필수입니다");
        }
        if (restoreLines == null) {
            throw new IllegalArgumentException("복구 상품 정보는 필수입니다");
        }
        if (!order.cancel()) {
            return false;
        }

        for (ProductOrderLine line : restoreLines) {
            if (line == null || line.product() == null) {
                throw new IllegalArgumentException("상품 정보는 필수입니다");
            }
            line.product().increaseStock(line.quantity());
        }
        return true;
    }

    public record ProductOrderLine(Product product, Integer quantity) {
    }
}
