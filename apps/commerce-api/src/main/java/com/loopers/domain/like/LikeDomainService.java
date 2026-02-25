package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import org.springframework.stereotype.Component;

/**
 * Like 도메인 서비스
 * 좋아요 도메인의 상태 전이와 상품 likeCount 변경 규칙을 담당한다.
 */
@Component
public class LikeDomainService {

    public LikeProcessResult like(Long userId, Long productId, Like existingLike, Product product) {
        if (existingLike != null && !existingLike.isDeleted()) {
            return new LikeProcessResult(existingLike, false);
        }

        if (existingLike != null) {
            existingLike.restore();
            product.increaseLikeCount();
            return new LikeProcessResult(existingLike, true);
        }

        Like like = Like.create(userId, productId);
        product.increaseLikeCount();
        return new LikeProcessResult(like, true);
    }

    public boolean unlike(Like like, Product product) {
        if (like == null) {
            return false;
        }

        like.delete();
        product.decreaseLikeCount();
        return true;
    }

    public record LikeProcessResult(Like like, boolean requiresPersistence) {
    }
}
