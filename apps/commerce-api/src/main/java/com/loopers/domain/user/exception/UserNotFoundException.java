package com.loopers.domain.user.exception;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends UserException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다: " + userId);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
