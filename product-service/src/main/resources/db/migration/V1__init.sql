CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    brand VARCHAR(255),
    description TEXT,
    price NUMERIC(19, 2),
    category_id BIGINT REFERENCES category(id)
);

CREATE TABLE image (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255),
    file_type VARCHAR(128),
    image BYTEA,
    download_url VARCHAR(512),
    product_id BIGINT REFERENCES product(id)
);

CREATE INDEX idx_image_product_id ON image(product_id);
