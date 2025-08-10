

#  동시성 이슈 분석 보고서


## 1. 동시성 이슈 분석

## 요약 

| 구분     | 적용 락 방식 | 정합성 수준 | 트랜잭션 구조   | 비고               |
|--------| ------- | ------ | --------- |------------------|
| 재고 차감  | 비관적 락   | 매우 중요  | 상위 트랜잭션 내 | 락 점유시간 길어짐       |
| 쿠폰 발급  | 비관적 락   | 중요     | 단독 트랜잭션   | 추후 Redis 리팩토링 고려 |
| 포인트 충전 | 낙관적 락   | 사용자 단위 | 단독 트랜잭션   | 충돌 시 예외 처리만 수행   |
| 포인트 사용 | 낙관적 락   | 사용자 단위 | 상위 트랜잭션 내  | 충돌 시 예외 처리만 수행   |


---

###  A. 재고 관리 - **비관적 락**

* **문제 상황**
  여러 사용자가 동시에 동일 상품의 재고를 주문할 경우, 재고 수량이 음수로 내려가거나 중복 차감되는 문제가 발생할 수 있습니다.

* **해결 방식**
  `SELECT ... FOR UPDATE`를 통해 **비관적 락(PESSIMISTIC\_WRITE)** 을 걸고, 상품 재고를 조회한 뒤 차감합니다.

* **선정 이유**

    * 실물 재고는 수량 오차가 발생할 경우 실제 비즈니스 손실로 이어지기때문에 데이터 일관성이 중요하다고 생각했습니다.
    * 현재 구조에서는 OrderFacade에서 ProductService를 호출하며 상위 트랜잭션이 존재하고 있습니다.
    * 비관락은 DB 수준에서 직접 락을 걸기 때문에 트랜잭션 범위와 락의 범위가 일치하여 데이터 정합성을 보다 안전하게 보장할 수 있습니다.
    * 하지만 전체 트랜잭션 종료 시까지 락이 유지되기 때문에, 데이터 정합성 확보에는 유리하지만 락 점유 시간이 길어지는 단점도 존재합니다.
#### 🔧 관련 코드

```java
// ProductStockJpaRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT ps FROM ProductStock ps WHERE ps.id IN :productIds")
List<ProductStock> findAllByIdsWithLock(List<Long> productIds);
```

```java
// ProductService.java
@Transactional
public void deductStock(List<OrderCreateCommand.OrderItemCommand> commands) {
    List<Long> productIds = commands.stream()
            .map(OrderCreateCommand.OrderItemCommand::productId)
            .collect(Collectors.toList());

    List<ProductStock> stocks = productRepository.findAllByIdsWithLock(productIds);

    if (stocks.size() != productIds.size()) {
        throw new ApiException(NOT_FOUND);
    }

    Map<Long, ProductStock> stockMap = stocks.stream()
            .collect(Collectors.toMap(ProductStock::getId, stock -> stock));

    for (OrderCreateCommand.OrderItemCommand command : commands) {
        ProductStock stock = stockMap.get(command.productId());
        stock.deduct(command.quantity());
    }

    productRepository.saveAll(stocks);
}
```

---

###  B. 쿠폰 발급 - **비관적 락**

* **문제 상황**
  선착순 쿠폰을 다수의 사용자가 동시에 발급받으면, 발급 가능한 수량을 초과하는 문제가 발생할 수 있습니다.

* **해결 방식**
  쿠폰 발급 시 `SELECT ... FOR UPDATE`를 통해 **비관적 락(PESSIMISTIC\_WRITE)** 을 적용합니다.

* **선정 이유**

    * 쿠폰은 제한 수량이 있으므로 중복 발급도 정합성이 중요하다고 생각했습니다.
    * 쿠폰 발급은 **단독 트랜잭션**으로 실행되므로 Redis 등을 활용한 분산락으로 리팩토링이 적합해 보입니다.
    * 현재는 비관적 락으로 DB 트랜잭션 충돌을 사전에 방지하고 순차 실행을 보장해주기 때문에
    * 별도의 충돌 감지나 재시도 로직 없이도 데이터 정합성을 확보할 수 있어서 선택했습니다.

#### 🔧 관련 코드

```java
// CouponJpaRepository.java
@Lock(value = PESSIMISTIC_WRITE)
@Query("SELECT c FROM Coupon c WHERE c.id = :id")
Optional<Coupon> findByIdWithLock(@Param("id") Long id);
```

```java
// CouponService.java
@Transactional
public CouponInfo issueCoupon(User user, CouponIssueCommand command) {
    Coupon coupon = couponRepository.findByIdWithLock(command.couponId())
        .orElseThrow(() -> new ApiException(NOT_FOUND));

    CouponIssue couponIssue = coupon.issue(user);
    try {
        couponIssue = couponRepository.save(couponIssue);
    } catch (DataIntegrityViolationException ex) {
        throw new ApiException(ApiErrorCode.CONFLICT);
    }
    return CouponInfo.from(couponIssue);
}
```

---

###  C. 포인트 충전/사용 - **낙관적 락**

* **문제 상황**
  동일 사용자가 포인트를 동시에 사용하거나 충전하려고 할 때 충돌이 발생할 수 있습니다.

* **해결 방식**
  `@Version`을 활용한 **낙관적 락(OPTIMISTIC)** 을 사용합니다. 충돌 시에는 예외를 발생시키고, **재시도는 하지 않습니다.**

* **선정 이유**

    * 포인트는 사용자 단위로만 정합성을 관리하면 됩니다.
    * 이중 클릭이나 중복 요청은 1건만 성공하면 되며, 실패 응답을 주면 됩니다.
    * 낙관적 락은 성능에 유리하고, 구현이 간단하며, 사용자의 동시 요청 빈도가 낮을 경우 효율적이라고 생각했습니다.

#### 🔧 관련 코드

```java
// PointJpaRepository.java
@Lock(LockModeType.OPTIMISTIC)
@Query("SELECT p FROM Point p WHERE p.user = :user")
Optional<Point> findByUserWithLock(@Param("user") User user);
```

```java
// PointService.java
@Transactional
public PointInfo charge(PointCommand.Charge command) {
    try{
        // DB에서 사용자의 포인트 정보를 읽어옴 (OPTIMISTIC)
        Point point = pointRepository.findByUserWithLock(command.user()).orElseGet(()
                -> pointRepository.save(Point.create(command.user())));
        point.charge(command.amount());
        point = pointRepository.save(point);
        return PointInfo.from(point);

    } catch (ObjectOptimisticLockingFailureException e) {
        //재시도 로직없음 - 실패시 이미처리되었습니다 문구
        throw new ApiException(ApiErrorCode.CONFLICT);
    }
}
```

```java
@Transactional
public void use(PointCommand.Use command) {
  try{
    // DB에서 사용자의 포인트 정보를 읽어옴 (OPTIMISTIC)
    Point point = pointRepository.findByUserWithLock(command.user()).orElseThrow(()
            -> new ApiException(NOT_FOUND));
    point.use(command.amount());
    pointRepository.save(point);
  } catch (ObjectOptimisticLockingFailureException e) {
    //재시도 로직없음 - 실패시 중복결제입니다 다시결재요청 문구
    throw new ApiException(ApiErrorCode.CONFLICT);
  }
}
```

---


> ** 추후 개선 방향**
>
> * 쿠폰 발급: Redis 분산락으로 리팩토링 (빠른 처리, 부하 분산 목적)
> * 포인트 차감: 필요 시 재시도 로직 추가 고려

---
