package com.loopers.domain.like.exception;

public class DuplicateLikeException extends LikeException {

    public DuplicateLikeException(Long userId, Long productId) {
        super("이미 좋아요한 상품입니다: userId=" + userId + ", productId=" + productId);
    }
}
