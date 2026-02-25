package com.loopers.domain.like.exception;

public class LikeNotFoundException extends LikeException {

    public LikeNotFoundException(Long userId, Long productId) {
        super("좋아요를 찾을 수 없습니다: userId=" + userId + ", productId=" + productId);
    }
}
