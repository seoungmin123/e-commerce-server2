package kr.hhplus.be.server.order.domain;

import kr.hhplus.be.server.product.dto.PopularProductQuery;

import java.util.List;
import java.util.Optional;

public interface IOrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long aLong);

    List<PopularProductQuery> findTopFivePopularProducts();

    List<Order> findByUserId(long l);
}
