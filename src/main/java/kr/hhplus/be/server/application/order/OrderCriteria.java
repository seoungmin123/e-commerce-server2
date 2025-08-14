package kr.hhplus.be.server.application.order;


import kr.hhplus.be.server.order.dto.OrderCommand;
import kr.hhplus.be.server.user.domain.User;

import java.util.List;


public class OrderCriteria {
    public record Order(User user, List<Item> products, Long couponIssueId) {
        public OrderCommand.Order toCommand() {
            return new OrderCommand.Order(
                    this.user(),
                    this.products().stream()
                            .map(item -> new OrderCommand.Item(item.productId(), null, item.quantity()))
                            .toList(),
                    this.couponIssueId()
            );
        }

        //락 상품 키
        public List<String> toOptionIds() {
            return products().stream()
                    .map(Item::productId)
                    .peek(id -> {
                        if (id == null) throw new IllegalArgumentException("productId is null");
                    })
                    .distinct()            // 같은 상품 중복 주문 시 중복 키 제거
                    .sorted()              // 항상 같은 순서로 잠금 → 교차 순서 데드락 방지
                    .map(String::valueOf)  // AOP에서 문자열 키로 쓰기 좋게
                    .toList();
        }
    }

    public record Item(Long productId, int quantity) {
    }
}
