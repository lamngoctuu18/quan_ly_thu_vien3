package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class BorrowListUI extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnBack, btnRefresh;
    private int userId;

    public BorrowListUI(int userId) {
        this.userId = userId;
        setTitle("Danh sách sách đã mượn");
        setMinimumSize(new Dimension(700, 400));
        setPreferredSize(new Dimension(900, 600));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(232, 242, 255));
        setContentPane(mainPanel);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel lblTitle = new JLabel("Danh sách sách đã mượn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 0));

        String[] cols = {"ID sách", "Tên sách", "Tác giả", "Ngày mượn", "Ngày trả", "Hạn trả"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        // Renderer màu cho cột Hạn trả
        table.getColumn("Hạn trả").setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String hanTra = value == null ? "" : value.toString();
                if (hanTra.matches("\\d+ ngày")) {
                    int days = Integer.parseInt(hanTra.replaceAll("[^0-9]", ""));
                    if (days > 3) {
                        c.setForeground(new Color(0, 128, 0)); // xanh
                    } else if (days > 0) {
                        c.setForeground(new Color(255, 140, 0)); // cam cảnh báo
                    } else {
                        c.setForeground(Color.RED);
                    }
                } else if (hanTra.startsWith("Quá hạn")) {
                    c.setForeground(Color.RED);
                } else if (hanTra.equals("Hết hạn")) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(232, 242, 255));
        GroupLayout btnLayout = new GroupLayout(btnPanel);
        btnPanel.setLayout(btnLayout);
        btnLayout.setAutoCreateGaps(true);
        btnLayout.setAutoCreateContainerGaps(true);

        btnBack = new JButton("Quay lại");
        btnBack.setBackground(new Color(0, 102, 204));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 15));

        // Xóa nút làm mới
        btnLayout.setHorizontalGroup(
            btnLayout.createSequentialGroup()
                .addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 720, Short.MAX_VALUE)
        );
        btnLayout.setVerticalGroup(
            btnLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnBack)
        );

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(lblTitle)
                .addComponent(scrollPane)
                .addComponent(btnPanel)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(18)
                .addComponent(lblTitle)
                .addComponent(scrollPane)
                .addGap(10)
                .addComponent(btnPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        );

        btnBack.addActionListener(e -> dispose());

        // Tự động cập nhật danh sách khi cửa sổ được kích hoạt lại (focus lại)
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                loadBorrowedBooks();
            }
        });

        // Tự động cập nhật khi cửa sổ được hiển thị lại
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                loadBorrowedBooks();
            }
        });

        loadBorrowedBooks();
        pack();
        setLocationRelativeTo(null);
    }

    // Tải danh sách tất cả sách đã mượn (chỉ hiển thị sách chưa trả)
    private void loadBorrowedBooks() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            String sql = "SELECT b.id, b.title, b.author, br.borrow_date, br.return_date " +
                         "FROM borrows br JOIN books b ON br.book_id = b.id " +
                         "WHERE br.user_id = ? AND (br.return_date IS NULL OR br.return_date = '' OR br.return_date = 'null') " +
                         "ORDER BY br.borrow_date DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            java.time.LocalDate now = java.time.LocalDate.now();
            while (rs.next()) {
                String borrowDate = rs.getString("borrow_date");
                String returnDate = rs.getString("return_date");
                // Tính hạn trả dựa trên ngày trả đã đăng ký (nếu có)
                String hanTra = "";
                try {
                    java.time.LocalDate returnDateObj = null;
                    if (returnDate != null && !returnDate.trim().isEmpty() && !"null".equalsIgnoreCase(returnDate)) {
                        returnDateObj = java.time.LocalDate.parse(returnDate);
                    }
                    if (returnDateObj != null) {
                        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(now, returnDateObj);
                        if (now.isAfter(returnDateObj)) {
                            hanTra = "Quá hạn " + Math.abs(daysLeft) + " ngày";
                        } else if (daysLeft > 1) {
                            hanTra = daysLeft + " ngày";
                        } else if (daysLeft == 1) {
                            hanTra = "1 ngày";
                        } else if (daysLeft == 0) {
                            hanTra = "Hết hạn";
                        }
                    }
                } catch (Exception ignore) {}

                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    borrowDate != null ? borrowDate : "",
                    returnDate != null ? returnDate : "",
                    hanTra
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc dữ liệu: " + ex.getMessage());
        }
    }
}
