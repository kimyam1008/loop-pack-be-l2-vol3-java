package com.loopers.domain.product.exception;

public class DuplicateProductNameException extends ProductException {

    private final String productName;

    public DuplicateProductNameException(String productName) {
        super("이미 존재하는 상품명입니다: " + productName);
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }
}
