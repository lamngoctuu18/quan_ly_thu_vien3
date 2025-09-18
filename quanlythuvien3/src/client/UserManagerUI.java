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
        setSize(700, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel: nhập số điện thoại, tìm kiếm, quay lại
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtPhone = new JTextField(15);
        btnSearch = new JButton("Tìm kiếm");
        btnBack = new JButton("Quay lại");
        topPanel.add(new JLabel("Ô nhập số điện thoại"));
        topPanel.add(txtPhone);
        topPanel.add(btnSearch);
        topPanel.add(btnBack);
        add(topPanel, BorderLayout.NORTH);

        // Center panel: thông tin người dùng
        JPanel infoPanel = new JPanel(new GridLayout(5, 3));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblName = new JLabel();
        lblEmail = new JLabel();
        lblPhone = new JLabel();
        lblBorrowCount = new JLabel();
        lblReturnSoonCount = new JLabel();
        btnBorrowDetail = new JButton("Xem chi tiết");
        btnReturnDetail = new JButton("Xem chi tiết");

        infoPanel.add(new JLabel("Tên người dùng"));
        infoPanel.add(lblName);
        infoPanel.add(new JLabel(""));

        infoPanel.add(new JLabel("Email người dùng"));
        infoPanel.add(lblEmail);
        infoPanel.add(new JLabel(""));

        infoPanel.add(new JLabel("Số điện thoại"));
        infoPanel.add(lblPhone);
        infoPanel.add(new JLabel(""));

        infoPanel.add(new JLabel("Số lượng sách đang mượn"));
        infoPanel.add(lblBorrowCount);
        infoPanel.add(btnBorrowDetail);

        infoPanel.add(new JLabel("Số lượng sách sắp đến hạn trả"));
        infoPanel.add(lblReturnSoonCount);
        infoPanel.add(btnReturnDetail);

        add(infoPanel, BorderLayout.CENTER);

        // Bottom panel: nút sửa, xóa
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnEdit = new JButton("Sửa thông tin người dùng");
        btnDelete = new JButton("Xóa người dùng"); // Đổi tên nút
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        // Sự kiện tìm kiếm
        btnSearch.addActionListener(e -> searchUser());
        btnBack.addActionListener(e -> dispose());

        // Sửa thông tin người dùng
        btnEdit.addActionListener(e -> editUser());

        // Xóa người dùng
        btnDelete.addActionListener(e -> deleteUser());

        btnBorrowDetail.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chi tiết sách đang mượn"));
        btnReturnDetail.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chi tiết sách sắp đến hạn trả"));

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
