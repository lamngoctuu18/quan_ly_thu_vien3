package client;

import javax.swing.*;
import java.awt.*;

public class AdminUI extends JFrame implements DarkModeManager.DarkModeListener {
    private JPanel mainContent;
    private CardLayout cardLayout;
    private ModernSidebarButton currentSelectedButton;
    
    // Resource managers để giữ app không sập
    private DatabaseManager dbManager;
    private BackgroundTaskManager taskManager;
    private KeepAliveManager keepAliveManager;
    private SystemTimeoutManager timeoutManager;
    
    // Dark Mode support
    private DarkModeManager darkModeManager;
    
    public AdminUI() {
        setTitle("Quản lý thư viện");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1500, 1000));
        
        // Khởi tạo resource managers
        initializeResourceManagers();
        initializeDarkMode();
        setupKeepAlive();
        setupShutdownHook();

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
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(52, 152, 219), 
                                                         getWidth(), 0, new Color(41, 128, 185));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle shadow at bottom
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRect(0, getHeight()-2, getWidth(), 2);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(getWidth(), 70));
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Left side - Title
        JLabel title = new JLabel(" Hệ thống quản lý thư viện");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        
        // Right side - User info and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        // Admin avatar
        JLabel adminAvatar = new JLabel("👤");
        adminAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        adminAvatar.setForeground(Color.WHITE);
        
        // Admin name
        JLabel adminName = new JLabel("Admin");
        adminName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        adminName.setForeground(Color.WHITE);
        
        // Modern logout button
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect for logout button
        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnLogout.setBackground(new Color(192, 57, 43));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnLogout.setBackground(new Color(231, 76, 60));
            }
        });
        
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn đăng xuất?", 
                "Xác nhận đăng xuất", 
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> app.MainApp.main(null));
            }
        });
        
        // Dark Mode toggle button
        JButton btnDarkMode = darkModeManager.createDarkModeToggleButton();
        
        // Theme selector button
        JButton btnTheme = new JButton("🎨");
        btnTheme.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btnTheme.setForeground(Color.WHITE);
        btnTheme.setBackground(new Color(155, 89, 182));
        btnTheme.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnTheme.setFocusPainted(false);
        btnTheme.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTheme.setToolTipText("Thay đổi theme sidebar");
        
        btnTheme.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnTheme.setBackground(new Color(142, 68, 173));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnTheme.setBackground(new Color(155, 89, 182));
            }
        });
        
        btnTheme.addActionListener(e -> ThemeSelector.showThemeSelector(this));
        
        rightPanel.add(adminAvatar);
        rightPanel.add(adminName);
        rightPanel.add(btnDarkMode);
        rightPanel.add(btnTheme);
        rightPanel.add(btnLogout);
        
        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern gradient sidebar background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(44, 62, 80), 
                                                         0, getHeight(), new Color(52, 73, 94));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Right border shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRect(getWidth()-3, 0, 3, getHeight());
            }
        };
        menuPanel.setOpaque(false);
        menuPanel.setPreferredSize(new Dimension(250, getHeight()));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        // Modern sidebar buttons with smooth animations
        ModernSidebarButton btnDashboard = createModernSidebarButton("🏠", "Dashboard", "DASHBOARD");
        ModernSidebarButton btnBooks = createModernSidebarButton("📚", "Quản lý sách", "BOOKS");
        ModernSidebarButton btnUsers = createModernSidebarButton("👥", "Quản lý người dùng", "USERS");
        ModernSidebarButton btnBorrows = createModernSidebarButton("📋", "Quản lý mượn/trả", "BORROWS");
        ModernSidebarButton btnBorrowRequests = createModernSidebarButton("📝", "Quản lý đăng ký mượn", "BORROW_REQUESTS");

        // Add buttons to menu with proper spacing
        menuPanel.add(Box.createVerticalStrut(30));
        menuPanel.add(btnDashboard);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(btnBooks);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(btnUsers);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(btnBorrows);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(btnBorrowRequests);
        menuPanel.add(Box.createVerticalGlue());

        // Select dashboard by default
        selectModernButton(btnDashboard);
        
        return menuPanel;
    }

    private ModernSidebarButton createModernSidebarButton(String icon, String text, String cardName) {
        ModernSidebarButton btn = new ModernSidebarButton(icon, text);
        
        btn.addActionListener(e -> {
            selectModernButton(btn);
            cardLayout.show(mainContent, cardName);
        });
        
        return btn;
    }



    private void selectModernButton(ModernSidebarButton btn) {
        if (currentSelectedButton != null) {
            currentSelectedButton.setSelected(false);
        }
        btn.setSelected(true);
        currentSelectedButton = btn;
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
    
    /**
     * Khởi tạo resource managers để giữ app ổn định
     */
    private void initializeResourceManagers() {
        try {
            dbManager = DatabaseManager.getInstance();
            taskManager = BackgroundTaskManager.getInstance();
            keepAliveManager = KeepAliveManager.getInstance();
            timeoutManager = SystemTimeoutManager.getInstance();
            
            System.out.println("✅ Admin resource managers initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize admin resource managers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Khởi tạo Dark Mode support
     */
    private void initializeDarkMode() {
        try {
            darkModeManager = DarkModeManager.getInstance();
            darkModeManager.addDarkModeListener(this);
            
            // Apply current theme
            applyCurrentTheme();
            
            System.out.println("✅ Dark Mode support initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Dark Mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Setup keep-alive system để ngăn admin interface timeout
     */
    private void setupKeepAlive() {
        try {
            if (keepAliveManager != null) {
                keepAliveManager.start();
                System.out.println("🔄 Keep-alive system started for AdminUI");
            }
            
            if (timeoutManager != null) {
                timeoutManager.startActivityMonitoring();
                System.out.println("⏰ Timeout prevention system activated");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to start keep-alive system: " + e.getMessage());
        }
    }
    
    /**
     * Setup shutdown hook để cleanup resources khi thoát
     */
    private void setupShutdownHook() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(AdminUI.this,
                    "Bạn có chắc chắn muốn thoát khỏi hệ thống quản lý?",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION);
                    
                if (choice == JOptionPane.YES_OPTION) {
                    cleanup();
                    System.exit(0);
                }
            }
        });
    }
    
    /**
     * Apply current theme to all components
     */
    private void applyCurrentTheme() {
        SwingUtilities.invokeLater(() -> {
            if (darkModeManager != null) {
                // Update main content background
                if (mainContent != null) {
                    mainContent.setBackground(darkModeManager.getBackgroundColor());
                    darkModeManager.applyDarkMode(mainContent);
                }
                
                // Update content pane background
                getContentPane().setBackground(darkModeManager.getBackgroundColor());
                
                repaint();
            }
        });
    }
    
    /**
     * Dark mode change callback
     */
    @Override
    public void onDarkModeChanged(boolean isDarkMode) {
        applyCurrentTheme();
    }
    
    /**
     * Cleanup tất cả resources khi thoát
     */
    private void cleanup() {
        try {
            // Cleanup sidebar button animations
            if (currentSelectedButton != null) {
                currentSelectedButton.cleanup();
            }
            
            // Cleanup dark mode listener
            if (darkModeManager != null) {
                darkModeManager.removeDarkModeListener(this);
            }
            
            if (timeoutManager != null) {
                timeoutManager.stop();
            }
            if (keepAliveManager != null && keepAliveManager.isActive()) {
                keepAliveManager.stop();
            }
            if (taskManager != null && !taskManager.isShutdown()) {
                taskManager.shutdown();
            }
            if (dbManager != null) {
                dbManager.shutdown();
            }
            System.out.println("✅ Admin resources cleanup completed");
        } catch (Exception e) {
            System.err.println("❌ Error during admin cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Execute admin operations với loading dialog
     */
    public void executeWithLoading(String message, Runnable task) {
        if (taskManager != null) {
            taskManager.executeWithLoading(
                this,
                message,
                () -> {
                    task.run();
                    return null;
                },
                result -> {
                    // Success callback
                },
                error -> {
                    JOptionPane.showMessageDialog(this,
                        "Lỗi thực thi: " + error.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            );
        } else {
            // Fallback: run directly
            task.run();
        }
    }
}