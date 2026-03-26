package com.loopers.application.coupon;

import com.loopers.domain.coupon.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CouponIssueProcessorTest {

    private CouponRepository couponRepository;
    private CouponIssueRepository couponIssueRepository;
    private CouponIssueRequestRepository couponIssueRequestRepository;
    private CouponService couponService;
    private CouponIssueProcessor processor;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        couponIssueRepository = mock(CouponIssueRepository.class);
        couponIssueRequestRepository = mock(CouponIssueRequestRepository.class);
        couponService = new CouponService();
        processor = new CouponIssueProcessor(
            couponRepository, couponIssueRepository, couponIssueRequestRepository, couponService
        );
    }

    @DisplayName("process: 수량이 남아있고 중복이 아니면 발급에 성공한다")
    @Test
    void process_success() {
        CouponIssueRequest request = CouponIssueRequest.create(1L, 100L);
        Coupon coupon = Coupon.create("쿠폰", "설명", CouponType.FIXED,
            BigDecimal.valueOf(5000), ZonedDateTime.now().plusDays(30), 100);

        when(couponIssueRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponIssueRepository.findByCouponIdAndUserId(1L, 100L)).thenReturn(Optional.empty());
        when(couponRepository.incrementIssuedQuantity(eq(coupon.getId()), eq(100))).thenReturn(1);
        when(couponIssueRepository.save(any(CouponIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.process(1L);

        assertThat(request.getStatus()).isEqualTo(CouponIssueRequestStatus.SUCCESS);
        verify(couponIssueRepository).save(any(CouponIssue.class));
        verify(couponIssueRequestRepository).save(request);
    }

    @DisplayName("process: 이미 발급받은 사용자면 FAILED 처리한다")
    @Test
    void process_duplicateUser_fails() {
        CouponIssueRequest request = CouponIssueRequest.create(1L, 100L);
        Coupon coupon = Coupon.create("쿠폰", "설명", CouponType.FIXED,
            BigDecimal.valueOf(5000), ZonedDateTime.now().plusDays(30), 100);
        CouponIssue existingIssue = mock(CouponIssue.class);

        when(couponIssueRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponIssueRepository.findByCouponIdAndUserId(1L, 100L)).thenReturn(Optional.of(existingIssue));

        processor.process(1L);

        assertThat(request.getStatus()).isEqualTo(CouponIssueRequestStatus.FAILED);
        assertThat(request.getFailReason()).contains("중복");
        verify(couponIssueRepository, never()).save(any(CouponIssue.class));
    }

    @DisplayName("process: 수량이 소진되면 FAILED 처리한다")
    @Test
    void process_quantityExhausted_fails() {
        CouponIssueRequest request = CouponIssueRequest.create(1L, 100L);
        Coupon coupon = Coupon.create("쿠폰", "설명", CouponType.FIXED,
            BigDecimal.valueOf(5000), ZonedDateTime.now().plusDays(30), 100);

        when(couponIssueRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponIssueRepository.findByCouponIdAndUserId(1L, 100L)).thenReturn(Optional.empty());
        when(couponRepository.incrementIssuedQuantity(eq(coupon.getId()), eq(100))).thenReturn(0);

        processor.process(1L);

        assertThat(request.getStatus()).isEqualTo(CouponIssueRequestStatus.FAILED);
        assertThat(request.getFailReason()).contains("소진");
        verify(couponIssueRepository, never()).save(any(CouponIssue.class));
    }

    @DisplayName("process: maxQuantity가 null인 무제한 쿠폰은 항상 발급 성공한다")
    @Test
    void process_unlimitedCoupon_success() {
        CouponIssueRequest request = CouponIssueRequest.create(2L, 100L);
        Coupon coupon = Coupon.create("무제한 쿠폰", "설명", CouponType.FIXED,
            BigDecimal.valueOf(5000), ZonedDateTime.now().plusDays(30));

        when(couponIssueRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(couponRepository.findById(2L)).thenReturn(Optional.of(coupon));
        when(couponIssueRepository.findByCouponIdAndUserId(2L, 100L)).thenReturn(Optional.empty());
        when(couponIssueRepository.save(any(CouponIssue.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.process(1L);

        assertThat(request.getStatus()).isEqualTo(CouponIssueRequestStatus.SUCCESS);
        verify(couponRepository, never()).incrementIssuedQuantity(anyLong(), anyInt());
    }
}
