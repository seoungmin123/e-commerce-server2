package kr.hhplus.be.server.product.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

public record PopularProductQuery(
        Long productId,
        String name,
        BigDecimal price,
        int totalQuantity
) {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String toProductInfoString() {
        try {
            return objectMapper.writeValueAsString(this); //직렬화 dto -> json
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}