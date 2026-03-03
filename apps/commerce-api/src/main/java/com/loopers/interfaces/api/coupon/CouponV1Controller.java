package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponApplicationService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class CouponV1Controller {

    private final CouponApplicationService couponApplicationService;

    // ─────────────────────────────────────────
    // Admin APIs
    // ─────────────────────────────────────────

    @GetMapping("/api-admin/v1/coupons")
    public ApiResponse<CouponV1Dto.CouponTemplatePageResponse> getTemplates(Pageable pageable) {
        return ApiResponse.success(
            CouponV1Dto.CouponTemplatePageResponse.from(couponApplicationService.getTemplates(pageable))
        );
    }

    @GetMapping("/api-admin/v1/coupons/{couponId}")
    public ApiResponse<CouponV1Dto.CouponTemplateResponse> getTemplate(@PathVariable Long couponId) {
        return ApiResponse.success(
            CouponV1Dto.CouponTemplateResponse.from(couponApplicationService.getTemplate(couponId))
        );
    }

    @PostMapping("/api-admin/v1/coupons")
    public ApiResponse<CouponV1Dto.CouponTemplateResponse> registerTemplate(
        @Valid @RequestBody CouponV1Dto.RegisterTemplateRequest request
    ) {
        try {
            return ApiResponse.success(CouponV1Dto.CouponTemplateResponse.from(
                couponApplicationService.registerTemplate(
                    request.name(),
                    request.description(),
                    request.type(),
                    request.discountValue(),
                    request.validDays()
                )
            ));
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/api-admin/v1/coupons/{couponId}")
    public ApiResponse<CouponV1Dto.CouponTemplateResponse> updateTemplate(
        @PathVariable Long couponId,
        @Valid @RequestBody CouponV1Dto.UpdateTemplateRequest request
    ) {
        try {
            return ApiResponse.success(CouponV1Dto.CouponTemplateResponse.from(
                couponApplicationService.updateTemplate(
                    couponId,
                    request.name(),
                    request.description(),
                    request.type(),
                    request.discountValue(),
                    request.validDays()
                )
            ));
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/api-admin/v1/coupons/{couponId}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long couponId) {
        couponApplicationService.deleteTemplate(couponId);
        return ApiResponse.success(null);
    }

    @GetMapping("/api-admin/v1/coupons/{couponId}/issues")
    public ApiResponse<CouponV1Dto.CouponIssuePageResponse> getIssues(
        @PathVariable Long couponId,
        Pageable pageable
    ) {
        return ApiResponse.success(
            CouponV1Dto.CouponIssuePageResponse.from(couponApplicationService.getIssues(couponId, pageable))
        );
    }

    // ─────────────────────────────────────────
    // User APIs
    // ─────────────────────────────────────────

    @PostMapping("/api/v1/coupons/{couponId}/issue")
    public ApiResponse<CouponV1Dto.CouponIssueResponse> issue(
        @RequestHeader("X-Loopers-User-Id") Long userId,
        @PathVariable Long couponId
    ) {
        return ApiResponse.success(
            CouponV1Dto.CouponIssueResponse.from(couponApplicationService.issue(userId, couponId))
        );
    }

    @GetMapping("/api/v1/users/me/coupons")
    public ApiResponse<CouponV1Dto.MyCouponPageResponse> getMyCoupons(
        @RequestHeader("X-Loopers-User-Id") Long userId,
        Pageable pageable
    ) {
        return ApiResponse.success(
            CouponV1Dto.MyCouponPageResponse.from(couponApplicationService.getMyCoupons(userId, pageable))
        );
    }
}
