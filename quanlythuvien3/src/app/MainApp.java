package app;

import server.LibraryServer;
import client.ClientUI;
import client.AdminUI;
import client.RegisterUI; 

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainApp {
    public static void main(String[] args) {
        // Kiểm tra server đã chạy chưa, nếu chưa thì mới khởi động
        boolean serverStarted = false;
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
            serverStarted = true;
        } catch (Exception ex) {
            // Nếu không tạo được ServerSocket thì server đã chạy
            serverStarted = false;
        }

        // Hiển thị giao diện đăng nhập
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Đăng nhập hệ thống thư viện");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(420, 320);

            JPanel panel = new JPanel();
            panel.setBackground(new Color(232, 242, 255)); // màu nền xanh nhạt
            frame.setContentPane(panel);

            // Sử dụng GroupLayout cho form đăng nhập
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            JLabel lblUser = new JLabel("Tên đăng nhập:");
            lblUser.setForeground(new Color(0, 51, 102));
            JTextField txtUser = new JTextField(18);
            txtUser.setBackground(new Color(255, 255, 255));
            txtUser.setForeground(new Color(0, 51, 102));

            JLabel lblPass = new JLabel("Mật khẩu:");
            lblPass.setForeground(new Color(0, 51, 102));
            JPasswordField txtPass = new JPasswordField(18);
            txtPass.setBackground(new Color(255, 255, 255));
            txtPass.setForeground(new Color(0, 51, 102));

            JCheckBox chkShowPass = new JCheckBox("Hiện mật khẩu");
            chkShowPass.setBackground(new Color(232, 242, 255));
            chkShowPass.setForeground(new Color(0, 102, 204));

            JButton btnLogin = new JButton("Đăng nhập");
            btnLogin.setBackground(new Color(0, 102, 204));
            btnLogin.setForeground(Color.WHITE);

            JButton btnRegister = new JButton("Đăng ký");
            btnRegister.setBackground(new Color(0, 153, 76));
            btnRegister.setForeground(Color.WHITE);

            JButton btnForgot = new JButton("Quên mật khẩu?");
            btnForgot.setBorderPainted(false);
            btnForgot.setFocusPainted(false);
            btnForgot.setContentAreaFilled(false);
            btnForgot.setForeground(new Color(255, 102, 0));

            JLabel lblMsg = new JLabel("");
            lblMsg.setForeground(new Color(204, 0, 0));

            // Hiện/ẩn mật khẩu
            chkShowPass.addActionListener(e -> {
                txtPass.setEchoChar(chkShowPass.isSelected() ? (char) 0 : '\u2022');
            });

            // Layout cho các thành phần
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(lblUser)
                            .addComponent(lblPass))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(txtUser)
                            .addComponent(txtPass)
                            .addComponent(chkShowPass)))
                    .addComponent(lblMsg)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnLogin, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRegister, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnForgot)
            );
            layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addGap(30)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblUser)
                        .addComponent(txtUser))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPass)
                        .addComponent(txtPass))
                    .addComponent(chkShowPass)
                    .addGap(10)
                    .addComponent(lblMsg)
                    .addGap(10)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(btnLogin)
                        .addComponent(btnRegister))
                    .addComponent(btnForgot)
            );

            btnLogin.addActionListener(e -> {
                String username = txtUser.getText();
                String password = new String(txtPass.getPassword());
                lblMsg.setText("");
                if (username.isEmpty() || password.isEmpty()) {
                    lblMsg.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
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
                        frame.dispose();
                        if ("admin".equals(role)) {
                            SwingUtilities.invokeLater(() -> new AdminUI().setVisible(true));
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                ClientUI ui = new ClientUI();
                                ui.setUserInfo(userId, username);
                                ui.setVisible(true);
                            });
                        }
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

            // Quên mật khẩu
            btnForgot.addActionListener(e -> {
                JDialog dialog = new JDialog(frame, "Khôi phục mật khẩu", true);
                dialog.setSize(370, 200);

                JPanel recoverPanel = new JPanel();
                recoverPanel.setBackground(new Color(255, 245, 230)); // màu nền cam nhạt
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
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
