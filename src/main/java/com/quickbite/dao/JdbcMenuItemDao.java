package com.quickbite.dao;

import com.quickbite.exception.DataAccessException;
import com.quickbite.model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcMenuItemDao implements MenuItemDao {
    private static final String COLUMNS = "id, name, category, price, available";

    @Override
    public List<MenuItem> findAll() {
        return search("", "");
    }

    @Override
    public List<MenuItem> search(String keyword, String category) {
        String sql = "SELECT " + COLUMNS + " FROM menu_item "
                + "WHERE name LIKE ? AND (? = '' OR category = ?) ORDER BY category, name";
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, category);
            ps.setString(3, category);
            return readAll(ps);
        } catch (SQLException e) {
            throw new DataAccessException("Could not search the menu", e);
        }
    }

    @Override
    public MenuItem findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM menu_item WHERE id = ?";
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            List<MenuItem> rows = readAll(ps);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the menu item", e);
        }
    }

    @Override
    public MenuItem findByName(String name) {
        String sql = "SELECT " + COLUMNS + " FROM menu_item WHERE name = ?";
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            List<MenuItem> rows = readAll(ps);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (SQLException e) {
            throw new DataAccessException("Could not read the menu item", e);
        }
    }

    @Override
    public int insert(MenuItem item) {
        String sql = "INSERT INTO menu_item (name, category, price, available) VALUES (?, ?, ?, ?)";
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not add the menu item", e);
        }
    }

    @Override
    public boolean update(MenuItem item) {
        String sql = "UPDATE menu_item SET name = ?, category = ?, price = ?, available = ? WHERE id = ?";
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setInt(5, item.getId());
            return ps.executeUpdate() > 0;      // 0 rows affected means "not found"
        } catch (SQLException e) {
            throw new DataAccessException("Could not update the menu item", e);
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM menu_item WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete the menu item", e);
        }
    }

    @Override
    public boolean isUsedInOrders(int id) {
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM order_item WHERE menu_item_id = ? LIMIT 1")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check the menu item", e);
        }
    }

    private static List<MenuItem> readAll(PreparedStatement ps) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                        rs.getBigDecimal("price"), rs.getBoolean("available")));
            }
        }
        return items;
    }
}
