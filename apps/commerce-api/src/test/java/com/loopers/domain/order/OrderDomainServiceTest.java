package com.loopers.domain.order;

import com.loopers.domain.order.exception.EmptyOrderItemException;
import com.loopers.domain.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderDomainServiceTest {

    private OrderDomainService orderDomainService;

    @BeforeEach
    void setUp() {
        orderDomainService = new OrderDomainService();
    }

    @DisplayName("createOrder: 주문 생성 시 재고를 차감하고 스냅샷 항목을 만든다")
    @Test
    void createOrder_success() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("테스트 상품");
        when(product.getPrice()).thenReturn(BigDecimal.valueOf(2000));

        Order order = orderDomainService.createOrder(
            10L,
            List.of(new OrderDomainService.ProductOrderLine(product, 3))
        );

        verify(product).decreaseStock(3);
        assertThat(order.getUserId()).isEqualTo(10L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("6000");
    }

    @DisplayName("createOrder: 주문 항목이 비어있으면 예외가 발생한다")
    @Test
    void createOrder_fail_emptyItems() {
        assertThatThrownBy(() -> orderDomainService.createOrder(10L, List.of()))
            .isInstanceOf(EmptyOrderItemException.class)
            .hasMessageContaining("주문 항목은 1개 이상이어야 합니다");
    }

    @DisplayName("createOrder: 재고가 부족하면 도메인 예외를 전파한다")
    @Test
    void createOrder_fail_notEnoughStock() {
        Product product = mock(Product.class);
        doThrow(new IllegalArgumentException("재고가 부족합니다"))
            .when(product).decreaseStock(2);

        assertThatThrownBy(() ->
            orderDomainService.createOrder(
                10L,
                List.of(new OrderDomainService.ProductOrderLine(product, 2))
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("재고가 부족합니다");
    }

    @DisplayName("cancelOrder: 주문 취소 시 재고를 복구한다")
    @Test
    void cancelOrder_success() {
        Product placeProduct = mock(Product.class);
        when(placeProduct.getId()).thenReturn(1L);
        when(placeProduct.getName()).thenReturn("테스트 상품");
        when(placeProduct.getPrice()).thenReturn(BigDecimal.valueOf(1000));

        Order order = orderDomainService.createOrder(
            10L,
            List.of(new OrderDomainService.ProductOrderLine(placeProduct, 2))
        );
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        Product restoreProduct = mock(Product.class);
        boolean cancelled = orderDomainService.cancelOrder(
            order,
            List.of(new OrderDomainService.ProductOrderLine(restoreProduct, 2))
        );

        assertThat(cancelled).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(restoreProduct).increaseStock(2);
    }

    @DisplayName("cancelOrder: 이미 취소된 주문이면 재고 복구를 수행하지 않는다")
    @Test
    void cancelOrder_idempotent_whenAlreadyCancelled() {
        Product placeProduct = mock(Product.class);
        when(placeProduct.getId()).thenReturn(1L);
        when(placeProduct.getName()).thenReturn("테스트 상품");
        when(placeProduct.getPrice()).thenReturn(BigDecimal.valueOf(1000));

        Order order = orderDomainService.createOrder(
            10L,
            List.of(new OrderDomainService.ProductOrderLine(placeProduct, 1))
        );
        order.cancel();

        Product restoreProduct = mock(Product.class);
        boolean cancelled = orderDomainService.cancelOrder(
            order,
            List.of(new OrderDomainService.ProductOrderLine(restoreProduct, 1))
        );

        assertThat(cancelled).isFalse();
        verify(restoreProduct, never()).increaseStock(anyInt());
    }
}
