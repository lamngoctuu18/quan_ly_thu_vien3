package client;

import javax.swing.*;
import java.awt.*;

public class AdminUI extends JFrame {
    private JPanel mainContent;
    private CardLayout cardLayout;
    private JButton currentSelectedButton;
    
    public AdminUI() {
        setTitle("Quản lý thư viện");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));

        // Main layout using BorderLayout
        setLayout(new BorderLayout());

        // Top header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Left menu panel
        JPanel menuPanel = createMenuPanel();
        add(menuPanel, BorderLayout.WEST);

        // Main content panel with CardLayout
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(Color.WHITE);
        add(mainContent, BorderLayout.CENTER);

        // Only add JPanel/JComponent, not JFrame
        DashboardUI dashboard = new DashboardUI();

        JPanel bookManagerPanel = new JPanel(new BorderLayout());
        BookManagerUI bookManagerUI = new BookManagerUI();
        bookManagerPanel.add(bookManagerUI.getContentPane(), BorderLayout.CENTER);

        JPanel userManagerPanel = new JPanel(new BorderLayout());
        UserManagerUI userManagerUI = new UserManagerUI();
        userManagerPanel.add(userManagerUI.getContentPane(), BorderLayout.CENTER);

        // Create BorrowManagementUI as embedded panel instead of separate window
        JPanel borrowClientPanel = createBorrowManagementPanel();

        JPanel borrowRequestPanel = new JPanel(new BorderLayout());
        BorrowRequestManagerUI borrowRequestUI = new BorrowRequestManagerUI();
        borrowRequestPanel.add(borrowRequestUI, BorderLayout.CENTER);

        mainContent.add(dashboard, "DASHBOARD");
        mainContent.add(bookManagerPanel, "BOOKS");
        mainContent.add(userManagerPanel, "USERS");
        mainContent.add(borrowClientPanel, "BORROWS");
        mainContent.add(borrowRequestPanel, "BORROW_REQUESTS");

        setLocationRelativeTo(null);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setPreferredSize(new Dimension(getWidth(), 60));

        JLabel title = new JLabel("Hệ thống quản lý thư viện");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(title, BorderLayout.WEST);

        JButton btnLogout = new JButton("Đăng xuất");
        styleButton(btnLogout, new Color(255, 102, 0));
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> app.MainApp.main(null));
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(0, 102, 204));
        rightPanel.add(btnLogout);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setPreferredSize(new Dimension(250, getHeight()));
        menuPanel.setBackground(new Color(51, 51, 51));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        // Menu buttons
        JButton btnDashboard = createMenuButton("Dashboard", "DASHBOARD");
        JButton btnBooks = createMenuButton("Quản lý sách", "BOOKS");
        JButton btnUsers = createMenuButton("Quản lý người dùng", "USERS");
        JButton btnBorrows = createMenuButton("Quản lý mượn/trả", "BORROWS");
        JButton btnBorrowRequests = createMenuButton("Quản lý đăng ký mượn", "BORROW_REQUESTS");

        // Add buttons to menu
        menuPanel.add(Box.createVerticalStrut(20));
        menuPanel.add(btnDashboard);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(btnBooks);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(btnUsers);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(btnBorrows);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(btnBorrowRequests);
        menuPanel.add(Box.createVerticalGlue());

        // Select dashboard by default
        selectButton(btnDashboard);
        
        return menuPanel;
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(230, 40));
        btn.setMaximumSize(new Dimension(230, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(51, 51, 51));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        btn.addActionListener(e -> {
            selectButton(btn);
            cardLayout.show(mainContent, cardName);
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != currentSelectedButton) {
                    btn.setBackground(new Color(70, 70, 70));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != currentSelectedButton) {
                    btn.setBackground(new Color(51, 51, 51));
                }
            }
        });

        return btn;
    }

    private void selectButton(JButton btn) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackground(new Color(51, 51, 51));
        }
        btn.setBackground(new Color(0, 102, 204));
        currentSelectedButton = btn;
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }
    
    private JPanel createBorrowManagementPanel() {
        // Create a wrapper panel for the borrow management UI
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Color.WHITE);
        
        try {
            // Create BorrowManagementUI but extract its content instead of opening as window
            BorrowManagementUI borrowUI = new BorrowManagementUI();
            borrowUI.setVisible(false); // Don't show as separate window
            
            // Get the content pane and add to wrapper
            wrapperPanel.add(borrowUI.getContentPane(), BorderLayout.CENTER);
            
        } catch (Exception e) {
            // Fallback: create a simple message panel
            JLabel errorLabel = new JLabel("Không thể tải giao diện quản lý mượn/trả: " + e.getMessage());
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            wrapperPanel.add(errorLabel, BorderLayout.CENTER);
        }
        
        return wrapperPanel;
    }
}