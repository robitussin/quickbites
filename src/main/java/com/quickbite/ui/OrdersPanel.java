package com.quickbite.ui;

import com.quickbite.model.Order;
import com.quickbite.service.OrderService;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Orders tab: see all orders, move them forward, or cancel them. */
public class OrdersPanel extends JPanel {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderService service;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"No.", "Customer", "Status", "Created", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Order> shown = new ArrayList<>();

    public OrdersPanel(OrderService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton nextButton = new JButton("Next Status");
        JButton cancelButton = new JButton("Cancel Order");
        JButton refreshButton = new JButton("Refresh");
        bottom.add(nextButton);
        bottom.add(cancelButton);
        bottom.add(refreshButton);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        nextButton.addActionListener(e -> change(true));
        cancelButton.addActionListener(e -> change(false));
        refreshButton.addActionListener(e -> refresh());
    }

    public void refresh() {
        BackgroundTask.run(this, null, service::findAll, orders -> {
            shown = orders;
            model.setRowCount(0);
            for (Order order : orders) {
                model.addRow(new Object[]{order.getId(), order.getCustomerName(), order.getStatus(),
                        order.getCreatedAt().format(TIME), order.getTotal().toPlainString()});
            }
        }, null);
    }

    private void change(boolean advance) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }
        int orderId = shown.get(row).getId();
        BackgroundTask.run(this, null, () -> {
            if (advance) {
                service.advance(orderId);
            } else {
                service.cancel(orderId);
            }
            return null;
        }, done -> refresh(), this::refresh);
    }
}
