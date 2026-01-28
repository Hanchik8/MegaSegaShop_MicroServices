CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    product_name VARCHAR(255),
    unit_price NUMERIC(19, 2),
    quantity INTEGER NOT NULL,
    cart_id BIGINT REFERENCES carts(id) ON DELETE CASCADE
);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
