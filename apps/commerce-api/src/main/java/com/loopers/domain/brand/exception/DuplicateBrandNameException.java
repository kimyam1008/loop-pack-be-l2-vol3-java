package com.loopers.domain.brand.exception;

public class DuplicateBrandNameException extends BrandException {

    private final String brandName;

    public DuplicateBrandNameException(String brandName) {
        super("이미 등록된 브랜드명입니다: " + brandName);
        this.brandName = brandName;
    }

    public String getBrandName() {
        return brandName;
    }
}
