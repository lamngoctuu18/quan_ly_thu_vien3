package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BorrowListUI extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnBack, btnRefresh;
    private int userId;

    public BorrowListUI(int userId) {
        this.userId = userId;
        setTitle("List danh sách đang mượn");
        setSize(800, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("List danh sách đang mượn");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"ID sách", "Tên sách", "Tác giả", "Ngày mượn", "Ngày trả"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnBack = new JButton("Quay lại trang chủ");
        btnRefresh = new JButton("Làm mới");
        btnPanel.add(btnBack, BorderLayout.WEST);
        btnPanel.add(btnRefresh, BorderLayout.EAST);
        add(btnPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> dispose());
        btnRefresh.addActionListener(e -> loadBorrows());

        loadBorrows();
        setLocationRelativeTo(null);
    }

    private void loadBorrows() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            String sql = "SELECT b.id, b.title, b.author, br.borrow_date, br.return_date " +
                         "FROM borrows br JOIN books b ON br.book_id = b.id " +
                         "WHERE br.user_id = ? ORDER BY br.borrow_date DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("borrow_date"),
                    rs.getString("return_date") == null ? "" : rs.getString("return_date")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }
}
       