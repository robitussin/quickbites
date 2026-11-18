package com.quickbite.dao;

import com.quickbite.model.MenuItem;

import java.util.List;

/** What the service layer needs from the menu_item table. Implemented with JDBC. */
public interface MenuItemDao {
    List<MenuItem> findAll();

    /** Name contains keyword; category "" means all categories. */
    List<MenuItem> search(String keyword, String category);

    MenuItem findById(int id);

    MenuItem findByName(String name);

    int insert(MenuItem item);

    /** @return false if no row has that id */
    boolean update(MenuItem item);

    /** @return false if no row has that id */
    boolean delete(int id);

    boolean isUsedInOrders(int id);
}
