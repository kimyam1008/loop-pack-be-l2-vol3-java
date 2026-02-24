package com.loopers.domain.user.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserExceptionTest {

    @DisplayName("DuplicateLoginIdException: 중복 ID 예외 생성 시 메시지와 loginId가 설정된다")
    @Test
    void duplicateLoginIdException() {
        // given
        String loginId = "duplicateId";

        // when
        DuplicateLoginIdException exception = new DuplicateLoginIdException(loginId);

        // then
        assertThat(exception.getMessage()).contains("이미 가입된 ID입니다");
        assertThat(exception.getMessage()).contains(loginId);
        assertThat(exception.getLoginId()).isEqualTo(loginId);
    }

    @DisplayName("UserNotFoundException: 사용자 미존재 예외 생성 시 메시지와 userId가 설정된다")
    @Test
    void userNotFoundException() {
        // given
        Long userId = 999L;

        // when
        UserNotFoundException exception = new UserNotFoundException(userId);

        // then
        assertThat(exception.getMessage()).contains("사용자를 찾을 수 없습니다");
        assertThat(exception.getMessage()).contains(userId.toString());
        assertThat(exception.getUserId()).isEqualTo(userId);
    }

    @DisplayName("InvalidPasswordException: 비밀번호 유효성 예외 생성 시 메시지가 설정된다")
    @Test
    void invalidPasswordException() {
        // given
        String message = "기존 비밀번호가 일치하지 않습니다";

        // when
        InvalidPasswordException exception = new InvalidPasswordException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @DisplayName("UserException: 모든 User 예외는 UserException을 상속한다")
    @Test
    void userExceptionHierarchy() {
        // when & then
        assertThat(new DuplicateLoginIdException("test")).isInstanceOf(UserException.class);
        assertThat(new UserNotFoundException(1L)).isInstanceOf(UserException.class);
        assertThat(new InvalidPasswordException("test")).isInstanceOf(UserException.class);
    }
}
