package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.common.response.ApiResponse;
import kr.hhplus.be.server.common.response.ApiResponseCode;
import kr.hhplus.be.server.product.controller.swagger.ProductSwaggerDocs;
import kr.hhplus.be.server.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController implements ProductSwaggerDocs {

    private final ProductService productService;

    /**
     * 상품 목록 조회 API
     */
    @GetMapping
    public ApiResponse<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts().stream().map(ProductResponse::from).toList();
        return ApiResponse.success(ApiResponseCode.SUCCESS_OK_200,products);
    }


    /**
     * 인기 상품 조회 API
     */
    @GetMapping("/popular/top5")
    public ApiResponse<List<PopularProductResponse>> getTopFivePopularProducts() {

        List<PopularProductResponse> topFiveProducts =
                productService.getTopFivePopularProducts()
                        .stream().map(PopularProductResponse::from).toList();

        return ApiResponse.success(ApiResponseCode.SUCCESS_OK_200,topFiveProducts);
    }


}