package kr.hhplus.be.server;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.hibernate.internal.util.collections.CollectionHelper.listOf;

@Component
public class DataBaseCleanUp {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void execute() {
        List<String> tableNames = listOf(
                "coupon", "coupon_issue"
                , "users", "point", "point_history"
                , "orders", "order_item" , "payment"
                , "product","product_stock"
        );

        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
    }

}