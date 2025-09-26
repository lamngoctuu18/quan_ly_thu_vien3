package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

public class RegisterUI extends JFrame {
    private JTextField txtUser, txtPhone, txtEmail, txtAvatar;
    private JPasswordField txtPass, txtConfirmPass;
    private JLabel lblMsg;

    public RegisterUI(Runnable onRegisterSuccess) {
        setTitle("Đăng ký tài khoản - Hệ thống quản lý thư viện");
        setMinimumSize(new Dimension(520, 650));
        setPreferredSize(new Dimension(580, 850));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
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
        setContentPane(mainPanel);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Header section
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel lblTitle = new JLabel("ĐĂNG KÝ TÀI KHOẢN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(70, 130, 180));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSubtitle = new JLabel("Tạo tài khoản mới để sử dụng hệ thống", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(108, 117, 125));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblSubtitle);

        // Form panel with modern styling
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        formPanel.setBackground(new Color(255, 255, 255, 220));

        JLabel lblUser = new JLabel("Họ và tên *");
        lblUser.setForeground(new Color(52, 58, 64));
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtUser = new JTextField(20);
        txtUser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setPreferredSize(new Dimension(320, 40));

        JLabel lblPhone = new JLabel("Số điện thoại *");
        lblPhone.setForeground(new Color(52, 58, 64));
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPhone = new JTextField(20);
        txtPhone.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhone.setPreferredSize(new Dimension(320, 40));

        JLabel lblEmail = new JLabel("Email *");
        lblEmail.setForeground(new Color(52, 58, 64));
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtEmail = new JTextField(20);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEmail.setPreferredSize(new Dimension(320, 40));

        JLabel lblAvatar = new JLabel("Link ảnh đại diện");
        lblAvatar.setForeground(new Color(52, 58, 64));
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtAvatar = new JTextField(20);
        txtAvatar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtAvatar.setPreferredSize(new Dimension(250, 40));

        JLabel lblPass = new JLabel("Mật khẩu *");
        lblPass.setForeground(new Color(52, 58, 64));
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPass = new JPasswordField(20);
        txtPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setPreferredSize(new Dimension(320, 40));

        JLabel lblConfirmPass = new JLabel("Nhập lại mật khẩu *");
        lblConfirmPass.setForeground(new Color(52, 58, 64));
        lblConfirmPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtConfirmPass = new JPasswordField(20);
        txtConfirmPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        txtConfirmPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtConfirmPass.setPreferredSize(new Dimension(320, 40));

        JButton btnRegister = new JButton("ĐĂNG KÝ");
        btnRegister.setBackground(new Color(40, 167, 69));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnRegister.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnRegister.setPreferredSize(new Dimension(150, 45));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnRegister.setBackground(new Color(30, 157, 59));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnRegister.setBackground(new Color(40, 167, 69));
            }
        });

        JButton btnBack = new JButton("QUAY LẠI");
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(70, 130, 180));
        btnBack.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnBack.setPreferredSize(new Dimension(150, 45));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnBack.setBackground(new Color(70, 130, 180));
                btnBack.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnBack.setBackground(Color.WHITE);
                btnBack.setForeground(new Color(70, 130, 180));
            }
        });

        JButton btnPreview = new JButton("👁");
        btnPreview.setBackground(new Color(70, 130, 180));
        btnPreview.setForeground(Color.WHITE);
        btnPreview.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnPreview.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnPreview.setPreferredSize(new Dimension(60, 40));
        btnPreview.setFocusPainted(false);
        btnPreview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPreview.setToolTipText("Xem trước ảnh đại diện");

        lblMsg = new JLabel("");
        lblMsg.setForeground(new Color(220, 53, 69));
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Layout for form elements inside formPanel
        GroupLayout formLayout = new GroupLayout(formPanel);
        formPanel.setLayout(formLayout);
        formLayout.setAutoCreateGaps(true);
        formLayout.setAutoCreateContainerGaps(false);

        formLayout.setHorizontalGroup(
            formLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(lblUser, GroupLayout.Alignment.LEADING)
                .addComponent(txtUser, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblPhone, GroupLayout.Alignment.LEADING)
                .addComponent(txtPhone, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblEmail, GroupLayout.Alignment.LEADING)
                .addComponent(txtEmail, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblAvatar, GroupLayout.Alignment.LEADING)
                .addGroup(formLayout.createSequentialGroup()
                    .addComponent(txtAvatar, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE)
                    .addGap(10)
                    .addComponent(btnPreview, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
                .addComponent(lblPass, GroupLayout.Alignment.LEADING)
                .addComponent(txtPass, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblConfirmPass, GroupLayout.Alignment.LEADING)
                .addComponent(txtConfirmPass, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblMsg)
                .addGroup(formLayout.createSequentialGroup()
                    .addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                    .addGap(20)
                    .addComponent(btnRegister, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
        );
        formLayout.setVerticalGroup(
            formLayout.createSequentialGroup()
                .addComponent(lblUser)
                .addGap(5)
                .addComponent(txtUser, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(lblPhone)
                .addGap(5)
                .addComponent(txtPhone, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(lblEmail)
                .addGap(5)
                .addComponent(txtEmail, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(lblAvatar)
                .addGap(5)
                .addGroup(formLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(txtAvatar, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPreview, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                .addGap(10)
                .addComponent(lblPass)
                .addGap(5)
                .addComponent(txtPass, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(lblConfirmPass)
                .addGap(5)
                .addComponent(txtConfirmPass, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addGap(15)
                .addComponent(lblMsg)
                .addGap(15)
                .addGroup(formLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegister, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
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
                .addGap(20)
        );

        btnRegister.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());
            String confirmPassword = new String(txtConfirmPass.getPassword());
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String avatar = txtAvatar.getText().trim();
            
            lblMsg.setText("");

            // Validation for registration
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
                phone.isEmpty() || email.isEmpty()) {
                lblMsg.setText("Vui lòng nhập đầy đủ thông tin bắt buộc!");
                return;
            }

            // Username validation
            if (username.length() < 3) {
                lblMsg.setText("Tên người dùng phải có ít nhất 3 ký tự!");
                return;
            }
            if (username.matches(".*[<>/'\"].*")) {
                lblMsg.setText("Tên người dùng không được chứa ký tự đặc biệt!");
                return;
            }

            // Password validation
            if (password.length() < 6) {
                lblMsg.setText("Mật khẩu phải có ít nhất 6 ký tự!");
                return;
            }
            if (!password.matches(".*[A-Z].*")) {
                lblMsg.setText("Mật khẩu phải chứa ít nhất 1 chữ hoa!");
                return;
            }
            if (!password.matches(".*[0-9].*")) {
                lblMsg.setText("Mật khẩu phải chứa ít nhất 1 số!");
                return;
            }

            // Confirm password
            if (!password.equals(confirmPassword)) {
                lblMsg.setText("Mật khẩu xác nhận không khớp!");
                return;
            }

            // Phone validation
            if (!phone.matches("\\d{10}")) {
                lblMsg.setText("Số điện thoại phải có 10 chữ số!");
                return;
            }

            // Email validation
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!email.matches(emailRegex)) {
                lblMsg.setText("Email không hợp lệ!");
                return;
            }

            // Avatar URL validation (if provided)
            if (!avatar.isEmpty() && !avatar.matches("^(http|https)://.*")) {
                lblMsg.setText("Link ảnh đại diện phải bắt đầu bằng http:// hoặc https://");
                return;
            }
            
            try (Socket socket = new Socket("localhost", 12345);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                in.readLine(); // WELCOME
                out.println("REGISTER|" + username + "|" + password + "|" + phone + "|" + email + "|" + avatar);
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

        btnPreview.addActionListener(e -> {
            String imageUrl = txtAvatar.getText().trim();
            if (!imageUrl.isEmpty()) {
                try {
                    ImageIcon icon = new ImageIcon(new URL(imageUrl));
                    Image img = icon.getImage();
                    // Scale image for preview
                    Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    JLabel previewLabel = new JLabel(new ImageIcon(scaledImg));
                    
                    JDialog previewDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(btnPreview), "Xem trước ảnh đại diện", true);
                    previewDialog.add(previewLabel);
                    previewDialog.pack();
                    previewDialog.setLocationRelativeTo(btnPreview);
                    previewDialog.setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(btnPreview, "Không thể tải ảnh từ URL này!");
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
    }
}
