package com.loopers.domain.user.exception;

/**
 * User 도메인의 최상위 예외
 * 모든 User 관련 도메인 예외는 이 클래스를 상속합니다.
 */
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
