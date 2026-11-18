package com.quickbite.service;

import com.quickbite.dao.MenuItemDao;
import com.quickbite.exception.DomainException;
import com.quickbite.exception.NotFoundException;
import com.quickbite.exception.ValidationException;
import com.quickbite.model.MenuItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Business rules for the menu: BR-01 (unique name) and BR-02 (price above zero). */
public class MenuService {
    private static final BigDecimal MAX_PRICE = new BigDecimal("999999.99");

    private final MenuItemDao dao;

    public MenuService(MenuItemDao dao) {
        this.dao = dao;
    }

    public List<MenuItem> findAll() {
        return dao.findAll();
    }

    /** Blank keyword or blank category means "no filter". */
    public List<MenuItem> search(String keyword, String category) {
        String k = keyword == null ? "" : keyword.trim();
        String c = category == null ? "" : category.trim();
        return dao.search(k, c);
    }

    public List<String> categories() {
        return dao.findAll().stream().map(MenuItem::getCategory).distinct().sorted().toList();
    }

    public void add(String name, String category, String priceText, boolean available)
            throws ValidationException {
        String n = clean(name);
        String c = clean(category);
        BigDecimal price = validate(n, c, priceText);
        if (dao.findByName(n) != null) {
            throw new ValidationException("An item with this name already exists.");
        }
        dao.insert(new MenuItem(0, n, c, price, available));
    }

    public void update(int id, String name, String category, String priceText, boolean available)
            throws ValidationException, NotFoundException {
        String n = clean(name);
        String c = clean(category);
        BigDecimal price = validate(n, c, priceText);
        MenuItem sameName = dao.findByName(n);
        if (sameName != null && sameName.getId() != id) {
            throw new ValidationException("An item with this name already exists.");
        }
        if (!dao.update(new MenuItem(id, n, c, price, available))) {
            throw new NotFoundException("Menu item not found.");
        }
    }

    public void delete(int id) throws DomainException, NotFoundException {
        if (dao.isUsedInOrders(id)) {
            throw new DomainException("This item is part of existing orders. Mark it unavailable instead.");
        }
        if (!dao.delete(id)) {
            throw new NotFoundException("Menu item not found.");
        }
    }

    private static String clean(String text) {
        return text == null ? "" : text.trim();
    }

    /** Checks all inputs and returns the price as a BigDecimal with 2 decimal places. */
    private static BigDecimal validate(String name, String category, String priceText)
            throws ValidationException {
        if (name.isEmpty()) {
            throw new ValidationException("Name is required.");
        }
        if (name.length() > 60) {
            throw new ValidationException("Name must be 60 characters or fewer.");
        }
        if (category.isEmpty()) {
            throw new ValidationException("Category is required.");
        }
        if (category.length() > 30) {
            throw new ValidationException("Category must be 30 characters or fewer.");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(clean(priceText));
        } catch (NumberFormatException e) {
            throw new ValidationException("Price must be a number.");
        }
        if (price.signum() <= 0) {
            throw new ValidationException("Price must be greater than zero.");
        }
        price = price.setScale(2, RoundingMode.HALF_UP);
        if (price.compareTo(MAX_PRICE) > 0) {
            throw new ValidationException("Price is too large.");
        }
        return price;
    }
}
