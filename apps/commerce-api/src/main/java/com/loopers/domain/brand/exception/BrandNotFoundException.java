package com.loopers.domain.brand.exception;

public class BrandNotFoundException extends BrandException {

    private final Long brandId;

    public BrandNotFoundException(Long brandId) {
        super("브랜드를 찾을 수 없습니다: " + brandId);
        this.brandId = brandId;
    }

    public Long getBrandId() {
        return brandId;
    }
}
