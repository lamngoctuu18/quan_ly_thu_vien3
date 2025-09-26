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

public class ClientUI extends JFrame {
    private JTextField txtSearch, txtAuthor, txtPublisher;
    private JComboBox<String> cbCategory;
    private JButton btnSearch, btnBorrow, btnLogout, btnFavorite, btnActivity;
    private JLabel lblUser;
    private int userId = -1;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JPopupMenu suggestPopup;

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
        
        // Initialize database tables
        initializeBorrowRequestsTable();
        
        // Create main interface
        createMainInterface();
        
        // Connect to server
        connectToServer();
        
        // Setup window
        pack();
        setLocationRelativeTo(null);
    }

    private void createMainInterface() {
        // Main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(248, 249, 250));
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
        
        // Search section
        JPanel searchSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        searchSection.setOpaque(false);
        
        // Search icon
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        
        // Search field
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtSearch.setBackground(Color.WHITE);
        
        // Search button
        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 40));
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        searchSection.add(searchIcon);
        searchSection.add(txtSearch);
        searchSection.add(btnSearch);
        
        // Filter section
        JPanel filterSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        filterSection.setOpaque(false);
        
        // Author filter
        JLabel lblAuthor = new JLabel("Tác giả:");
        lblAuthor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtAuthor = new JTextField(12);
        txtAuthor.setPreferredSize(new Dimension(120, 35));
        txtAuthor.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Publisher filter
        JLabel lblPublisher = new JLabel("Nhà XB:");
        lblPublisher.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPublisher = new JTextField(12);
        txtPublisher.setPreferredSize(new Dimension(120, 35));
        txtPublisher.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Category filter
        JLabel lblCategory = new JLabel("Thể loại:");
        lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cbCategory = new JComboBox<>(CATEGORIES);
        cbCategory.setPreferredSize(new Dimension(150, 35));
        cbCategory.setBackground(Color.WHITE);
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        filterSection.add(lblAuthor);
        filterSection.add(txtAuthor);
        filterSection.add(lblPublisher);
        filterSection.add(txtPublisher);
        filterSection.add(lblCategory);
        filterSection.add(cbCategory);
        
        // User info section
        JPanel userSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        userSection.setOpaque(false);
        
        lblUser = new JLabel("Chưa đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(new Color(52, 58, 64));
        
        btnLogout = new JButton("Đăng xuất");
        btnLogout.setPreferredSize(new Dimension(100, 40));
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        userSection.add(lblUser);
        userSection.add(btnLogout);
        
        // Combine sections
        JPanel searchFilterPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        searchFilterPanel.setOpaque(false);
        searchFilterPanel.add(searchSection);
        searchFilterPanel.add(filterSection);
        
        topPanel.add(searchFilterPanel, BorderLayout.CENTER);
        topPanel.add(userSection, BorderLayout.EAST);
        
        return topPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        // Books grid panel
        JPanel booksGridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        booksGridPanel.setBackground(new Color(248, 249, 250));
        booksGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Scroll pane for books
        JScrollPane scrollPane = new JScrollPane(booksGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Load books initially
        loadBooksGrid(booksGridPanel);
        
        return centerPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        bottomPanel.setOpaque(false);
        
        // Create navigation buttons
        btnFavorite = createNavigationButton("📚 Sách yêu thích", new Color(255, 140, 0));
        btnActivity = createNavigationButton("📊 Hoạt động", new Color(40, 167, 69));
        btnBorrow = createNavigationButton("� Đăng ký mượn sách", new Color(0, 123, 255));
        JButton btnRefresh = createNavigationButton("🔄 Làm mới", new Color(23, 162, 184));
        
        // Add refresh functionality
        btnRefresh.addActionListener(e -> refreshBookDisplay());
        
        JButton btnBorrowedBooks = createNavigationButton("📖 Sách đã mượn", new Color(220, 53, 69));
        btnBorrowedBooks.addActionListener(e -> showBorrowedBooksDialog());
        
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
        
        // Logout
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn đăng xuất?", 
                "Xác nhận đăng xuất", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                // Reset user info
                userId = -1;
                
                // Close socket connection
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (Exception ex) {
                    // Ignore closing errors
                }
                
                // Close current window
                dispose();
                
                // Open login window
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Import app.MainApp and call its main method to restart the login window
                        app.MainApp.main(new String[]{});
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, 
                            "Không thể mở cửa sổ đăng nhập: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
        
        // Search suggestions
        setupSearchSuggestions();
    }
    
    private void refreshBookDisplay() {
        // Find the books grid panel and refresh it
        JPanel centerPanel = (JPanel) ((JPanel) getContentPane()).getComponent(1);
        JScrollPane scrollPane = (JScrollPane) centerPanel.getComponent(0);
        JPanel booksGridPanel = (JPanel) scrollPane.getViewport().getView();
        loadBooksGrid(booksGridPanel);
    }
    
    private void setupSearchSuggestions() {
        suggestPopup = new JPopupMenu();
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String keyword = txtSearch.getText().trim();
                suggestPopup.setVisible(false);
                
                if (keyword.length() < 2) return;
                
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
    
    private void showBorrowedBooksDialog() {
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem sách đã mượn!");
            return;
        }
        
        try {
            if (socket == null || socket.isClosed() || out == null || in == null) {
                connectToServer();
            }
            
            out.println("LIST_BORROWED|" + userId);
            String resp = in.readLine();
            
            if (resp != null && resp.startsWith("BORROWED_LIST|")) {
                String data = resp.substring("BORROWED_LIST|".length());
                String[] books = data.split(";");
                
                if (books.length > 0 && !books[0].isEmpty()) {
                    createBorrowedBooksDialog(books);
                } else {
                    JOptionPane.showMessageDialog(this, "Bạn chưa mượn sách nào!", "Sách đang mượn", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể tải danh sách sách đã mượn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createBorrowedBooksDialog(String[] books) {
        JDialog dialog = new JDialog(this, "📚 Sách đang mượn", true);
        dialog.setSize(1000, 650);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("📚 Danh sách sách đang mượn", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(new Color(220, 53, 69));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Table with enhanced styling
        String[] columnNames = {"STT", "📖 Tên sách", "✍️ Tác giả", "📅 Ngày mượn", "⏰ Hạn trả", "📊 Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Populate table data
        for (int i = 0; i < books.length; i++) {
            String book = books[i].trim();
            if (!book.isEmpty()) {
                String[] parts = book.split(",");
                Object[] row = {
                    i + 1,
                    parts.length > 0 ? parts[0] : "N/A",
                    parts.length > 1 ? parts[1] : "N/A", 
                    parts.length > 2 ? parts[2] : "Gần đây",
                    parts.length > 3 ? parts[3] : "30 ngày",
                    "🔄 Đang mượn"
                };
                model.addRow(row);
            }
        }
        
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(50);
        table.setSelectionBackground(new Color(220, 53, 69, 50));
        table.setSelectionForeground(new Color(220, 53, 69));
        table.setGridColor(new Color(222, 226, 230));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Enhanced table header
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(220, 53, 69));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 45));
        
        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 53, 69), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Info cards panel
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        infoPanel.setOpaque(false);
        
        JPanel totalPanel = createBorrowInfoCard("📊 Tổng số sách", String.valueOf(model.getRowCount()), new Color(220, 53, 69));
        JPanel overduePanel = createBorrowInfoCard("⏰ Quá hạn", "0", new Color(255, 193, 7));
        JPanel nearDuePanel = createBorrowInfoCard("⚠️ Sắp hết hạn", "1", new Color(255, 108, 0));
        
        infoPanel.add(totalPanel);
        infoPanel.add(overduePanel);
        infoPanel.add(nearDuePanel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton refreshBtn = new JButton("🔄 Làm mới");
        refreshBtn.setPreferredSize(new Dimension(120, 40));
        refreshBtn.setBackground(new Color(40, 167, 69));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            showBorrowedBooksDialog();
        });
        
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.setBackground(new Color(220, 53, 69));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(refreshBtn);
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
    }
    
    private JPanel createBorrowInfoCard(String title, String value, Color color) {
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
        
        // Show detailed borrow request dialog
        showBorrowRequestDialog(bookId, title);
            java.sql.Connection conn = null;
            java.sql.PreparedStatement checkPs = null;
            java.sql.PreparedStatement insertPs = null;
            java.sql.ResultSet rs = null;
            
            try {
                // Create borrow request directly in database
                conn = java.sql.DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000");
                
                // Check if user already has a pending request for this book
                String checkQuery = "SELECT id FROM borrow_requests WHERE user_id = ? AND book_id = ? AND status = 'PENDING'";
                checkPs = conn.prepareStatement(checkQuery);
                checkPs.setInt(1, userId);
                checkPs.setString(2, bookId);
                rs = checkPs.executeQuery();
                
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, 
                        "Bạn đã có đăng ký mượn sách này đang chờ duyệt!", 
                        "Thông báo", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Close the first query resources
                if (rs != null) rs.close();
                if (checkPs != null) checkPs.close();
                
                // Create new borrow request
                String insertQuery = "INSERT INTO borrow_requests (user_id, book_id, request_date, status) VALUES (?, ?, ?, 'PENDING')";
                insertPs = conn.prepareStatement(insertQuery);
                insertPs.setInt(1, userId);
                insertPs.setString(2, bookId);
                insertPs.setString(3, java.time.LocalDateTime.now().toString());
                
                int rowsInserted = insertPs.executeUpdate();
                
                if (rowsInserted > 0) {
                    // Record activity
                    recordActivity(bookId, "borrow_request");
                    
                    JOptionPane.showMessageDialog(this, 
                        "Đăng ký mượn sách thành công!\n" +
                        "Vui lòng chờ quản trị viên duyệt đăng ký.", 
                        "Thành công", 
                        JOptionPane.INFORMATION_MESSAGE);
                    refreshBookDisplay();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Không thể tạo đăng ký mượn sách!", 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception dbEx) {
                JOptionPane.showMessageDialog(this, 
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
    
    private boolean checkBorrowingLimit() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            // Count current borrowed books (approved requests that haven't been returned)
            String countQuery = "SELECT COUNT(*) FROM borrow_requests br " +
                "WHERE br.user_id = ? AND br.status = 'APPROVED' " +
                "AND br.id NOT IN (SELECT DISTINCT request_id FROM returns WHERE request_id IS NOT NULL)";
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
        JDialog dialog = new JDialog(this, "📚 Đăng ký mượn sách", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("📚 Đăng ký mượn sách", SwingConstants.CENTER);
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
        formPanel.add(createStyledLabel("📖 Tên sách:"), gbc);
        gbc.gridx = 1;
        JLabel bookTitleLabel = new JLabel(title);
        bookTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookTitleLabel.setForeground(new Color(0, 123, 255));
        formPanel.add(bookTitleLabel, gbc);
        
        // Borrower info
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("👤 Người mượn:"), gbc);
        gbc.gridx = 1;
        JLabel borrowerLabel = new JLabel(lblUser.getText().replace("Xin chào, ", ""));
        borrowerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(borrowerLabel, gbc);
        
        // Borrow date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("📅 Ngày mượn:"), gbc);
        gbc.gridx = 1;
        JLabel borrowDateLabel = new JLabel(java.time.LocalDate.now().toString());
        borrowDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(borrowDateLabel, gbc);
        
        // Return date picker
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("📤 Ngày trả dự kiến:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> returnDateCombo = new JComboBox<>();
        for (int i = 1; i <= 7; i++) {
            java.time.LocalDate returnDate = java.time.LocalDate.now().plusDays(i);
            returnDateCombo.addItem(returnDate.toString() + " (" + i + " ngày)");
        }
        returnDateCombo.setPreferredSize(new Dimension(200, 35));
        returnDateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(returnDateCombo, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createStyledLabel("📝 Ghi chú:"), gbc);
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
        formPanel.add(notesScroll, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton submitBtn = new JButton("📚 Gửi đăng ký");
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
        
        JButton cancelBtn = new JButton("❌ Hủy");
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

    private void loadBooksGrid(JPanel booksGridPanel) {
        booksGridPanel.removeAll();
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            StringBuilder query = new StringBuilder("SELECT id, title, author, publisher, year, category, quantity, cover_image FROM books WHERE 1=1");
            
            // Add search filters
            String searchText = txtSearch.getText().trim();
            String authorText = txtAuthor.getText().trim();
            String publisherText = txtPublisher.getText().trim();
            String categoryText = cbCategory.getSelectedItem().toString();
            
            if (!searchText.isEmpty()) {
                query.append(" AND (title LIKE ? OR author LIKE ? OR publisher LIKE ?)");
            }
            if (!authorText.isEmpty()) {
                query.append(" AND author LIKE ?");
            }
            if (!publisherText.isEmpty()) {
                query.append(" AND publisher LIKE ?");
            }
            if (!"Tất cả".equals(categoryText)) {
                query.append(" AND category = ?");
            }
            
            PreparedStatement ps = conn.prepareStatement(query.toString());
            int paramIndex = 1;
            
            if (!searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
            }
            if (!authorText.isEmpty()) {
                ps.setString(paramIndex++, "%" + authorText + "%");
            }
            if (!publisherText.isEmpty()) {
                ps.setString(paramIndex++, "%" + publisherText + "%");
            }
            if (!"Tất cả".equals(categoryText)) {
                ps.setString(paramIndex++, categoryText);
            }
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String bookId = rs.getString("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String category = rs.getString("category");
                int quantity = rs.getInt("quantity");
                String coverImage = rs.getString("cover_image");
                
                JPanel bookPanel = createBookPanel(bookId, title, author, category, quantity, coverImage);
                booksGridPanel.add(bookPanel);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải sách: " + ex.getMessage());
        }
        
        booksGridPanel.revalidate();
        booksGridPanel.repaint();
    }
    
    private JPanel createBookPanel(String bookId, String title, String author, String category, int quantity, String coverImage) {
        JPanel bookPanel = new JPanel();
        bookPanel.setLayout(new BoxLayout(bookPanel, BoxLayout.Y_AXIS));
        bookPanel.setBackground(Color.WHITE);
        bookPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        bookPanel.setPreferredSize(new Dimension(250, 350));
        
        // Book image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(200, 180));
        imagePanel.setBackground(new Color(240, 240, 240));
        imagePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        JLabel imageLabel;
        if (coverImage != null && !coverImage.trim().isEmpty()) {
            try {
                // Load image from URL with better error handling
                java.net.URL imageUrl = new java.net.URL(coverImage);
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                
                // Wait for image to load completely
                if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                    // Scale image to fit perfectly
                    Image scaledImage = originalIcon.getImage().getScaledInstance(180, 160, Image.SCALE_SMOOTH);
                    imageLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                    imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                } else {
                    // If image loading fails, show category icon
                    String bookIcon = getBookIcon(category);
                    imageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
                    imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
                    imageLabel.setForeground(getCategoryColor(category));
                }
            } catch (Exception e) {
                // If image loading fails, show category icon
                String bookIcon = getBookIcon(category);
                imageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
                imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
                imageLabel.setForeground(getCategoryColor(category));
            }
        } else {
            // Default book icon with category-based styling
            String bookIcon = getBookIcon(category);
            imageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
            imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
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
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setOpaque(false);
        
        // Favorite button
        JButton favoriteBtn = new JButton("❤️");
        favoriteBtn.setBackground(new Color(255, 140, 0));
        favoriteBtn.setForeground(Color.WHITE);
        favoriteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        favoriteBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        favoriteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        favoriteBtn.setFocusPainted(false);
        favoriteBtn.setToolTipText("Thêm vào yêu thích");
        favoriteBtn.addActionListener(e -> addToFavorite(bookId));
        
        // Borrow button
        JButton borrowBtn = new JButton("�");
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
        JDialog dialog = new JDialog(this, "📚 Danh sách sách yêu thích", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(255, 248, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("❤️ Sách yêu thích của bạn", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(255, 140, 0));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Create table model
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "📖 Tên sách", "✍️ Tác giả", "❤️ Yêu thích", "🗑️ Thao tác"}, 0) {
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
                    model.addRow(new Object[]{parts[0], parts[1], parts[2], "❤️", "Xóa"});
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
        JDialog dialog = new JDialog(this, "📊 Lịch sử hoạt động", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("📊 Lịch sử hoạt động của bạn", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(40, 167, 69));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Create table model for activities
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "📖 Sách", "🎯 Hoạt động", "📅 Thời gian", "🗑️ Thao tác"}, 0) {
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
                return "❤️ Thêm yêu thích";
            case "remove_favorite":
                return "💔 Bỏ yêu thích";
            case "borrow":
            case "borrow_request":
                return "📚 Đăng ký mượn";
            case "return":
                return "📤 Trả sách";
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
    
    private JPanel createActivityItem(String activity, int index) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        // Index label
        JLabel indexLabel = new JLabel(String.valueOf(index));
        indexLabel.setPreferredSize(new Dimension(30, 30));
        indexLabel.setBackground(new Color(40, 167, 69));
        indexLabel.setForeground(Color.WHITE);
        indexLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
        indexLabel.setOpaque(true);
        indexLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Activity text
        JLabel activityLabel = new JLabel(activity);
        activityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        activityLabel.setForeground(new Color(33, 37, 41));
        
        // Activity icon based on type
        String icon = "📋";
        if (activity.toLowerCase().contains("favorite")) {
            icon = "❤️";
        } else if (activity.toLowerCase().contains("borrow")) {
            icon = "📖";
        } else if (activity.toLowerCase().contains("return")) {
            icon = "↩️";
        }
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        item.add(indexLabel, BorderLayout.WEST);
        item.add(activityLabel, BorderLayout.CENTER);
        item.add(iconLabel, BorderLayout.EAST);
        
        return item;
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
            JDialog dialog = new JDialog(this, "📝 Đăng ký mượn sách của bạn", true);
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
            JLabel headerLabel = new JLabel("📝 Danh sách đăng ký mượn sách", SwingConstants.CENTER);
            headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            headerLabel.setForeground(new Color(0, 123, 255));
            headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            
            // Table
            String[] columnNames = {"ID", "📖 Tên sách", "✍️ Tác giả", "📅 Ngày đăng ký", "📊 Trạng thái", "📝 Ghi chú"};
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
                        statusDisplay = "🟡 Chờ duyệt";
                        break;
                    case "APPROVED":
                        statusDisplay = "✅ Đã duyệt";
                        break;
                    case "REJECTED":
                        statusDisplay = "❌ Đã từ chối";
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
            
            JPanel totalPanel = createRequestInfoCard("📊 Tổng số", String.valueOf(totalRequests), new Color(0, 123, 255));
            JPanel pendingPanel = createRequestInfoCard("🟡 Chờ duyệt", String.valueOf(pendingCount), new Color(255, 193, 7));
            JPanel rejectedPanel = createRequestInfoCard("❌ Từ chối", String.valueOf(rejectedCount), new Color(220, 53, 69));
            
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
        JDialog dialog = new JDialog(this, "📖 Thông tin chi tiết sách", true);
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
        JLabel headerLabel = new JLabel("📖 Thông tin chi tiết", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);
        
        // Left panel - Book image
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(250, 350));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel bookImageLabel;
        if (coverImage != null && !coverImage.trim().isEmpty()) {
            try {
                java.net.URL imageUrl = new java.net.URL(coverImage);
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                
                if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                    Image scaledImage = originalIcon.getImage().getScaledInstance(220, 300, Image.SCALE_SMOOTH);
                    bookImageLabel = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                } else {
                    String bookIcon = getBookIcon(category);
                    bookImageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
                    bookImageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
                    bookImageLabel.setForeground(getCategoryColor(category));
                }
            } catch (Exception e) {
                String bookIcon = getBookIcon(category);
                bookImageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
                bookImageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
                bookImageLabel.setForeground(getCategoryColor(category));
            }
        } else {
            String bookIcon = getBookIcon(category);
            bookImageLabel = new JLabel(bookIcon, SwingConstants.CENTER);
            bookImageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
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
        JPanel authorPanel = createInfoRow("👤 Tác giả:", author);
        
        // Publisher
        JPanel publisherPanel = createInfoRow("🏢 Nhà xuất bản:", publisher);
        
        // Year
        JPanel yearPanel = createInfoRow("📅 Năm xuất bản:", String.valueOf(year));
        
        // Category
        JPanel categoryPanel = createInfoRow("📚 Thể loại:", category);
        
        // Quantity
        JPanel quantityPanel = createInfoRow("📦 Số lượng:", quantity + " cuốn");
        
        // Status
        String statusText = quantity > 0 ? "✅ Có sẵn" : "❌ Hết sách";
        Color statusColor = quantity > 0 ? new Color(40, 167, 69) : new Color(220, 53, 69);
        JPanel statusPanel = createInfoRow("📊 Trạng thái:", statusText, statusColor);
        
        // Description
        JLabel descLabel = new JLabel("📝 Mô tả:");
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
        JButton favoriteBtn = new JButton("❤️ Thêm yêu thích");
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
            case "Văn học – Tiểu thuyết": return "📚";
            case "Khoa học – Công nghệ": return "🔬";
            case "Kinh tế – Quản trị": return "💼";
            case "Tâm lý – Kỹ năng sống": return "🧠";
            case "Giáo trình – Học thuật": return "🎓";
            case "Trẻ em – Thiếu nhi": return "🧸";
            case "Lịch sử – Địa lý": return "🌍";
            case "Tôn giáo – Triết học": return "☯️";
            case "Ngoại ngữ – Từ điển": return "🗣️";
            case "Nghệ thuật – Âm nhạc": return "🎨";
            default: return "📖";
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
}