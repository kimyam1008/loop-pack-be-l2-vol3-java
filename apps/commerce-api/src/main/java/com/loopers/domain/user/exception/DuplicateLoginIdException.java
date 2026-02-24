package com.loopers.domain.user.exception;

/**
 * 중복된 로그인 ID로 가입 시도 시 발생하는 예외
 */
public class DuplicateLoginIdException extends UserException {

    private final String loginId;

    public DuplicateLoginIdException(String loginId) {
        super("이미 가입된 ID입니다: " + loginId);
        this.loginId = loginId;
    }

    public String getLoginId() {
        return loginId;
    }
}
