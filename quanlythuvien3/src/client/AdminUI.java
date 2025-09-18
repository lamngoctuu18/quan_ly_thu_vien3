package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminUI extends JFrame {
    public AdminUI() {
        setTitle("Giao diện admin");
        setMinimumSize(new Dimension(700, 400));
        setPreferredSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(232, 242, 255));
        setContentPane(mainPanel);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Thanh tiêu đề và nút đăng xuất
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(232, 242, 255));
        GroupLayout topLayout = new GroupLayout(topPanel);
        topPanel.setLayout(topLayout);
        topLayout.setAutoCreateGaps(true);
        topLayout.setAutoCreateContainerGaps(true);

        JLabel lblTitle = new JLabel("Giao diện admin");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(255, 102, 0));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogout.setPreferredSize(new Dimension(120, 32));

        topLayout.setHorizontalGroup(
            topLayout.createSequentialGroup()
                .addComponent(lblTitle)
                .addGap(0, 600, Short.MAX_VALUE)
                .addComponent(btnLogout, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
        );
        topLayout.setVerticalGroup(
            topLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblTitle)
                .addComponent(btnLogout)
        );

        // Panel chứa các nút chức năng
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(245, 248, 255));
        GroupLayout centerLayout = new GroupLayout(centerPanel);
        centerPanel.setLayout(centerLayout);
        centerLayout.setAutoCreateGaps(true);
        centerLayout.setAutoCreateContainerGaps(true);

        JButton btnBook = new JButton("Quản lý sách");
        btnBook.setBackground(new Color(0, 153, 76));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JButton btnUser = new JButton("Quản lý người dùng");
        btnUser.setBackground(new Color(0, 102, 204));
        btnUser.setForeground(Color.WHITE);
        btnUser.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JButton btnBorrow = new JButton("Quản lý mượn / trả");
        btnBorrow.setBackground(new Color(255, 153, 51));
        btnBorrow.setForeground(Color.WHITE);
        btnBorrow.setFont(new Font("Segoe UI", Font.BOLD, 17));

        centerLayout.setHorizontalGroup(
            centerLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGap(0, 300, Short.MAX_VALUE)
                .addComponent(btnBook, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnUser, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnBorrow, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE)
        );
        centerLayout.setVerticalGroup(
            centerLayout.createSequentialGroup()
                .addGap(40)
                .addComponent(btnBook, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
                .addGap(20)
                .addComponent(btnUser, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
                .addGap(20)
                .addComponent(btnBorrow, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
        );

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(topPanel)
                .addComponent(centerPanel)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(topPanel, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                .addGap(30)
                .addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
        );

        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> app.MainApp.main(null));
        });

        btnBook.addActionListener(e -> new BookManagerUI().setVisible(true));
        btnUser.addActionListener(e -> new UserManagerUI().setVisible(true));
        btnBorrow.addActionListener(e -> new BorrowClientUI().setVisible(true));

        pack();
        setLocationRelativeTo(null);
    }
}