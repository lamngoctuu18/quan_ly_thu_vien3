package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookManagerUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtTitle, txtAuthor, txtPublisher, txtYear, txtQuantity;
    private JComboBox<String> cbCategory;
    private JLabel lblCount;

    private static final String[] CATEGORIES = {
        "Văn học – Tiểu thuyết",
        "Khoa học – Công nghệ",
        "Kinh tế – Quản trị",
        "Tâm lý – Kỹ năng sống",
        "Giáo trình – Học thuật",
        "Trẻ em – Thiếu nhi",
        "Lịch sử – Địa lý",
        "Tôn giáo – Triết học",
        "Ngoại ngữ – Từ điển",
        "Nghệ thuật – Âm nhạc"
    };

    public BookManagerUI() {
        setTitle("Quản lý sách");
        setMinimumSize(new Dimension(700, 400));
        setPreferredSize(new Dimension(900, 600));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panel quản lý sách (bảng)
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(232, 242, 255));
        JLabel lblTitle = new JLabel("Quản lý sách");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 0));
        listPanel.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"ID sách", "Tên sách", "Tác giả", "Nhà XB", "Năm XB", "Thể loại", "SL"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        listPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(232, 242, 255));
        lblCount = new JLabel("Số lượng sách hiện tại:");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblCount.setForeground(new Color(0, 102, 204));
        bottomPanel.add(lblCount, BorderLayout.WEST);
        JButton btnAddNew = new JButton("Thêm sách mới");
        btnAddNew.setBackground(new Color(0, 153, 76));
        btnAddNew.setForeground(Color.WHITE);
        btnAddNew.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bottomPanel.add(btnAddNew, BorderLayout.EAST);

        // Thêm nút quay lại giao diện admin
        JButton btnBackAdmin = new JButton("Quay lại quản lý");
        btnBackAdmin.setBackground(new Color(0, 102, 204));
        btnBackAdmin.setForeground(Color.WHITE);
        btnBackAdmin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bottomPanel.add(btnBackAdmin, BorderLayout.CENTER);

        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        listPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Panel thêm sách mới (form) với GridBagLayout
        JPanel addPanel = new JPanel();
        addPanel.setBackground(new Color(255, 245, 230));
        GridBagLayout gbl = new GridBagLayout();
        addPanel.setLayout(gbl);

        JLabel lblAddTitle = new JLabel("Thêm sách mới");
        lblAddTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAddTitle.setForeground(new Color(255, 102, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(18, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        addPanel.add(lblAddTitle, gbc);

        gbc.gridwidth = 2; gbc.gridy++;
        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(400, 2));
        addPanel.add(sep, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10);
        addPanel.add(new JLabel("Tên sách:"), gbc);
        gbc.gridx = 1;
        txtTitle = new JTextField(18);
        txtTitle.setBackground(Color.WHITE);
        addPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(new JLabel("Tác giả:"), gbc);
        gbc.gridx = 1;
        txtAuthor = new JTextField(18);
        txtAuthor.setBackground(Color.WHITE);
        addPanel.add(txtAuthor, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(new JLabel("Nhà xuất bản:"), gbc);
        gbc.gridx = 1;
        txtPublisher = new JTextField(18);
        txtPublisher.setBackground(Color.WHITE);
        addPanel.add(txtPublisher, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(new JLabel("Năm xuất bản:"), gbc);
        gbc.gridx = 1;
        txtYear = new JTextField(18);
        txtYear.setBackground(Color.WHITE);
        addPanel.add(txtYear, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(new JLabel("Số lượng:"), gbc);
        gbc.gridx = 1;
        txtQuantity = new JTextField(18);
        txtQuantity.setBackground(Color.WHITE);
        addPanel.add(txtQuantity, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(new JLabel("Thể loại:"), gbc);
        gbc.gridx = 1;
        cbCategory = new JComboBox<>(CATEGORIES);
        cbCategory.setBackground(Color.WHITE);
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addPanel.add(cbCategory, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 10, 10, 10);
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(255, 245, 230));
        JButton btnAddBook = new JButton("Thêm sách");
        btnAddBook.setBackground(new Color(0, 153, 76));
        btnAddBook.setForeground(Color.WHITE);
        btnAddBook.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JButton btnBack = new JButton("Quay lại");
        btnBack.setBackground(new Color(0, 102, 204));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnPanel.add(btnAddBook);
        btnPanel.add(btnBack);
        addPanel.add(btnPanel, gbc);

        // Thêm panel vào CardLayout
        mainPanel.add(listPanel, "LIST");
        mainPanel.add(addPanel, "ADD");
        add(mainPanel);

        // Sự kiện chuyển đổi giao diện
        btnAddNew.addActionListener(e -> {
            clearAddForm();
            cardLayout.show(mainPanel, "ADD");
        });
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "LIST"));

        btnBackAdmin.addActionListener(e -> {
            dispose();
            // Không mở lại hoặc sinh thêm bất kỳ giao diện admin nào nữa.
            // Chỉ đóng BookManagerUI.
        });

        btnAddBook.addActionListener(e -> {
            if (addBook()) {
                cardLayout.show(mainPanel, "LIST");
            }
        });

        loadBooks();
        pack();
        setLocationRelativeTo(null);
    }

    private Connection getConn() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:C:/data/library.db");
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        int total = 0;
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM books")) {
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                total += qty;
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("year"),
                    rs.getString("category"),
                    qty
                });
            }
            lblCount.setText("Số lượng sách hiện tại: " + total);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
            lblCount.setText("Số lượng sách hiện tại: ?");
        }
    }

    private boolean addBook() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String year = txtYear.getText().trim();
        String quantity = txtQuantity.getText().trim();
        String category = (String) cbCategory.getSelectedItem();
        if (title.isEmpty() || author.isEmpty() || publisher.isEmpty() || year.isEmpty() || quantity.isEmpty() || category == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return false;
        }
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books(title, author, publisher, year, quantity, category) VALUES(?,?,?,?,?,?)");
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, publisher);
            ps.setString(4, year);
            ps.setInt(5, Integer.parseInt(quantity));
            ps.setString(6, category);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm sách thành công!");
            loadBooks();
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm sách: " + ex.getMessage());
            return false;
        }
    }

    private void clearAddForm() {
        txtTitle.setText("");
        txtAuthor.setText("");
        txtPublisher.setText("");
        txtYear.setText("");
        txtQuantity.setText("");
        cbCategory.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookManagerUI().setVisible(true));
    }
}

