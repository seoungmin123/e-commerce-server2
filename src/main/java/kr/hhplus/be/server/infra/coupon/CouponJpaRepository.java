package kr.hhplus.be.server.infra.coupon;


import kr.hhplus.be.server.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    // 비관적 쓰기락 적용
    @Lock(value = PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithLock(@Param("id") Long id);


    @Query("SELECT c FROM Coupon c WHERE c.issueStartAt <= :now AND c.issueEndAt >= :now " +
            "and (c.totalIssueQuantity is null or c.totalIssueQuantity > c.issuedQuantity)")
    List<Coupon> findIssuableCoupons(@Param("now") LocalDateTime now);
}
