package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserManagerUI extends JFrame {
    private JTextField txtPhone, txtLimit;
    private JButton btnSearch, btnBack;
    private JTable userTable;
    private DefaultTableModel userModel;

    public UserManagerUI() {
        setTitle("Quản lý người dùng");
        setMinimumSize(new Dimension(900, 500));
        setPreferredSize(new Dimension(1100, 600));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(240, 240, 240));
        setContentPane(mainPanel);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel lblTitle = new JLabel("Quản lý người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 0));

        JLabel lblPhoneInput = new JLabel("Số điện thoại:");
        txtPhone = new JTextField(15);
        JLabel lblLimit = new JLabel("Hạn:");
        txtLimit = new JTextField(8);
        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(new Color(0, 102, 204));
        btnSearch.setForeground(Color.WHITE);
        btnBack = new JButton("Quay lại");
        btnBack.setBackground(new Color(255, 153, 51));
        btnBack.setForeground(Color.WHITE);

        // Table columns
        String[] cols = {"Họ và tên", "Số điện thoại", "Email", "Số sách đang mượn", "Ngày tạo tài khoản", "Thao tác"};
        userModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 5; // Chỉ cột thao tác
            }
        };
        userTable = new JTable(userModel);
        userTable.setRowHeight(28);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userTable.getTableHeader().setBackground(new Color(0, 102, 204));
        userTable.getTableHeader().setForeground(Color.WHITE);

        // Renderer cho nút "Xem chi tiết"
        userTable.getColumn("Thao tác").setCellRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JButton btn = new JButton("Xem chi tiết");
                btn.setBackground(new Color(0, 153, 76));
                btn.setForeground(Color.WHITE);
                return btn;
            }
        });
        userTable.getColumn("Thao tác").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JButton btn = new JButton("Xem chi tiết");
            {
                btn.setBackground(new Color(0, 153, 76));
                btn.setForeground(Color.WHITE);
                btn.addActionListener(e -> {
                    int row = userTable.getEditingRow();
                    String userPhone = userTable.getValueAt(row, 1).toString();
                    fireEditingStopped();
                    showUserDetail(userPhone);
                });
            }
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                return btn;
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        // Layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(lblTitle)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblPhoneInput)
                    .addComponent(txtPhone, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLimit)
                    .addComponent(txtLimit, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                )
                .addComponent(scrollPane)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 800, Short.MAX_VALUE)
                    .addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                )
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(18)
                .addComponent(lblTitle)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPhoneInput)
                    .addComponent(txtPhone)
                    .addComponent(lblLimit)
                    .addComponent(txtLimit)
                    .addComponent(btnSearch))
                .addComponent(scrollPane)
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack))
        );

        btnSearch.addActionListener(e -> loadUsers());
        btnBack.addActionListener(e -> dispose());

        loadUsers();
        pack();
        setLocationRelativeTo(null);
    }

    private void loadUsers() {
        userModel.setRowCount(0);
        String phone = txtPhone.getText().trim();
        String limit = txtLimit.getText().trim();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            String sql = "SELECT id, username, phone, email, created_at FROM users WHERE role != 'admin'";
            if (!phone.isEmpty()) sql += " AND phone LIKE ?";
            if (!limit.isEmpty()) sql += " AND id <= ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            int idx = 1;
            if (!phone.isEmpty()) ps.setString(idx++, "%" + phone + "%");
            if (!limit.isEmpty()) ps.setString(idx++, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                String userPhone = rs.getString("phone");
                String email = rs.getString("email");
                String createdAt = rs.getString("created_at");

                // Đếm số sách đang mượn
                PreparedStatement psBorrow = conn.prepareStatement(
                    "SELECT COUNT(*) FROM borrows WHERE user_id=? AND return_date IS NULL");
                psBorrow.setInt(1, userId);
                ResultSet rsBorrow = psBorrow.executeQuery();
                String borrowCount = rsBorrow.next() ? rsBorrow.getString(1) : "0";

                userModel.addRow(new Object[]{
                    username, userPhone, email, borrowCount, createdAt, "Xem chi tiết"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void showUserDetail(String phone) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE phone=?");
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("id");
                UserDetailUI detailUI = new UserDetailUI(userId);
                detailUI.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserManagerUI().setVisible(true));
    }
    }
