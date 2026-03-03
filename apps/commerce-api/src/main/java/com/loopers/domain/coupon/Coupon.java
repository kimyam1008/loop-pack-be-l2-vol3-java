package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "coupons")
@Getter
public class Coupon extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CouponType type;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "valid_days", nullable = false)
    private int validDays;

    protected Coupon() {
    }

    private Coupon(String name, String description, CouponType type, BigDecimal discountValue, int validDays) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.discountValue = discountValue;
        this.validDays = validDays;
    }

    public static Coupon create(String name, String description, CouponType type, BigDecimal discountValue, int validDays) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("쿠폰 이름은 필수입니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("쿠폰 타입은 필수입니다.");
        }
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("할인 값은 0보다 커야 합니다.");
        }
        if (type == CouponType.RATE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("정률 할인은 100%를 초과할 수 없습니다.");
        }
        if (validDays <= 0) {
            throw new IllegalArgumentException("유효 기간은 1일 이상이어야 합니다.");
        }
        return new Coupon(name, description, type, discountValue, validDays);
    }

    public void update(String name, String description, CouponType type, BigDecimal discountValue, int validDays) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("쿠폰 이름은 필수입니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("쿠폰 타입은 필수입니다.");
        }
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("할인 값은 0보다 커야 합니다.");
        }
        if (type == CouponType.RATE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("정률 할인은 100%를 초과할 수 없습니다.");
        }
        if (validDays <= 0) {
            throw new IllegalArgumentException("유효 기간은 1일 이상이어야 합니다.");
        }
        this.name = name;
        this.description = description;
        this.type = type;
        this.discountValue = discountValue;
        this.validDays = validDays;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return switch (type) {
            case FIXED -> discountValue.min(orderAmount);
            case RATE -> orderAmount.multiply(discountValue)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        };
    }
}
