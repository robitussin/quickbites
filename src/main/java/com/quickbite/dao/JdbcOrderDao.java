package com.quickbite.dao;

import com.quickbite.exception.DataAccessException;
import com.quickbite.model.MenuItem;
import com.quickbite.model.Order;
import com.quickbite.model.OrderItem;
import com.quickbite.model.Status;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrderDao implements OrderDao {
    private static final String ORDER_COLUMNS = "id, customer_name, status, created_at, total";

    @Override
    public int insert(Order order) {
        try (Connection con = DbConnection.getConnection()) {
            con.setAutoCommit(false);                       // start the transaction
            try {
                int orderId = insertOrderRow(con, order);
                insertLines(con, orderId, order);
                con.commit();                               // both parts saved together
                return orderId;
            } catch (SQLException e) {
                con.rollback();                             // or neither is saved
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save the order", e);
        }
    }

    private int insertOrderRow(Connection con, Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_name, status, created_at, total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getCustomerName());
            ps.setString(2, order.getStatus().name());
            ps.setTimestamp(3, Timestamp.valueOf(order.getCreatedAt()));
            ps.setBigDecimal(4, order.getTotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void insertLines(Connection con, int orderId, Order order) throws SQLException {
        String sql = "INSERT INTO order_item (order_id, menu_item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (OrderItem line : order.getLines()) {
                ps.setInt(1, orderId);
                ps.setInt(2, line.getItem().getId());
                ps.setInt(3, line.getQuantity());
                ps.setBigDecimal(4, line.getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT " + ORDER_COLUMNS + " FROM orders ORDER BY id DESC";
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return readOrders(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the orders", e);
        }
    }

    @Override
    public Order findById(int id) {
        String sql = "SELECT " + ORDER_COLUMNS + " FROM orders WHERE id = ?";
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            List<Order> rows = readOrders(ps);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the order", e);
        }
    }

    @Override
    public boolean updateStatus(int id, Status status) {
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update the order status", e);
        }
    }

    @Override
    public List<Order> findCompletedByDate(LocalDate date) {
        String sql = "SELECT " + ORDER_COLUMNS + " FROM orders "
                + "WHERE status = 'COMPLETED' AND created_at >= ? AND created_at < ?";
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            List<Order> orders = readOrders(ps);
            for (Order order : orders) {
                loadLines(con, order);                      // one small query per order: simple, not fastest
            }
            return orders;
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the completed orders", e);
        }
    }

    private void loadLines(Connection con, Order order) throws SQLException {
        String sql = "SELECT oi.quantity, oi.unit_price, m.id, m.name, m.category, m.price, m.available "
                + "FROM order_item oi JOIN menu_item m ON m.id = oi.menu_item_id WHERE oi.order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, order.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MenuItem item = new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                            rs.getBigDecimal("price"), rs.getBoolean("available"));
                    order.getLines().add(new OrderItem(item, rs.getInt("quantity"), rs.getBigDecimal("unit_price")));
                }
            }
        }
    }

    private static List<Order> readOrders(PreparedStatement ps) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(new Order(rs.getInt("id"), rs.getString("customer_name"),
                        Status.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toLocalDateTime(), rs.getBigDecimal("total")));
            }
        }
        return orders;
    }
}
