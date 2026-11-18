package com.quickbite.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A customer order (table orders) with its lines. */
public class Order {
    private final int id;
    private final String customerName;
    private final Status status;
    private final LocalDateTime createdAt;
    private BigDecimal total;
    private final List<OrderItem> lines = new ArrayList<>();

    public Order(int id, String customerName, Status status, LocalDateTime createdAt, BigDecimal total) {
        this.id = id;
        this.customerName = customerName;
        this.status = status;
        this.createdAt = createdAt;
        this.total = total;
    }

    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getTotal() { return total; }
    public List<OrderItem> getLines() { return lines; }

    /** Adds up the lines. Call this after the lines are filled in. */
    public void recalculateTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem line : lines) {
            sum = sum.add(line.subtotal());
        }
        total = sum;
    }
}
