package com.loopers.domain.coupon;

import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class CouponDomainService {

    public CouponIssue createIssue(Long couponId, Long userId, int validDays) {
        ZonedDateTime expiredAt = ZonedDateTime.now().plusDays(validDays);
        return CouponIssue.create(couponId, userId, expiredAt);
    }
}
