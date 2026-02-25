package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderItemTest {

    @DisplayName("createSnapshot: 상품 스냅샷과 소계를 생성한다")
    @Test
    void createSnapshot_success() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("테스트 상품");
        when(product.getPrice()).thenReturn(BigDecimal.valueOf(15000));

        OrderItem orderItem = OrderItem.createSnapshot(product, 2);

        assertThat(orderItem.getProductId()).isEqualTo(1L);
        assertThat(orderItem.getProductName()).isEqualTo("테스트 상품");
        assertThat(orderItem.getProductPrice()).isEqualByComparingTo("15000");
        assertThat(orderItem.getQuantity()).isEqualTo(2);
        assertThat(orderItem.getSubtotal()).isEqualByComparingTo("30000");
    }

    @DisplayName("createSnapshot: 수량이 0 이하이면 예외가 발생한다")
    @Test
    void createSnapshot_fail_invalidQuantity() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("테스트 상품");
        when(product.getPrice()).thenReturn(BigDecimal.valueOf(15000));

        assertThatThrownBy(() -> OrderItem.createSnapshot(product, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("주문 수량은 1 이상이어야 합니다");
    }

    @DisplayName("createSnapshot: 상품이 null이면 예외가 발생한다")
    @Test
    void createSnapshot_fail_nullProduct() {
        assertThatThrownBy(() -> OrderItem.createSnapshot(null, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("상품 정보는 필수입니다");
    }
}
