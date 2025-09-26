package client;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.net.URL;

public class UserDetailUI extends JFrame {
    private String userId;
    private JLabel lblName, lblEmail, lblPhone, lblJoinDate;
    private JTable borrowTable;
    private DefaultTableModel borrowModel;
    private JLabel lblAvatar;
    private JButton btnLock;

    public UserDetailUI(String userId) {
        this.userId = userId;
        setTitle("Thông tin chi tiết người dùng");
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Main panel with background color
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 245, 255));
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // User info panel
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Borrow history panel
        JPanel historyPanel = createHistoryPanel();
        mainPanel.add(historyPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 245, 255));
        JButton btnBack = new JButton("Quay lại");
        btnBack.setBackground(new Color(0, 102, 204));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLock = new JButton("Khóa tài khoản");
        btnLock.setBackground(new Color(204, 0, 0));
        btnLock.setForeground(Color.WHITE);
        btnLock.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bottomPanel.add(btnBack, BorderLayout.WEST);
        bottomPanel.add(btnLock, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> dispose());
        btnLock.addActionListener(e -> lockAccount());

        // Load user data
        loadUserData();
        
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 255), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Avatar
        lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(100, 120));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setVerticalAlignment(SwingConstants.CENTER);
        lblAvatar.setText("avata");
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(new Color(150, 120, 120));
        panel.add(lblAvatar, BorderLayout.WEST);

        // Info fields
        JPanel infoFields = new JPanel();
        infoFields.setBackground(new Color(255, 255, 255));
        infoFields.setLayout(new BoxLayout(infoFields, BoxLayout.Y_AXIS));
        infoFields.add(Box.createVerticalStrut(10));
        infoFields.add(new JLabel("Họ và tên:"));
        lblName = new JLabel();
        infoFields.add(lblName);
        infoFields.add(new JLabel("Số điện thoại:"));
        lblPhone = new JLabel();
        infoFields.add(lblPhone);
        infoFields.add(new JLabel("Email:"));
        lblEmail = new JLabel();
        infoFields.add(lblEmail);
        infoFields.add(new JLabel("Số sách đang mượn:"));
        JLabel lblBorrowCount = new JLabel();
        infoFields.add(lblBorrowCount);

        panel.add(infoFields, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 255), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel headerLabel = new JLabel("Sách đang mượn");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(0, 102, 204));
        panel.add(headerLabel, BorderLayout.NORTH);

        String[] columns = {"Tên sách", "Tác giả", "Nhà XB", "Năm XB", "Thể loại", "Ngày mượn", "Ngày trả", "Hạn trả"};
        borrowModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        borrowTable = new JTable(borrowModel);
        borrowTable.setRowHeight(28);
        borrowTable.getTableHeader().setBackground(new Color(0, 102, 204));
        borrowTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(borrowTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadUserData() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT username, email, phone, avatar FROM users WHERE id = ?");
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblName.setText(rs.getString("username"));
                lblEmail.setText(rs.getString("email"));
                lblPhone.setText(rs.getString("phone"));
                String avatarUrl = rs.getString("avatar");
                if (avatarUrl != null && avatarUrl.startsWith("http")) {
                    try {
                        ImageIcon icon = new ImageIcon(new URL(avatarUrl));
                        Image img = icon.getImage().getScaledInstance(100, 120, Image.SCALE_SMOOTH);
                        lblAvatar.setIcon(new ImageIcon(img));
                        lblAvatar.setText("");
                    } catch (Exception ex) {
                        lblAvatar.setIcon(null);
                        lblAvatar.setText("avata");
                    }
                }
            }

            // Số sách đang mượn
            PreparedStatement psBorrow = conn.prepareStatement(
                "SELECT COUNT(*) FROM borrows WHERE user_id=? AND return_date IS NULL");
            psBorrow.setString(1, userId);
            ResultSet rsBorrow = psBorrow.executeQuery();
            if (rsBorrow.next()) {
                // lblBorrowCount.setText(rsBorrow.getString(1)); // Nếu muốn hiển thị số sách đang mượn
            }

            // Load borrow history
            ps = conn.prepareStatement(
                "SELECT bk.title, bk.author, bk.publisher, bk.year, bk.category, b.borrow_date, b.return_date " +
                "FROM borrows b JOIN books bk ON b.book_id = bk.id WHERE b.user_id = ? ORDER BY b.borrow_date DESC");
            ps.setString(1, userId);
            rs = ps.executeQuery();
            borrowModel.setRowCount(0);
            while (rs.next()) {
                borrowModel.addRow(new Object[]{
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("year"),
                    rs.getString("category"),
                    rs.getString("borrow_date"),
                    rs.getString("return_date"),
                    "" // Hạn trả, có thể tính toán thêm
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void lockAccount() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn khóa tài khoản này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET role='locked' WHERE id=?");
            ps.setString(1, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Tài khoản đã bị khóa!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể khóa tài khoản!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khóa tài khoản: " + ex.getMessage());
        }
    }

    private void printUserReport() {
        try {
            // Create print dialog
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                
                Graphics2D g2d = (Graphics2D)graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                
                // Print user info
                int y = 20;
                g2d.drawString("THỐNG KÊ NGƯỜI DÙNG", 50, y);
                y += 30;
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.drawString("Họ và tên: " + lblName.getText(), 50, y);
                y += 20;
                g2d.drawString("Email: " + lblEmail.getText(), 50, y);
                y += 20;
                g2d.drawString("Số điện thoại: " + lblPhone.getText(), 50, y);
                
                return Printable.PAGE_EXISTS;
            });
            
            if (job.printDialog()) {
                job.print();
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi in thống kê: " + ex.getMessage());
        }
    }
}
