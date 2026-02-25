package com.loopers.application.product;

import org.springframework.data.domain.Sort;

public enum ProductSortType {

    LATEST {
        @Override
        public Sort toSort() {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    },
    PRICE_ASC {
        @Override
        public Sort toSort() {
            return Sort.by(Sort.Direction.ASC, "price");
        }
    },
    LIKES_DESC {
        @Override
        public Sort toSort() {
            return Sort.by(Sort.Direction.DESC, "likeCount");
        }
    };

    public abstract Sort toSort();
}
