package client;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class RegisterUI extends JFrame {
    public RegisterUI() {
        setTitle("Đăng ký tài khoản");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 2));

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        JTextField txtUser = new JTextField();
        JLabel lblPhone = new JLabel("Số điện thoại:");
        JTextField txtPhone = new JTextField();
        JLabel lblEmail = new JLabel("Email:");
        JTextField txtEmail = new JTextField();
        JLabel lblPass = new JLabel("Mật khẩu:");
        JPasswordField txtPass = new JPasswordField();
        JLabel lblRePass = new JLabel("Nhập lại mật khẩu:");
        JPasswordField txtRePass = new JPasswordField();
        JButton btnRegister = new JButton("Đăng ký");
        JLabel lblMsg = new JLabel("");

        add(lblUser); add(txtUser);
        add(lblPhone); add(txtPhone);
        add(lblEmail); add(txtEmail);
        add(lblPass); add(txtPass);
        add(lblRePass); add(txtRePass);
        add(new JLabel("")); add(btnRegister);
        add(lblMsg);

        btnRegister.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String password = new String(txtPass.getPassword());
            String repass = new String(txtRePass.getPassword());

            if (username.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || repass.isEmpty()) {
                lblMsg.setText("Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            if (!password.equals(repass)) {
                lblMsg.setText("Mật khẩu nhập lại không khớp!");
                return;
            }

            try (Socket socket = new Socket("localhost", 12345);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                in.readLine(); // WELCOME
                // Đăng ký: truyền thêm phone, email vào username để lưu (hoặc sửa server để nhận đủ)
                out.println("REGISTER|" + username + "|" + password + "|" + phone + "|" + email);
                String resp = in.readLine();
                if (resp.startsWith("REGISTER_SUCCESS")) {
                    JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.");
                    dispose();
                    SwingUtilities.invokeLater(() -> app.MainApp.main(null));
                } else {
                    lblMsg.setText("Đăng ký thất bại: " + resp);
                }
            } catch (Exception ex) {
                lblMsg.setText("Không kết nối được server!");
            }
        });

        setLocationRelativeTo(null);
    }
}
