package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeDomainService;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.exception.ProductNotFoundException;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.exception.UserNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeApplicationService {

    private final LikeRepository likeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LikeDomainService likeDomainService;

    @Transactional
    @Retryable(
        retryFor = {
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockException.class,
            DataIntegrityViolationException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 30)
    )
    public LikeDto.LikeInfo like(Long userId, Long productId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        Like existing = likeRepository.findByUserIdAndProductIdIncludingDeleted(userId, productId)
            .orElse(null);

        LikeDomainService.LikeProcessResult result = likeDomainService.like(userId, productId, existing, product);

        if (!result.requiresPersistence()) {
            return LikeDto.LikeInfo.from(result.like());
        }

        Like saved = likeRepository.save(result.like());
        productRepository.save(product);
        return LikeDto.LikeInfo.from(saved);
    }

    @Transactional
    @Retryable(
        retryFor = {
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockException.class
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 30)
    )
    public void unlike(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        Like like = likeRepository.findByUserIdAndProductId(userId, productId)
            .orElse(null);

        if (!likeDomainService.unlike(like, product)) {
            return;
        }

        likeRepository.save(like);
        productRepository.save(product);
    }
}
