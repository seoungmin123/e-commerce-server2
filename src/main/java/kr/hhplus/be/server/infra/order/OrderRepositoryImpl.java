package kr.hhplus.be.server.infra.order;

import kr.hhplus.be.server.order.domain.IOrderRepository;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.product.dto.PopularProductQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements IOrderRepository {
    private final OrderJpaRepository orderJpaRepository;
    private final OrderQueryRepository orderQueryRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long aLong) {
        return orderJpaRepository.findById(aLong);
    }

    @Override
    public List<PopularProductQuery> findTopFivePopularProducts() {
        return orderQueryRepository.findTopFivePopularProducts();
    }

    @Override
    public List<Order> findByUserId(long userId) {
        return orderJpaRepository.findByUserId(userId);
    }

}
