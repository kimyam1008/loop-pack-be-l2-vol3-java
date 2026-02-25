package com.loopers.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @DisplayName("Product: 재고가 충분하면 차감할 수 있다")
    @Test
    void decreaseStock_success() {
        Product product = Product.create(1L, "상품", "설명", BigDecimal.valueOf(1000), 10);

        product.decreaseStock(3);

        assertThat(product.getStock()).isEqualTo(7);
    }

    @DisplayName("Product: 재고가 부족하면 차감 시 예외가 발생한다")
    @Test
    void decreaseStock_fail_notEnough() {
        Product product = Product.create(1L, "상품", "설명", BigDecimal.valueOf(1000), 1);

        assertThatThrownBy(() -> product.decreaseStock(2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("재고가 부족합니다");
    }

    @DisplayName("Product: 좋아요 수는 0 미만으로 내려가지 않는다")
    @Test
    void decreaseLikeCount_neverNegative() {
        Product product = Product.create(1L, "상품", "설명", BigDecimal.valueOf(1000), 10);

        product.decreaseLikeCount();

        assertThat(product.getLikeCount()).isZero();
    }

    @DisplayName("Product: soft delete / restore가 동작한다")
    @Test
    void delete_restore_success() {
        Product product = Product.create(1L, "상품", "설명", BigDecimal.valueOf(1000), 10);

        product.delete();
        assertThat(product.isDeleted()).isTrue();

        product.restore();
        assertThat(product.isDeleted()).isFalse();
    }
}
