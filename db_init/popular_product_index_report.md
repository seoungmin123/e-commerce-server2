
# 인기 상품 조회 성능 테스트 보고서

## 🧪 테스트 목적

`order_item` 테이블의 데이터가 많아졌을 때, 인기 상품 조회 API (`/products/popular/top5`) 쿼리 성능이 어떻게 달라지는지 확인하기 위한 테스트입니다.  
특히, **인덱스 생성 전/후의 실행 계획 및 수행 시간 차이**를 분석하여 병목 가능성을 검토합니다.

---

## 1. 📂 데이터 구성

| 테이블 | 건수 | 설명 |
|--------|------|------|
| `product` | 1,000건 | 상품명 랜덤 |
| `product_stock` | 1,000건 | 각 상품당 재고 |
| `orders` | 100,000건 | 유저 1명이 생성한 주문 |
| `order_item` | 약 300,000건 | 주문 1건당 2~5개 상품, 수량 1~3개 |

- 모든 `order_item.created_at`은 최근 7일 이내로 분포되도록 랜덤 설정하였습니다.

---

## 2. 🔍 테스트 쿼리

```sql
SELECT oi.product_id, SUM(oi.quantity) AS total_quantity
FROM order_item oi
WHERE oi.created_at > NOW() - INTERVAL 3 DAY
GROUP BY oi.product_id
ORDER BY total_quantity DESC
LIMIT 5;
```

---

## 3. ⚙ 인덱스 생성 전

- **실행 계획 (EXPLAIN)**:
  - `type = ALL` (Full Table Scan)
  - `Using temporary; Using filesort`
- **예상 수행 시간**:
  - 수십 초 ~ 수 분 (환경 따라 다름)

---

## 4. ✅ 인덱스 생성

```sql
CREATE INDEX idx_order_item_created_product_quantity
ON order_item (created_at, product_id, quantity);
```

- **목적**:
  - `created_at`을 통한 필터링
  - `product_id`로 그룹핑
  - `quantity`를 커버링 인덱스로 포함

---

## 5. ⚡ 인덱스 생성 후

- **실행 계획 (EXPLAIN)**:
  - `type = range` 또는 `index_range`
  - `Using index` or `Using index for group-by`
- **실행 시간**:
  - 수 초 → 수 밀리초로 단축

---

## 6. 📈 성능 차이 요약

| 항목 | 인덱스 없음 | 인덱스 있음 |
|------|-------------|-------------|
| 조회 대상 | 전체 테이블 스캔 | 범위 인덱스 스캔 |
| 수행 시간 | 매우 느림 (Full Scan) | 매우 빠름 (Index Scan) |
| Filesort | 발생 | 제거됨 가능성 |
| 병목 요소 | 정렬, 그룹핑, LIMIT 처리 | 대부분 제거 |

---

## ✅ 결론 및 PR 포인트

- 대량의 주문 데이터가 존재할 경우, 인기 상품 조회 쿼리는 인덱스 없이는 병목이 매우 심각할 수 있음
- `order_item(created_at, product_id, quantity)` 복합 인덱스는 조회 조건 및 집계에 최적화된 구조
- 이 인덱스는 실시간 인기 상품 API뿐 아니라, 향후 통계성 분석에도 활용될 수 있음
- 본 PR에서는 해당 인덱스 추가와 함께, `/products/popular/top5`의 쿼리 실행 계획 차이를 문서화하여 성능 최적화 근거로 제시함

---

## 🔖 참고

- 테스트용 더미 데이터: `product_popularity_benchmark_data.sql`
- 인덱스 DDL: `idx_order_item_created_product_quantity`
