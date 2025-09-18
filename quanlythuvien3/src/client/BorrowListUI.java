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
        setTitle("Danh sách sách đang mượn");
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

        JLabel lblTitle = new JLabel("Danh sách sách đang mượn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 0));

        String[] cols = {"ID sách", "Tên sách", "Tác giả", "Ngày mượn", "Ngày trả"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(232, 242, 255));
        GroupLayout btnLayout = new GroupLayout(btnPanel);
        btnPanel.setLayout(btnLayout);
        btnLayout.setAutoCreateGaps(true);
        btnLayout.setAutoCreateContainerGaps(true);

        btnBack = new JButton("Quay lại trang chủ");
        btnBack.setBackground(new Color(0, 102, 204));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 15));

        btnRefresh = new JButton("Làm mới");
        btnRefresh.setBackground(new Color(0, 153, 76));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 15));

        btnLayout.setHorizontalGroup(
            btnLayout.createSequentialGroup()
                .addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 600, Short.MAX_VALUE)
                .addComponent(btnRefresh, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
        );
        btnLayout.setVerticalGroup(
            btnLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnBack)
                .addComponent(btnRefresh)
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
        btnRefresh.addActionListener(e -> loadBorrows());

        loadBorrows();
        pack();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BorrowListUI(1).setVisible(true));
    }
}
