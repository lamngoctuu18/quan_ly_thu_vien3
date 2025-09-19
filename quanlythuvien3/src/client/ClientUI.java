package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClientUI extends JFrame {
    private JTextField txtSearch, txtAuthor, txtPublisher;
    private JComboBox<String> cbCategory;
    private JButton btnSearch, btnBorrow, btnLogout, btnFavorite, btnActivity, btnInvoice;
    private JLabel lblUser;
    private JTable table;
    private DefaultTableModel tableModel;
    private int userId = -1;
    private String username = "";

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JPopupMenu suggestPopup; // Popup gợi ý

    private static final String[] CATEGORIES = {
        "Tất cả",
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

    public ClientUI() {
        setTitle("Giao diện người dùng");
        setMinimumSize(new Dimension(900, 500));
        setPreferredSize(new Dimension(1200, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(232, 242, 255));
        setContentPane(mainPanel);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Tiêu đề
        JLabel lblTitle = new JLabel("Giao diện người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 0));

        // Top panel: search + user + logout
        JLabel lblSearch = new JLabel("Ô tìm kiếm sách");
        lblSearch.setForeground(new Color(0, 51, 102));
        txtSearch = new JTextField(12);
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setForeground(new Color(0, 51, 102));
        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(new Color(0, 102, 204));
        btnSearch.setForeground(Color.WHITE);

        lblUser = new JLabel("Tên tài khoản");
        lblUser.setForeground(new Color(0, 51, 102));
        btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(255, 102, 0));
        btnLogout.setForeground(Color.WHITE);

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(232, 242, 255));
        GroupLayout topLayout = new GroupLayout(topPanel);
        topPanel.setLayout(topLayout);
        topLayout.setAutoCreateGaps(true);
        topLayout.setAutoCreateContainerGaps(true);
        topLayout.setHorizontalGroup(
            topLayout.createSequentialGroup()
                .addComponent(lblSearch)
                .addComponent(txtSearch, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 400, Short.MAX_VALUE)
                .addComponent(lblUser)
                .addComponent(btnLogout, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
        );
        topLayout.setVerticalGroup(
            topLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblSearch)
                .addComponent(txtSearch)
                .addComponent(btnSearch)
                .addComponent(lblUser)
                .addComponent(btnLogout)
        );

        // Filter panel: tác giả, NXB, thể loại
        JLabel lblAuthor = new JLabel("Tác giả:");
        lblAuthor.setForeground(new Color(0, 51, 102));
        txtAuthor = new JTextField(10);
        txtAuthor.setBackground(Color.WHITE);
        txtAuthor.setForeground(new Color(0, 51, 102));
        JLabel lblPublisher = new JLabel("Nhà XB:");
        lblPublisher.setForeground(new Color(0, 51, 102));
        txtPublisher = new JTextField(10);
        txtPublisher.setBackground(Color.WHITE);
        txtPublisher.setForeground(new Color(0, 51, 102));
        JLabel lblCategory = new JLabel("Thể loại:");
        lblCategory.setForeground(new Color(0, 51, 102));
        cbCategory = new JComboBox<>(CATEGORIES);
        cbCategory.setBackground(Color.WHITE);
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel filterPanel = new JPanel();
        filterPanel.setBackground(new Color(232, 242, 255));
        GroupLayout filterLayout = new GroupLayout(filterPanel);
        filterPanel.setLayout(filterLayout);
        filterLayout.setAutoCreateGaps(true);
        filterLayout.setAutoCreateContainerGaps(true);
        filterLayout.setHorizontalGroup(
            filterLayout.createSequentialGroup()
                .addComponent(lblAuthor)
                .addComponent(txtAuthor, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblPublisher)
                .addComponent(txtPublisher, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblCategory)
                .addComponent(cbCategory, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
        );
        filterLayout.setVerticalGroup(
            filterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblAuthor)
                .addComponent(txtAuthor)
                .addComponent(lblPublisher)
                .addComponent(txtPublisher)
                .addComponent(lblCategory)
                .addComponent(cbCategory)
        );

        // Table
        String[] cols = {"ID sách", "Tên sách", "Tác giả", "Nhà XB", "Năm XB", "Thể loại", "SL", "Yêu thích", "Mượn"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                // Chỉ cho phép thao tác ở cột "Yêu thích" và "Mượn"
                return col == 7 || col == 8;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        // Renderer cho nút Yêu thích và Mượn
        table.getColumn("Yêu thích").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JButton btn = new JButton("Yêu thích");
                btn.setBackground(new Color(255, 153, 51));
                btn.setForeground(Color.WHITE);
                btn.setEnabled(true);
                return btn;
            }
        });
        table.getColumn("Yêu thích").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JButton btn = new JButton("Yêu thích");
            {
                btn.setBackground(new Color(255, 153, 51));
                btn.setForeground(Color.WHITE);
                btn.addActionListener(e -> {
                    int row = table.getEditingRow();
                    String bookId = table.getValueAt(row, 0).toString();
                    fireEditingStopped();
                    addToFavorite(bookId);
                });
            }
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                return btn;
            }
        });

        table.getColumn("Mượn").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JButton btn = new JButton("Mượn");
                btn.setBackground(new Color(0, 153, 76));
                btn.setForeground(Color.WHITE);
                btn.setEnabled(true);
                return btn;
            }
        });
        table.getColumn("Mượn").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JButton btn = new JButton("Mượn");
            {
                btn.setBackground(new Color(0, 153, 76));
                btn.setForeground(Color.WHITE);
                btn.addActionListener(e -> {
                    int row = table.getEditingRow();
                    String bookId = table.getValueAt(row, 0).toString();
                    fireEditingStopped();
                    borrowBook(bookId);
                });
            }
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                return btn;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        // Bottom panel: Sách yêu thích, Hoạt động, Sách đang mượn
        btnFavorite = new JButton("Sách yêu thích");
        btnFavorite.setBackground(new Color(255, 153, 51));
        btnFavorite.setForeground(Color.WHITE);
        btnFavorite.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnActivity = new JButton("Hoạt động");
        btnActivity.setBackground(new Color(0, 153, 76));
        btnActivity.setForeground(Color.WHITE);
        btnActivity.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnInvoice = new JButton("Hóa đơn");
        btnInvoice.setBackground(new Color(0, 102, 204));
        btnInvoice.setForeground(Color.WHITE);
        btnInvoice.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnBorrow = new JButton("Sách đang mượn");
        btnBorrow.setBackground(new Color(255, 102, 0));
        btnBorrow.setForeground(Color.WHITE);
        btnBorrow.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(232, 242, 255));
        GroupLayout bottomLayout = new GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomLayout);
        bottomLayout.setAutoCreateGaps(true);
        bottomLayout.setAutoCreateContainerGaps(true);
        bottomLayout.setHorizontalGroup(
            bottomLayout.createSequentialGroup()
                .addComponent(btnFavorite, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnActivity, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnInvoice, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 400, Short.MAX_VALUE)
                .addComponent(btnBorrow, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
        );
        bottomLayout.setVerticalGroup(
            bottomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnFavorite)
                .addComponent(btnActivity)
                .addComponent(btnInvoice)
                .addComponent(btnBorrow)
        );

        // Responsive layout cho mainPanel
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(lblTitle)
                .addComponent(topPanel)
                .addComponent(filterPanel)
                .addComponent(scrollPane)
                .addComponent(bottomPanel)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(18)
                .addComponent(lblTitle)
                .addComponent(topPanel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addComponent(filterPanel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addComponent(scrollPane)
                .addGap(10)
                .addComponent(bottomPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        );

        // Kết nối server
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            in.readLine(); // WELCOME
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server");
            System.exit(0);
        }

        // Sự kiện
        btnSearch.addActionListener(e -> loadBooks());
        btnFavorite.addActionListener(e -> showFavoriteBooks());
        btnActivity.addActionListener(e -> showActivities());
        btnInvoice.addActionListener(e -> showInvoices());
        btnBorrow.addActionListener(e -> {
            // Mở giao diện danh sách sách đang mượn
            BorrowListUI borrowListUI = new BorrowListUI(userId);
            borrowListUI.setVisible(true);
        });
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> app.MainApp.main(null));
        });
        txtAuthor.addActionListener(e -> loadBooks());
        txtPublisher.addActionListener(e -> loadBooks());
        cbCategory.addActionListener(e -> loadBooks());
        txtSearch.addActionListener(e -> loadBooks());

        // Gợi ý khi nhập vào txtSearch
        suggestPopup = new JPopupMenu();
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String kw = txtSearch.getText().trim();
                suggestPopup.setVisible(false);
                if (kw.length() < 1) return;

                suggestPopup.removeAll();
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT title, author, publisher FROM books WHERE title LIKE ? OR author LIKE ? OR publisher LIKE ? LIMIT 10");
                    String likeKw = "%" + kw + "%";
                    ps.setString(1, likeKw);
                    ps.setString(2, likeKw);
                    ps.setString(3, likeKw);
                    ResultSet rs = ps.executeQuery();
                    java.util.Set<String> suggestions = new java.util.LinkedHashSet<>();
                    while (rs.next()) {
                        String t = rs.getString("title");
                        String a = rs.getString("author");
                        String p = rs.getString("publisher");
                        if (t != null && t.toLowerCase().contains(kw.toLowerCase())) suggestions.add(t);
                        if (a != null && a.toLowerCase().contains(kw.toLowerCase())) suggestions.add(a);
                        if (p != null && p.toLowerCase().contains(kw.toLowerCase())) suggestions.add(p);
                    }
                    if (!suggestions.isEmpty()) {
                        for (String s : suggestions) {
                            JMenuItem item = new JMenuItem(s);
                            item.addActionListener(ev -> {
                                txtSearch.setText(s);
                                suggestPopup.setVisible(false);
                            });
                            suggestPopup.add(item);
                        }
                        suggestPopup.show(txtSearch, 0, txtSearch.getHeight());
                    }
                } catch (Exception ex) {
                    suggestPopup.setVisible(false);
                }
            }
        });
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                suggestPopup.setVisible(false);
            }
        });

        pack();
        setLocationRelativeTo(null);
        loadBooks();
    }

    // Đặt tên tài khoản khi đăng nhập thành công
    public void setUserInfo(int id, String username) {
        this.userId = id;
        this.username = username;
        lblUser.setText("Tên tài khoản " + username);
    }

    private void addToFavorite(String bookId) {
        // Gửi lệnh FAVORITE lên server để lưu lịch sử
        out.println("FAVORITE|" + userId + "|" + bookId);
        try {
            String resp = in.readLine();
            if (resp != null && resp.trim().length() > 0 && resp.startsWith("FAVORITE_SUCCESS")) {
                JOptionPane.showMessageDialog(this, "Đã thêm vào yêu thích!");
                loadBooks();
            } else if (resp != null && resp.startsWith("FAVORITE_FAIL|")) {
                String msg = resp.substring("FAVORITE_FAIL|".length());
                // Nếu đã là yêu thích thì không báo lỗi, chỉ thông báo đã có trong danh sách
                if (msg.toLowerCase().contains("update failed")) {
                    JOptionPane.showMessageDialog(this, "Sách đã có trong danh sách yêu thích!");
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi thêm vào yêu thích: " + msg);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi không xác định khi thêm vào yêu thích!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm vào yêu thích: " + ex.getMessage());
        }
    }

    private void showActivities() {
        out.println("LIST_ACTIVITIES|" + userId);
        try {
            String resp = in.readLine();
            if (resp != null && resp.startsWith("ACTIVITIES_RESULT|")) {
                String[] rows = resp.substring("ACTIVITIES_RESULT|".length()).split(";");
                DefaultTableModel model = new DefaultTableModel(new String[]{"Hành động", "Thời gian", "Tên sách"}, 0);
                for (String row : rows) {
                    if (row.trim().isEmpty()) continue;
                    String[] vals = row.split(",", 3);
                    model.addRow(new Object[]{
                        actionToText(vals[0]), vals[1], vals.length > 2 ? vals[2] : ""
                    });
                }
                JTable tbl = new JTable(model);
                tbl.setRowHeight(28);
                tbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                tbl.getTableHeader().setBackground(new Color(0, 102, 204));
                tbl.getTableHeader().setForeground(Color.WHITE);

                JPanel panel = new JPanel();
                panel.setBackground(new Color(245, 248, 255));
                panel.setLayout(new BorderLayout());
                JLabel lbl = new JLabel("Lịch sử hoạt động", SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
                lbl.setForeground(new Color(0, 102, 204));
                lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                panel.add(lbl, BorderLayout.NORTH);
                JScrollPane scroll = new JScrollPane(tbl);
                scroll.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
                panel.add(scroll, BorderLayout.CENTER);

                JDialog dialog = new JDialog(this, "Lịch sử hoạt động", true);
                dialog.setContentPane(panel);
                dialog.setMinimumSize(new Dimension(700, 400));
                dialog.setPreferredSize(new Dimension(900, 500));
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu hoạt động.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải lịch sử hoạt động: " + ex.getMessage());
        }
    }

    private String actionToText(String action) {
        switch (action) {
            case "borrow": return "Mượn sách";
            case "return": return "Trả sách";
            case "favorite": return "Yêu thích";
            default: return action;
        }
    }

    // Khi load/search sách, thêm giá trị cho cột Yêu thích (có thể là null)
    private void searchBooks() {
        String kw = txtSearch.getText().trim();
        out.println("SEARCH|" + kw);
        try {
            String resp = in.readLine();
            // Nếu dòng đầu là WELCOME, đọc tiếp dòng kết quả
            if (resp != null && resp.startsWith("WELCOME|")) {
                resp = in.readLine();
            }
            tableModel.setRowCount(0);
            if (resp != null && resp.startsWith("SEARCH_RESULT|")) {
                String[] rows = resp.substring("SEARCH_RESULT|".length()).split(";");
                for (String row : rows) {
                    if (row.trim().isEmpty()) continue;
                    String[] vals = row.split(",");
                    Object[] data = new Object[6];
                    data[0] = vals.length > 0 ? vals[0] : ""; // ID sách
                    data[1] = vals.length > 1 ? vals[1] : ""; // Tên sách
                    data[2] = vals.length > 2 ? vals[2] : ""; // Tác giả
                    data[3] = vals.length > 3 ? vals[3] : ""; // NXB
                    data[4] = vals.length > 5 ? vals[5] : ""; // Số lượng
                    data[5] = "Yêu thích"; // Nút
                    tableModel.addRow(data);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu sách.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi nhận dữ liệu từ server");
        }
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        String keyword = txtSearch.getText().trim();
        String author = txtAuthor.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String category = (String) cbCategory.getSelectedItem();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
            if (!keyword.isEmpty()) sql.append(" AND (title LIKE ? OR author LIKE ?)");
            if (!author.isEmpty()) sql.append(" AND author LIKE ?");
            if (!publisher.isEmpty()) sql.append(" AND publisher LIKE ?");
            if (category != null && !"Tất cả".equals(category)) sql.append(" AND category=?");
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (!keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            if (!author.isEmpty()) ps.setString(idx++, "%" + author + "%");
            if (!publisher.isEmpty()) ps.setString(idx++, "%" + publisher + "%");
            if (category != null && !"Tất cả".equals(category)) ps.setString(idx++, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("year"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    "Yêu thích",
                    "Mượn"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void showFavoriteBooks() {
        DefaultTableModel favModel = new DefaultTableModel(
            new String[]{"ID sách", "Tên sách", "Tác giả", "NXB", "Năm XB", "Thể loại", "Số lượng", "Xóa"}, 0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db");
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM books WHERE favorite=1")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                favModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("year"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    "Xóa"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải sách yêu thích: " + ex.getMessage());
            return;
        }
        JTable favTable = new JTable(favModel) {
            public boolean isCellEditable(int row, int col) {
                return col == 7;
            }
        };
        favTable.setRowHeight(28);
        favTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        favTable.getTableHeader().setBackground(new Color(0, 102, 204));
        favTable.getTableHeader().setForeground(Color.WHITE);

        favTable.getColumn("Xóa").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JButton btn = new JButton("Xóa");
                btn.setBackground(new Color(204, 0, 0));
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btn.setEnabled(false);
                return btn;
            }
        });
        favTable.getColumn("Xóa").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JButton btn = new JButton("Xóa");
            {
                btn.setBackground(new Color(204, 0, 0));
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btn.addActionListener(e -> {
                    int row = favTable.getEditingRow();
                    String bookId = null;
                    if (row >= 0 && row < favTable.getRowCount()) {
                        bookId = favTable.getValueAt(row, 0).toString();
                    }
                    fireEditingStopped();
                    if (bookId != null) {
                        removeFromFavorite(bookId);
                        int removeIdx = -1;
                        for (int i = 0; i < favTable.getRowCount(); i++) {
                            if (bookId.equals(favTable.getValueAt(i, 0).toString())) {
                                removeIdx = i;
                                break;
                            }
                        }
                        if (removeIdx != -1) {
                            ((DefaultTableModel) favTable.getModel()).removeRow(removeIdx);
                        }
                    }
                });
            }
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                return btn;
            }
        });

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 248, 255));
        panel.setLayout(new BorderLayout());
        JLabel lbl = new JLabel("Danh sách sách yêu thích", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(new Color(0, 102, 204));
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(lbl, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(favTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        panel.add(scroll, BorderLayout.CENTER);

        JDialog dialog = new JDialog(this, "Danh sách sách yêu thích", true);
        dialog.setContentPane(panel);
        dialog.setMinimumSize(new Dimension(700, 400));
        dialog.setPreferredSize(new Dimension(900, 500));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void removeFromFavorite(String bookId) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement("UPDATE books SET favorite=0 WHERE id=?");
            ps.setString(1, bookId);
            ps.executeUpdate();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa khỏi yêu thích: " + ex.getMessage());
        }
    }

    // Thêm phương thức mượn sách (gửi lệnh BORROW lên server)
    private void borrowBook(String bookId) {
        // Lấy thông tin sách từ DB
        String[] bookInfo = new String[5];
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM books WHERE id=?");
            ps.setString(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                bookInfo[0] = rs.getString("title");
                bookInfo[1] = rs.getString("author");
                bookInfo[2] = rs.getString("publisher");
                bookInfo[3] = rs.getString("year");
                bookInfo[4] = rs.getString("category");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không lấy được thông tin sách: " + ex.getMessage());
            return;
        }

        // Lấy thông tin người dùng từ DB
        String[] userInfo = new String[3];
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement("SELECT username, phone, email FROM users WHERE id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                userInfo[0] = rs.getString("username");
                userInfo[1] = rs.getString("phone");
                userInfo[2] = rs.getString("email");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không lấy được thông tin người dùng: " + ex.getMessage());
            return;
        }

        // Hiển thị dialog đăng ký mượn sách với GridBagLayout và responsive, các ô nhập dài hơn
        JDialog dialog = new JDialog(this, "Đăng ký mượn sách", true);
        dialog.setMinimumSize(new Dimension(500, 480));
        dialog.setPreferredSize(new Dimension(600, 520));

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 248, 255));
        dialog.setContentPane(panel);
        GridBagLayout gbl = new GridBagLayout();
        panel.setLayout(gbl);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 12, 10, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lUser = new JLabel("Tên người mượn:");
        lUser.setForeground(new Color(0, 51, 102));
        JTextField txtUser = new JTextField(userInfo[0]);
        txtUser.setEditable(false);
        txtUser.setBackground(Color.WHITE);

        JLabel lPhone = new JLabel("Số điện thoại:");
        lPhone.setForeground(new Color(0, 51, 102));
        JTextField txtPhone = new JTextField(userInfo[1]);
        txtPhone.setEditable(false);
        txtPhone.setBackground(Color.WHITE);

        JLabel lEmail = new JLabel("Email:");
        lEmail.setForeground(new Color(0, 51, 102));
        JTextField txtEmail = new JTextField(userInfo[2]);
        txtEmail.setEditable(false);
        txtEmail.setBackground(Color.WHITE);

        JLabel lBook = new JLabel("Tên sách:");
        lBook.setForeground(new Color(0, 51, 102));
        JTextField txtBook = new JTextField(bookInfo[0]);
        txtBook.setEditable(false);
        txtBook.setBackground(Color.WHITE);

        JLabel lAuthor = new JLabel("Tác giả:");
        lAuthor.setForeground(new Color(0, 51, 102));
        JTextField txtAuthor = new JTextField(bookInfo[1]);
        txtAuthor.setEditable(false);
        txtAuthor.setBackground(Color.WHITE);

        JLabel lPublisher = new JLabel("Nhà XB:");
        lPublisher.setForeground(new Color(0, 51, 102));
        JTextField txtPublisher = new JTextField(bookInfo[2]);
        txtPublisher.setEditable(false);
        txtPublisher.setBackground(Color.WHITE);

        JLabel lYear = new JLabel("Năm XB:");
        lYear.setForeground(new Color(0, 51, 102));
        JTextField txtYear = new JTextField(bookInfo[3]);
        txtYear.setEditable(false);
        txtYear.setBackground(Color.WHITE);

        JLabel lCategory = new JLabel("Thể loại:");
        lCategory.setForeground(new Color(0, 51, 102));
        JTextField txtCategory = new JTextField(bookInfo[4]);
        txtCategory.setEditable(false);
        txtCategory.setBackground(Color.WHITE);

        JLabel lBorrowDate = new JLabel("Ngày mượn:");
        lBorrowDate.setForeground(new Color(0, 51, 102));
        JTextField txtBorrowDate = new JTextField(java.time.LocalDate.now().toString());
        txtBorrowDate.setEditable(false);
        txtBorrowDate.setBackground(Color.WHITE);

        JLabel lReturnDate = new JLabel("Ngày trả:");
        lReturnDate.setForeground(new Color(0, 51, 102));
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spReturnDate = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spReturnDate, "yyyy-MM-dd");
        spReturnDate.setEditor(dateEditor);
        // Giới hạn tối đa 7 ngày
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DATE, 7);
        dateModel.setStart(java.sql.Date.valueOf(java.time.LocalDate.now()));
        dateModel.setEnd(new java.sql.Date(cal.getTimeInMillis()));
        dateModel.setValue(java.sql.Date.valueOf(java.time.LocalDate.now().plusDays(1)));

        JButton btnReg = new JButton("Đăng ký");
        btnReg.setBackground(new Color(0, 153, 76));
        btnReg.setForeground(Color.WHITE);
        btnReg.setFont(new Font("Segoe UI", Font.BOLD, 15));

        int labelWidth = 120;
        int fieldWidth = 340;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(lUser, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtUser.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lPhone, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPhone.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtPhone, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lEmail, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtEmail.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lBook, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtBook.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtBook, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lAuthor, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtAuthor.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtAuthor, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lPublisher, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPublisher.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtPublisher, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lYear, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtYear.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtYear, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lCategory, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtCategory.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtCategory, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lBorrowDate, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtBorrowDate.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(txtBorrowDate, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.weightx = 0.0;
        panel.add(lReturnDate, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        spReturnDate.setPreferredSize(new Dimension(fieldWidth, 28));
        panel.add(spReturnDate, gbc);

        gbc.gridx = 1; gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        panel.add(btnReg, gbc);

        btnReg.addActionListener(ev -> {
            String borrowDate = txtBorrowDate.getText();
            String returnDate = dateEditor.getFormat().format(spReturnDate.getValue());
            // Gửi lệnh mượn sách lên server
            out.println("BORROW|" + userId + "|" + bookId);
            try {
                String resp = in.readLine();
                if (resp != null && resp.startsWith("BORROW_SUCCESS")) {
                    // Lưu hóa đơn vào file hoadon.csv
                    saveInvoice(userInfo, bookInfo, borrowDate, returnDate);
                    JOptionPane.showMessageDialog(dialog, "Đăng ký mượn thành công!\nHóa đơn đã được lưu.");
                    dialog.dispose();
                    loadBooks();
                } else if (resp != null && resp.startsWith("BORROW_FAIL|")) {
                    JOptionPane.showMessageDialog(dialog, "Mượn sách thất bại: " + resp.substring("BORROW_FAIL|".length()));
                } else {
                    JOptionPane.showMessageDialog(dialog, "Mượn sách thất bại!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi mượn sách: " + ex.getMessage());
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Lưu hóa đơn vào file hoadon.csv
    private void saveInvoice(String[] userInfo, String[] bookInfo, String borrowDate, String returnDate) {
        File file = new File("hoadon.csv");
        boolean fileExists = file.exists();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (!fileExists) {
                pw.println("Tên người mượn,SĐT,Email,Tên sách,Tác giả,NXB,Năm XB,Thể loại,Ngày mượn,Ngày trả");
            }
            pw.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                userInfo[0], userInfo[1], userInfo[2],
                bookInfo[0], bookInfo[1], bookInfo[2], bookInfo[3], bookInfo[4],
                borrowDate, returnDate
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu hóa đơn: " + ex.getMessage());
        }
    }

    // Hiển thị danh sách hóa đơn từ file hoadon.csv
    private void showInvoices() {
        File file = new File("hoadon.csv");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Chưa có hóa đơn nào được lưu!");
            return;
        }
        DefaultTableModel model = new DefaultTableModel();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null) {
                JOptionPane.showMessageDialog(this, "Chưa có hóa đơn nào được lưu!");
                return;
            }
            String[] headers = line.split(",");
            for (String h : headers) model.addColumn(h);
            while ((line = br.readLine()) != null) {
                model.addRow(line.split(",", -1));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc hóa đơn: " + ex.getMessage());
            return;
        }
        JTable tbl = new JTable(model);
        tbl.setRowHeight(28);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tbl.getTableHeader().setBackground(new Color(0, 102, 204));
        tbl.getTableHeader().setForeground(Color.WHITE);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 248, 255));
        panel.setLayout(new BorderLayout());
        JLabel lbl = new JLabel("Danh sách hóa đơn mượn sách", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(new Color(255, 102, 0));
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(lbl, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        panel.add(scroll, BorderLayout.CENTER);

        JDialog dialog = new JDialog(this, "Danh sách hóa đơn mượn sách", true);
        dialog.setContentPane(panel);
        dialog.setMinimumSize(new Dimension(900, 400));
        dialog.setPreferredSize(new Dimension(1100, 500));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            ui.setVisible(true);
        });
    }
}