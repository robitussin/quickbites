package com.quickbite.ui;

import com.quickbite.dao.JdbcMenuItemDao;
import com.quickbite.dao.JdbcOrderDao;
import com.quickbite.dao.MenuItemDao;
import com.quickbite.dao.OrderDao;
import com.quickbite.service.MenuService;
import com.quickbite.service.OrderService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** The main window. It creates the DAOs and services and hands them to the panels. */
public class MainFrame extends JFrame {
    private final OrderService orderService;
    private final MenuPanel menuPanel;
    private final OrderPanel orderPanel;
    private final OrdersPanel ordersPanel;
    private final JButton reportButton = new JButton("Report");

    public MainFrame() {
        super("QuickBite - Food Ordering System");

        MenuItemDao menuDao = new JdbcMenuItemDao();
        OrderDao orderDao = new JdbcOrderDao();
        MenuService menuService = new MenuService(menuDao);
        orderService = new OrderService(orderDao, menuDao);

        menuPanel = new MenuPanel(menuService);
        orderPanel = new OrderPanel(menuService, orderService);
        ordersPanel = new OrdersPanel(orderService);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Menu", menuPanel);
        tabs.addTab("New Order", orderPanel);
        tabs.addTab("Orders", ordersPanel);
        tabs.addChangeListener(e -> {                        // always show fresh data when a tab opens
            switch (tabs.getSelectedIndex()) {
                case 0 -> menuPanel.refresh();
                case 1 -> orderPanel.refresh();
                default -> ordersPanel.refresh();
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(reportButton);
        reportButton.addActionListener(e -> showReport());

        add(tabs, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        menuPanel.refresh();
    }

    private void showReport() {
        String input = JOptionPane.showInputDialog(this, "Report date (yyyy-MM-dd):", LocalDate.now().toString());
        if (input == null) {
            return;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(input.trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Use the date format yyyy-MM-dd.");
            return;
        }
        BackgroundTask.run(this, reportButton, () -> orderService.salesReport(date), report -> {
            JTextArea text = new JTextArea(report.toText(), 12, 35);
            text.setEditable(false);
            text.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
            JOptionPane.showMessageDialog(this, new JScrollPane(text), "Sales Report",
                    JOptionPane.INFORMATION_MESSAGE);
        }, null);
    }
}
