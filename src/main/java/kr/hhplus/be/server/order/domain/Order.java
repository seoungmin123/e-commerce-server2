package kr.hhplus.be.server.order.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.domain.BaseEntity;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static kr.hhplus.be.server.common.exception.ApiErrorCode.INVALID_REQUEST;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    private static final BigDecimal SHIPPING_AMOUNT = BigDecimal.valueOf(3000);
    private static final BigDecimal FREE_SHIPPING_AMOUNT = BigDecimal.valueOf(30000);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(name = "coupon_id")
    private Long couponId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "item_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemAmount; // 상품 금액

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // 총 금액

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount; // 할인 된 금액

    @Column(name = "payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount; //결제 금액

    @OneToMany(mappedBy = "order", fetch = LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(User user) {
        this.user = user;
        this.status = OrderStatus.PENDING;
        this.discountAmount = BigDecimal.ZERO;
        this.itemAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.paymentAmount = BigDecimal.ZERO;
        this.couponId = null;
    }

    public static Order create(User user) {
        return new Order(user);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 주문 상품가격 계산
    public void calculateOrderAmounts() {
        this.itemAmount = orderItems.stream()
                .map(item -> item.getOrderPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = this.itemAmount;
        this.paymentAmount = this.totalAmount.subtract(this.discountAmount);
    }

    // 쿠폰 사용 : 할인금액 계산
    public void applyCoupon(Long couponIssueId, BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        this.paymentAmount = this.totalAmount.subtract(this.discountAmount);
        this.couponId = couponIssueId;
    }

    //fixme 주문확인
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new ApiException(INVALID_REQUEST);
        }
        this.status = OrderStatus.PAID;
    }

    public int getTotalQuantity() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public enum OrderStatus {
        PENDING("결제 대기"),
        PAID("결제 완료"),
        CANCELLED("주문 취소");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
