package client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RegisterUI extends JFrame {
    private JTextField txtUser, txtPass, txtPhone, txtEmail;
    private JLabel lblMsg;

    public RegisterUI(Runnable onRegisterSuccess) {
        setTitle("Đăng ký tài khoản");
        setMinimumSize(new Dimension(420, 320));
        setPreferredSize(new Dimension(500, 400));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(245, 248, 255));
        setContentPane(mainPanel);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel lblTitle = new JLabel("Đăng ký tài khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setForeground(new Color(0, 51, 102));
        txtUser = new JTextField(18);
        txtUser.setBackground(Color.WHITE);
        txtUser.setForeground(new Color(0, 51, 102));

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setForeground(new Color(0, 51, 102));
        txtPass = new JTextField(18);
        txtPass.setBackground(Color.WHITE);
        txtPass.setForeground(new Color(0, 51, 102));

        JLabel lblPhone = new JLabel("Số điện thoại:");
        lblPhone.setForeground(new Color(0, 51, 102));
        txtPhone = new JTextField(18);
        txtPhone.setBackground(Color.WHITE);
        txtPhone.setForeground(new Color(0, 51, 102));

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setForeground(new Color(0, 51, 102));
        txtEmail = new JTextField(18);
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setForeground(new Color(0, 51, 102));

        JButton btnRegister = new JButton("Đăng ký");
        btnRegister.setBackground(new Color(0, 153, 76));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JButton btnBack = new JButton("Quay lại");
        btnBack.setBackground(new Color(0, 102, 204));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 15));

        lblMsg = new JLabel("");
        lblMsg.setForeground(new Color(204, 0, 0));

        // Layout cho các thành phần
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(lblTitle)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(lblUser)
                        .addComponent(lblPass)
                        .addComponent(lblPhone)
                        .addComponent(lblEmail))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(txtUser)
                        .addComponent(txtPass)
                        .addComponent(txtPhone)
                        .addComponent(txtEmail)))
                .addComponent(lblMsg)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnRegister, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(30)
                .addComponent(lblTitle)
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser)
                    .addComponent(txtUser))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPass)
                    .addComponent(txtPass))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPhone)
                    .addComponent(txtPhone))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmail)
                    .addComponent(txtEmail))
                .addGap(10)
                .addComponent(lblMsg)
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegister)
                    .addComponent(btnBack))
        );

        btnRegister.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = txtPass.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            lblMsg.setText("");
            if (username.isEmpty() || password.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                lblMsg.setText("Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            try (Socket socket = new Socket("localhost", 12345);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                in.readLine(); // WELCOME
                out.println("REGISTER|" + username + "|" + password + "|" + phone + "|" + email);
                String resp = in.readLine();
                if (resp.startsWith("REGISTER_SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
                    dispose();
                    if (onRegisterSuccess != null) onRegisterSuccess.run();
                } else {
                    lblMsg.setText("Đăng ký thất bại! " + resp.replace("REGISTER_FAIL|", ""));
                }
            } catch (Exception ex) {
                lblMsg.setText("Không kết nối được server!");
            }
        });

        btnBack.addActionListener(e -> {
            dispose();
            if (onRegisterSuccess != null) onRegisterSuccess.run();
        });

        pack();
        setLocationRelativeTo(null);
    }
}
