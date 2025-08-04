-- user
INSERT INTO user (id, name, created_at, updated_at) VALUES
(11, 'Alice', NOW(), NOW()),
(12, 'Bob', NOW(), NOW());

-- point
INSERT INTO point (id, user_id, point, created_at, updated_at) VALUES
(11, 1, 5000.0000, NOW(), NOW()),
(12, 2, 10000.0000, NOW(), NOW());

-- point_history
INSERT INTO point_history (id, point_id, payment_id, amount, created_at, updated_at) VALUES
(11, 1, NULL, 1000.0000, NOW(), NOW()),
(12, 2, NULL, -2000.0000, NOW(), NOW());

-- coupon
INSERT INTO coupon (
    id, name, discount_type, discount_value, issue_start_at,
    issue_end_at, validity_period, issued_quantity, created_at, updated_at
) VALUES (
    11, '할인쿠폰', 'FIXED', 10.0000,
    NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 30, 0, NOW(), NOW()
);


-- coupon_issue
INSERT INTO coupon_issue (id, user_id, coupon_id, used_at, expired_at, created_at, updated_at) VALUES
(11, 1, 1, NULL, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), NOW());
INSERT INTO coupon_issue (id, user_id, coupon_id, used_at, expired_at, created_at, updated_at) VALUES
(12, 1, 2, NULL, DATE_ADD(NOW(), INTERVAL 10 DAY), NOW(), NOW());

-- order
INSERT INTO `order` (id, user_id, status, item_amount, total_amount, discount_amount, payment_amount, created_at, updated_at) VALUES
(11, 1, 'PAID', 10000.0000, 9000.0000, 1000.0000, 9000.0000, NOW(), NOW());

-- payment
INSERT INTO payment (id, order_id, payment_amount, created_at, updated_at) VALUES
(11, 1, 9000.0000, NOW(), NOW());

-- product
INSERT INTO product (id, name, price, created_at, updated_at) VALUES
(11, '노트북', 10000.0000, NOW(), NOW()),
(12, '마우스', 2000.0000, NOW(), NOW());

-- order_item
INSERT INTO order_item (id, order_id, product_id, order_item_name, order_price, quantity, created_at, updated_at) VALUES
(11, 1, 1, '노트북', 10000.0000, 1, NOW(), NOW());
