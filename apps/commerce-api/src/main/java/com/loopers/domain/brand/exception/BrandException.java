package com.loopers.domain.brand.exception;

public class BrandException extends RuntimeException {

    public BrandException(String message) {
        super(message);
    }

    public BrandException(String message, Throwable cause) {
        super(message, cause);
    }
}
