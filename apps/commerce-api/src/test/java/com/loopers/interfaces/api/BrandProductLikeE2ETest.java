package com.loopers.interfaces.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.user.UserApplicationService;
import com.loopers.application.user.UserDto;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandDescription;
import com.loopers.domain.brand.BrandName;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.Gender;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BrandProductLikeE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserApplicationService userApplicationService;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Long userId;

    @BeforeEach
    void setUp() {
        UserDto.UserInfo user = userApplicationService.register(
            "brandlike1",
            "TestPass1!",
            "브랜드유저",
            LocalDate.of(2000, 1, 1),
            "brand-like@loopers.com",
            Gender.MALE
        );
        userId = user.id();
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("브랜드 생성 후 고객 API로 브랜드 조회에 성공한다")
    @Test
    void brand_create_and_get_success() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "NIKE");
        body.put("description", "스포츠 브랜드");

        String created = mockMvc.perform(post("/api-admin/v1/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.name").value("NIKE"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long brandId = objectMapper.readTree(created).get("data").get("id").asLong();

        mockMvc.perform(get("/api/v1/brands/" + brandId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.id").value(brandId))
            .andExpect(jsonPath("$.data.name").value("NIKE"));
    }

    @DisplayName("상품 생성 후 목록/상세 조회에 성공한다")
    @Test
    void product_create_and_get_success() throws Exception {
        Brand brand = brandJpaRepository.save(
            Brand.create(new BrandName("ADIDAS"), new BrandDescription("아디다스"))
        );

        Map<String, Object> createBody = new HashMap<>();
        createBody.put("brandId", brand.getId());
        createBody.put("name", "슈퍼스타");
        createBody.put("description", "오리지널 라인");
        createBody.put("price", 129000);
        createBody.put("stock", 20);

        String created = mockMvc.perform(post("/api-admin/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBody)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.name").value("슈퍼스타"))
            .andExpect(jsonPath("$.data.brandId").value(brand.getId()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long productId = objectMapper.readTree(created).get("data").get("id").asLong();

        mockMvc.perform(get("/api/v1/products")
                .param("brandId", String.valueOf(brand.getId()))
                .param("sort", "likes_desc")
                .param("page", "0")
                .param("size", "20"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.content[0].id").value(productId));

        mockMvc.perform(get("/api/v1/products/" + productId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.id").value(productId))
            .andExpect(jsonPath("$.data.brandName").value("ADIDAS"));
    }

    @DisplayName("좋아요 등록/취소 및 내 좋아요 목록 조회에 성공한다")
    @Test
    void like_flow_success() throws Exception {
        Brand brand = brandJpaRepository.save(
            Brand.create(new BrandName("LIKE_BRAND"), new BrandDescription("좋아요 브랜드"))
        );

        Product product = productJpaRepository.save(
            Product.create(
                brand.getId(),
                "좋아요 상품",
                "설명",
                BigDecimal.valueOf(30000),
                5
            )
        );

        mockMvc.perform(post("/api/v1/products/" + product.getId() + "/likes")
                .header("X-Loopers-User-Id", userId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.productId").value(product.getId()))
            .andExpect(jsonPath("$.data.liked").value(true))
            .andExpect(jsonPath("$.data.message").value("success"));

        mockMvc.perform(post("/api/v1/products/" + product.getId() + "/likes")
                .header("X-Loopers-User-Id", userId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.message").value("already_liked"));

        mockMvc.perform(get("/api/v1/users/" + userId + "/likes")
                .header("X-Loopers-User-Id", userId)
                .param("page", "0")
                .param("size", "20"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].productId").value(product.getId()));

        mockMvc.perform(delete("/api/v1/products/" + product.getId() + "/likes")
                .header("X-Loopers-User-Id", userId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"));

        mockMvc.perform(get("/api/v1/users/" + userId + "/likes")
                .header("X-Loopers-User-Id", userId)
                .param("page", "0")
                .param("size", "20"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @DisplayName("좋아요 목록은 본인 userId로만 조회할 수 있다")
    @Test
    void like_list_forbidden_for_other_user() throws Exception {
        UserDto.UserInfo another = userApplicationService.register(
            "brandlike2",
            "TestPass2!",
            "다른유저",
            LocalDate.of(2001, 1, 1),
            "brand-like2@loopers.com",
            Gender.FEMALE
        );

        mockMvc.perform(get("/api/v1/users/" + userId + "/likes")
                .header("X-Loopers-User-Id", another.id())
                .param("page", "0")
                .param("size", "20"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.meta.result").value("FAIL"));
    }
}
