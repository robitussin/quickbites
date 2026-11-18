package com.quickbite.ui;

import com.quickbite.model.MenuItem;
import com.quickbite.service.MenuService;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Menu tab: view, search, add, edit, and delete menu items. */
public class MenuPanel extends JPanel {
    private final MenuService service;
    private final JTextField searchField = new JTextField(15);
    private final JComboBox<String> categoryBox = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Name", "Category", "Price", "Available"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<MenuItem> shown = new ArrayList<>();

    private record Data(List<String> categories, List<MenuItem> items) {
    }

    private record Form(String name, String category, String price, boolean available) {
    }

    public MenuPanel(MenuService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        top.add(new JLabel("Name:"));
        top.add(searchField);
        top.add(new JLabel("Category:"));
        top.add(categoryBox);
        top.add(searchButton);
        top.add(clearButton);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Item");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        bottom.add(addButton);
        bottom.add(editButton);
        bottom.add(deleteButton);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> refresh());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            if (categoryBox.getItemCount() > 0) {
                categoryBox.setSelectedIndex(0);             // "All"
            }
            refresh();
        });
        addButton.addActionListener(e -> addItem());
        editButton.addActionListener(e -> editItem());
        deleteButton.addActionListener(e -> deleteItem());
    }

    /** Reloads the categories and the table using the current search filters. */
    public void refresh() {
        String keyword = searchField.getText();
        String category = selectedCategory();
        BackgroundTask.run(this, null,
                () -> new Data(service.categories(), service.search(keyword, category)),
                this::show, null);
    }

    private void show(Data data) {
        String selected = selectedCategory();
        categoryBox.removeAllItems();
        categoryBox.addItem("All");
        data.categories().forEach(categoryBox::addItem);
        categoryBox.setSelectedItem(selected.isEmpty() ? "All" : selected);

        shown = data.items();
        model.setRowCount(0);
        for (MenuItem item : shown) {
            model.addRow(new Object[]{item.getName(), item.getCategory(),
                    item.getPrice().toPlainString(), item.isAvailable() ? "Yes" : "No"});
        }
    }

    private String selectedCategory() {
        Object selected = categoryBox.getSelectedItem();
        return selected == null || "All".equals(selected) ? "" : selected.toString();
    }

    private void addItem() {
        Form form = openForm(null);
        if (form == null) {
            return;
        }
        BackgroundTask.run(this, null, () -> {
            service.add(form.name(), form.category(), form.price(), form.available());
            return null;
        }, done -> refresh(), null);
    }

    private void editItem() {
        MenuItem selected = selectedItem();
        if (selected == null) {
            return;
        }
        Form form = openForm(selected);
        if (form == null) {
            return;
        }
        BackgroundTask.run(this, null, () -> {
            service.update(selected.getId(), form.name(), form.category(), form.price(), form.available());
            return null;
        }, done -> refresh(), this::refresh);
    }

    private void deleteItem() {
        MenuItem selected = selectedItem();
        if (selected == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete " + selected.getName() + "?",
                "Delete Item", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        BackgroundTask.run(this, null, () -> {
            service.delete(selected.getId());
            return null;
        }, done -> refresh(), this::refresh);
    }

    private MenuItem selectedItem() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return null;
        }
        return shown.get(row);
    }

    /** Shows the Add/Edit form. Returns null if the cashier cancels. */
    private Form openForm(MenuItem existing) {
        JTextField name = new JTextField(existing == null ? "" : existing.getName(), 20);
        JTextField category = new JTextField(existing == null ? "" : existing.getCategory(), 20);
        JTextField price = new JTextField(existing == null ? "" : existing.getPrice().toPlainString(), 20);
        JCheckBox available = new JCheckBox("Available", existing == null || existing.isAvailable());

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name"));
        panel.add(name);
        panel.add(new JLabel("Category"));
        panel.add(category);
        panel.add(new JLabel("Price"));
        panel.add(price);
        panel.add(new JLabel(""));
        panel.add(available);

        int answer = JOptionPane.showConfirmDialog(this, panel,
                existing == null ? "Add Item" : "Edit Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return null;
        }
        return new Form(name.getText(), category.getText(), price.getText(), available.isSelected());
    }
}
