package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.exception.BrandNotFoundException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.exception.ProductNotDeletedException;
import com.loopers.domain.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public ProductDto.ProductInfo register(Long brandId, String name, String description, BigDecimal price, Integer stock) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new BrandNotFoundException(brandId));

        Product product = Product.create(brandId, name, description, price, stock);
        Product saved = productRepository.save(product);
        return ProductDto.ProductInfo.of(saved, brand.getName());
    }

    @Transactional(readOnly = true)
    public Page<ProductDto.ProductInfo> getProducts(ProductSortType sortType, Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortType.toSort());
        Page<Product> products = productRepository.findAll(sorted);

        Set<Long> brandIds = products.stream()
            .map(Product::getBrandId)
            .collect(Collectors.toSet());

        Map<Long, String> brandNameMap = brandRepository.findAllByIds(brandIds).stream()
            .collect(Collectors.toMap(Brand::getId, Brand::getName));

        return products.map(product ->
            ProductDto.ProductInfo.of(product, brandNameMap.getOrDefault(product.getBrandId(), ""))
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductDto.ProductInfo> getProductsByBrand(Long brandId, ProductSortType sortType, Pageable pageable) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new BrandNotFoundException(brandId));

        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortType.toSort());
        return productRepository.findByBrandId(brandId, sorted)
            .map(product -> ProductDto.ProductInfo.of(product, brand.getName()));
    }

    @Transactional(readOnly = true)
    public ProductDto.ProductInfo getProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        Brand brand = brandRepository.findById(product.getBrandId())
            .orElseThrow(() -> new BrandNotFoundException(product.getBrandId()));
        return ProductDto.ProductInfo.of(product, brand.getName());
    }

    @Transactional
    public ProductDto.ProductInfo increaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        product.increaseStock(quantity);
        Product saved = productRepository.save(product);
        Brand brand = brandRepository.findById(saved.getBrandId())
            .orElseThrow(() -> new BrandNotFoundException(saved.getBrandId()));
        return ProductDto.ProductInfo.of(saved, brand.getName());
    }

    @Transactional
    public ProductDto.ProductInfo decreaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        product.decreaseStock(quantity);
        Product saved = productRepository.save(product);
        Brand brand = brandRepository.findById(saved.getBrandId())
            .orElseThrow(() -> new BrandNotFoundException(saved.getBrandId()));
        return ProductDto.ProductInfo.of(saved, brand.getName());
    }

    @Transactional
    public void delete(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        product.delete();
        productRepository.save(product);
    }

    @Transactional
    public ProductDto.ProductInfo restore(Long productId) {
        Product product = productRepository.findByIdIncludingDeleted(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.isDeleted()) {
            throw new ProductNotDeletedException(productId);
        }

        product.restore();
        Product saved = productRepository.save(product);
        Brand brand = brandRepository.findByIdIncludingDeleted(saved.getBrandId())
            .orElseThrow(() -> new BrandNotFoundException(saved.getBrandId()));
        return ProductDto.ProductInfo.of(saved, brand.getName());
    }
}
