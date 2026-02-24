package com.loopers.domain.user.exception;

/**
 * 비밀번호가 유효하지 않을 때 발생하는 예외
 * - 기존 비밀번호 불일치
 * - 비밀번호 형식 오류
 */
public class InvalidPasswordException extends UserException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
