package kr.hhplus.be.server.domain.product.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

public record PopularProductInfo(
        Long productId,
        String name,
        BigDecimal price,
        int totalQuantity
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static PopularProductInfo from (String hashValue){
        try {
            return objectMapper.readValue(hashValue, PopularProductInfo.class); // 역직렬화 json -> dto
        }catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse product info", e);
        }
    }
}
