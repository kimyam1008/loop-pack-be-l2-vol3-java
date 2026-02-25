package com.loopers.domain.product.exception;

public class ProductNotDeletedException extends ProductException {

    private final Long productId;

    public ProductNotDeletedException(Long productId) {
        super("삭제되지 않은 상품입니다: " + productId);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
