package com.quickbite.ui;

import com.quickbite.exception.ValidationException;
import com.quickbite.model.MenuItem;
import com.quickbite.model.OrderItem;
import com.quickbite.service.MenuService;
import com.quickbite.service.OrderService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** New Order tab: build a cart, then place the order. */
public class OrderPanel extends JPanel {
    private final MenuService menuService;
    private final OrderService orderService;
    private final JTextField customerField = new JTextField(15);
    private final JComboBox<MenuItem> itemBox = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 0, 99, 1));
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Item", "Qty", "Unit price", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel totalLabel = new JLabel("Total: 0.00");
    private final JButton placeButton = new JButton("Place Order");
    private final List<OrderItem> cart = new ArrayList<>();

    public OrderPanel(MenuService menuService, OrderService orderService) {
        this.menuService = menuService;
        this.orderService = orderService;
        setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add to Order");
        top.add(new JLabel("Customer:"));
        top.add(customerField);
        top.add(new JLabel("Item:"));
        top.add(itemBox);
        top.add(new JLabel("Qty:"));
        top.add(quantitySpinner);
        top.add(addButton);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton removeButton = new JButton("Remove Selected");
        bottom.add(removeButton);
        bottom.add(placeButton);
        bottom.add(totalLabel);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addToCart());
        removeButton.addActionListener(e -> removeFromCart());
        placeButton.addActionListener(e -> placeOrder());
    }

    /** Reloads the item drop-down from the database. */
    public void refresh() {
        BackgroundTask.run(this, null, menuService::findAll, items -> {
            itemBox.removeAllItems();
            items.forEach(itemBox::addItem);
        }, null);
    }

    private void addToCart() {
        MenuItem item = (MenuItem) itemBox.getSelectedItem();
        if (item == null) {
            return;
        }
        int quantity = (Integer) quantitySpinner.getValue();
        try {
            orderService.validateQuantity(quantity);
        } catch (ValidationException e) {
            BackgroundTask.showError(this, e);
            return;
        }
        cart.add(new OrderItem(item, quantity, item.getPrice()));
        showCart();
    }

    private void removeFromCart() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            cart.remove(row);
            showCart();
        }
    }

    private void showCart() {
        model.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem line : cart) {
            model.addRow(new Object[]{line.getItem().getName(), line.getQuantity(),
                    line.getUnitPrice().toPlainString(), line.subtotal().toPlainString()});
            total = total.add(line.subtotal());
        }
        totalLabel.setText("Total: " + total.toPlainString());
    }

    private void placeOrder() {
        String customer = customerField.getText();
        List<OrderItem> lines = new ArrayList<>(cart);
        BackgroundTask.run(this, placeButton,
                () -> orderService.placeOrder(customer, lines),
                orderId -> {
                    JOptionPane.showMessageDialog(this, "Order #" + orderId + " placed.");
                    cart.clear();
                    customerField.setText("");
                    showCart();
                }, this::refresh);
    }
}
