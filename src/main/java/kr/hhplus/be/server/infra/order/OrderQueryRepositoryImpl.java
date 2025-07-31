package kr.hhplus.be.server.infra.order;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.product.dto.PopularProductQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static kr.hhplus.be.server.order.domain.QOrderItem.orderItem;
import static kr.hhplus.be.server.product.domain.QProduct.product;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularProductQuery> findTopFivePopularProducts() {
        return queryFactory
                .select(Projections.constructor(PopularProductQuery.class,
                        product.id,
                        product.name,
                        product.price,
                        orderItem.quantity.sum()
                ))
                .from(orderItem)
                .join(orderItem.product, product)
                .where(orderItem.createdAt.after(LocalDateTime.now().minusDays(3)))
                .groupBy(product.id, product.name, product.price)
                .orderBy(orderItem.quantity.sum().desc())
                .limit(5)
                .fetch();
    }
}
