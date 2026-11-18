package com.quickbite.service;

import com.quickbite.dao.MenuItemDao;
import com.quickbite.dao.OrderDao;
import com.quickbite.exception.DomainException;
import com.quickbite.exception.NotFoundException;
import com.quickbite.exception.ValidationException;
import com.quickbite.model.MenuItem;
import com.quickbite.model.Order;
import com.quickbite.model.OrderItem;
import com.quickbite.model.Status;
import com.quickbite.report.SalesReport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Business rules for orders: BR-03, BR-04, BR-05, BR-06. */
public class OrderService {
    public static final int MAX_QUANTITY = 10;

    private final OrderDao orderDao;
    private final MenuItemDao menuDao;

    public OrderService(OrderDao orderDao, MenuItemDao menuDao) {
        this.orderDao = orderDao;
        this.menuDao = menuDao;
    }

    /** BR-03: quantity per line must be 1 to 10. */
    public void validateQuantity(int quantity) throws ValidationException {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new ValidationException("Quantity must be between 1 and " + MAX_QUANTITY + ".");
        }
    }

    /**
     * Checks everything, reads current prices from the database, and saves the order.
     * @return the new order number
     */
    public int placeOrder(String customerName, List<OrderItem> requested)
            throws ValidationException, DomainException, NotFoundException {
        String name = customerName == null ? "" : customerName.trim();
        if (name.isEmpty()) {
            throw new ValidationException("Customer name is required.");
        }
        if (name.length() > 60) {
            throw new ValidationException("Customer name must be 60 characters or fewer.");
        }
        if (requested == null || requested.isEmpty()) {
            throw new ValidationException("Add at least one item to the order.");
        }

        Order order = new Order(0, name, Status.PENDING, LocalDateTime.now(), BigDecimal.ZERO);
        for (OrderItem line : requested) {
            validateQuantity(line.getQuantity());
            MenuItem current = menuDao.findById(line.getItem().getId());
            if (current == null) {
                throw new NotFoundException("Menu item not found.");
            }
            if (!current.isAvailable()) {                   // BR-04
                throw new DomainException("Item is not available.");
            }
            order.getLines().add(new OrderItem(current, line.getQuantity(), current.getPrice()));
        }
        order.recalculateTotal();
        return orderDao.insert(order);
    }

    public List<Order> findAll() {
        return orderDao.findAll();
    }

    /** Moves an order one step forward (the Next Status button). */
    public void advance(int orderId) throws DomainException, NotFoundException {
        Status next = find(orderId).getStatus().next();
        if (next == null) {
            throw new DomainException("This order cannot be advanced.");
        }
        changeStatus(orderId, next);
    }

    /** BR-06: only PENDING orders can be cancelled. */
    public void cancel(int orderId) throws DomainException, NotFoundException {
        if (find(orderId).getStatus() != Status.PENDING) {
            throw new DomainException("Only Pending orders can be cancelled.");
        }
        changeStatus(orderId, Status.CANCELLED);
    }

    /** BR-05: the one place where a status is changed, so the rule cannot be skipped. */
    public void changeStatus(int orderId, Status target) throws DomainException, NotFoundException {
        Order order = find(orderId);
        if (!order.getStatus().canTransitionTo(target)) {
            throw new DomainException("Cannot change status from " + order.getStatus() + " to " + target + ".");
        }
        if (!orderDao.updateStatus(orderId, target)) {
            throw new NotFoundException("Order not found.");
        }
    }

    public SalesReport salesReport(LocalDate date) {
        return new SalesReport(date, orderDao.findCompletedByDate(date));
    }

    private Order find(int orderId) throws NotFoundException {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found.");
        }
        return order;
    }
}
