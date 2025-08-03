package kr.hhplus.be.server.infra.product;


import kr.hhplus.be.server.product.domain.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "productStock")
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithStock(@Param("id") Long id);
}
