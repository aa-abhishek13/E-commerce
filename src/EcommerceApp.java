import java.awt.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class EcommerceApp {
    private JFrame frame;
    private JTextField nameField, categoryField, priceField, imageField;
    private JCheckBox visibilityCheck;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private java.util.List<Product> products;
    private CustomerUI customerUI;

    public EcommerceApp() {
        products = new ArrayList<>();
        customerUI = new CustomerUI(products);

        frame = new JFrame("🧑‍💼 Admin - Mini E-Commerce App");
        frame.setSize(900, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Form Section
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        nameField = new JTextField();
        categoryField = new JTextField();
        priceField = new JTextField();
        imageField = new JTextField();
        visibilityCheck = new JCheckBox("Visible");
        JButton addBtn = new JButton("Add Product");

        formPanel.add(new JLabel("Name:"));
        formPanel.add(new JLabel("Category:"));
        formPanel.add(new JLabel("Price:"));
        formPanel.add(new JLabel("Image Path / URL:"));
        formPanel.add(new JLabel("Visibility:"));
        formPanel.add(new JLabel("Action:"));

        formPanel.add(nameField);
        formPanel.add(categoryField);
        formPanel.add(priceField);
        formPanel.add(imageField);
        formPanel.add(visibilityCheck);
        formPanel.add(addBtn);

        frame.add(formPanel, BorderLayout.NORTH);

        // Table Section
        tableModel = new DefaultTableModel(new Object[]{"Name", "Category", "Price", "Image", "Visible"}, 0);
        productTable = new JTable(tableModel);
        frame.add(new JScrollPane(productTable), BorderLayout.CENTER);

        // Bottom Button
        JPanel bottomPanel = new JPanel();
        JButton openCustomerBtn = new JButton("Open Customer View");
        bottomPanel.add(openCustomerBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        addBtn.addActionListener(e -> addProduct());
        openCustomerBtn.addActionListener(e -> customerUI.showWindow());

        frame.setVisible(true);
    }

    private void addProduct() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String image = imageField.getText().trim();
        boolean visible = visibilityCheck.isSelected();

        if (name.isEmpty() || category.isEmpty() || priceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid price!");
            return;
        }

        Product p = new Product(name, category, price, image, visible);
        products.add(p);

        // Update Table
        tableModel.addRow(new Object[]{name, category, price, image, visible});

        // Update Customer UI
        customerUI.refreshView();

        // Clear form
        nameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        imageField.setText("");
        visibilityCheck.setSelected(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EcommerceApp::new);
    }
}

// --------------------------------
// Product Model
// --------------------------------
class Product {
    String name;
    String category;
    double price;
    String imagePath;
    boolean isVisible;

    public Product(String name, String category, double price, String imagePath, boolean isVisible) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.imagePath = imagePath;
        this.isVisible = isVisible;
    }
}

// --------------------------------
// Cart Item Model
// --------------------------------
class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getTotal() {
        return product.price * quantity;
    }
}

// --------------------------------
// Customer UI
// --------------------------------
class CustomerUI {
    private JFrame frame;
    private JPanel categoryPanel;
    private java.util.List<Product> products;
    private java.util.List<CartItem> cart;

