package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;

public class ClientUI extends JFrame implements DarkModeManager.DarkModeListener {
    private JTextField txtSearch, txtAuthor, txtPublisher;
    private JComboBox<String> cbCategory;
    private JButton btnSearch, btnBorrow, btnFavorite, btnActivity;
    private JLabel lblUser;
    private JLabel lblAvatar;
    private JButton btnNotification;
    private int userId = -1;
    
    // Pagination variables
    private int currentPage = 1;
    private int itemsPerPage = 18;
    private int totalItems = 0;
    private int totalPages = 0;
    private JPanel booksGridPanel;
    private JLabel lblPageInfo;
    private JButton btnPrevPage, btnNextPage, btnFirstPage, btnLastPage;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JPopupMenu suggestPopup;
    
    // Resource management
    private DatabaseManager dbManager;
    private BackgroundTaskManager taskManager;
    private ScheduledFuture<?> keepAliveTask;
    private Timer refreshTimer;
    
    // Dark Mode support
    private DarkModeManager darkModeManager;

    private static final String[] CATEGORIES = {
        "Tất cả",
        "Văn học – Tiểu thuyết",
        "Khoa học – Công nghệ",
        "Kinh tế – Quản trị",
        "Tâm lý – Kỹ năng sống",
        "Giáo trình – Học thuật",
        "Trẻ em – Thiếu nhi",
        "Lịch sử – Địa lý",
        "Tôn giáo – Triết học",
        "Ngoại ngữ – Từ điển",
        "Nghệ thuật – Âm nhạc"
    };

