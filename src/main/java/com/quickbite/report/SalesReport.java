package com.quickbite.report;

import com.quickbite.model.Order;
import com.quickbite.model.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Completed orders and sales per category for one day. Cancelled orders are never passed in. */
public class SalesReport {
    private final LocalDate date;
    private final int orderCount;
    private final BigDecimal totalSales;
    private final Map<String, BigDecimal> salesByCategory = new TreeMap<>();

    public SalesReport(LocalDate date, List<Order> completedOrders) {
        this.date = date;
        this.orderCount = completedOrders.size();
        BigDecimal sum = BigDecimal.ZERO;
        for (Order order : completedOrders) {
            for (OrderItem line : order.getLines()) {
                salesByCategory.merge(line.getItem().getCategory(), line.subtotal(), BigDecimal::add);
                sum = sum.add(line.subtotal());
            }
        }
        this.totalSales = sum;
    }

    public LocalDate getDate() { return date; }
    public int getOrderCount() { return orderCount; }
    public BigDecimal getTotalSales() { return totalSales; }
    public Map<String, BigDecimal> getSalesByCategory() { return salesByCategory; }

    /** Plain text for the report window. */
    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sales report for ").append(date).append("\n");
        sb.append("Completed orders: ").append(orderCount).append("\n\n");
        for (Map.Entry<String, BigDecimal> e : salesByCategory.entrySet()) {
            sb.append(String.format("%-20s %10s%n", e.getKey(), e.getValue().toPlainString()));
        }
        sb.append(String.format("%n%-20s %10s%n", "TOTAL", totalSales.toPlainString()));
        return sb.toString();
    }
}
