package com.quickbite.model;

import java.math.BigDecimal;

/** One line of an order (table order_item). The price is copied from the menu at order time. */
public class OrderItem {
    private final MenuItem item;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(MenuItem item, int quantity, BigDecimal unitPrice) {
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
