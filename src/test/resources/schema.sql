CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS point (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    point DECIMAL(10,4),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS point_history (
    id BIGINT PRIMARY KEY,
    point_id BIGINT NOT NULL,
    payment_id BIGINT,
    amount DECIMAL(10,4) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    discount_type VARCHAR(20),
    discount_value DECIMAL(10,4) ,
    issue_start_at DATETIME ,
    issue_end_at DATETIME ,
    validity_period INT NOT NULL,
    total_issue_quantity INT NOT NULL,
    issued_quantity INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS coupon_issue (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    used_at DATETIME,
    expired_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT,
    status VARCHAR(20) NOT NULL,
    item_amount DECIMAL(10,4) NOT NULL,
    total_amount DECIMAL(10,4) NOT NULL,
    discount_amount DECIMAL(10,4) NOT NULL,
    payment_amount DECIMAL(10,4) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS payment (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_amount DECIMAL(10,4) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    price DECIMAL(10,4) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_item_name VARCHAR(30) NOT NULL,
    order_price DECIMAL(10,4) NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS product_stock (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

