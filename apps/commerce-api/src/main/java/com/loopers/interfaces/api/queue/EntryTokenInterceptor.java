package com.loopers.interfaces.api.queue;

import com.loopers.domain.queue.QueueRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class EntryTokenInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-Loopers-User-Id";
    private static final String TOKEN_HEADER = "X-Entry-Token";

    private final QueueRepository queueRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String token = request.getHeader(TOKEN_HEADER);

        if (userIdHeader == null || token == null) {
            throw new CoreException(ErrorType.ENTRY_TOKEN_NOT_FOUND);
        }

        Long userId = Long.parseLong(userIdHeader);
        String storedToken = queueRepository.getToken(userId);

        if (storedToken == null) {
            throw new CoreException(ErrorType.ENTRY_TOKEN_NOT_FOUND);
        }

        if (!storedToken.equals(token)) {
            throw new CoreException(ErrorType.ENTRY_TOKEN_INVALID);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        if (response.getStatus() == 200 && ex == null) {
            String userIdHeader = request.getHeader(USER_ID_HEADER);
            if (userIdHeader != null) {
                Long userId = Long.parseLong(userIdHeader);
                queueRepository.removeToken(userId);
                log.debug("입장 토큰 삭제 - userId:{}", userId);
            }
        }
    }
}
