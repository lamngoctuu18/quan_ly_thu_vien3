package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminUI extends JFrame {
    public AdminUI() {
        setTitle("Giao diện admin");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Thanh tiêu đề và nút đăng xuất
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Giao diện admin");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Panel chứa các nút chức năng
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.LIGHT_GRAY);

        JButton btnBook = new JButton("Quản lý sách");
        JButton btnUser = new JButton("Quản lý người dùng");
        JButton btnBorrow = new JButton("Quản lý mượn / trả");

        Dimension btnSize = new Dimension(220, 40);
        btnBook.setMaximumSize(btnSize);
        btnUser.setMaximumSize(btnSize);
        btnBorrow.setMaximumSize(btnSize);

        btnBook.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBorrow.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(btnBook);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(btnUser);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(btnBorrow);

        add(centerPanel, BorderLayout.CENTER);

        // Sự kiện đăng xuất
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> app.MainApp.main(null));
        });

        // Sửa lại các sự kiện nút chức năng
        btnBook.addActionListener(e -> {
            new BookManagerUI().setVisible(true);
        });
        btnUser.addActionListener(e -> {
            new UserManagerUI().setVisible(true);
        });
        btnBorrow.addActionListener(e -> {
            // Đảm bảo mở đúng giao diện BorrowClientUI
            BorrowClientUI borrowUI = new BorrowClientUI();
            borrowUI.setVisible(true);
        });

        setLocationRelativeTo(null);
    }
}