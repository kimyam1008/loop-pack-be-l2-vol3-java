package com.loopers.domain.brand.exception;

public class BrandNotDeletedException extends BrandException {

    private final Long brandId;

    public BrandNotDeletedException(Long brandId) {
        super("삭제되지 않은 브랜드입니다: " + brandId);
        this.brandId = brandId;
    }

    public Long getBrandId() {
        return brandId;
    }
}
