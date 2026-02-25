package com.loopers.domain.product.exception;

public class ProductNotFoundException extends ProductException {

    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("상품을 찾을 수 없습니다: " + productId);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
