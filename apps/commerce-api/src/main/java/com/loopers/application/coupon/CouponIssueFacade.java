package com.loopers.application.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.coupon.CouponIssueRequest;
import com.loopers.domain.coupon.CouponIssueRequestRepository;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueFacade {

    private static final String COUPON_ISSUE_TOPIC = "coupon.issue-requests.topic-v1";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CouponRepository couponRepository;
    private final CouponIssueRequestRepository couponIssueRequestRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Transactional
    public CouponDto.CouponIssueRequestInfo requestIssue(Long userId, Long couponId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
        couponRepository.findById(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.COUPON_NOT_FOUND));

        CouponIssueRequest request = CouponIssueRequest.create(couponId, userId);
        CouponIssueRequest savedRequest = couponIssueRequestRepository.save(request);

        try {
            String message = objectMapper.writeValueAsString(Map.of("requestId", savedRequest.getId()));
            kafkaTemplate.send(COUPON_ISSUE_TOPIC, String.valueOf(couponId), message);
        } catch (JsonProcessingException e) {
            log.error("쿠폰 발급 요청 Kafka 발행 실패 - requestId: {}", savedRequest.getId(), e);
        }

        return CouponDto.CouponIssueRequestInfo.from(savedRequest);
    }

    @Transactional(readOnly = true)
    public CouponDto.CouponIssueRequestInfo getIssueRequestStatus(Long requestId) {
        CouponIssueRequest request = couponIssueRequestRepository.findById(requestId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        return CouponDto.CouponIssueRequestInfo.from(request);
    }
}
