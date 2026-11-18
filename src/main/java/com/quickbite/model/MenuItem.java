package com.quickbite.model;

import java.math.BigDecimal;

/** One item on the menu (table menu_item). */
public class MenuItem {
    private final int id;
    private final String name;
    private final String category;
    private final BigDecimal price;
    private final boolean available;

    public MenuItem(int id, String name, String category, BigDecimal price, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public boolean isAvailable() { return available; }

    /** Shown in the item drop-down on the New Order tab. */
    @Override
    public String toString() {
        return name + " - " + price.toPlainString() + (available ? "" : " (unavailable)");
    }
}
