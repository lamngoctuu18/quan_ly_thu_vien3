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
            frame.setSize(350, 200);
            frame.setLayout(new GridLayout(5, 2));

            JLabel lblUser = new JLabel("Tên đăng nhập:");
            JTextField txtUser = new JTextField();
            JLabel lblPass = new JLabel("Mật khẩu:");
            JPasswordField txtPass = new JPasswordField();
            JButton btnLogin = new JButton("Đăng nhập");
            JButton btnRegister = new JButton("Đăng ký");
            JLabel lblMsg = new JLabel("");

            frame.add(lblUser);
            frame.add(txtUser);
            frame.add(lblPass);
            frame.add(txtPass);
            frame.add(new JLabel(""));
            frame.add(btnLogin);
            frame.add(new JLabel(""));
            frame.add(btnRegister);
            frame.add(lblMsg);

            btnLogin.addActionListener(e -> {
                String username = txtUser.getText();
                String password = new String(txtPass.getPassword());
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
                SwingUtilities.invokeLater(() -> new RegisterUI().setVisible(true));
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
