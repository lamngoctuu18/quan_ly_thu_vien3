package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserManagerUI extends JFrame {
    private JTextField txtPhone;
    private JLabel lblName, lblEmail, lblPhone, lblBorrowCount, lblReturnSoonCount;
    private JButton btnSearch, btnBack, btnEdit, btnDelete;
    private JButton btnBorrowDetail, btnReturnDetail;

    public UserManagerUI() {
        setTitle("Quản lý người dùng");
        setMinimumSize(new Dimension(700, 400));
        setPreferredSize(new Dimension(900, 600));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 248, 255));
        setContentPane(mainPanel);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Top panel: nhập số điện thoại, tìm kiếm, quay lại
        JLabel lblTitle = new JLabel("Quản lý người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 0));

        JLabel lblPhoneInput = new JLabel("Ô nhập số điện thoại:");
        lblPhoneInput.setForeground(new Color(0, 51, 102));
        txtPhone = new JTextField(15);
        txtPhone.setBackground(Color.WHITE);
        txtPhone.setForeground(new Color(0, 51, 102));
        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(new Color(0, 102, 204));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnBack = new JButton("Quay lại");
        btnBack.setBackground(new Color(255, 153, 51));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(245, 248, 255));
        GroupLayout topLayout = new GroupLayout(topPanel);
        topPanel.setLayout(topLayout);
        topLayout.setAutoCreateGaps(true);
        topLayout.setAutoCreateContainerGaps(true);
        topLayout.setHorizontalGroup(
            topLayout.createSequentialGroup()
                .addComponent(lblPhoneInput)
                .addComponent(txtPhone, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 400, Short.MAX_VALUE)
                .addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
        );
        topLayout.setVerticalGroup(
            topLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblPhoneInput)
                .addComponent(txtPhone)
                .addComponent(btnSearch)
                .addComponent(btnBack)
        );

        // Center panel: thông tin người dùng
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(255, 245, 230));
        GroupLayout infoLayout = new GroupLayout(infoPanel);
        infoPanel.setLayout(infoLayout);
        infoLayout.setAutoCreateGaps(true);
        infoLayout.setAutoCreateContainerGaps(true);

        // Tạo các label tiêu đề để dùng cho cả horizontal/vertical group
        JLabel lblNameTitle = new JLabel("Tên người dùng:");
        JLabel lblEmailTitle = new JLabel("Email người dùng:");
        JLabel lblPhoneTitle = new JLabel("Số điện thoại:");
        JLabel lblBorrowTitle = new JLabel("Số lượng sách đang mượn:");
        JLabel lblReturnTitle = new JLabel("Số lượng sách sắp đến hạn trả:");

        lblName = new JLabel();
        lblEmail = new JLabel();
        lblPhone = new JLabel();
        lblBorrowCount = new JLabel();
        lblReturnSoonCount = new JLabel();
        btnBorrowDetail = new JButton("Xem chi tiết");
        btnBorrowDetail.setBackground(new Color(0, 153, 76));
        btnBorrowDetail.setForeground(Color.WHITE);
        btnBorrowDetail.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnReturnDetail = new JButton("Xem chi tiết");
        btnReturnDetail.setBackground(new Color(255, 102, 0));
        btnReturnDetail.setForeground(Color.WHITE);
        btnReturnDetail.setFont(new Font("Segoe UI", Font.BOLD, 15));

        infoLayout.setHorizontalGroup(
            infoLayout.createSequentialGroup()
                .addGroup(infoLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(lblNameTitle)
                    .addComponent(lblEmailTitle)
                    .addComponent(lblPhoneTitle)
                    .addComponent(lblBorrowTitle)
                    .addComponent(lblReturnTitle)
                )
                .addGroup(infoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(lblName)
                    .addComponent(lblEmail)
                    .addComponent(lblPhone)
                    .addComponent(lblBorrowCount)
                    .addComponent(lblReturnSoonCount)
                )
                .addGroup(infoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGap(0)
                    .addGap(0)
                    .addGap(0)
                    .addComponent(btnBorrowDetail, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReturnDetail, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                )
        );
        infoLayout.setVerticalGroup(
            infoLayout.createSequentialGroup()
                .addGap(18)
                .addGroup(infoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNameTitle)
                    .addComponent(lblName)
                    .addGap(0))
                .addGroup(infoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmailTitle)
                    .addComponent(lblEmail)
                    .addGap(0))
                .addGroup(infoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPhoneTitle)
                    .addComponent(lblPhone)
                    .addGap(0))
                .addGroup(infoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBorrowTitle)
                    .addComponent(lblBorrowCount)
                    .addComponent(btnBorrowDetail))
                .addGroup(infoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReturnTitle)
                    .addComponent(lblReturnSoonCount)
                    .addComponent(btnReturnDetail))
        );

        // Bottom panel: nút sửa, xóa
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245, 248, 255));
        GroupLayout bottomLayout = new GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomLayout);
        bottomLayout.setAutoCreateGaps(true);
        bottomLayout.setAutoCreateContainerGaps(true);

        btnEdit = new JButton("Sửa thông tin người dùng");
        btnEdit.setBackground(new Color(0, 102, 204));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDelete = new JButton("Xóa người dùng");
        btnDelete.setBackground(new Color(204, 0, 0));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 15));

        bottomLayout.setHorizontalGroup(
            bottomLayout.createSequentialGroup()
                .addComponent(btnEdit, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnDelete, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
        );
        bottomLayout.setVerticalGroup(
            bottomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnEdit)
                .addComponent(btnDelete)
        );

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(lblTitle)
                .addComponent(topPanel)
                .addComponent(infoPanel)
                .addComponent(bottomPanel)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(18)
                .addComponent(lblTitle)
                .addComponent(topPanel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addComponent(infoPanel, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(bottomPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        );

        // Sự kiện tìm kiếm
        btnSearch.addActionListener(e -> searchUser());
        btnBack.addActionListener(e -> dispose());

        // Sửa thông tin người dùng
        btnEdit.addActionListener(e -> editUser());

        // Xóa người dùng
        btnDelete.addActionListener(e -> deleteUser());

        btnBorrowDetail.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chi tiết sách đang mượn"));
        btnReturnDetail.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chi tiết sách sắp đến hạn trả"));

        pack();
        setLocationRelativeTo(null);
    }

    private Connection getConn() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:C:/data/library.db");
    }

    private void searchUser() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!");
            return;
        }
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement("SELECT username, email, phone FROM users WHERE phone=?");
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblName.setText(rs.getString("username"));
                lblEmail.setText(rs.getString("email"));
                lblPhone.setText(rs.getString("phone"));

                // Đếm số sách đang mượn
                PreparedStatement psBorrow = conn.prepareStatement(
                    "SELECT COUNT(*) FROM borrows WHERE user_id=(SELECT id FROM users WHERE phone=?) AND return_date IS NULL");
                psBorrow.setString(1, phone);
                ResultSet rsBorrow = psBorrow.executeQuery();
                lblBorrowCount.setText(rsBorrow.next() ? rsBorrow.getString(1) : "0");

                // Đếm số sách sắp đến hạn trả (ví dụ: hạn trả trong 3 ngày tới)
                PreparedStatement psReturn = conn.prepareStatement(
                    "SELECT COUNT(*) FROM borrows WHERE user_id=(SELECT id FROM users WHERE phone=?) AND return_date IS NULL AND julianday('now') - julianday(borrow_date) >= 27");
                psReturn.setString(1, phone);
                ResultSet rsReturn = psReturn.executeQuery();
                lblReturnSoonCount.setText(rsReturn.next() ? rsReturn.getString(1) : "0");
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng với số điện thoại này!");
                lblName.setText(""); lblEmail.setText(""); lblPhone.setText("");
                lblBorrowCount.setText(""); lblReturnSoonCount.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn: " + ex.getMessage());
        }
    }

    private void editUser() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại để sửa!");
            return;
        }
        String newName = JOptionPane.showInputDialog(this, "Nhập tên mới:", lblName.getText());
        if (newName == null || newName.trim().isEmpty()) return;
        String newEmail = JOptionPane.showInputDialog(this, "Nhập email mới:", lblEmail.getText());
        if (newEmail == null || newEmail.trim().isEmpty()) return;
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET username=?, email=? WHERE phone=?");
            ps.setString(1, newName.trim());
            ps.setString(2, newEmail.trim());
            ps.setString(3, phone);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Sửa thông tin thành công!");
                searchUser();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng với số điện thoại này!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi sửa thông tin: " + ex.getMessage());
        }
    }

    private void deleteUser() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa người dùng này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE phone=?");
            ps.setString(1, phone);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Xóa người dùng thành công!");
                lblName.setText(""); lblEmail.setText(""); lblPhone.setText("");
                lblBorrowCount.setText(""); lblReturnSoonCount.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng với số điện thoại này!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa người dùng: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserManagerUI().setVisible(true));
    }
}
