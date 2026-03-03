package com.loopers.application.coupon;

import com.loopers.application.user.UserApplicationService;
import com.loopers.application.user.UserDto;
import com.loopers.domain.coupon.CouponIssueStatus;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.user.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CouponApplicationServiceIntegrationTest {

    @Autowired
    private CouponApplicationService couponApplicationService;

    @Autowired
    private UserApplicationService userApplicationService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long userId;
    private Long couponId;

    @BeforeEach
    void setUp() {
        UserDto.UserInfo user = userApplicationService.register(
            "cpnuser1", "TestPass1!", "쿠폰테스터",
            LocalDate.of(2000, 1, 1), "coupon1@loopers.com", Gender.FEMALE
        );
        userId = user.id();

        CouponDto.CouponInfo coupon = couponApplicationService.registerTemplate(
            "신규 가입 쿠폰", "가입 혜택", CouponType.FIXED, BigDecimal.valueOf(5000), 30
        );
        couponId = coupon.id();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    // ─────────────────────────────────────────
    // Admin: 쿠폰 템플릿 등록
    // ─────────────────────────────────────────

    @DisplayName("registerTemplate: 쿠폰 템플릿 등록 시 DB에 저장된다")
    @Test
    void registerTemplate_success() {
        CouponDto.CouponInfo result = couponApplicationService.registerTemplate(
            "여름 할인 쿠폰", "여름 이벤트", CouponType.RATE, BigDecimal.valueOf(10), 7
        );

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("여름 할인 쿠폰");
        assertThat(result.type()).isEqualTo(CouponType.RATE);
        assertThat(result.discountValue()).isEqualByComparingTo("10");
        assertThat(result.validDays()).isEqualTo(7);
    }

    // ─────────────────────────────────────────
    // Admin: 쿠폰 템플릿 목록/상세 조회
    // ─────────────────────────────────────────

    @DisplayName("getTemplates: 등록된 쿠폰 템플릿 목록을 페이징으로 조회할 수 있다")
    @Test
    void getTemplates_success() {
        Page<CouponDto.CouponInfo> result = couponApplicationService.getTemplates(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result.getContent()).anyMatch(c -> c.id().equals(couponId));
    }

    @DisplayName("getTemplate: 등록된 쿠폰 템플릿 상세를 조회할 수 있다")
    @Test
    void getTemplate_success() {
        CouponDto.CouponInfo result = couponApplicationService.getTemplate(couponId);

        assertThat(result.id()).isEqualTo(couponId);
        assertThat(result.name()).isEqualTo("신규 가입 쿠폰");
        assertThat(result.type()).isEqualTo(CouponType.FIXED);
        assertThat(result.discountValue()).isEqualByComparingTo("5000");
    }

    @DisplayName("getTemplate: 존재하지 않는 쿠폰 조회 시 예외가 발생한다")
    @Test
    void getTemplate_fail_notFound() {
        assertThatThrownBy(() -> couponApplicationService.getTemplate(9999L))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.COUPON_NOT_FOUND));
    }

    // ─────────────────────────────────────────
    // Admin: 쿠폰 템플릿 수정
    // ─────────────────────────────────────────

    @DisplayName("updateTemplate: 쿠폰 템플릿 수정 시 변경 내용이 DB에 반영된다")
    @Test
    void updateTemplate_success() {
        CouponDto.CouponInfo result = couponApplicationService.updateTemplate(
            couponId, "수정된 쿠폰", "수정된 설명", CouponType.RATE, BigDecimal.valueOf(15), 14
        );

        assertThat(result.name()).isEqualTo("수정된 쿠폰");
        assertThat(result.type()).isEqualTo(CouponType.RATE);
        assertThat(result.discountValue()).isEqualByComparingTo("15");
        assertThat(result.validDays()).isEqualTo(14);

        CouponDto.CouponInfo reloaded = couponApplicationService.getTemplate(couponId);
        assertThat(reloaded.name()).isEqualTo("수정된 쿠폰");
    }

    // ─────────────────────────────────────────
    // Admin: 쿠폰 템플릿 삭제
    // ─────────────────────────────────────────

    @DisplayName("deleteTemplate: 쿠폰 템플릿 삭제 후 조회하면 예외가 발생한다")
    @Test
    void deleteTemplate_success() {
        couponApplicationService.deleteTemplate(couponId);

        assertThatThrownBy(() -> couponApplicationService.getTemplate(couponId))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.COUPON_NOT_FOUND));
    }

    // ─────────────────────────────────────────
    // Admin: 발급 내역 조회
    // ─────────────────────────────────────────

    @DisplayName("getIssues: 쿠폰 발급 후 발급 내역을 조회할 수 있다")
    @Test
    void getIssues_success() {
        couponApplicationService.issue(userId, couponId);

        Page<CouponDto.CouponIssueInfo> result = couponApplicationService.getIssues(couponId, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().userId()).isEqualTo(userId);
        assertThat(result.getContent().getFirst().couponId()).isEqualTo(couponId);
        assertThat(result.getContent().getFirst().status()).isEqualTo(CouponIssueStatus.AVAILABLE);
    }

    @DisplayName("getIssues: 발급 내역이 없으면 빈 페이지를 반환한다")
    @Test
    void getIssues_empty() {
        Page<CouponDto.CouponIssueInfo> result = couponApplicationService.getIssues(couponId, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
    }

    // ─────────────────────────────────────────
    // User: 쿠폰 발급 요청
    // ─────────────────────────────────────────

    @DisplayName("issue: 쿠폰 발급 요청 시 발급 내역이 저장되고 만료일이 설정된다")
    @Test
    void issue_success() {
        CouponDto.CouponIssueInfo result = couponApplicationService.issue(userId, couponId);

        assertThat(result.id()).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.couponId()).isEqualTo(couponId);
        assertThat(result.status()).isEqualTo(CouponIssueStatus.AVAILABLE);
        assertThat(result.expiredAt()).isNotNull();
    }

    @DisplayName("issue: 이미 발급된 쿠폰을 재요청하면 기존 발급 내역을 반환한다 (멱등)")
    @Test
    void issue_idempotent_whenAlreadyIssued() {
        CouponDto.CouponIssueInfo first = couponApplicationService.issue(userId, couponId);
        CouponDto.CouponIssueInfo second = couponApplicationService.issue(userId, couponId);

        assertThat(second.id()).isEqualTo(first.id());

        Page<CouponDto.CouponIssueInfo> issues = couponApplicationService.getIssues(couponId, PageRequest.of(0, 10));
        assertThat(issues.getTotalElements()).isEqualTo(1);
    }

    @DisplayName("issue: 존재하지 않는 사용자로 발급 요청 시 예외가 발생한다")
    @Test
    void issue_fail_userNotFound() {
        assertThatThrownBy(() -> couponApplicationService.issue(9999L, couponId))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.USER_NOT_FOUND));
    }

    @DisplayName("issue: 존재하지 않는 쿠폰으로 발급 요청 시 예외가 발생한다")
    @Test
    void issue_fail_couponNotFound() {
        assertThatThrownBy(() -> couponApplicationService.issue(userId, 9999L))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.COUPON_NOT_FOUND));
    }

    // ─────────────────────────────────────────
    // User: 내 쿠폰 목록 조회
    // ─────────────────────────────────────────

    @DisplayName("getMyCoupons: 발급된 쿠폰이 내 쿠폰 목록에 포함된다")
    @Test
    void getMyCoupons_success() {
        couponApplicationService.issue(userId, couponId);

        Page<CouponDto.MyCouponInfo> result = couponApplicationService.getMyCoupons(userId, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().couponId()).isEqualTo(couponId);
        assertThat(result.getContent().getFirst().status()).isEqualTo(CouponIssueStatus.AVAILABLE);
        assertThat(result.getContent().getFirst().expiredAt()).isNotNull();
    }

    @DisplayName("getMyCoupons: 쿠폰이 없으면 빈 목록을 반환한다")
    @Test
    void getMyCoupons_empty() {
        Page<CouponDto.MyCouponInfo> result = couponApplicationService.getMyCoupons(userId, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
    }

    @DisplayName("getMyCoupons: 존재하지 않는 사용자 조회 시 예외가 발생한다")
    @Test
    void getMyCoupons_fail_userNotFound() {
        assertThatThrownBy(() -> couponApplicationService.getMyCoupons(9999L, PageRequest.of(0, 10)))
            .isInstanceOf(CoreException.class)
            .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                .isEqualTo(ErrorType.USER_NOT_FOUND));
    }
}
