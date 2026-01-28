CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    email VARCHAR(255),
    status VARCHAR(32),
    total_amount NUMERIC(19, 2),
    created_at TIMESTAMP
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_email ON orders(email);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    product_name VARCHAR(255),
    unit_price NUMERIC(19, 2),
    quantity INTEGER NOT NULL,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
