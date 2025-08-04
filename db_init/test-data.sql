INSERT INTO `user` (id, name, created_at, updated_at)
VALUES (1, '테스트유저1', NOW(), NOW()),
       (2, '테스트유저2', NOW(), NOW()),
       (3, '테스트유저3', NOW(), NOW()),
       (4, '테스트유저4', NOW(), NOW()),
       (5, '테스트유저5', NOW(), NOW()),
       (6, '테스트유저6', NOW(), NOW());

INSERT INTO point (id, user_id, point, created_at, updated_at, version)
VALUES (1, 1, 100000, NOW(), NOW(), 0);
INSERT INTO point (id, user_id, point, created_at, updated_at, version)
VALUES (2, 2, 100000, NOW(), NOW(), 0);
INSERT INTO point (id, user_id, point, created_at, updated_at, version)
VALUES (3, 3, 100000, NOW(), NOW(), 0);
INSERT INTO point (id, user_id, point, created_at, updated_at, version)
VALUES (4, 4, 100000, NOW(), NOW(), 0);
INSERT INTO point (id, user_id, point, created_at, updated_at, version)
VALUES (5, 5, 1, NOW(), NOW(), 0);


INSERT INTO product (id, name, price, created_at, updated_at)
VALUES (1, '테스트상품1', 10000.00, NOW(), NOW());
INSERT INTO product (id, name, price, created_at, updated_at)
VALUES (2, '테스트상품2', 15000.00, NOW(), NOW());
INSERT INTO product (id, name, price, created_at, updated_at)
VALUES (3, '테스트상품3', 1, NOW(), NOW());

INSERT INTO product_stock (id, product_id, quantity, created_at, updated_at)
VALUES (1, 1, 100, NOW(), NOW());

INSERT INTO product_stock (id, product_id, quantity, created_at, updated_at)
VALUES (2, 2, 50, NOW(), NOW());

INSERT INTO product_stock (id, product_id, quantity, created_at, updated_at)
VALUES (3, 3, 10, NOW(), NOW());

INSERT INTO `order` (id, user_id, coupon_id, status, item_amount, total_amount,
                     discount_amount, payment_amount, created_at, updated_at)
VALUES (1, 1, NULL, 'PENDING', 50000.00, 50000.00, 0.00, 50000.00, NOW(), NOW());
INSERT INTO order_item (id, order_id, product_id, order_item_name, order_price, quantity, created_at, updated_at)
VALUES (1, 1, 1, '테스트상품1', 20000.00, 2, NOW(), NOW()),
       (2, 1, 2, '테스트상품2', 15000.00, 1, NOW(), NOW());

INSERT INTO coupon (id, name, discount_type, discount_value, issue_start_at, issue_end_at,
                    validity_period, total_issue_quantity, issued_quantity, created_at, updated_at)
VALUES (1, '신규회원 할인', 'PERCENTAGE', 10.00, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 30, 30, 0, NOW(),NOW()),
       (2, '정액 할인 쿠폰', 'FIXED', 5000.00, '2025-01-01 00:00:00', '2025-06-30 23:59:59', 60, 3, 0, NOW(), NOW()),
       (3, '정액 할인 쿠폰', 'FIXED', 5000.00, '2025-01-01 00:00:00', '2025-06-30 23:59:59', 60, 3, 0, NOW(), NOW()),
       (4, '신규회원 할인', 'PERCENTAGE', 10.00, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 30, 30, 0, NOW(),NOW());
INSERT INTO coupon_issue (id, user_id, coupon_id, used_at, expired_at, created_at, updated_at)
VALUES (1, 2, 1, '2025-01-15 10:00:00', '2025-01-31 23:59:59', NOW(), NOW()),
       (2, 3, 1, NULL, '2025-02-28 23:59:59', NOW(), NOW()),
       (3, 2, 3, NULL, '2025-02-28 23:59:59', NOW(), NOW()),
         (4, 1, 4, NULL, '2025-02-28 23:59:59', NOW(), NOW());
