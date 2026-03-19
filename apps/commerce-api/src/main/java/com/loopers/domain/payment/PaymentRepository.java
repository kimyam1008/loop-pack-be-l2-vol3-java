package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    /** 해당 주문의 가장 최근 결제 내역 조회 */
    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByPgTransactionId(String pgTransactionId);
}
