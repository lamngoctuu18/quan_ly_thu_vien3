package app;

import server.LibraryServer;
import client.ClientUI;
import client.RegisterUI;
import client.AdminLoginUI; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainApp {
    public static void main(String[] args) {
        // Kiểm tra server đã chạy chưa, nếu chưa thì mới khởi động
        try (ServerSocket ss = new ServerSocket(12345)) {
            ss.close();
            // Nếu tạo được ServerSocket thì server chưa chạy, khởi động server
            new Thread(() -> {
                try {
                    LibraryServer.main(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception ex) {
            // Nếu không tạo được ServerSocket thì server đã chạy
        }

        // Hiển thị giao diện đăng nhập
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hệ thống quản lý thư viện");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 850);
            frame.setMinimumSize(new Dimension(500, 750));
            frame.setResizable(false);

            // Main panel with gradient background
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    int w = getWidth(), h = getHeight();
                    GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255), 0, h, new Color(230, 240, 250));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, w, h);
                }
            };
            frame.setContentPane(panel);

            // Sử dụng GroupLayout cho form đăng nhập
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            // Header panel with logo and title
            JPanel headerPanel = new JPanel();
            headerPanel.setOpaque(false);
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            
            // Logo panel
            JPanel logoPanel = new JPanel();
            logoPanel.setOpaque(false);
            logoPanel.setPreferredSize(new Dimension(180, 180));
            
            try {
                ImageIcon originalIcon = new ImageIcon("c:\\Users\\84916\\Downloads\\logothuvien.jpg");
                Image originalImage = originalIcon.getImage();
                Image scaledImage = originalImage.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                JLabel logoLabel = new JLabel(scaledIcon);
                logoLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 130, 180), 3),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                logoPanel.add(logoLabel);
            } catch (Exception ex) {
                JLabel logoLabel = new JLabel("📚 LIBRARY", SwingConstants.CENTER);
                logoLabel.setForeground(new Color(70, 130, 180));
                logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                logoLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 130, 180), 3),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)
                ));
                logoPanel.add(logoLabel);
            }
            
            // Title panel
            JPanel titlePanel = new JPanel();
            titlePanel.setOpaque(false);
            JLabel titleLabel = new JLabel("HỆ THỐNG QUẢN LÝ THƯ VIỆN", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titleLabel.setForeground(new Color(70, 130, 180));
            titlePanel.add(titleLabel);
            
            JLabel subtitleLabel = new JLabel("Đăng nhập để tiếp tục", SwingConstants.CENTER);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitleLabel.setForeground(new Color(108, 117, 125));
            
            headerPanel.add(logoPanel);
            headerPanel.add(Box.createVerticalStrut(10));
            headerPanel.add(titlePanel);
            headerPanel.add(Box.createVerticalStrut(5));
            headerPanel.add(subtitleLabel);

            // Form panel with modern styling
            JPanel formPanel = new JPanel();
            formPanel.setOpaque(false);
            formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
            ));
            formPanel.setBackground(new Color(255, 255, 255, 220));

            JLabel lblRole = new JLabel("Vai trò:");
            lblRole.setForeground(new Color(52, 58, 64));
            lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String[] roles = {"Người dùng", "Quản trị viên"};
            JComboBox<String> cbRole = new JComboBox<>(roles);
            cbRole.setBackground(Color.WHITE);
            cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cbRole.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            cbRole.setPreferredSize(new Dimension(280, 40));

            // Add action listener to role ComboBox for automatic admin redirect
            cbRole.addActionListener(e -> {
                String selectedRole = cbRole.getSelectedItem().toString();
                if ("Quản trị viên".equals(selectedRole)) {
                    // Reset ComboBox to prevent auto-redirect on return
                    cbRole.setSelectedIndex(0);
                    frame.dispose();
                    SwingUtilities.invokeLater(() -> {
                        new AdminLoginUI(() -> {
                            // Callback to return to main login
                            main(null);
                        }).setVisible(true);
                    });
                }
            });

            JLabel lblUser = new JLabel("Tên đăng nhập:");
            lblUser.setForeground(new Color(52, 58, 64));
            lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JTextField txtUser = new JTextField(20);
            txtUser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtUser.setPreferredSize(new Dimension(280, 40));

            JLabel lblPass = new JLabel("Mật khẩu:");
            lblPass.setForeground(new Color(52, 58, 64));
            lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));

            // Password panel with show/hide button
            JPanel passPanel = new JPanel(new BorderLayout());
            passPanel.setBackground(Color.WHITE);
            passPanel.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));
            passPanel.setPreferredSize(new Dimension(280, 40));
            
            final JPasswordField txtPass = new JPasswordField(20);
            txtPass.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 5));
            txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            final JToggleButton btnShowPass = new JToggleButton("👁");
            btnShowPass.setFocusPainted(false);
            btnShowPass.setBackground(Color.WHITE);
            btnShowPass.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 12));
            btnShowPass.setPreferredSize(new Dimension(40, 40));
            btnShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            btnShowPass.addActionListener(e -> {
                if (btnShowPass.isSelected()) {
                    txtPass.setEchoChar((char) 0);
                    btnShowPass.setText("🙈");
                } else {
                    txtPass.setEchoChar('●');
                    btnShowPass.setText("👁");
                }
            });

            passPanel.add(txtPass, BorderLayout.CENTER);
            passPanel.add(btnShowPass, BorderLayout.EAST);

            JLabel lblForgot = new JLabel("Quên mật khẩu?");
            lblForgot.setForeground(new Color(70, 130, 180));
            lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblForgot.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    lblForgot.setText("<html><u>Quên mật khẩu?</u></html>");
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    lblForgot.setText("Quên mật khẩu?");
                }
            });

            JButton btnLogin = new JButton("ĐĂNG NHẬP");
            btnLogin.setBackground(new Color(70, 130, 180));
            btnLogin.setForeground(Color.WHITE);
            btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
            btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnLogin.setPreferredSize(new Dimension(280, 45));
            btnLogin.setFocusPainted(false);
            btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnLogin.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnLogin.setBackground(new Color(60, 120, 170));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    btnLogin.setBackground(new Color(70, 130, 180));
                }
            });

            JButton btnRegister = new JButton("ĐĂNG KÝ TÀI KHOẢN");
            btnRegister.setBackground(Color.WHITE);
            btnRegister.setForeground(new Color(70, 130, 180));
            btnRegister.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
            ));
            btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnRegister.setPreferredSize(new Dimension(280, 45));
            btnRegister.setFocusPainted(false);
            btnRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnRegister.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnRegister.setBackground(new Color(70, 130, 180));
                    btnRegister.setForeground(Color.WHITE);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    btnRegister.setBackground(Color.WHITE);
                    btnRegister.setForeground(new Color(70, 130, 180));
                }
            });

            JLabel lblMsg = new JLabel("");
            lblMsg.setForeground(new Color(204, 0, 0));

            // Layout for form elements inside formPanel
            GroupLayout formLayout = new GroupLayout(formPanel);
            formPanel.setLayout(formLayout);
            formLayout.setAutoCreateGaps(true);
            formLayout.setAutoCreateContainerGaps(false);

            formLayout.setHorizontalGroup(
                formLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lblRole, GroupLayout.Alignment.LEADING)
                    .addComponent(cbRole, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUser, GroupLayout.Alignment.LEADING)
                    .addComponent(txtUser, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPass, GroupLayout.Alignment.LEADING)
                    .addComponent(passPanel, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
                    .addGroup(formLayout.createSequentialGroup()
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblForgot))
                    .addComponent(lblMsg)
                    .addComponent(btnLogin, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegister, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
            );
            formLayout.setVerticalGroup(
                formLayout.createSequentialGroup()
                    .addComponent(lblRole)
                    .addGap(5)
                    .addComponent(cbRole, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addGap(15)
                    .addComponent(lblUser)
                    .addGap(5)
                    .addComponent(txtUser, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addGap(15)
                    .addComponent(lblPass)
                    .addGap(5)
                    .addComponent(passPanel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addGap(5)
                    .addComponent(lblForgot)
                    .addGap(15)
                    .addComponent(lblMsg)
                    .addGap(15)
                    .addComponent(btnLogin, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
                    .addGap(15)
                    .addComponent(btnRegister, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
                    .addGap(10)
            );

            // Main layout
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(headerPanel)
                    .addComponent(formPanel, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
            );
            layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addGap(20)
                    .addComponent(headerPanel)
                    .addGap(20)
                    .addComponent(formPanel)
                    .addGap(30)
            );

            btnLogin.addActionListener(e -> {
                lblMsg.setText("");

                // Handle regular user login (admin is handled by ComboBox listener)
                String username = txtUser.getText();
                String password = new String(txtPass.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    lblMsg.setText("Vui lòng nhập đầy đủ thông tin!");
                    return;
                }

                try (Socket socket = new Socket("localhost", 12345);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    in.readLine(); // WELCOME
                    out.println("LOGIN|" + username + "|" + password);
                    String resp = in.readLine();
                    if (resp.startsWith("LOGIN_SUCCESS")) {
                        String[] p = resp.split("\\|");
                        int userId = Integer.parseInt(p[1]);
                        String role = p[2];

                        // Only allow regular users in this interface
                        if ("admin".equals(role)) {
                            lblMsg.setText("Vui lòng chọn vai trò quản trị viên để đăng nhập!");
                            return;
                        }

                        frame.dispose();
                        SwingUtilities.invokeLater(() -> {
                            ClientUI ui = new ClientUI();
                            ui.setUserInfo(userId, username);
                            ui.setVisible(true);
                        });
                    } else if (resp.startsWith("LOGIN_FAIL|ACCOUNT_LOCKED")) {
                        // Handle locked account
                        String[] parts = resp.split("\\|", 3);
                        String message = parts.length > 2 ? parts[2] : "Tài khoản đã bị khóa";
                        
                        JOptionPane.showMessageDialog(frame,
                            "🔒 " + message,
                            "Tài khoản bị khóa",
                            JOptionPane.WARNING_MESSAGE);
                        
                        lblMsg.setText("Tài khoản đã bị khóa!");
                    } else {
                        lblMsg.setText("Đăng nhập thất bại!");
                    }
                } catch (Exception ex) {
                    lblMsg.setText("Không kết nối được server!");
                }
            });

            btnRegister.addActionListener(e -> {
                frame.dispose();
                SwingUtilities.invokeLater(() -> new RegisterUI(() -> {
                    main(null);
                }).setVisible(true));
            });

            // Remove the previous mouse listener and add the click handler
            lblForgot.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    JDialog dialog = new JDialog(frame, "Khôi phục mật khẩu", true);
                    dialog.setSize(370, 200);

                    JPanel recoverPanel = new JPanel();
                    recoverPanel.setBackground(new Color(255, 245, 230));
                    dialog.setContentPane(recoverPanel);
                    GridBagLayout gbl = new GridBagLayout();
                    recoverPanel.setLayout(gbl);

                    JLabel l1 = new JLabel("Nhập email hoặc số điện thoại:");
                    l1.setForeground(new Color(255, 102, 0));
                    JTextField txtInfo = new JTextField(22);
                    txtInfo.setBackground(Color.WHITE);
                    txtInfo.setForeground(new Color(102, 51, 0));
                    JButton btnSend = new JButton("Gửi yêu cầu");
                    btnSend.setBackground(new Color(255, 153, 51));
                    btnSend.setForeground(Color.WHITE);
                    JLabel lblStatus = new JLabel("");
                    lblStatus.setForeground(new Color(204, 0, 0));

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(8, 8, 8, 8);
                    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
                    recoverPanel.add(l1, gbc);
                    gbc.gridy++;
                    recoverPanel.add(txtInfo, gbc);
                    gbc.gridy++;
                    gbc.gridwidth = 1;
                    recoverPanel.add(btnSend, gbc);
                    gbc.gridx = 1;
                    recoverPanel.add(lblStatus, gbc);

                    btnSend.addActionListener(ev -> {
                        String info = txtInfo.getText().trim();
                        if (info.isEmpty()) {
                            lblStatus.setText("Vui lòng nhập email hoặc số điện thoại.");
                            return;
                        }
                        try (Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
                            PreparedStatement ps = conn.prepareStatement(
                                "SELECT username, password FROM users WHERE email=? OR phone=?");
                            ps.setString(1, info);
                            ps.setString(2, info);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                String user = rs.getString("username");
                                String pass = rs.getString("password");
                                lblStatus.setForeground(new Color(0,128,0));
                                lblStatus.setText("Tài khoản: " + user + ", Mật khẩu: " + pass);
                            } else {
                                lblStatus.setForeground(Color.RED);
                                lblStatus.setText("Không tìm thấy tài khoản phù hợp.");
                            }
                        } catch (Exception ex) {
                            lblStatus.setForeground(Color.RED);
                            lblStatus.setText("Lỗi truy vấn: " + ex.getMessage());
                        }
                    });

                    dialog.setLocationRelativeTo(frame);
                    dialog.setVisible(true);
                }
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
