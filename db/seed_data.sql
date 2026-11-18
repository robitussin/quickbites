-- Optional starter menu. Run after schema.sql.
INSERT INTO menu_item (name, category, price, available) VALUES
    ('Chicken Burger', 'Mains',  89.00, TRUE),
    ('Spaghetti',      'Mains',  95.00, TRUE),
    ('French Fries',   'Sides',  49.00, TRUE),
    ('Iced Tea',       'Drinks', 35.00, TRUE),
    ('Cola',           'Drinks', 30.00, TRUE),
    ('Halo-Halo',      'Dessert',65.00, FALSE);