    public ClientUI() {
        setTitle("Hệ thống quản lý thư viện - Giao diện người dùng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 800));
        setPreferredSize(new Dimension(1400, 900));
        
        // Initialize resource managers
        initializeResourceManagers();
        
        // Initialize Dark Mode
        initializeDarkMode();
        
        // Initialize database tables
        initializeBorrowRequestsTable();
        
        // Create main interface
        createMainInterface();
        
        // Set default avatar initially
        setDefaultAvatar();
        
        // Connect to server
        connectToServer();
        
        // Setup keep-alive system to prevent timeout  
        try {
            ClientUIEnhancement enhancement = ClientUIEnhancement.getInstance();
            enhancement.initializeKeepAlive(this);
            
            // Setup additional periodic tasks and shutdown hooks
            setupPeriodicTasks();
            setupShutdownHook();
        } catch (Exception e) {
            System.err.println("Failed to initialize enhancement: " + e.getMessage());
        }
        
        // Setup window
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Initialize resource managers to prevent memory leaks and timeouts
     */
    private void initializeResourceManagers() {
        try {
            dbManager = DatabaseManager.getInstance();
            taskManager = BackgroundTaskManager.getInstance();
            System.out.println("Resource managers initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize resource managers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize Dark Mode support
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
     * Apply current theme to all components - Cải thiện hiển thị
     */
    private void applyCurrentTheme() {
        SwingUtilities.invokeLater(() -> {
            if (darkModeManager != null) {
                // Update main content background với gradient đẹp
                JPanel mainPanel = (JPanel) getContentPane();
                if (mainPanel != null) {
                    if (darkModeManager.isDarkMode()) {
                        // Dark Mode - nền tối hiện đại
                        mainPanel.setBackground(DarkModeManager.DarkTheme.PRIMARY_BG);
                    } else {
                        // Light Mode - nền sáng tinh tế
                        mainPanel.setBackground(DarkModeManager.LightTheme.SECONDARY_BG);
                    }
                    
                    // Apply theme to all components
                    darkModeManager.applyDarkMode(mainPanel);
                }
                
                // Update books grid panel với màu phù hợp
                if (booksGridPanel != null) {
                    if (darkModeManager.isDarkMode()) {
                        booksGridPanel.setBackground(DarkModeManager.DarkTheme.SECONDARY_BG);
                    } else {
                        booksGridPanel.setBackground(DarkModeManager.LightTheme.PRIMARY_BG);
                    }
                }
                
                // Update search button colors
                updateSearchButtonColors();
                
                // Update all component colors in search and filter sections
                updateComponentColors(getContentPane());
                
                // Update window title để hiển thị mode hiện tại
                String currentTitle = getTitle();
                if (!currentTitle.contains("Dark Mode") && !currentTitle.contains("Light Mode")) {
                    String mode = darkModeManager.isDarkMode() ? " (Dark Mode)" : " (Light Mode)";
                    setTitle(currentTitle + mode);
                } else {
                    // Replace existing mode indicator
                    String baseTitle = currentTitle.replaceAll(" \\((Dark|Light) Mode\\)", "");
                    String mode = darkModeManager.isDarkMode() ? " (Dark Mode)" : " (Light Mode)";
                    setTitle(baseTitle + mode);
                }
                
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
     * Setup periodic tasks to keep application alive
     */
    private void setupPeriodicTasks() {
        // Keep database connection alive with periodic ping
        refreshTimer = new Timer(300000, e -> { // 5 minutes
            if (dbManager != null) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        dbManager.executeWithConnection(conn -> {
                            // Simple query to keep connection alive
                            conn.createStatement().executeQuery("SELECT 1").close();
                            return null;
                        });
                    } catch (Exception ex) {
                        System.err.println("Keep-alive query failed: " + ex.getMessage());
                    }
                });
            }
        });
        refreshTimer.start();
        
        // Auto-refresh notification badge every 2 minutes
        Timer notificationTimer = new Timer(120000, e -> {
            if (userId != -1) {
                SwingUtilities.invokeLater(() -> {
                    if (btnNotification != null) {
                        updateNotificationBadge(btnNotification);
                    }
                });
            }
        });
        notificationTimer.start();
        
        System.out.println("Periodic tasks setup completed");
    }
    
    /**
     * Configure smooth scrolling for JScrollPane to reduce jerkiness
     */
    private void configureSmoothScrolling(JScrollPane scrollPane) {
        // Set smooth scroll increments
        scrollPane.getVerticalScrollBar().setUnitIncrement(8);    // Small increment for smooth scrolling
        scrollPane.getVerticalScrollBar().setBlockIncrement(24);  // Medium increment for page scrolling
        
        // Add custom mouse wheel listener for better control
        scrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    return; // Let default zoom behavior work
                }
                
                JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
                // Reduced sensitivity: từ 20 xuống 6 để giảm giật
                int scrollAmount = e.getUnitsToScroll() * 6;
                int currentValue = scrollBar.getValue();
                int newValue = Math.max(0, Math.min(
                    currentValue + scrollAmount, 
                    scrollBar.getMaximum() - scrollBar.getVisibleAmount()
                ));
                
                // Direct smooth scroll without complex animation
                scrollBar.setValue(newValue);
                e.consume(); // Prevent default scrolling
            }
        });
    }

    /**
     * Setup shutdown hook to cleanup resources
     */
    private void setupShutdownHook() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
                System.exit(0);
            }
        });
        
        // JVM shutdown hook as backup
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }
    
    /**
     * Cleanup all resources
     */
    private void cleanup() {
        try {
            System.out.println("Starting application cleanup...");
            
            // Cleanup dark mode listener
            if (darkModeManager != null) {
                darkModeManager.removeDarkModeListener(this);
            }
            
            // Stop timers
            if (refreshTimer != null && refreshTimer.isRunning()) {
                refreshTimer.stop();
            }
            
            // Stop keep-alive task
            if (keepAliveTask != null && !keepAliveTask.isCancelled()) {
                keepAliveTask.cancel(true);
            }
            
            // Close socket connection
            if (socket != null && !socket.isClosed()) {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    socket.close();
                } catch (Exception e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
            
            // Shutdown background task manager
            if (taskManager != null) {
                taskManager.shutdown();
            }
            
            // Shutdown database manager
            if (dbManager != null) {
                dbManager.shutdown();
            }
            
            System.out.println("Application cleanup completed");
            
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createMainInterface() {
        // Main panel with modern styling - sẽ được cập nhật bởi Dark Mode
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(248, 249, 250)); // Light mode mặc định
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // Top panel - Header with search and user info
        JPanel topPanel = createTopPanel();
        
        // Center panel - Books display
        JPanel centerPanel = createCenterPanel();
        
        // Bottom panel - Navigation buttons
        JPanel bottomPanel = createBottomPanel();
        
        // Add panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add event listeners
        addEventListeners();
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        
        // Modern search section
        JPanel searchSection = createModernSearchSection();
        
        // Modern filter section
        JPanel filterSection = createModernFilterSection();
        
        // User info section
        JPanel userSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        userSection.setOpaque(false);
        
        // Create a combined user profile panel
        JPanel userProfilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userProfilePanel.setOpaque(true);
        userProfilePanel.setBackground(Color.WHITE);
        userProfilePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        userProfilePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Avatar label
        lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(28, 28));
        lblAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAvatar.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1));
        lblAvatar.setOpaque(false);
        
        // Clickable user profile label
        lblUser = new JLabel("Chưa đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(new Color(0, 123, 255));
        lblUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUser.setOpaque(false);
        
        // Add components to user profile panel
        userProfilePanel.add(lblAvatar);
        userProfilePanel.add(lblUser);
        
        // Add click listener for the entire user profile panel
        userProfilePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showUserProfile();
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                userProfilePanel.setBackground(new Color(0, 123, 255, 20));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                userProfilePanel.setBackground(Color.WHITE);
            }
        });
        
        // Dark Mode toggle button
        JButton btnDarkMode = darkModeManager.createDarkModeToggleButton();
        
        // Notification button (replace logout button)
        btnNotification = new JButton("🔔 Thông báo");
        btnNotification.setPreferredSize(new Dimension(120, 40));
        btnNotification.setBackground(new Color(255, 140, 0));
        btnNotification.setForeground(Color.WHITE);
        btnNotification.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNotification.setFocusPainted(false);
        btnNotification.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnNotification.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotification.setToolTipText("Xem thông báo của bạn");
        btnNotification.addActionListener(e -> showNotifications());
        
        // Add hover effects for notification button
        btnNotification.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color currentBg = btnNotification.getBackground();
                if (currentBg.equals(new Color(255, 140, 0))) {
                    // Orange -> Darker orange
                    btnNotification.setBackground(new Color(230, 120, 0));
                } else if (currentBg.equals(new Color(231, 76, 60))) {
                    // Red -> Darker red
                    btnNotification.setBackground(new Color(200, 60, 45));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Restore original color based on notification count
                if (userId != -1) {
                    int unreadCount = NotificationUI.getUnreadNotificationCount(userId);
                    if (unreadCount > 0) {
                        btnNotification.setBackground(new Color(231, 76, 60)); // Red
                    } else {
                        btnNotification.setBackground(new Color(255, 140, 0)); // Orange
                    }
                }
            }
        });
        
        // Update notification badge periodically
        updateNotificationBadge(btnNotification);
        
        userSection.add(userProfilePanel);
        userSection.add(btnDarkMode);
        userSection.add(btnNotification);
        
        // Combine sections with better spacing
        JPanel searchFilterPanel = new JPanel(new BorderLayout(0, 15));
        searchFilterPanel.setOpaque(false);
        searchFilterPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        searchFilterPanel.add(searchSection, BorderLayout.NORTH);
        searchFilterPanel.add(filterSection, BorderLayout.CENTER);
        
        topPanel.add(searchFilterPanel, BorderLayout.CENTER);
        topPanel.add(userSection, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Books grid panel - save as instance variable
        booksGridPanel = new JPanel(new GridLayout(0, 6, 15, 15));
        booksGridPanel.setBackground(new Color(248, 249, 250));
        booksGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Scroll pane for books - with smooth scrolling enabled
        JScrollPane scrollPane = new JScrollPane(booksGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        // Configure smooth scrolling to reduce jerkiness
        configureSmoothScrolling(scrollPane);
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Pagination panel
        JPanel paginationPanel = createPaginationPanel();
        centerPanel.add(paginationPanel, BorderLayout.SOUTH);
        
        // Load books initially
        loadBooksGrid();
        
        return centerPanel;
    }
    
    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new BorderLayout());
        paginationPanel.setOpaque(false);
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        // Navigation buttons panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navPanel.setOpaque(false);
        
        // Create pagination buttons with modern style
        btnFirstPage = createPaginationButton("<<", "Trang đầu");
        btnPrevPage = createPaginationButton("<", "Trang trước");
        btnNextPage = createPaginationButton(">", "Trang sau");
        btnLastPage = createPaginationButton(">>", "Trang cuối");
        
        // Page info label
        lblPageInfo = new JLabel("Trang 1 / 1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPageInfo.setForeground(new Color(52, 73, 94));
        lblPageInfo.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        
        // Add event listeners for pagination buttons
        btnFirstPage.addActionListener(e -> navigateToPage(1));
        btnPrevPage.addActionListener(e -> navigateToPage(currentPage - 1));
        btnNextPage.addActionListener(e -> navigateToPage(currentPage + 1));
        btnLastPage.addActionListener(e -> navigateToPage(totalPages));
        
        // Add components to navigation panel
        navPanel.add(btnFirstPage);
        navPanel.add(btnPrevPage);
        navPanel.add(lblPageInfo);
        navPanel.add(btnNextPage);
        navPanel.add(btnLastPage);
        
        paginationPanel.add(navPanel, BorderLayout.CENTER);
        
        // Items info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setOpaque(false);
        
        JLabel lblItemsInfo = new JLabel();
        lblItemsInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblItemsInfo.setForeground(new Color(108, 117, 125));
        
        infoPanel.add(lblItemsInfo);
        paginationPanel.add(infoPanel, BorderLayout.EAST);
        
        return paginationPanel;
    }
    
    private JButton createPaginationButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(40, 32));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(41, 128, 185));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(52, 152, 219));
                }
            }
        });
        
        return button;
    }
    
    private void navigateToPage(int page) {
        if (page < 1 || page > totalPages) return;
        
        currentPage = page;
        loadBooksGrid();
        updatePaginationUI();
    }
    
    private void updatePaginationUI() {
        // Update page info
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        
        // Update button states
        btnFirstPage.setEnabled(currentPage > 1);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
        btnLastPage.setEnabled(currentPage < totalPages);
        
        // Update button colors based on enabled state
        updateButtonAppearance(btnFirstPage);
        updateButtonAppearance(btnPrevPage);
        updateButtonAppearance(btnNextPage);
        updateButtonAppearance(btnLastPage);
    }
    
    private void updateButtonAppearance(JButton button) {
        if (button.isEnabled()) {
            button.setBackground(new Color(52, 152, 219));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(176, 190, 197));
            button.setForeground(new Color(127, 140, 141));
        }
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        bottomPanel.setOpaque(false);
        
        // Create navigation buttons
        btnFavorite = createNavigationButton("Sách yêu thích", new Color(255, 140, 0));
        btnActivity = createNavigationButton("Hoạt động", new Color(40, 167, 69));
        btnBorrow = createNavigationButton("Đăng ký mượn sách", new Color(0, 123, 255));
        JButton btnRefresh = createNavigationButton("Làm mới", new Color(23, 162, 184));
        
        // Add refresh functionality
        btnRefresh.addActionListener(e -> refreshBookDisplay());
        
        JButton btnBorrowedBooks = createNavigationButton("Sách đã mượn", new Color(220, 53, 69));
        btnBorrowedBooks.addActionListener(e -> {
            if (userId == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem sách đã mượn!");
                return;
            }
            new BorrowListUI(userId).setVisible(true);
        });
        
        bottomPanel.add(btnFavorite);
        bottomPanel.add(btnActivity);
        bottomPanel.add(btnBorrow);
        bottomPanel.add(btnBorrowedBooks);
        bottomPanel.add(btnRefresh);
        
        return bottomPanel;
    }
    
    private JButton createNavigationButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(180, 50));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void addEventListeners() {
        // Search functionality
        btnSearch.addActionListener(e -> refreshBookDisplay());
        txtSearch.addActionListener(e -> refreshBookDisplay());
        txtAuthor.addActionListener(e -> refreshBookDisplay());
        txtPublisher.addActionListener(e -> refreshBookDisplay());
        cbCategory.addActionListener(e -> refreshBookDisplay());
        
        // Navigation buttons
        btnFavorite.addActionListener(e -> showFavoriteBooks());
        btnActivity.addActionListener(e -> showActivities());
        btnBorrow.addActionListener(e -> showBorrowRequestsDialog());
        
        // Search suggestions
        setupSearchSuggestions();
    }
    
    private void refreshBookDisplay() {
        // Reset to page 1 when refreshing/searching
        currentPage = 1;
        loadBooksGrid();
    }
    
    private void setupSearchSuggestions() {
        suggestPopup = new JPopupMenu();
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String keyword = txtSearch.getText().trim();
                suggestPopup.setVisible(false);
                
                // Skip if placeholder text or too short
                if (keyword.length() < 2 || "Nhập tên sách hoặc tác giả...".equals(keyword)) return;
                
                suggestPopup.removeAll();
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT DISTINCT title FROM books WHERE title LIKE ? LIMIT 8");
                    ps.setString(1, "%" + keyword + "%");
                    ResultSet rs = ps.executeQuery();
                    
                    boolean hasSuggestions = false;
                    while (rs.next()) {
                        String title = rs.getString("title");
                        JMenuItem item = new JMenuItem(title);
                        item.addActionListener(ev -> {
                            txtSearch.setText(title);
                            suggestPopup.setVisible(false);
                            refreshBookDisplay();
                        });
                        suggestPopup.add(item);
                        hasSuggestions = true;
                    }
                    
                    if (hasSuggestions) {
                        suggestPopup.show(txtSearch, 0, txtSearch.getHeight());
                    }
                } catch (Exception ex) {
                    // Ignore suggestion errors
                }
            }
        });
        
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Delay hiding to allow clicking on suggestions
                Timer timer = new Timer(200, ev -> suggestPopup.setVisible(false));
                timer.setRepeats(false);
                timer.start();
            }
        });
    }


    private void connectToServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            in.readLine(); // WELCOME message
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Không thể kết nối đến server: " + ex.getMessage(), 
                "Lỗi kết nối", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Đặt thông tin người dùng khi đăng nhập thành công
    public void setUserInfo(int id, String username) {
        this.userId = id;
        lblUser.setText("Xin chào, " + username);
        
        System.out.println("🚀 Setting user info - ID: " + id + ", Username: " + username);
        
        // Ensure avatar label is visible
        if (lblAvatar != null) {
            lblAvatar.setVisible(true);
            System.out.println("📍 Avatar label visibility: " + lblAvatar.isVisible());
        }
        
        // Load and display user avatar from database
        loadUserAvatarFromDB(id);
        
        // Cập nhật notification badge khi user đăng nhập
        if (btnNotification != null) {
            updateNotificationBadge(btnNotification);
        }
        
        // Hiển thị thông báo quan trọng khi đăng nhập
        showImportantNotificationsOnLogin();
        
        // Force repaint of user profile panel
        SwingUtilities.invokeLater(() -> {
            if (lblAvatar != null && lblAvatar.getParent() != null) {
                lblAvatar.getParent().repaint();
                System.out.println("🔄 Repainted user profile panel");
            }
        });
    }

    private void addToFavorite(String bookId) {
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để sử dụng tính năng này!");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            // Check if already in favorites
            String checkQuery = "SELECT id FROM favorites WHERE user_id = ? AND book_id = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkQuery);
            checkPs.setInt(1, userId);
            checkPs.setString(2, bookId);
            ResultSet rs = checkPs.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Sách này đã có trong danh sách yêu thích!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Add to favorites
            String insertQuery = "INSERT INTO favorites (user_id, book_id) VALUES (?, ?)";
            PreparedStatement insertPs = conn.prepareStatement(insertQuery);
            insertPs.setInt(1, userId);
            insertPs.setString(2, bookId);
            
            int rowsInserted = insertPs.executeUpdate();
            
            if (rowsInserted > 0) {
                // Record activity
                recordActivity(bookId, "add_favorite");
                
                JOptionPane.showMessageDialog(this, "Đã thêm vào danh sách yêu thích!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshBookDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể thêm vào yêu thích!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrowBook(String bookId, String title) {
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để đăng ký mượn sách!");
            return;
        }
        
        // Check if user has reached borrowing limit
        if (checkBorrowingLimit()) {
            JOptionPane.showMessageDialog(this, 
                "Bạn đã đạt giới hạn mượn sách (10 cuốn). Vui lòng trả sách trước khi mượn thêm!", 
                "Giới hạn mượn sách", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show detailed borrow request dialog - user will confirm there
        showBorrowRequestDialog(bookId, title);
    }
    
    private boolean checkBorrowingLimit() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            // Count current borrowed books that haven't been returned
            String countQuery = "SELECT COUNT(*) FROM borrows " +
                "WHERE user_id = ? AND (return_date IS NULL OR return_date = '' OR return_date = 'null')";
            PreparedStatement ps = conn.prepareStatement(countQuery);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int currentBorrowCount = rs.getInt(1);
                return currentBorrowCount >= 10; // Max 10 books
            }
        } catch (Exception e) {
            System.err.println("Error checking borrowing limit: " + e.getMessage());
        }
        return false;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }
    
    private void showBorrowRequestDialog(String bookId, String title) {
        JDialog dialog = new JDialog(this, "Đăng ký mượn sách", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("ĐĂNG KÝ MƯỢN SÁCH", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Book info
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Tên sách:"), gbc);
        gbc.gridx = 1;
        JLabel bookTitleLabel = new JLabel(title);
        bookTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookTitleLabel.setForeground(new Color(0, 123, 255));
        formPanel.add(bookTitleLabel, gbc);
        
        // Borrower info
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Người mượn:"), gbc);
        gbc.gridx = 1;
        JLabel borrowerLabel = new JLabel(lblUser.getText().replace("Xin chào, ", ""));
        borrowerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(borrowerLabel, gbc);
        
        // Borrow date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Ngày mượn:"), gbc);
        gbc.gridx = 1;
        JLabel borrowDateLabel = new JLabel(java.time.LocalDate.now().toString());
        borrowDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(borrowDateLabel, gbc);
        
        // Return date picker
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Ngày trả dự kiến:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> returnDateCombo = new JComboBox<>();
        
        // Add more reasonable borrowing periods (7, 14, 21, 30 days)
        int[] borrowDays = {7, 14, 21, 30};
        String[] labels = {"1 tuần", "2 tuần", "3 tuần", "1 tháng"};
        
        for (int i = 0; i < borrowDays.length; i++) {
            java.time.LocalDate returnDate = java.time.LocalDate.now().plusDays(borrowDays[i]);
            returnDateCombo.addItem(returnDate.toString() + " (" + labels[i] + ")");
        }
        
        // Set default to 14 days (2 tuần)
        returnDateCombo.setSelectedIndex(1);
        returnDateCombo.setPreferredSize(new Dimension(250, 35));
        returnDateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(returnDateCombo, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createStyledLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        JTextArea notesArea = new JTextArea(4, 20);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(300, 100));
        configureSmoothScrolling(notesScroll); // Apply smooth scrolling
        formPanel.add(notesScroll, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton submitBtn = new JButton("GỬI ĐĂNG KÝ");
        submitBtn.setPreferredSize(new Dimension(140, 40));
        submitBtn.setBackground(new Color(40, 167, 69));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setFocusPainted(false);
        submitBtn.addActionListener(e -> {
            String returnDate = returnDateCombo.getSelectedItem().toString().split(" ")[0];
            String notes = notesArea.getText().trim();
            submitBorrowRequest(bookId, returnDate, notes, dialog);
        });
        
        JButton cancelBtn = new JButton("HỦY");
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void submitBorrowRequest(String bookId, String returnDate, String notes, JDialog dialog) {
        Connection conn = null;
        PreparedStatement checkPs = null;
        PreparedStatement insertPs = null;
        ResultSet rs = null;
        
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000");
            
            // Check if user already has a pending request for this book
            String checkQuery = "SELECT id FROM borrow_requests WHERE user_id = ? AND book_id = ? AND status = 'PENDING'";
            checkPs = conn.prepareStatement(checkQuery);
            checkPs.setInt(1, userId);
            checkPs.setString(2, bookId);
            rs = checkPs.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Bạn đã có đăng ký mượn sách này đang chờ duyệt!", 
                    "Thông báo", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Close the first query resources
            if (rs != null) rs.close();
            if (checkPs != null) checkPs.close();
            
            // Create new borrow request with additional fields
            String insertQuery = "INSERT INTO borrow_requests (user_id, book_id, request_date, expected_return_date, notes, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
            insertPs = conn.prepareStatement(insertQuery);
            insertPs.setInt(1, userId);
            insertPs.setString(2, bookId);
            insertPs.setString(3, java.time.LocalDateTime.now().toString());
            insertPs.setString(4, returnDate);
            insertPs.setString(5, notes);
            
            int rowsInserted = insertPs.executeUpdate();
            
            if (rowsInserted > 0) {
                // Record activity
                recordActivity(bookId, "borrow_request");
                
                dialog.dispose();
                JOptionPane.showMessageDialog(this, 
                    "Đăng ký mượn sách thành công!\n" +
                    "Ngày trả dự kiến: " + returnDate + "\n" +
                    "Vui lòng chờ quản trị viên duyệt đăng ký.", 
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
                refreshBookDisplay();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Không thể tạo đăng ký mượn sách!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception dbEx) {
            JOptionPane.showMessageDialog(dialog, 
                "Lỗi database: " + dbEx.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close all resources properly
            try {
                if (rs != null) rs.close();
                if (checkPs != null) checkPs.close();
                if (insertPs != null) insertPs.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                System.err.println("Error closing database resources: " + ex.getMessage());
            }
        }
    }


    
    private void loadBooksGrid() {
        if (booksGridPanel == null) return;
        
        // Show loading dialog if available
        LoadingDialog loadingDialog = null;
        if (taskManager != null) {
            loadingDialog = LoadingDialog.show(this, "Đang tải danh sách sách");
        }
        
        final LoadingDialog finalLoadingDialog = loadingDialog;
        
        // Execute in background
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadBooksDirectly();
                return null;
            }
            
            @Override
            protected void done() {
                if (finalLoadingDialog != null) {
                    finalLoadingDialog.hideLoading();
                }
                
                // Update UI
                updatePaginationUI();
                booksGridPanel.revalidate();
                booksGridPanel.repaint();
            }
        };
        
        worker.execute();
    }
    
    /**
     * Load books directly (can be called from background thread)
     */
    private void loadBooksDirectly() {
        SwingUtilities.invokeLater(() -> booksGridPanel.removeAll());
        
        try {
            if (dbManager != null) {
                dbManager.executeWithConnection(this::loadBooksFromDatabase);
            } else {
                // Fallback to direct connection
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                    loadBooksFromDatabase(conn);
                }
            }
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi tải sách: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    /**
     * Load books from database connection
     */
    private void loadBooksFromDatabase(Connection conn) throws SQLException {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("ALTER TABLE books ADD COLUMN status TEXT DEFAULT 'approved'");
            // Update existing books without status to approved
            stmt.execute("UPDATE books SET status = 'approved' WHERE status IS NULL OR status = 'pending'");
        } catch (SQLException e) {
            // Column already exists, just update existing books to approved
            try {
                Statement updateStmt = conn.createStatement();
                updateStmt.execute("UPDATE books SET status = 'approved' WHERE status IS NULL OR status = 'pending'");
            } catch (SQLException ex) {
                // Ignore if update fails
            }
        }
        
        // Build base query for counting total items
        StringBuilder baseQuery = new StringBuilder("SELECT COUNT(*) FROM books WHERE 1=1");
        
        // Add search filters (handle placeholder text)
        String searchText = txtSearch.getText().trim();
        if ("Nhập tên sách hoặc tác giả...".equals(searchText)) searchText = "";
        
        String authorText = txtAuthor.getText().trim();
        if ("Nhập tên tác giả...".equals(authorText)) authorText = "";
        
        String publisherText = txtPublisher.getText().trim();
        if ("Nhập nhà xuất bản...".equals(publisherText)) publisherText = "";
        
        String categoryText = cbCategory.getSelectedItem().toString();
        
        if (!searchText.isEmpty()) {
            baseQuery.append(" AND (title LIKE ? OR author LIKE ? OR publisher LIKE ?)");
        }
        if (!authorText.isEmpty()) {
            baseQuery.append(" AND author LIKE ?");
        }
        if (!publisherText.isEmpty()) {
            baseQuery.append(" AND publisher LIKE ?");
        }
        if (!"Tất cả".equals(categoryText)) {
            baseQuery.append(" AND category = ?");
        }
        
        // First, count total items
        PreparedStatement countPs = conn.prepareStatement(baseQuery.toString());
        int paramIndex = 1;
        
        if (!searchText.isEmpty()) {
            String searchPattern = "%" + searchText + "%";
            countPs.setString(paramIndex++, searchPattern);
            countPs.setString(paramIndex++, searchPattern);
            countPs.setString(paramIndex++, searchPattern);
        }
        if (!authorText.isEmpty()) {
            countPs.setString(paramIndex++, "%" + authorText + "%");
        }
        if (!publisherText.isEmpty()) {
            countPs.setString(paramIndex++, "%" + publisherText + "%");
        }
        if (!"Tất cả".equals(categoryText)) {
            countPs.setString(paramIndex++, categoryText);
        }
        
        ResultSet countRs = countPs.executeQuery();
        totalItems = countRs.getInt(1);
        totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        countRs.close();
        countPs.close();
        
        // Ensure current page is valid
        if (currentPage > totalPages && totalPages > 0) {
            currentPage = totalPages;
        } else if (currentPage < 1) {
            currentPage = 1;
        }
        
        // Build query for actual data with pagination
        StringBuilder dataQuery = new StringBuilder("SELECT id, title, author, publisher, year, category, quantity, cover_image, status FROM books WHERE 1=1");
        
        if (!searchText.isEmpty()) {
            dataQuery.append(" AND (title LIKE ? OR author LIKE ? OR publisher LIKE ?)");
        }
        if (!authorText.isEmpty()) {
            dataQuery.append(" AND author LIKE ?");
        }
        if (!publisherText.isEmpty()) {
            dataQuery.append(" AND publisher LIKE ?");
        }
        if (!"Tất cả".equals(categoryText)) {
            dataQuery.append(" AND category = ?");
        }
        
        // Add pagination
        dataQuery.append(" ORDER BY id LIMIT ? OFFSET ?");
        
        PreparedStatement dataPs = conn.prepareStatement(dataQuery.toString());
        paramIndex = 1;
        
        if (!searchText.isEmpty()) {
            String searchPattern = "%" + searchText + "%";
            dataPs.setString(paramIndex++, searchPattern);
            dataPs.setString(paramIndex++, searchPattern);
            dataPs.setString(paramIndex++, searchPattern);
        }
        if (!authorText.isEmpty()) {
            dataPs.setString(paramIndex++, "%" + authorText + "%");
        }
        if (!publisherText.isEmpty()) {
            dataPs.setString(paramIndex++, "%" + publisherText + "%");
        }
        if (!"Tất cả".equals(categoryText)) {
            dataPs.setString(paramIndex++, categoryText);
        }
        
        // Set pagination parameters
        dataPs.setInt(paramIndex++, itemsPerPage);
        dataPs.setInt(paramIndex++, (currentPage - 1) * itemsPerPage);
        
        ResultSet rs = dataPs.executeQuery();
        
        while (rs.next()) {
            String bookId = rs.getString("id");
            String title = rs.getString("title");
            String author = rs.getString("author");
            String category = rs.getString("category");
            int quantity = rs.getInt("quantity");
            String coverImage = rs.getString("cover_image");
            String status = rs.getString("status");
            
            // Create panel for all books - no status restriction for display
            JPanel bookPanel = createBookPanelWithStatus(bookId, title, author, category, quantity, coverImage, status);
            SwingUtilities.invokeLater(() -> booksGridPanel.add(bookPanel));
        }
        
        rs.close();
        dataPs.close();
        
        // Update pagination UI on EDT
        SwingUtilities.invokeLater(() -> {
            updatePaginationUI();
            booksGridPanel.revalidate();
            booksGridPanel.repaint();
        });
    }

    private JPanel createBookPanelWithStatus(String bookId, String title, String author, String category, int quantity, String coverImage, String status) {
        JPanel bookPanel = new JPanel();
        bookPanel.setLayout(new BoxLayout(bookPanel, BoxLayout.Y_AXIS));
        bookPanel.setBackground(Color.WHITE);
        bookPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        bookPanel.setPreferredSize(new Dimension(200, 300));
        
        // Book image panel - compact design with no distortion
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(160, 140));
        imagePanel.setBackground(new Color(248, 249, 250));
        imagePanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        JLabel imageLabel;
        if (coverImage != null && !coverImage.trim().isEmpty()) {
            try {
                // Load image from URL with better error handling
                java.net.URL imageUrl = new java.net.URL(coverImage);
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                
                // Wait for image to load completely
                if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                    // Calculate dimensions to maintain aspect ratio without distortion
                    int maxWidth = 150;
                    int maxHeight = 130;
                    int originalWidth = originalIcon.getIconWidth();
                    int originalHeight = originalIcon.getIconHeight();
                    
                    // Calculate scale factor to fit within bounds without distortion
                    double scaleWidth = (double) maxWidth / originalWidth;
                    double scaleHeight = (double) maxHeight / originalHeight;
                    double scale = Math.min(scaleWidth, scaleHeight);
                    
                    int scaledWidth = (int) (originalWidth * scale);
                    int scaledHeight = (int) (originalHeight * scale);
                    
                    Image scaledImage = originalIcon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    imageLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                    imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                } else {
                    // If image loading fails, show category name
                    String bookIcon = getBookIcon(category);
                    imageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
                    imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    imageLabel.setForeground(getCategoryColor(category));
                }
            } catch (Exception e) {
                // If image loading fails, show category name
                String bookIcon = getBookIcon(category);
                imageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
                imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                imageLabel.setForeground(getCategoryColor(category));
            }
        } else {
            // Default category name with category-based styling
            String bookIcon = getBookIcon(category);
            imageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
            imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            imageLabel.setForeground(getCategoryColor(category));
        }
        
        // Add click listener to image to show book details
        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showBookDetails(bookId, title, author, category, quantity, coverImage);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                imagePanel.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                imagePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            }
        });
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        
        // Book information
        JLabel titleLabel = new JLabel("<html><div style='width:180px;text-align:center'><b>" + title + "</b></div></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(33, 37, 41));
        
        JLabel authorLabel = new JLabel("<html><div style='width:180px;text-align:center'>" + author + "</div></html>");
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        authorLabel.setForeground(new Color(108, 117, 125));
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel categoryLabel = new JLabel("<html><div style='width:180px;text-align:center'><i>" + category + "</i></div></html>");
        categoryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        categoryLabel.setForeground(new Color(134, 142, 150));
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel quantityLabel = new JLabel("Còn lại: " + quantity + " cuốn");
        quantityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        quantityLabel.setForeground(quantity > 0 ? new Color(40, 167, 69) : new Color(220, 53, 69));
        quantityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // No status label needed - all books are available for interaction
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setOpaque(false);
        
        // Favorite button
        JButton favoriteBtn = new JButton("Yêu thích");
        favoriteBtn.setBackground(new Color(255, 140, 0));
        favoriteBtn.setForeground(Color.WHITE);
        favoriteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        favoriteBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        favoriteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        favoriteBtn.setFocusPainted(false);
        favoriteBtn.setToolTipText("Thêm vào yêu thích");
        favoriteBtn.addActionListener(e -> addToFavorite(bookId));
        
        // Borrow button
        JButton borrowBtn = new JButton("+");
        borrowBtn.setBackground(new Color(0, 123, 255));
        borrowBtn.setForeground(Color.WHITE);
        borrowBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        borrowBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        borrowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        borrowBtn.setFocusPainted(false);
        borrowBtn.setToolTipText("Đăng ký mượn sách");
        borrowBtn.setEnabled(quantity > 0);
        borrowBtn.addActionListener(e -> borrowBook(bookId, title));
        
        // Add hover effects
        favoriteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                favoriteBtn.setBackground(new Color(255, 120, 0));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                favoriteBtn.setBackground(new Color(255, 140, 0));
            }
        });
        
        borrowBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (borrowBtn.isEnabled()) {
                    borrowBtn.setBackground(new Color(0, 103, 235));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (borrowBtn.isEnabled()) {
                    borrowBtn.setBackground(new Color(0, 123, 255));
                }
            }
        });
        
        buttonPanel.add(favoriteBtn);
        buttonPanel.add(borrowBtn);
        
        // Assemble panel
        bookPanel.add(imagePanel);
        bookPanel.add(Box.createVerticalStrut(10));
        bookPanel.add(titleLabel);
        bookPanel.add(Box.createVerticalStrut(5));
        bookPanel.add(authorLabel);
        bookPanel.add(Box.createVerticalStrut(3));
        bookPanel.add(categoryLabel);
        bookPanel.add(Box.createVerticalStrut(5));
        bookPanel.add(quantityLabel);
        bookPanel.add(Box.createVerticalStrut(10));
        bookPanel.add(buttonPanel);
        
        return bookPanel;
    }

    private void showFavoriteBooks() {
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem sách yêu thích!");
            return;
        }
        
        try {
            if (socket == null || socket.isClosed() || out == null || in == null) {
                connectToServer();
            }
            
            out.println("LIST_FAVORITES|" + userId);
            String resp = in.readLine();
            
            if (resp != null && resp.startsWith("FAVORITES_LIST|")) {
                String data = resp.substring("FAVORITES_LIST|".length());
                if (!data.trim().isEmpty()) {
                    showFavoriteBooksDialog(data);
                } else {
                    JOptionPane.showMessageDialog(this, "Bạn chưa có sách yêu thích nào!", "Sách yêu thích", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể tải danh sách sách yêu thích!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showFavoriteBooksDialog(String data) {
        JDialog dialog = new JDialog(this, "Danh sách sách yêu thích", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(255, 248, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("SÁCH YÊU THÍCH CỦA BẠN", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(255, 140, 0));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Create table model
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Tên sách", "Tác giả", "Yêu thích", "Thao tác"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Parse data and add to table
        String[] books = data.split(";");
        System.out.println("DEBUG: Favorites data: " + data); // Debug line
        
        for (String book : books) {
            if (!book.trim().isEmpty()) {
                String[] parts = book.split(" - ");
                System.out.println("DEBUG: Book parts: " + java.util.Arrays.toString(parts)); // Debug line
                
                if (parts.length >= 3) { // ID - Title - Author
                    model.addRow(new Object[]{parts[0], parts[1], parts[2], "Yêu thích", "Xóa"});
                }
            }
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));
        table.getTableHeader().setBackground(new Color(255, 140, 0));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionBackground(new Color(255, 140, 0, 50));
        
        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        // Add double-click listener for delete action
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int column = table.columnAtPoint(e.getPoint());
                        if (column == 4) { // Delete column
                            deleteFavoriteBook(table, model, row, dialog);
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 140, 0), 2));
        configureSmoothScrolling(scrollPane); // Apply smooth scrolling
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        // Delete selected button
        JButton deleteBtn = new JButton("🗑️ Xóa đã chọn");
        deleteBtn.setPreferredSize(new Dimension(140, 35));
        deleteBtn.setBackground(new Color(220, 53, 69));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                deleteFavoriteBook(table, model, selectedRow, dialog);
            } else {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn một sách để xóa!");
            }
        });
        
        // Delete all button
        JButton deleteAllBtn = new JButton("🗑️ Xóa tất cả");
        deleteAllBtn.setPreferredSize(new Dimension(120, 35));
        deleteAllBtn.setBackground(new Color(255, 193, 7));
        deleteAllBtn.setForeground(Color.WHITE);
        deleteAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteAllBtn.setFocusPainted(false);
        deleteAllBtn.addActionListener(e -> deleteAllFavorites(dialog));
        
        // Close button
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.setBackground(new Color(108, 117, 125));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(deleteBtn);
        buttonPanel.add(deleteAllBtn);
        buttonPanel.add(closeBtn);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void deleteFavoriteBook(JTable table, DefaultTableModel model, int row, JDialog dialog) {
        String bookId = model.getValueAt(row, 0).toString();
        String bookTitle = model.getValueAt(row, 1).toString();
        
        int choice = JOptionPane.showConfirmDialog(dialog,
            "Bạn có chắc muốn xóa sách '" + bookTitle + "' khỏi danh sách yêu thích?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                String deleteQuery = "DELETE FROM favorites WHERE user_id = ? AND book_id = ?";
                PreparedStatement ps = conn.prepareStatement(deleteQuery);
                ps.setInt(1, userId);
                ps.setString(2, bookId);
                
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(dialog, "Đã xóa sách khỏi danh sách yêu thích!");
                    
                    // Record activity
                    recordActivity(bookId, "remove_favorite");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Không thể xóa sách!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog, "Lỗi xóa: " + e.getMessage());
            }
        }
    }
    
    private void deleteAllFavorites(JDialog dialog) {
        int choice = JOptionPane.showConfirmDialog(dialog,
            "Bạn có chắc muốn xóa TẤT CẢ sách khỏi danh sách yêu thích?",
            "Xác nhận xóa tất cả",
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                String deleteQuery = "DELETE FROM favorites WHERE user_id = ?";
                PreparedStatement ps = conn.prepareStatement(deleteQuery);
                ps.setInt(1, userId);
                
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    JOptionPane.showMessageDialog(dialog, "Đã xóa tất cả sách khỏi danh sách yêu thích!");
                    dialog.dispose(); // Close dialog
                    showFavoriteBooks(); // Refresh list
                } else {
                    JOptionPane.showMessageDialog(dialog, "Không có sách nào để xóa!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog, "Lỗi xóa: " + e.getMessage());
            }
        }
    }
    
    private void showActivities() {
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem hoạt động!");
            return;
        }
        
        try {
            if (socket == null || socket.isClosed() || out == null || in == null) {
                connectToServer();
            }
            
            out.println("LIST_ACTIVITIES|" + userId);
            String resp = in.readLine();
            
            if (resp != null && resp.startsWith("ACTIVITIES_LIST|")) {
                String data = resp.substring("ACTIVITIES_LIST|".length());
                if (!data.trim().isEmpty()) {
                    showActivitiesDialog(data);
                } else {
                    JOptionPane.showMessageDialog(this, "Chưa có hoạt động nào!", "Lịch sử hoạt động", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể tải lịch sử hoạt động!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showActivitiesDialog(String data) {
        JDialog dialog = new JDialog(this, "Lịch sử hoạt động", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("LỊCH SỬ HOẠT ĐỘNG CỦA BẠN", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(40, 167, 69));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Create table model for activities
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Sách", "Hoạt động", "Thời gian", "Thao tác"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Parse activities and add to table
        String[] activities = data.split(";");
        System.out.println("DEBUG: Activities data: " + data); // Debug line
        
        for (String activity : activities) {
            if (!activity.trim().isEmpty()) {
                String[] parts = activity.split(" - ");
                System.out.println("DEBUG: Activity parts: " + java.util.Arrays.toString(parts)); // Debug line
                
                if (parts.length >= 4) { // ID - Book - Action - Time
                    String actionText = getActionDisplayText(parts[2]);
                    model.addRow(new Object[]{parts[0], parts[1], actionText, parts[3], "Xóa"});
                }
            }
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));
        table.getTableHeader().setBackground(new Color(40, 167, 69));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionBackground(new Color(40, 167, 69, 50));
        
        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        // Add double-click listener for delete action
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int column = table.columnAtPoint(e.getPoint());
                        if (column == 4) { // Delete column
                            deleteActivity(table, model, row, dialog);
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(40, 167, 69), 2));
        configureSmoothScrolling(scrollPane); // Apply smooth scrolling
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        // Delete selected activity button
        JButton deleteBtn = new JButton("🗑️ Xóa đã chọn");
        deleteBtn.setPreferredSize(new Dimension(140, 40));
        deleteBtn.setBackground(new Color(220, 53, 69));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setFocusPainted(false);
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                deleteActivity(table, model, selectedRow, dialog);
            } else {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn một hoạt động để xóa!");
            }
        });
        
        // Clear all activities button
        JButton clearAllBtn = new JButton("🗑️ Xóa tất cả");
        clearAllBtn.setPreferredSize(new Dimension(120, 40));
        clearAllBtn.setBackground(new Color(255, 193, 7));
        clearAllBtn.setForeground(Color.WHITE);
        clearAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearAllBtn.setFocusPainted(false);
        clearAllBtn.addActionListener(e -> clearAllActivities(dialog));
        
        // Close button
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.setBackground(new Color(108, 117, 125));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearAllBtn);
        buttonPanel.add(closeBtn);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private String getActionDisplayText(String action) {
        switch (action.toLowerCase()) {
            case "favorite":
            case "add_favorite":
                return "Thêm yêu thích";
            case "remove_favorite":
                return "💔 Bỏ yêu thích";
            case "borrow":
            case "borrow_request":
                return "Đăng ký mượn";
            case "return":
                return "Trả sách";
            case "view":
                return "👀 Xem chi tiết";
            default:
                return action;
        }
    }
    
    private void deleteActivity(JTable table, DefaultTableModel model, int row, JDialog dialog) {
        String activityId = model.getValueAt(row, 0).toString();
        String bookTitle = model.getValueAt(row, 1).toString();
        
        int choice = JOptionPane.showConfirmDialog(dialog,
            "Bạn có chắc muốn xóa hoạt động với sách '" + bookTitle + "'?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                String deleteQuery = "DELETE FROM activities WHERE id = ? AND user_id = ?";
                PreparedStatement ps = conn.prepareStatement(deleteQuery);
                ps.setString(1, activityId);
                ps.setInt(2, userId);
                
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(dialog, "Đã xóa hoạt động!");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Không thể xóa hoạt động!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog, "Lỗi xóa: " + e.getMessage());
            }
        }
    }
    
    private void clearAllActivities(JDialog dialog) {
        int choice = JOptionPane.showConfirmDialog(dialog,
            "Bạn có chắc muốn xóa TẤT CẢ lịch sử hoạt động?",
            "Xác nhận xóa tất cả",
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                String deleteQuery = "DELETE FROM activities WHERE user_id = ?";
                PreparedStatement ps = conn.prepareStatement(deleteQuery);
                ps.setInt(1, userId);
                
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    JOptionPane.showMessageDialog(dialog, "Đã xóa tất cả lịch sử hoạt động!");
                    dialog.dispose(); // Close dialog
                    showActivities(); // Refresh list
                } else {
                    JOptionPane.showMessageDialog(dialog, "Không có hoạt động nào để xóa!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog, "Lỗi xóa: " + e.getMessage());
            }
        }
    }
    
    private void recordActivity(String bookId, String action) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            String insertQuery = "INSERT INTO activities (user_id, book_id, action, action_time) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertQuery);
            ps.setInt(1, userId);
            ps.setString(2, bookId);
            ps.setString(3, action);
            ps.setString(4, java.time.LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error recording activity: " + e.getMessage());
        }
    }


    private void showBorrowRequestsDialog() {
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem đăng ký mượn sách!");
            return;
        }
        
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            String query = "SELECT br.id, b.title, b.author, br.request_date, br.status, br.admin_notes " +
                "FROM borrow_requests br " +
                "JOIN books b ON br.book_id = b.id " +
                "WHERE br.user_id = ? " +
                "ORDER BY br.request_date DESC";
            
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            java.sql.ResultSet rs = ps.executeQuery();
            
            // Create dialog
            JDialog dialog = new JDialog(this, "Đăng ký mượn sách của bạn", true);
            dialog.setSize(900, 600);
            dialog.setLocationRelativeTo(this);
            
            // Main panel
            JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    
                    Color startColor = new Color(240, 248, 255);
                    Color endColor = new Color(230, 240, 250);
                    GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Header
            JLabel headerLabel = new JLabel("Danh sách đăng ký mượn sách", SwingConstants.CENTER);
            headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            headerLabel.setForeground(new Color(0, 123, 255));
            headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            
            // Table
            String[] columnNames = {"ID", "Tên sách", "Tác giả", "Ngày đăng ký", "Trạng thái", "Ghi chú"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            while (rs.next()) {
                String status = rs.getString("status");
                String statusDisplay;
                switch (status) {
                    case "PENDING":
                        statusDisplay = "Chờ duyệt";
                        break;
                    case "APPROVED":
                        statusDisplay = "Đã duyệt";
                        break;
                    case "REJECTED":
                        statusDisplay = "Đã từ chối";
                        break;
                    default:
                        statusDisplay = status;
                        break;
                }
                
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("request_date"),
                    statusDisplay,
                    rs.getString("admin_notes") != null ? rs.getString("admin_notes") : ""
                };
                model.addRow(row);
            }
            
            JTable table = new JTable(model);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.setRowHeight(45);
            table.setSelectionBackground(new Color(0, 123, 255, 50));
            table.setSelectionForeground(new Color(0, 123, 255));
            table.setGridColor(new Color(222, 226, 230));
            table.setShowGrid(true);
            
            // Table header styling
            JTableHeader header = table.getTableHeader();
            header.setBackground(new Color(0, 123, 255));
            header.setForeground(Color.WHITE);
            header.setFont(new Font("Segoe UI", Font.BOLD, 14));
            header.setPreferredSize(new Dimension(0, 45));
            
            // Column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(1).setPreferredWidth(200);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
            table.getColumnModel().getColumn(3).setPreferredWidth(130);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
            table.getColumnModel().getColumn(5).setPreferredWidth(200);
            
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2));
            configureSmoothScrolling(scrollPane); // Apply smooth scrolling
            
            // Info panel
            JPanel infoPanel = new JPanel(new GridLayout(1, 3, 15, 0));
            infoPanel.setOpaque(false);
            
            int totalRequests = model.getRowCount();
            int pendingCount = 0, rejectedCount = 0;
            
            for (int i = 0; i < totalRequests; i++) {
                String status = model.getValueAt(i, 4).toString();
                if (status.contains("Chờ duyệt")) pendingCount++;
                else if (status.contains("Đã từ chối")) rejectedCount++;
            }
            
            JPanel totalPanel = createRequestInfoCard("Tổng số", String.valueOf(totalRequests), new Color(0, 123, 255));
            JPanel pendingPanel = createRequestInfoCard("Chờ duyệt", String.valueOf(pendingCount), new Color(255, 193, 7));
            JPanel rejectedPanel = createRequestInfoCard("Từ chối", String.valueOf(rejectedCount), new Color(220, 53, 69));
            
            infoPanel.add(totalPanel);
            infoPanel.add(pendingPanel);
            infoPanel.add(rejectedPanel);
            
            // Close button
            JButton closeBtn = new JButton("Đóng");
            closeBtn.setPreferredSize(new Dimension(100, 40));
            closeBtn.setBackground(new Color(0, 123, 255));
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            closeBtn.setFocusPainted(false);
            closeBtn.addActionListener(e -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);
            buttonPanel.add(closeBtn);
            
            // Layout assembly
            JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
            centerPanel.setOpaque(false);
            centerPanel.add(infoPanel, BorderLayout.NORTH);
            centerPanel.add(scrollPane, BorderLayout.CENTER);
            
            mainPanel.add(headerLabel, BorderLayout.NORTH);
            mainPanel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(mainPanel);
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải đăng ký: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createRequestInfoCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                Color startColor = Color.WHITE;
                Color endColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 20);
                GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(color);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(new Color(33, 37, 41));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private void showBookDetails(String bookId, String title, String author, String category, int quantity, String coverImage) {
        // Fetch additional book details from database
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM books WHERE id = ?");
            ps.setString(1, bookId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String publisher = rs.getString("publisher");
                int year = rs.getInt("year");
                String description = rs.getString("description");
                
                showBookDetailsDialog(bookId, title, author, publisher, year, category, quantity, coverImage, description);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải thông tin sách: " + ex.getMessage());
        }
    }
    
    private void showBookDetailsDialog(String bookId, String title, String author, String publisher, 
                                     int year, String category, int quantity, String coverImage, String description) {
        JDialog dialog = new JDialog(this, "Thông tin chi tiết sách", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                Color startColor = new Color(248, 249, 250);
                Color endColor = new Color(233, 236, 239);
                GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // Header
        JLabel headerLabel = new JLabel("Thông tin chi tiết", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);
        
        // Left panel - Book image (compact with no distortion)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(230, 320));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JLabel bookImageLabel;
        if (coverImage != null && !coverImage.trim().isEmpty()) {
            try {
                java.net.URL imageUrl = new java.net.URL(coverImage);
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                
                if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                    // Calculate dimensions to maintain aspect ratio without distortion
                    int maxWidth = 214;  // 230 - 16 (8px padding each side)
                    int maxHeight = 304; // 320 - 16 (8px padding each side)
                    int originalWidth = originalIcon.getIconWidth();
                    int originalHeight = originalIcon.getIconHeight();
                    
                    // Calculate scale factor to fit within bounds without distortion
                    double scaleWidth = (double) maxWidth / originalWidth;
                    double scaleHeight = (double) maxHeight / originalHeight;
                    double scale = Math.min(scaleWidth, scaleHeight);
                    
                    int scaledWidth = (int) (originalWidth * scale);
                    int scaledHeight = (int) (originalHeight * scale);
                    
                    Image scaledImage = originalIcon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    bookImageLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                } else {
                    String bookIcon = getBookIcon(category);
                    bookImageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
                    bookImageLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    bookImageLabel.setForeground(getCategoryColor(category));
                }
            } catch (Exception e) {
                String bookIcon = getBookIcon(category);
                bookImageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
                bookImageLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                bookImageLabel.setForeground(getCategoryColor(category));
            }
        } else {
            String bookIcon = getBookIcon(category);
            bookImageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
            bookImageLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            bookImageLabel.setForeground(getCategoryColor(category));
        }
        imagePanel.add(bookImageLabel, BorderLayout.CENTER);
        
        // Right panel - Book information
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("<html><div style='width:400px'><b style='font-size:18px'>" + title + "</b></div></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 37, 41));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Author
        JPanel authorPanel = createInfoRow("Tác giả:", author);
        
        // Publisher
        JPanel publisherPanel = createInfoRow("🏢 Nhà xuất bản:", publisher);
        
        // Year
        JPanel yearPanel = createInfoRow("Năm xuất bản:", String.valueOf(year));
        
        // Category
        JPanel categoryPanel = createInfoRow("Thể loại:", category);
        
        // Quantity
        JPanel quantityPanel = createInfoRow("Số lượng:", quantity + " cuốn");
        
        // Status
        String statusText = quantity > 0 ? "Có sẵn" : "Hết sách";
        Color statusColor = quantity > 0 ? new Color(40, 167, 69) : new Color(220, 53, 69);
        JPanel statusPanel = createInfoRow("Trạng thái:", statusText, statusColor);
        
        // Description
        JLabel descLabel = new JLabel("Mô tả:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descLabel.setForeground(new Color(52, 58, 64));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String descText = (description != null && !description.trim().isEmpty()) ? description : "Chưa có mô tả cho sách này.";
        JTextArea descArea = new JTextArea(descText);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setForeground(new Color(73, 80, 87));
        descArea.setBackground(Color.WHITE);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane descScrollPane = new JScrollPane(descArea);
        descScrollPane.setPreferredSize(new Dimension(400, 100));
        descScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        configureSmoothScrolling(descScrollPane); // Apply smooth scrolling
        
        // Add components to info panel
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(authorPanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(publisherPanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(yearPanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(categoryPanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(quantityPanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(statusPanel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(descScrollPane);
        
        contentPanel.add(imagePanel, BorderLayout.WEST);
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        // Favorite button
        JButton favoriteBtn = new JButton("Thêm yêu thích");
        favoriteBtn.setPreferredSize(new Dimension(150, 40));
        favoriteBtn.setBackground(new Color(255, 140, 0));
        favoriteBtn.setForeground(Color.WHITE);
        favoriteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        favoriteBtn.setFocusPainted(false);
        favoriteBtn.addActionListener(e -> {
            addToFavorite(bookId);
            dialog.dispose();
        });
        
        // Borrow button
        JButton borrowBtn = new JButton("� Đăng ký mượn");
        borrowBtn.setPreferredSize(new Dimension(140, 40));
        borrowBtn.setBackground(new Color(0, 123, 255));
        borrowBtn.setForeground(Color.WHITE);
        borrowBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        borrowBtn.setFocusPainted(false);
        borrowBtn.setEnabled(quantity > 0);
        borrowBtn.addActionListener(e -> {
            borrowBook(bookId, title);
            dialog.dispose();
        });
        
        // Close button
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.setBackground(new Color(108, 117, 125));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(favoriteBtn);
        buttonPanel.add(borrowBtn);
        buttonPanel.add(closeBtn);
        
        // Assembly
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createInfoRow(String label, String value) {
        return createInfoRow(label, value, new Color(73, 80, 87));
    }
    
    private JPanel createInfoRow(String label, String value, Color valueColor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComponent.setForeground(new Color(52, 58, 64));
        labelComponent.setPreferredSize(new Dimension(140, 25));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueComponent.setForeground(valueColor);
        
        panel.add(labelComponent);
        panel.add(valueComponent);
        
        return panel;
    }

    private String getBookIcon(String category) {
        switch (category) {
            case "Văn học – Tiểu thuyết": return "Văn học";
            case "Khoa học – Công nghệ": return "Khoa học";
            case "Kinh tế – Quản trị": return "Kinh tế";
            case "Tâm lý – Kỹ năng sống": return "Tâm lý";
            case "Giáo trình – Học thuật": return "Giáo trình";
            case "Trẻ em – Thiếu nhi": return "Trẻ em";
            case "Lịch sử – Địa lý": return "Lịch sử";
            case "Tôn giáo – Triết học": return "Tôn giáo";
            case "Ngoại ngữ – Từ điển": return "Ngoại ngữ";
            case "Nghệ thuật – Âm nhạc": return "Nghệ thuật";
            default: return "Sách";
        }
    }

    private Color getCategoryColor(String category) {
        switch (category) {
            case "Văn học – Tiểu thuyết": return new Color(139, 69, 19);
            case "Khoa học – Công nghệ": return new Color(0, 100, 200);
            case "Kinh tế – Quản trị": return new Color(255, 140, 0);
            case "Tâm lý – Kỹ năng sống": return new Color(255, 20, 147);
            case "Giáo trình – Học thuật": return new Color(34, 139, 34);
            case "Trẻ em – Thiếu nhi": return new Color(255, 105, 180);
            case "Lịch sử – Địa lý": return new Color(128, 128, 0);
            case "Tôn giáo – Triết học": return new Color(75, 0, 130);
            case "Ngoại ngữ – Từ điển": return new Color(255, 0, 255);
            case "Nghệ thuật – Âm nhạc": return new Color(255, 69, 0);
            default: return new Color(150, 150, 150);
        }
    }
    
    private void initializeBorrowRequestsTable() {
        Connection conn = null;
        java.sql.Statement stmt = null;
        try {
            // Enable WAL mode and set timeout
            conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000");
            
            stmt = conn.createStatement();
            
            // Enable WAL mode for better concurrent access
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA synchronous=NORMAL");
            stmt.execute("PRAGMA busy_timeout=30000");
            
            String createBorrowRequestsTable = "CREATE TABLE IF NOT EXISTS borrow_requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "book_id INTEGER NOT NULL, " +
                "request_date TEXT NOT NULL, " +
                "status TEXT DEFAULT 'PENDING', " +
                "admin_notes TEXT, " +
                "approved_date TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (book_id) REFERENCES books(id)" +
                ")";
            
            stmt.execute(createBorrowRequestsTable);
            
        } catch (Exception e) {
            System.err.println("Error initializing borrow_requests table: " + e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    System.err.println("Error closing statement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    // Static reference to keep track of open UserProfileUI
    private static UserProfileUI openUserProfileUI = null;
    
    private void showUserProfile() {
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem thông tin cá nhân!");
            return;
        }
        
        // Check if UserProfileUI is already open
        if (openUserProfileUI != null && openUserProfileUI.isDisplayable()) {
            // Bring existing window to front
            openUserProfileUI.toFront();
            openUserProfileUI.requestFocus();
            return;
        }
        
        String currentUsername = lblUser.getText().replace("Xin chào, ", "");
        openUserProfileUI = new UserProfileUI(userId, currentUsername);
        
        // Add window listener to clear reference when window is closed
        openUserProfileUI.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                openUserProfileUI = null;
            }
        });
        
        openUserProfileUI.setVisible(true);
    }
    
    private void showNotifications() {
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem thông báo!");
            return;
        }
        
        new NotificationUI(userId).setVisible(true);
    }
    
    private void updateNotificationBadge(JButton btnNotification) {
        // Update notification count in background
        SwingUtilities.invokeLater(() -> {
            if (userId != -1) {
                int unreadCount = NotificationUI.getUnreadNotificationCount(userId);
                if (unreadCount > 0) {
                    btnNotification.setText("🔔 Thông báo (" + unreadCount + ")");
                    btnNotification.setPreferredSize(new Dimension(150, 40));
                    btnNotification.setBackground(new Color(231, 76, 60)); // Red for unread notifications
                    btnNotification.setToolTipText("Bạn có " + unreadCount + " thông báo chưa đọc");
                } else {
                    btnNotification.setText("🔔 Thông báo");
                    btnNotification.setPreferredSize(new Dimension(120, 40));
                    btnNotification.setBackground(new Color(255, 140, 0)); // Orange for no notifications
                    btnNotification.setToolTipText("Xem thông báo của bạn");
                }
            }
        });
        
        // Set up timer to update notification count periodically
        Timer notificationTimer = new Timer(30000, e -> updateNotificationBadge(btnNotification)); // Update every 30 seconds
        notificationTimer.start();
    }
    
    private void setDefaultAvatar() {
        System.out.println("🔵 Setting default avatar");
        // Create a simple default avatar icon
        lblAvatar.setIcon(createDefaultAvatarIcon());
        lblAvatar.setToolTipText("Click để xem thông tin cá nhân - Default Avatar");
        lblAvatar.repaint();
        System.out.println("✅ Default avatar set successfully");
    }
    
    private ImageIcon createDefaultAvatarIcon() {
        // Create a simple circular default avatar
        int size = 28;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Choose colors based on Dark Mode
        Color bgColor1, bgColor2, borderColor, textColor;
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            bgColor1 = new Color(88, 166, 255);  // Bright blue
            bgColor2 = new Color(74, 144, 226);  // Darker blue
            borderColor = new Color(88, 166, 255, 180);
            textColor = Color.WHITE;
        } else {
            bgColor1 = new Color(52, 152, 219);  // Original blue
            bgColor2 = new Color(41, 128, 185);  // Original darker blue
            borderColor = new Color(255, 255, 255, 150);
            textColor = Color.WHITE;
        }
        
        // Draw circle background with gradient
        GradientPaint gradient = new GradientPaint(0, 0, bgColor1, size, size, bgColor2);
        g2d.setPaint(gradient);
        g2d.fillOval(0, 0, size, size);
        
        // Draw border
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(1, 1, size-3, size-3);
        
        // Draw user icon
        g2d.setColor(textColor);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "👤";  // Using emoji for better visibility
        
        // If emoji doesn't work, fall back to text
        try {
            int x = (size - fm.stringWidth(text)) / 2;
            int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(text, x, y);
        } catch (Exception e) {
            // Fallback to simple "U"
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            fm = g2d.getFontMetrics();
            text = "U";
            int x = (size - fm.stringWidth(text)) / 2;
            int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(text, x, y);
        }
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    private JPanel createModernSearchSection() {
        // Main search container with modern design
        JPanel searchContainer = new JPanel(new BorderLayout(0, 10));
        searchContainer.setOpaque(false);
        
        // Search title with Dark Mode support
        JLabel searchTitle = new JLabel("🔍 Tìm kiếm sách");
        searchTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
        
        // Set title color based on Dark Mode
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            searchTitle.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
        } else {
            searchTitle.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
        }
        
        // Search input panel with Dark Mode support
        JPanel searchInputPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get colors based on Dark Mode
                Color bgColor, borderColor;
                if (darkModeManager != null && darkModeManager.isDarkMode()) {
                    bgColor = DarkModeManager.DarkTheme.SECONDARY_BG;
                    borderColor = new Color(75, 85, 99); // Dark border color
                } else {
                    bgColor = Color.WHITE;
                    borderColor = new Color(206, 212, 218);
                }
                
                // Draw subtle shadow first
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 14, 14);
                
                // Draw rounded background
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Draw border with glow effect in Dark Mode
                if (darkModeManager != null && darkModeManager.isDarkMode()) {
                    // Subtle glow effect
                    g2d.setColor(new Color(59, 130, 246, 30));
                    g2d.setStroke(new BasicStroke(2.0f));
                    g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                }
                
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        searchInputPanel.setOpaque(false);
        searchInputPanel.setPreferredSize(new Dimension(500, 48));
        searchInputPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        
        // Search label with icon
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 8));
        
        // Set icon color based on Dark Mode
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            searchIcon.setForeground(DarkModeManager.DarkTheme.SECONDARY_TEXT);
        } else {
            searchIcon.setForeground(DarkModeManager.LightTheme.SECONDARY_TEXT);
        }
        
        // Search field with Dark Mode support
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(350, 46));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 12));
        txtSearch.setOpaque(false);
        txtSearch.setBackground(new Color(0, 0, 0, 0));
        
        // Add placeholder functionality with Dark Mode colors
        String placeholder = "Nhập tên sách hoặc tác giả...";
        Color placeholderColor = new Color(156, 163, 175);
        txtSearch.setForeground(placeholderColor);
        txtSearch.setText(placeholder);
        
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(placeholder)) {
                    txtSearch.setText("");
                    // Set text color based on Dark Mode
                    if (darkModeManager != null && darkModeManager.isDarkMode()) {
                        txtSearch.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
                    } else {
                        txtSearch.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
                    }
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(placeholderColor);
                    txtSearch.setText(placeholder);
                }
            }
        });
        
        // Modern search button with Dark Mode support
        btnSearch = new JButton("🔍 Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(110, 46));
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(BorderFactory.createEmptyBorder());
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Set colors based on Dark Mode
        updateSearchButtonColors();
        
        searchInputPanel.add(searchIcon, BorderLayout.WEST);
        searchInputPanel.add(txtSearch, BorderLayout.CENTER);
        searchInputPanel.add(btnSearch, BorderLayout.EAST);
        
        searchContainer.add(searchTitle, BorderLayout.NORTH);
        searchContainer.add(searchInputPanel, BorderLayout.CENTER);
        
        return searchContainer;
    }
    
    private JPanel createModernFilterSection() {
        // Main filter container
        JPanel filterContainer = new JPanel(new BorderLayout(0, 10));
        filterContainer.setOpaque(false);
        
        // Filter title with Dark Mode support
        JLabel filterTitle = new JLabel("⚙️ Bộ lọc nâng cao");
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        filterTitle.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
        
        // Set title color based on Dark Mode
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            filterTitle.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
        } else {
            filterTitle.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
        }
        
        // Filter panel with card design and Dark Mode support
        JPanel filterPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get colors based on Dark Mode
                Color bgColor, borderColor, shadowColor;
                if (darkModeManager != null && darkModeManager.isDarkMode()) {
                    bgColor = DarkModeManager.DarkTheme.CARD_BG;
                    borderColor = DarkModeManager.DarkTheme.BORDER_COLOR;
                    shadowColor = new Color(0, 0, 0, 20);
                } else {
                    bgColor = DarkModeManager.LightTheme.CARD_BG;
                    borderColor = DarkModeManager.LightTheme.BORDER_COLOR;
                    shadowColor = new Color(0, 0, 0, 8);
                }
                
                // Draw subtle shadow effect
                g2d.setColor(shadowColor);
                g2d.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, 16, 16);
                
                // Draw rounded background
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, 15, 15);
                
                // Draw modern border with glow effect in Dark Mode
                if (darkModeManager != null && darkModeManager.isDarkMode()) {
                    // Subtle glow effect
                    g2d.setColor(new Color(88, 166, 255, 15));
                    g2d.setStroke(new BasicStroke(2.0f));
                    g2d.drawRoundRect(1, 1, getWidth()-5, getHeight()-5, 15, 15);
                }
                
                g2d.setColor(borderColor);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth()-4, getHeight()-4, 15, 15);
            }
        };
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Author filter
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblAuthor = createDarkModeLabel("✍️ Tác giả:");
        filterPanel.add(lblAuthor, gbc);
        
        gbc.gridx = 1;
        txtAuthor = createDarkModeTextField("Nhập tên tác giả...", 140);
        filterPanel.add(txtAuthor, gbc);
        
        // Publisher filter
        gbc.gridx = 2; gbc.gridy = 0;
        JLabel lblPublisher = createDarkModeLabel("🏢 Nhà XB:");
        filterPanel.add(lblPublisher, gbc);
        
        gbc.gridx = 3;
        txtPublisher = createDarkModeTextField("Nhập nhà xuất bản...", 140);
        filterPanel.add(txtPublisher, gbc);
        
        // Category filter
        gbc.gridx = 4; gbc.gridy = 0;
        JLabel lblCategory = createDarkModeLabel("📚 Thể loại:");
        filterPanel.add(lblCategory, gbc);
        
        gbc.gridx = 5;
        cbCategory = createDarkModeComboBox(CATEGORIES);
        filterPanel.add(cbCategory, gbc);
        
        // Clear filter button with Dark Mode support
        gbc.gridx = 6; gbc.gridy = 0;
        gbc.insets = new Insets(8, 20, 8, 10);
        JButton btnClearFilter = createDarkModeClearButton();
        filterPanel.add(btnClearFilter, gbc);
        
        filterContainer.add(filterTitle, BorderLayout.NORTH);
        filterContainer.add(filterPanel, BorderLayout.CENTER);
        
        return filterContainer;
    }
    

    
    /**
     * Update search button colors based on Dark Mode
     */
    private void updateSearchButtonColors() {
        if (btnSearch == null) return;
        
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            // Dark Mode colors
            btnSearch.setBackground(DarkModeManager.DarkTheme.PRIMARY_ACCENT);
            btnSearch.setForeground(Color.WHITE);
            
            // Add modern hover effect for Dark Mode
            btnSearch.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnSearch.setBackground(DarkModeManager.DarkTheme.HOVER_BORDER);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    btnSearch.setBackground(DarkModeManager.DarkTheme.PRIMARY_ACCENT);
                }
            });
        } else {
            // Light Mode colors
            btnSearch.setBackground(DarkModeManager.LightTheme.PRIMARY_ACCENT);
            btnSearch.setForeground(Color.WHITE);
            
            // Add hover effect for Light Mode
            btnSearch.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnSearch.setBackground(DarkModeManager.LightTheme.HOVER_BORDER);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    btnSearch.setBackground(DarkModeManager.LightTheme.PRIMARY_ACCENT);
                }
            });
        }
    }
    

    
    private void clearAllFilters() {
        txtAuthor.setText("Nhập tên tác giả...");
        txtAuthor.setForeground(new Color(156, 163, 175));
        
        txtPublisher.setText("Nhập nhà xuất bản...");
        txtPublisher.setForeground(new Color(156, 163, 175));
        
        cbCategory.setSelectedIndex(0); // Select "Tất cả"
        
        // Refresh the book display
        refreshBookDisplay();
    }
    
    /**
     * Create Dark Mode compatible text field
     */
    private JTextField createDarkModeTextField(String placeholder, int width) {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(width, 42));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Set colors based on Dark Mode
        Color bgColor, borderColor, textColor;
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            bgColor = DarkModeManager.DarkTheme.SURFACE_BG;
            borderColor = DarkModeManager.DarkTheme.BORDER_COLOR;
            textColor = DarkModeManager.DarkTheme.PRIMARY_TEXT;
        } else {
            bgColor = DarkModeManager.LightTheme.SURFACE_BG;
            borderColor = DarkModeManager.LightTheme.BORDER_COLOR;
            textColor = DarkModeManager.LightTheme.PRIMARY_TEXT;
        }
        
        textField.setBackground(bgColor);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        // Add placeholder functionality with Dark Mode colors
        Color placeholderColor = new Color(156, 163, 175);
        textField.setForeground(placeholderColor);
        textField.setText(placeholder);
        
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(textColor);
                }
                // Add focus border effect
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(darkModeManager != null && darkModeManager.isDarkMode() 
                        ? DarkModeManager.DarkTheme.FOCUS_BORDER 
                        : DarkModeManager.LightTheme.FOCUS_BORDER, 2),
                    BorderFactory.createEmptyBorder(9, 11, 9, 11)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(placeholderColor);
                    textField.setText(placeholder);
                }
                // Remove focus border
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 1),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
            }
        });
        
        return textField;
    }
    
    /**
     * Create Dark Mode compatible combo box
     */
    private JComboBox<String> createDarkModeComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setPreferredSize(new Dimension(170, 42));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Set colors based on Dark Mode
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            comboBox.setBackground(DarkModeManager.DarkTheme.SURFACE_BG);
            comboBox.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
            comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DarkModeManager.DarkTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
        } else {
            comboBox.setBackground(DarkModeManager.LightTheme.SURFACE_BG);
            comboBox.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
            comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DarkModeManager.LightTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
        }
        
        return comboBox;
    }
    
    /**
     * Create Dark Mode compatible clear button
     */
    private JButton createDarkModeClearButton() {
        JButton btnClearFilter = new JButton("🗑️ Xóa bộ lọc");
        btnClearFilter.setPreferredSize(new Dimension(130, 42));
        btnClearFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClearFilter.setFocusPainted(false);
        btnClearFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Set colors based on Dark Mode
        Color bgColor, textColor, hoverColor, borderColor;
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            bgColor = DarkModeManager.DarkTheme.SURFACE_BG;
            textColor = DarkModeManager.DarkTheme.SECONDARY_TEXT;
            hoverColor = DarkModeManager.DarkTheme.HOVER_BG;
            borderColor = DarkModeManager.DarkTheme.BORDER_COLOR;
        } else {
            bgColor = new Color(248, 249, 250);
            textColor = new Color(73, 80, 87);
            hoverColor = new Color(233, 236, 239);
            borderColor = DarkModeManager.LightTheme.BORDER_COLOR;
        }
        
        btnClearFilter.setBackground(bgColor);
        btnClearFilter.setForeground(textColor);
        btnClearFilter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Add functionality and hover effects
        btnClearFilter.addActionListener(e -> clearAllFilters());
        btnClearFilter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClearFilter.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnClearFilter.setBackground(bgColor);
            }
        });
        
        return btnClearFilter;
    }
    
    /**
     * Create Dark Mode compatible label
     */
    private JLabel createDarkModeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Set color based on Dark Mode
        if (darkModeManager != null && darkModeManager.isDarkMode()) {
            label.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
        } else {
            label.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
        }
        
        return label;
    }
    

    
    /**
     * Recursively update component colors
     */
    private void updateComponentColors(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (darkModeManager != null && darkModeManager.isDarkMode()) {
                    label.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
                } else {
                    label.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
                }
            } else if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                if (darkModeManager != null && darkModeManager.isDarkMode()) {
                    textField.setBackground(DarkModeManager.DarkTheme.SURFACE_BG);
                    if (!textField.getText().contains("Nhập")) {
                        textField.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
                    }
                } else {
                    textField.setBackground(DarkModeManager.LightTheme.SURFACE_BG);
                    if (!textField.getText().contains("Nhập")) {
                        textField.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
                    }
                }
            } else if (comp instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) comp;
                if (darkModeManager != null && darkModeManager.isDarkMode()) {
                    comboBox.setBackground(DarkModeManager.DarkTheme.SURFACE_BG);
                    comboBox.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
                } else {
                    comboBox.setBackground(DarkModeManager.LightTheme.SURFACE_BG);
                    comboBox.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
                }
            } else if (comp instanceof Container) {
                updateComponentColors((Container) comp);
            }
        }
    }
    
    private void loadUserAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            System.out.println("⚡ Avatar URL is null/empty, using default");
            setDefaultAvatar();
            return;
        }
        
        System.out.println("🖼️ Loading avatar from: " + avatarUrl);
        
        // Load avatar from URL in background thread
        new Thread(() -> {
            try {
                ImageIcon icon = null;
                
                // Check if it's a URL or local file path
                if (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
                    System.out.println("🌐 Loading from web URL: " + avatarUrl);
                    URL url = new URL(avatarUrl);
                    icon = new ImageIcon(url);
                } else if (avatarUrl.startsWith("file://")) {
                    System.out.println("📁 Loading from file URL: " + avatarUrl);
                    URL url = new URL(avatarUrl);
                    icon = new ImageIcon(url);
                } else if (avatarUrl.startsWith("/") || avatarUrl.contains(":\\")) {
                    System.out.println("💾 Loading from local path: " + avatarUrl);
                    // Check if file exists
                    java.io.File file = new java.io.File(avatarUrl);
                    if (file.exists()) {
                        icon = new ImageIcon(avatarUrl);
                    } else {
                        System.out.println("❌ Local file not found: " + avatarUrl);
                        SwingUtilities.invokeLater(() -> setDefaultAvatar());
                        return;
                    }
                } else {
                    System.out.println("🌐 Assuming web URL (adding http): " + avatarUrl);
                    try {
                        URL url = new URL("https://" + avatarUrl);
                        icon = new ImageIcon(url);
                    } catch (Exception e1) {
                        System.out.println("🔄 Trying with http: " + avatarUrl);
                        URL url = new URL("http://" + avatarUrl);
                        icon = new ImageIcon(url);
                    }
                }
                
                // Check if image loaded successfully
                if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                    System.out.println("❌ Failed to load image or invalid dimensions");
                    SwingUtilities.invokeLater(() -> setDefaultAvatar());
                    return;
                }
                
                System.out.println("✅ Image loaded successfully: " + icon.getIconWidth() + "x" + icon.getIconHeight());
                
                // Resize image to fit avatar label
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                
                // Create circular avatar with border
                java.awt.image.BufferedImage circularImage = new java.awt.image.BufferedImage(28, 28, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = circularImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create circular clip
                g2d.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 28, 28));
                g2d.drawImage(scaledImg, 0, 0, null);
                
                // Draw border with better visibility
                g2d.setClip(null);
                if (darkModeManager != null && darkModeManager.isDarkMode()) {
                    g2d.setColor(new Color(88, 166, 255, 150)); // Blue border in dark mode
                } else {
                    g2d.setColor(new Color(0, 123, 255, 150)); // Blue border in light mode
                }
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(1, 1, 25, 25);
                
                g2d.dispose();
                
                SwingUtilities.invokeLater(() -> {
                    lblAvatar.setIcon(new ImageIcon(circularImage));
                    lblAvatar.setToolTipText("Click để xem thông tin cá nhân - Avatar loaded from: " + avatarUrl);
                    lblAvatar.repaint();
                    System.out.println("🎉 Avatar set successfully!");
                });
                
            } catch (Exception e) {
                System.err.println("💥 Error loading avatar from URL '" + avatarUrl + "': " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> setDefaultAvatar());
            }
        }).start();
    }
    
    private void loadUserAvatarFromDB(int userId) {
        System.out.println("🔍 Loading avatar for user ID: " + userId);
        
        // Load user avatar URL from database in background thread
        new Thread(() -> {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                String sql = "SELECT username, avatar FROM users WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    String username = rs.getString("username");
                    String avatarUrl = rs.getString("avatar");
                    System.out.println("📸 Found user: " + username + ", avatar: " + 
                        (avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : "null/empty"));
                    
                    SwingUtilities.invokeLater(() -> {
                        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                            loadUserAvatar(avatarUrl.trim());
                        } else {
                            System.out.println("⚡ Using default avatar for user: " + username);
                            setDefaultAvatar();
                        }
                    });
                } else {
                    System.out.println("❌ No user found with ID: " + userId);
                    SwingUtilities.invokeLater(() -> setDefaultAvatar());
                }
                
                rs.close();
                pstmt.close();
            } catch (Exception e) {
                System.err.println("💥 Error loading user avatar from database: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> setDefaultAvatar());
            }
        }).start();
    }
    
    private void showImportantNotificationsOnLogin() {
        // Show important notifications (overdue books, near-due books) when user logs in
        SwingUtilities.invokeLater(() -> {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                // Check for overdue/near-due notifications
                String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND type = 'Nhắc nhở' AND is_read = 0";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next() && rs.getInt(1) > 0) {
                    int count = rs.getInt(1);
                    
                    // Show popup notification for important reminders
                    int result = JOptionPane.showConfirmDialog(this,
                        String.format("Bạn có %d thông báo quan trọng về việc trả sách!\nBạn có muốn xem ngay không?", count),
                        "Thông báo quan trọng",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (result == JOptionPane.YES_OPTION) {
                        new NotificationUI(userId).setVisible(true);
                    }
                }
                
                rs.close();
                pstmt.close();
                
            } catch (SQLException e) {
                System.err.println("Error checking important notifications: " + e.getMessage());
            }
        });
    }
    

}