    public CustomerUI(java.util.List<Product> products) {
        this.products = products;
        this.cart = new ArrayList<>();

        frame = new JFrame("🛍 Customer View - E-Commerce Store");
        frame.setSize(850, 550);
        frame.setLayout(new BorderLayout());

        categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(categoryPanel);
        frame.add(new JLabel("Available Products by Category", SwingConstants.CENTER), BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        JButton viewCartBtn = new JButton("🛒 View Cart");
        frame.add(viewCartBtn, BorderLayout.SOUTH);

        viewCartBtn.addActionListener(e -> {
            CartPage cartPage = new CartPage(cart);
            cartPage.showWindow();
        });
    }

    public void showWindow() {
        refreshView();
        frame.setVisible(true);
    }

    public void refreshView() {
        categoryPanel.removeAll();

        Map<String, java.util.List<Product>> categoryMap = new HashMap<>();
        for (Product p : products) {
            if (!p.isVisible) continue;
            categoryMap.computeIfAbsent(p.category, k -> new ArrayList<>()).add(p);
        }

        for (String category : categoryMap.keySet()) {
            JPanel catPanel = new JPanel();
            catPanel.setBorder(BorderFactory.createTitledBorder(category));
            catPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            for (Product p : categoryMap.get(category)) {
                JPanel prodCard = new JPanel(new BorderLayout());
                prodCard.setPreferredSize(new Dimension(180, 250));
                prodCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                JLabel nameLabel = new JLabel(p.name, SwingConstants.CENTER);
                JLabel priceLabel = new JLabel("₹" + p.price, SwingConstants.CENTER);
                JLabel imgLabel = new JLabel();

                // ✅ Load image safely
                try {
                    ImageIcon icon;
                    if (p.imagePath.startsWith("http")) {
                        icon = new ImageIcon(new URL(p.imagePath));
                    } else {
                        icon = new ImageIcon(p.imagePath);
                    }
                    Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    imgLabel.setIcon(new ImageIcon(img));
                } catch (Exception ex) {
                    imgLabel.setText("[Image Not Found]");
                    imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                }

                JComboBox<Integer> qtyBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
                JButton addToCartBtn = new JButton("Add to Cart");
                addToCartBtn.addActionListener(e -> {
                    int qty = (Integer) qtyBox.getSelectedItem();
                    cart.add(new CartItem(p, qty));
                    JOptionPane.showMessageDialog(frame, qty + " x " + p.name + " added to cart!");
                });

                prodCard.add(nameLabel, BorderLayout.NORTH);
                prodCard.add(imgLabel, BorderLayout.CENTER);

                JPanel bottom = new JPanel(new GridLayout(3, 1));
                bottom.add(priceLabel);
                bottom.add(qtyBox);
                bottom.add(addToCartBtn);
                prodCard.add(bottom, BorderLayout.SOUTH);

                catPanel.add(prodCard);
            }

            categoryPanel.add(catPanel);
        }

        categoryPanel.revalidate();
        categoryPanel.repaint();
    }
}

// --------------------------------
// Cart Page
// --------------------------------
class CartPage {
    private JFrame frame;
    private java.util.List<CartItem> cart;
    private DefaultTableModel model;
    private JTable table;
    private JLabel totalLabel;

    public CartPage(java.util.List<CartItem> cart) {
        this.cart = cart;
        frame = new JFrame("🛒 Cart Summary");
        frame.setSize(700, 400);
        frame.setLayout(new BorderLayout());
    }

    public void showWindow() {
        frame.getContentPane().removeAll();

        // ✅ Build table first
        model = new DefaultTableModel(new Object[]{"Product", "Category", "Price", "Qty", "Total", "Action"}, 0);
        table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        // ✅ Then create total label
        totalLabel = new JLabel("Total: ₹" + calculateTotal(), SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(totalLabel, BorderLayout.NORTH);

        // ✅ Then fill table
        refreshTable();

        // ✅ Bottom section
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1));
        JTextField addressField = new JTextField();
        bottomPanel.add(new JLabel("Enter Delivery Address:"));
        bottomPanel.add(addressField);
        JButton placeOrderBtn = new JButton("✅ Place Order");
        bottomPanel.add(placeOrderBtn);

        placeOrderBtn.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Your cart is empty!");
            } else if (addressField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter delivery address!");
            } else {
                JOptionPane.showMessageDialog(frame, "Order placed successfully!\nDelivering to: " + addressField.getText());
                cart.clear();
                frame.dispose();
            }
        });

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (CartItem item : cart) {
            model.addRow(new Object[]{item.product.name, item.product.category, "₹" + item.product.price,
                    item.quantity, "₹" + item.getTotal(), "Remove"});
        }

        // ✅ Add button renderer/editor
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        totalLabel.setText("Total: ₹" + calculateTotal());
    }

    private double calculateTotal() {
        return cart.stream().mapToDouble(CartItem::getTotal).sum();
    }

    public void removeItem(int row) {
        if (row >= 0 && row < cart.size()) {
            cart.remove(row);
            refreshTable();
        }
    }
}

// --------------------------------
// Button Renderer & Editor
// --------------------------------
class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "Remove" : value.toString());
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean clicked;
    private CartPage cartPage;
    private int row;

    public ButtonEditor(JCheckBox checkBox, CartPage cartPage) {
        super(checkBox);
        this.cartPage = cartPage;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        label = (value == null) ? "Remove" : value.toString();
        button.setText(label);
        this.row = row;
        clicked = true;
        return button;
    }

    public Object getCellEditorValue() {
        if (clicked) {
            cartPage.removeItem(row);
        }
        clicked = false;
        return label;
    }
}
