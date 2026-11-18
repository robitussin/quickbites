package com.quickbite.dao;

import com.quickbite.model.Order;
import com.quickbite.model.Status;

import java.time.LocalDate;
import java.util.List;

/** What the service layer needs from the orders and order_item tables. Implemented with JDBC. */
public interface OrderDao {
    /** Saves the order and all its lines in one transaction. @return the new order id */
    int insert(Order order);

    /** All orders, newest first. Lines are not loaded. */
    List<Order> findAll();

    /** One order without its lines, or null. */
    Order findById(int id);

    /** @return false if no row has that id */
    boolean updateStatus(int id, Status status);

    /** COMPLETED orders created on that date, with their lines loaded. */
    List<Order> findCompletedByDate(LocalDate date);
}
