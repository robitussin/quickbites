-- QuickBite schema (MySQL 8.0.16 or later, so CHECK constraints are enforced)
-- Run:  mysql -u qb_user -p quickbite < db/schema.sql

CREATE TABLE menu_item (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(60)   NOT NULL UNIQUE,
    category  VARCHAR(30)   NOT NULL,
    price     DECIMAL(8,2)  NOT NULL,
    available BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_price CHECK (price > 0)
) ENGINE=InnoDB;

CREATE TABLE orders (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(60)    NOT NULL,
    status        VARCHAR(15)    NOT NULL,
    created_at    DATETIME       NOT NULL,
    total         DECIMAL(10,2)  NOT NULL,
    CONSTRAINT chk_status CHECK (status IN ('PENDING','PREPARING','READY','COMPLETED','CANCELLED'))
) ENGINE=InnoDB;

CREATE TABLE order_item (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    order_id     INT           NOT NULL,
    menu_item_id INT           NOT NULL,
    quantity     INT           NOT NULL,
    unit_price   DECIMAL(8,2)  NOT NULL,
    CONSTRAINT fk_line_order FOREIGN KEY (order_id)     REFERENCES orders (id),
    CONSTRAINT fk_line_item  FOREIGN KEY (menu_item_id) REFERENCES menu_item (id),
    CONSTRAINT chk_quantity  CHECK (quantity BETWEEN 1 AND 10)
) ENGINE=InnoDB;
