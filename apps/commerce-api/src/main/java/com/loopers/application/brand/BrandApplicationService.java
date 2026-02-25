package com.loopers.application.brand;

import com.loopers.domain.brand.*;
import com.loopers.domain.brand.exception.BrandNotFoundException;
import com.loopers.domain.brand.exception.DuplicateBrandNameException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandApplicationService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final BrandDomainService brandDomainService;

    @Transactional
    public BrandDto.BrandInfo register(String name, String description) {
        BrandName brandName = new BrandName(name);
        BrandDescription brandDescription = new BrandDescription(description);

        if (brandRepository.existsByName(brandName.value())) {
            throw new DuplicateBrandNameException(brandName.value());
        }

        Brand brand = brandDomainService.createBrand(brandName, brandDescription);
        Brand savedBrand = brandRepository.save(brand);
        return BrandDto.BrandInfo.from(savedBrand);
    }

    @Transactional(readOnly = true)
    public Page<BrandDto.BrandInfo> getAdminBrands(Pageable pageable) {
        return brandRepository.findAll(pageable)
            .map(BrandDto.BrandInfo::from);
    }

    @Transactional(readOnly = true)
    public BrandDto.BrandInfo getBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new BrandNotFoundException(brandId));

        return BrandDto.BrandInfo.from(brand);
    }

    @Transactional
    public BrandDto.BrandInfo update(Long brandId, String name, String description) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new BrandNotFoundException(brandId));

        BrandName brandName = new BrandName(name);
        BrandDescription brandDescription = new BrandDescription(description);

        if (!brand.getName().equals(brandName.value()) && brandRepository.existsByName(brandName.value())) {
            throw new DuplicateBrandNameException(brandName.value());
        }

        brandDomainService.updateBrand(brand, brandName, brandDescription);
        Brand savedBrand = brandRepository.save(brand);
        return BrandDto.BrandInfo.from(savedBrand);
    }

    @Transactional
    public void delete(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new BrandNotFoundException(brandId));

        brandDomainService.deleteBrand(brand);
        brandRepository.save(brand);

        for (Product product : productRepository.findAllByBrandId(brandId)) {
            brandDomainService.deleteOwnedProduct(product);
            productRepository.save(product);
        }
    }

    @Transactional
    public BrandDto.BrandInfo restore(Long brandId) {
        Brand brand = brandRepository.findByIdIncludingDeleted(brandId)
            .orElseThrow(() -> new BrandNotFoundException(brandId));

        brandDomainService.restoreBrand(brand, brandId);
        Brand savedBrand = brandRepository.save(brand);

        for (Product product : productRepository.findAllByBrandIdIncludingDeleted(brandId)) {
            if (!brandDomainService.restoreOwnedProduct(product)) {
                continue;
            }
            productRepository.save(product);
        }

        return BrandDto.BrandInfo.from(savedBrand);
    }
}
