package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BorrowManagementUI extends JFrame {
    private DefaultTableModel borrowModel;
    private JTable borrowTable;
    private JTextField txtSearchUser, txtSearchPhone, txtSearchBook;
    private JComboBox<String> cbStatus;
    private JLabel lblBorrowCount;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnRefresh, btnExport, btnStats;
    
    // Resource managers để giữ BorrowManagement ổn định
    private DatabaseManager dbManager;
    private BackgroundTaskManager taskManager;

    public BorrowManagementUI() {
        // Khởi tạo resource managers trước
        initializeResourceManagers();
        
        setTitle("Quản lý mượn/trả");
        setSize(1400, 850); // Increased size for better responsive layout
        setMinimumSize(new Dimension(1200, 700)); // Set minimum size
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeComponents();
        loadBorrowData();
        setupEventListeners();
        
        // Auto-send notifications on startup
        sendOverdueNotifications();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(0, 5));
        
        // North panel containing header and toolbar
        JPanel northPanel = createNorthPanel();
        add(northPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // Bottom panel with count info and statistics
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout(0, 2));
        northPanel.setBackground(new Color(70, 130, 180));
        
        // Header panel with title
        JPanel headerPanel = createHeaderPanel();
        
        // Toolbar panel with search and actions
        JPanel toolbarPanel = createToolbarPanel();
        
        northPanel.add(headerPanel, BorderLayout.NORTH);
        northPanel.add(toolbarPanel, BorderLayout.SOUTH);
        
        return northPanel;
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));
        
        // Title with icon
        JLabel titleLabel = new JLabel("Quản lý mượn/trả sách");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        // Statistics label
        JLabel statsLabel = new JLabel("Theo dõi và quản lý tình trạng mượn trả");
        statsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statsLabel.setForeground(new Color(220, 230, 255));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(statsLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel(new BorderLayout(0, 8));
        toolbarPanel.setBackground(new Color(60, 120, 170));
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 15, 20));
        
        // Search and filter panel (responsive)
        JPanel searchFilterPanel = createSearchFilterPanel();
        
        // Action buttons panel (responsive)
        JPanel actionPanel = createActionButtonsPanel();
        
        // Create a wrapper for better spacing
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(searchFilterPanel, BorderLayout.CENTER);
        contentPanel.add(actionPanel, BorderLayout.EAST);
        
        toolbarPanel.add(contentPanel, BorderLayout.CENTER);
        
        return toolbarPanel;
    }
    
    private JPanel createSearchFilterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
        mainPanel.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("Tìm kiếm & Lọc dữ liệu");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        
        // Search and filter controls
        JPanel controlsPanel = createSearchControlsPanel();
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(controlsPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createSearchControlsPanel() {
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 1: User name and Status
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUser = new JLabel("Người mượn:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(new Color(230, 240, 255));
        controlsPanel.add(lblUser, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        txtSearchUser = new JTextField(12);
        txtSearchUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchUser.setPreferredSize(new Dimension(120, 28));
        txtSearchUser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        controlsPanel.add(txtSearchUser, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(230, 240, 255));
        controlsPanel.add(lblStatus, gbc);
        
        gbc.gridx = 3; gbc.gridy = 0;
        String[] statusOptions = {"Tất cả", "Bình thường", "Sắp hết hạn", "Quá hạn"};
        cbStatus = new JComboBox<>(statusOptions);
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbStatus.setPreferredSize(new Dimension(110, 28));
        cbStatus.setBackground(Color.WHITE);
        controlsPanel.add(cbStatus, gbc);
        
        // Row 2: Phone and Book name
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblPhone = new JLabel("Điện thoại:");
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhone.setForeground(new Color(230, 240, 255));
        controlsPanel.add(lblPhone, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        txtSearchPhone = new JTextField(12);
        txtSearchPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchPhone.setPreferredSize(new Dimension(120, 28));
        txtSearchPhone.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        controlsPanel.add(txtSearchPhone, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        JLabel lblBook = new JLabel("Tên sách:");
        lblBook.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBook.setForeground(new Color(230, 240, 255));
        controlsPanel.add(lblBook, gbc);
        
        gbc.gridx = 3; gbc.gridy = 1;
        txtSearchBook = new JTextField(12);
        txtSearchBook.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchBook.setPreferredSize(new Dimension(110, 28));
        txtSearchBook.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        controlsPanel.add(txtSearchBook, gbc);
        
        return controlsPanel;
    }
    

    
    private JPanel createActionButtonsPanel() {
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        
        // Create buttons with smaller size for better responsive
        btnRefresh = createActionButton("Làm mới", new Color(40, 167, 69), "Làm mới dữ liệu");
        btnExport = createActionButton("Excel", new Color(0, 123, 255), "Xuất dữ liệu ra Excel");
        btnStats = createActionButton("Thống kê", new Color(255, 193, 7), "Xem thống kê chi tiết");
        
        // Add hover effects
        addButtonHoverEffects(btnRefresh, new Color(40, 167, 69), new Color(34, 139, 34));
        addButtonHoverEffects(btnExport, new Color(0, 123, 255), new Color(0, 103, 235));
        addButtonHoverEffects(btnStats, new Color(255, 193, 7), new Color(235, 173, 0));
        
        // Arrange buttons in compact layout
        gbc.gridx = 0; gbc.gridy = 0;
        actionPanel.add(btnStats, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        actionPanel.add(btnExport, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        actionPanel.add(btnRefresh, gbc);
        
        return actionPanel;
    }
    
    private JButton createActionButton(String text, Color bgColor, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(95, 32));
        return button;
    }
    
    private void addButtonHoverEffects(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
            }
        });
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.setBackground(new Color(248, 249, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Table container with shadow effect
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Create table
        String[] columns = {"Tên sách", "Người mượn", "Số điện thoại", "Ngày mượn", "Ngày trả", "Hạn trả", "Thao tác"};
        borrowModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only action column is editable
            }
        };
        
        borrowTable = new JTable(borrowModel);
        borrowTable.setRowHeight(50);
        borrowTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        borrowTable.setGridColor(new Color(235, 235, 235));
        borrowTable.setSelectionBackground(new Color(218, 230, 255));
        borrowTable.setSelectionForeground(new Color(0, 102, 204));
        borrowTable.setShowGrid(true);
        borrowTable.setIntercellSpacing(new Dimension(1, 1));
        
        // Header styling with gradient effect
        borrowTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        borrowTable.getTableHeader().setBackground(new Color(70, 130, 180));
        borrowTable.getTableHeader().setForeground(Color.WHITE);
        borrowTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        borrowTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        
        // Custom cell renderer for status coloring
        borrowTable.setDefaultRenderer(Object.class, new StatusCellRenderer());
        
        // Action column with buttons
        borrowTable.getColumn("Thao tác").setCellRenderer(new ActionButtonRenderer());
        borrowTable.getColumn("Thao tác").setCellEditor(new ActionButtonEditor());
        
        // Set optimized column widths
        borrowTable.getColumnModel().getColumn(0).setPreferredWidth(220); // Tên sách
        borrowTable.getColumnModel().getColumn(1).setPreferredWidth(160); // Tên người mượn
        borrowTable.getColumnModel().getColumn(2).setPreferredWidth(130); // Số điện thoại
        borrowTable.getColumnModel().getColumn(3).setPreferredWidth(110); // Ngày mượn
        borrowTable.getColumnModel().getColumn(4).setPreferredWidth(110); // Ngày trả
        borrowTable.getColumnModel().getColumn(5).setPreferredWidth(110); // Hạn trả
        borrowTable.getColumnModel().getColumn(6).setPreferredWidth(180); // Thao tác
        
        // Enhanced scroll pane
        JScrollPane scrollPane = new JScrollPane(borrowTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Custom scrollbar styling
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(150, 150, 150);
                this.trackColor = new Color(240, 240, 240);
            }
        });
        
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tableContainer, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 249, 250));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Statistics panel
        JPanel statsPanel = createStatsPanel();
        
        // Status legend panel
        JPanel legendPanel = createStatusLegendPanel();
        
        // Info text
        JLabel infoLabel = new JLabel("Mẹo: Nhấp vào tiêu đề cột để sắp xếp. Sách quá hạn được tô màu đỏ.");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(108, 117, 125));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setOpaque(false);
        infoPanel.add(infoLabel);
        
        bottomPanel.add(statsPanel, BorderLayout.WEST);
        bottomPanel.add(legendPanel, BorderLayout.CENTER);
        bottomPanel.add(infoPanel, BorderLayout.EAST);
        
        return bottomPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        statsPanel.setOpaque(false);
        
        lblBorrowCount = new JLabel("Tổng số đang mượn: 0");
        lblBorrowCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBorrowCount.setForeground(new Color(70, 130, 180));
        
        JLabel lblOverdue = new JLabel("Quá hạn: 0");
        lblOverdue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOverdue.setForeground(new Color(220, 53, 69));
        
        JLabel lblDueSoon = new JLabel("Sắp hết hạn: 0");
        lblDueSoon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDueSoon.setForeground(new Color(255, 140, 0));
        
        statsPanel.add(lblBorrowCount);
        statsPanel.add(createSeparator());
        statsPanel.add(lblOverdue);
        statsPanel.add(createSeparator());
        statsPanel.add(lblDueSoon);
        
        return statsPanel;
    }
    
    private JPanel createStatusLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        legendPanel.setOpaque(false);
        
        JLabel legendTitle = new JLabel("Chú thích màu sắc:");
        legendTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        legendTitle.setForeground(new Color(73, 80, 87));
        
        JPanel overdueExample = createColorExample("Quá hạn", new Color(255, 200, 200));
        JPanel normalExample = createColorExample("Bình thường", new Color(200, 255, 200));
        
        legendPanel.add(legendTitle);
        legendPanel.add(overdueExample);
        legendPanel.add(normalExample);
        
        return legendPanel;
    }
    
    private JPanel createColorExample(String text, Color color) {
        JPanel example = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        example.setOpaque(false);
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(20, 15));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(73, 80, 87));
        
        example.add(colorBox);
        example.add(label);
        
        return example;
    }
    
    private JLabel createSeparator() {
        JLabel separator = new JLabel("|");
        separator.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        separator.setForeground(new Color(200, 200, 200));
        return separator;
    }
    
    private void setupEventListeners() {
        // Search and filter functionality
        KeyListener searchListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        };
        
        // Add listeners to all search fields
        txtSearchUser.addKeyListener(searchListener);
        txtSearchPhone.addKeyListener(searchListener);
        txtSearchBook.addKeyListener(searchListener);
        
        // Status filter listener
        cbStatus.addActionListener(e -> applyFilters());
        
        // Setup table sorter
        sorter = new TableRowSorter<>(borrowModel);
        borrowTable.setRowSorter(sorter);
        
        // Custom comparator for priority sorting (overdue/near-due first)
        sorter.setComparator(5, new Comparator<String>() { // "Hạn trả" column
            @Override
            public int compare(String date1, String date2) {
                try {
                    // Defensive: handle nulls and empty strings
                    if (date1 == null || date1.trim().isEmpty()) return 1;
                    if (date2 == null || date2.trim().isEmpty()) return -1;

                    LocalDate d1 = LocalDate.parse(date1, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate d2 = LocalDate.parse(date2, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    LocalDate today = LocalDate.now();
                    long days1 = ChronoUnit.DAYS.between(today, d1);
                    long days2 = ChronoUnit.DAYS.between(today, d2);

                    // Priority: overdue and near-due (≤1 day) items first
                    boolean priority1 = days1 <= 1;
                    boolean priority2 = days2 <= 1;

                    if (priority1 && !priority2) return -1;
                    if (!priority1 && priority2) return 1;

                    // Among priority items, overdue first, then by days remaining
                    if (priority1 && priority2) {
                        return Long.compare(days1, days2);
                    }

                    return d1.compareTo(d2);
                } catch (Exception e) {
                    // Fallback: null-safe lexical comparison
                    String a = date1 == null ? "" : date1;
                    String b = date2 == null ? "" : date2;
                    return a.compareTo(b);
                }
            }
        });
        
        // Sort by due date initially (priority items first)
        sorter.toggleSortOrder(5);
        
        // Action buttons event listeners
        setupActionButtonsListeners();
    }
    
    private void setupActionButtonsListeners() {
        // Refresh button event listener
        btnRefresh.addActionListener(e -> {
            // Clear all search and filter fields
            txtSearchUser.setText("");
            txtSearchPhone.setText("");
            txtSearchBook.setText("");
            cbStatus.setSelectedIndex(0); // Reset to "Tất cả"
            
            // Reload data với loading
            executeWithLoading("Đang làm mới dữ liệu mượn/trả...", this::loadBorrowData);
            
            // Clear any active filters
            sorter.setRowFilter(null);
            
            // Show success message briefly
            String originalText = btnRefresh.getText();
            Color originalColor = btnRefresh.getBackground();
            
            btnRefresh.setText("Đã làm mới");
            btnRefresh.setBackground(new Color(40, 167, 69));
            
            // Reset button text after 1.5 seconds
            Timer timer = new Timer(1500, event -> {
                btnRefresh.setText(originalText);
                btnRefresh.setBackground(originalColor);
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        // Export to Excel button event listener
        btnExport.addActionListener(e -> exportToExcel());
        
        // Statistics button event listener
        btnStats.addActionListener(e -> showStatisticsDialog());
    }
    
    private void applyFilters() {
        String userText = txtSearchUser.getText().trim();
        String phoneText = txtSearchPhone.getText().trim();
        String bookText = txtSearchBook.getText().trim();
        String statusFilter = cbStatus.getSelectedItem().toString();
        
        // Create list of filters
    java.util.List<RowFilter<Object, Object>> filters = new java.util.ArrayList<RowFilter<Object, Object>>();
        
        // Add text filters
        if (!userText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + userText, 1)); // Column 1: Người mượn
        }
        if (!phoneText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + phoneText, 2)); // Column 2: Số điện thoại
        }
        if (!bookText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + bookText, 0)); // Column 0: Tên sách
        }
        
        // Add status filter
        if (!"Tất cả".equals(statusFilter)) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    try {
                        // Defensive checks: ensure the entry has at least 6 columns
                        if (entry.getValueCount() <= 5) return false;

                        Object obj = entry.getValue(5); // Column 5: Hạn trả
                        if (!(obj instanceof String)) return false;

                        String dueDateStr = ((String) obj).trim();
                        if (dueDateStr.isEmpty()) return false;

                        LocalDate dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        LocalDate today = LocalDate.now();
                        long daysRemaining = ChronoUnit.DAYS.between(today, dueDate);
                        
                        switch (statusFilter) {
                            case "Quá hạn":
                                return daysRemaining < 0;
                            case "Sắp hết hạn":
                                return daysRemaining >= 0 && daysRemaining <= 1;
                            case "Bình thường":
                                return daysRemaining > 1;
                            default:
                                return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        }
        
        // Apply combined filters
        if (sorter == null) return;

        // Build row filter (null if empty)
        RowFilter<Object, Object> combined = null;
        if (!filters.isEmpty()) {
            try {
                combined = RowFilter.andFilter(filters);
            } catch (Exception e) {
                combined = null;
            }
        }

        // Temporarily disable sorts-on-updates and clear current filter to avoid comparator running on partial model
        boolean previousSortsOnUpdates = sorter.getSortsOnUpdates();
        try {
            sorter.setSortsOnUpdates(false);
            sorter.setRowFilter(null);
            if (combined != null) {
                try {
                    sorter.setRowFilter(combined);
                } catch (Exception e) {
                    // If applying the filter fails (model in inconsistent state), fallback to no filter
                    System.err.println("Failed to apply row filter: " + e.getMessage());
                    sorter.setRowFilter(null);
                }
            }
        } finally {
            // Restore previous setting
            try { sorter.setSortsOnUpdates(previousSortsOnUpdates); } catch (Exception ignored) {}
        }
        
        updateBorrowCount();
    }
    

    
    private void loadBorrowData() {
        borrowModel.setRowCount(0);
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            // Ensure expected_return_date column exists
            try {
                conn.createStatement().execute("ALTER TABLE borrows ADD COLUMN expected_return_date TEXT");
            } catch (Exception e) {
                // Column already exists
            }
            
            // Prefer expected_return_date from the most recent borrow_request for this user/book
            String sql = "SELECT br.id, bk.title as book_title, u.username, u.phone, br.borrow_date, br.return_date, " +
                        "COALESCE((SELECT brq.expected_return_date FROM borrow_requests brq " +
                        "           WHERE brq.user_id = br.user_id AND brq.book_id = br.book_id " +
                        "           ORDER BY brq.request_date DESC LIMIT 1), " +
                        "         br.expected_return_date, date(br.borrow_date, '+14 days')) as due_date, u.id as user_id " +
                        "FROM borrows br " +
                        "JOIN users u ON br.user_id = u.id " +
                        "JOIN books bk ON br.book_id = bk.id " +
                        "WHERE br.return_date IS NULL " +
                        "ORDER BY due_date ASC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String bookTitle = rs.getString("book_title");
                String username = rs.getString("username");
                String phone = rs.getString("phone");
                String borrowDate = rs.getString("borrow_date");
                String returnDate = rs.getString("return_date");
                String dueDate = rs.getString("due_date");
                int userId = rs.getInt("user_id");
                
                // Create action buttons panel data
                String actionData = rs.getInt("id") + "|" + userId; // borrowId|userId for actions
                
                borrowModel.addRow(new Object[]{
                    bookTitle, username, phone, borrowDate, 
                    returnDate == null ? "" : returnDate, 
                    dueDate, actionData
                });
            }
            
            rs.close();
            pstmt.close();
            updateBorrowCount();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateBorrowCount() {
        int totalCount = borrowTable.getRowCount();
        int overdueCount = 0;
        int dueSoonCount = 0;
        
        // Count overdue and due soon items
        for (int i = 0; i < borrowTable.getRowCount(); i++) {
            try {
                String dueDateStr = (String) borrowTable.getValueAt(i, 5);
                if (dueDateStr != null && !dueDateStr.isEmpty()) {
                    LocalDate dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate today = LocalDate.now();
                    long daysRemaining = ChronoUnit.DAYS.between(today, dueDate);
                    
                    if (daysRemaining < 0) {
                        overdueCount++;
                    } else if (daysRemaining <= 1) {
                        dueSoonCount++;
                    }
                }
            } catch (Exception e) {
                // Skip invalid date entries
            }
        }
        
        // Update labels
        lblBorrowCount.setText("Tổng số đang mượn: " + totalCount);
        
        // Update other statistics labels if they exist
        updateStatisticsLabels(overdueCount, dueSoonCount);
    }
    
    private void updateStatisticsLabels(int overdueCount, int dueSoonCount) {
        // Find and update statistics labels in the bottom panel
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                updateLabelsInPanel((JPanel) comp, overdueCount, dueSoonCount);
            }
        }
    }
    
    private void updateLabelsInPanel(JPanel panel, int overdueCount, int dueSoonCount) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                updateLabelsInPanel((JPanel) comp, overdueCount, dueSoonCount);
            } else if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String text = label.getText();
                if (text.startsWith("Quá hạn:")) {
                    label.setText("Quá hạn: " + overdueCount);
                } else if (text.startsWith("Sắp hết hạn:")) {
                    label.setText("Sắp hết hạn: " + dueSoonCount);
                }
            }
        }
    }
    
    private void sendOverdueNotifications() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            // Use expected_return_date from borrow_requests if available, otherwise fallback
            String sql = "SELECT br.id, bk.title as book_title, u.id as user_id, u.username, " +
                        "COALESCE((SELECT brq.expected_return_date FROM borrow_requests brq " +
                        "           WHERE brq.user_id = br.user_id AND brq.book_id = br.book_id " +
                        "           ORDER BY brq.request_date DESC LIMIT 1), "+
                        "         br.expected_return_date, date(br.borrow_date, '+14 days')) as due_date " +
                        "FROM borrows br " +
                        "JOIN users u ON br.user_id = u.id " +
                        "JOIN books bk ON br.book_id = bk.id " +
                        "WHERE br.return_date IS NULL";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String dueDate = rs.getString("due_date");
                int userId = rs.getInt("user_id");
                String bookTitle = rs.getString("book_title");
                
                try {
                    LocalDate due = LocalDate.parse(dueDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate today = LocalDate.now();
                    long daysRemaining = ChronoUnit.DAYS.between(today, due);
                    
                    String notificationType = "";
                    String title = "";
                    String content = "";
                    
                    if (daysRemaining < 0) {
                        // Overdue
                        notificationType = "Nhắc nhở";
                        title = "Sách quá hạn trả";
                        content = String.format("Sách '%s' đã quá hạn trả %d ngày. Vui lòng trả sách sớm nhất có thể.", 
                            bookTitle, Math.abs(daysRemaining));
                    } else if (daysRemaining <= 1) {
                        // Due soon (today or tomorrow)
                        notificationType = "Nhắc nhở";
                        title = "Sách sắp đến hạn trả";
                        if (daysRemaining == 0) {
                            content = String.format("Sách '%s' hết hạn trả hôm nay. Vui lòng trả sách.", bookTitle);
                        } else {
                            content = String.format("Sách '%s' sẽ hết hạn trả vào ngày mai. Vui lòng chuẩn bị trả sách.", bookTitle);
                        }
                    }
                    
                    if (!title.isEmpty()) {
                        NotificationUI.addNotification(userId, notificationType, title, content);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error processing due date: " + e.getMessage());
                }
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error sending overdue notifications: " + e.getMessage());
        }
    }
    
    // Custom cell renderer for status-based coloring
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                // Get due date from column 5 defensively
                try {
                    Object obj = table.getValueAt(row, 5);
                    if (obj instanceof String) {
                        String dueDateStr = ((String) obj).trim();
                        if (!dueDateStr.isEmpty()) {
                            LocalDate dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            LocalDate today = LocalDate.now();
                            long daysRemaining = ChronoUnit.DAYS.between(today, dueDate);

                            if (daysRemaining <= 1) {
                                // Overdue or due soon - Red background
                                c.setBackground(new Color(255, 200, 200));
                                if (daysRemaining < 0) {
                                    c.setForeground(new Color(128, 0, 0)); // Dark red text for overdue
                                } else {
                                    c.setForeground(new Color(255, 0, 0)); // Red text for due soon
                                }
                            } else {
                                // Normal - Light green background
                                c.setBackground(new Color(200, 255, 200));
                                c.setForeground(new Color(0, 100, 0)); // Dark green text
                            }
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } catch (Exception e) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            }
            
            return c;
        }
    }
    
    // Custom button renderer for action column
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton btnNotify, btnReturn;
        
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            
            btnNotify = new JButton("Thông báo");
            btnNotify.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnNotify.setBackground(new Color(255, 165, 0));
            btnNotify.setForeground(Color.WHITE);
            btnNotify.setFocusPainted(false);
            btnNotify.setBorderPainted(false);
            btnNotify.setPreferredSize(new Dimension(100, 30));
            
            btnReturn = new JButton("Trả sách");
            btnReturn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnReturn.setBackground(new Color(34, 139, 34));
            btnReturn.setForeground(Color.WHITE);
            btnReturn.setFocusPainted(false);
            btnReturn.setBorderPainted(false);
            btnReturn.setPreferredSize(new Dimension(80, 30));
            
            add(btnNotify);
            add(btnReturn);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            
            return this;
        }
    }
    
    // Custom button editor for action column
    class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnNotify, btnReturn;
        private String currentValue;
        
        public ActionButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            
            btnNotify = new JButton("Thông báo");
            btnNotify.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnNotify.setBackground(new Color(255, 165, 0));
            btnNotify.setForeground(Color.WHITE);
            btnNotify.setFocusPainted(false);
            btnNotify.setBorderPainted(false);
            btnNotify.setPreferredSize(new Dimension(80, 30));
            
            btnReturn = new JButton("Trả sách");
            btnReturn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnReturn.setBackground(new Color(34, 139, 34));
            btnReturn.setForeground(Color.WHITE);
            btnReturn.setFocusPainted(false);
            btnReturn.setBorderPainted(false);
            btnReturn.setPreferredSize(new Dimension(80, 30));
            
            btnNotify.addActionListener(e -> {
                sendNotificationToUser();
                fireEditingStopped();
            });
            
            btnReturn.addActionListener(e -> {
                returnBook();
                fireEditingStopped();
            });
            
            panel.add(btnNotify);
            panel.add(btnReturn);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, 
                int row, int column) {
            currentValue = value.toString();
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }
        
        private void sendNotificationToUser() {
            try {
                String[] parts = currentValue.split("\\|");
                // int borrowId = Integer.parseInt(parts[0]); // Not needed for notification
                int userId = Integer.parseInt(parts[1]);
                
                // Get book info
                int selectedRow = borrowTable.getSelectedRow();
                String bookTitle = (String) borrowTable.getValueAt(selectedRow, 0);
                String username = (String) borrowTable.getValueAt(selectedRow, 1);
                String dueDate = (String) borrowTable.getValueAt(selectedRow, 5);
                
                // Create notification
                String title = "Nhắc nhở trả sách";
                String content = String.format("Xin chào %s, bạn có sách '%s' cần trả trước ngày %s. Vui lòng trả sách đúng hạn.", 
                    username, bookTitle, dueDate);
                
                NotificationUI.addNotification(userId, "Nhắc nhở", title, content);
                
                JOptionPane.showMessageDialog(BorrowManagementUI.this, 
                    "Đã gửi thông báo đến " + username, 
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(BorrowManagementUI.this, 
                    "Lỗi khi gửi thông báo: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void returnBook() {
            try {
                String[] parts = currentValue.split("\\|");
                int borrowId = Integer.parseInt(parts[0]);
                int userId = Integer.parseInt(parts[1]);
                
                int selectedRow = borrowTable.getSelectedRow();
                String bookTitle = (String) borrowTable.getValueAt(selectedRow, 0);
                String username = (String) borrowTable.getValueAt(selectedRow, 1);
                
                int result = JOptionPane.showConfirmDialog(BorrowManagementUI.this,
                    "Xác nhận trả sách '" + bookTitle + "' của " + username + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    // Update database
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                        String sql = "UPDATE borrows SET return_date = ? WHERE id = ?";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, LocalDate.now().toString());
                        pstmt.setInt(2, borrowId);
                        pstmt.executeUpdate();
                        pstmt.close();
                        
                        // Create notification for successful return
                        String title = "Trả sách thành công";
                        String content = String.format("Bạn đã trả sách '%s' thành công vào ngày %s.", 
                            bookTitle, LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        
                        NotificationUI.addNotification(userId, "Trả sách", title, content);
                        
                        // Reload data
                        loadBorrowData();
                        
                        JOptionPane.showMessageDialog(BorrowManagementUI.this, 
                            "Đã cập nhật trả sách thành công!", 
                            "Thành công", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(BorrowManagementUI.this, 
                    "Lỗi khi trả sách: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportToExcel() {
        try {
            // Create file chooser for saving Excel file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Xuất dữ liệu ra Excel");
            fileChooser.setSelectedFile(new File("Danh_sach_muon_tra_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
            
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
            fileChooser.setFileFilter(filter);
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                
                // Ensure .csv extension
                if (!fileToSave.getName().endsWith(".csv")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
                }
                
                // Export data to CSV (Excel compatible)
                exportTableDataToCSV(fileToSave);
                
                // Show success message
                int choice = JOptionPane.showConfirmDialog(this,
                    "Xuất dữ liệu thành công!\nFile đã được lưu tại: " + fileToSave.getAbsolutePath() + 
                    "\n\nBạn có muốn mở file ngay không?",
                    "Xuất dữ liệu thành công",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (choice == JOptionPane.YES_OPTION) {
                    // Open file with default application
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(fileToSave);
                    }
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi khi xuất dữ liệu: " + e.getMessage(),
                "Lỗi xuất dữ liệu",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportTableDataToCSV(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF-8"))) {
            
            // Write BOM for UTF-8 to ensure proper display in Excel
            writer.write('\ufeff');
            
            // Write header with additional info
            writer.println("DANH SÁCH QUẢN LÝ MƯỢN TRẢ SÁCH");
            writer.println("Ngày xuất: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            writer.println("Tổng số bản ghi: " + borrowTable.getRowCount());
            writer.println(); // Empty line
            
            // Write table headers
            writer.print("STT,");
            for (int col = 0; col < borrowTable.getColumnCount() - 1; col++) { // Exclude action column
                String columnName = borrowTable.getColumnName(col);
                writer.print("\"" + columnName + "\",");
            }
            writer.print("\"Trạng thái\",\"Số ngày còn lại\"");
            writer.println();
            
            // Write table data
            for (int row = 0; row < borrowTable.getRowCount(); row++) {
                writer.print((row + 1) + ","); // STT
                
                for (int col = 0; col < borrowTable.getColumnCount() - 1; col++) { // Exclude action column
                    Object value = borrowTable.getValueAt(row, col);
                    String cellValue = value != null ? value.toString() : "";
                    writer.print("\"" + cellValue.replace("\"", "\"\"") + "\",");
                }
                
                // Add status and days remaining
                try {
                    String dueDateStr = (String) borrowTable.getValueAt(row, 5);
                    if (dueDateStr != null && !dueDateStr.isEmpty()) {
                        LocalDate dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        LocalDate today = LocalDate.now();
                        long daysRemaining = ChronoUnit.DAYS.between(today, dueDate);
                        
                        String status = "";
                        if (daysRemaining < 0) {
                            status = "Quá hạn";
                        } else if (daysRemaining <= 1) {
                            status = "Sắp hết hạn";
                        } else {
                            status = "Bình thường";
                        }
                        
                        writer.print("\"" + status + "\",\"" + daysRemaining + "\"");
                    } else {
                        writer.print("\"\",\"\"");
                    }
                } catch (Exception e) {
                    writer.print("\"\",\"\"");
                }
                
                writer.println();
            }
            
            // Write summary statistics
            writer.println();
            writer.println("THỐNG KÊ TỔNG HỢP:");
            
            int totalCount = borrowTable.getRowCount();
            int overdueCount = 0;
            int dueSoonCount = 0;
            int normalCount = 0;
            
            for (int i = 0; i < borrowTable.getRowCount(); i++) {
                try {
                    String dueDateStr = (String) borrowTable.getValueAt(i, 5);
                    if (dueDateStr != null && !dueDateStr.isEmpty()) {
                        LocalDate dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        LocalDate today = LocalDate.now();
                        long daysRemaining = ChronoUnit.DAYS.between(today, dueDate);
                        
                        if (daysRemaining < 0) {
                            overdueCount++;
                        } else if (daysRemaining <= 1) {
                            dueSoonCount++;
                        } else {
                            normalCount++;
                        }
                    }
                } catch (Exception e) {
                    // Skip invalid entries
                }
            }
            
            writer.println("Tổng số đang mượn:," + totalCount);
            writer.println("Quá hạn:," + overdueCount);
            writer.println("Sắp hết hạn:," + dueSoonCount);
            writer.println("Bình thường:," + normalCount);
        }
    }
    
    /**
     * Khởi tạo resource managers
     */
    private void initializeResourceManagers() {
        try {
            dbManager = DatabaseManager.getInstance();
            taskManager = BackgroundTaskManager.getInstance();
            System.out.println("BorrowManagement resource managers initialized");
        } catch (Exception e) {
            System.err.println("Failed to initialize BorrowManagement resources: " + e.getMessage());
        }
    }
    
    /**
     * Execute operations với loading dialog
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
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                            "Lỗi: " + error.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    });
                }
            );
        } else {
            // Fallback
            task.run();
        }
    }

    /**
     * Hiển thị dialog thống kê chi tiết với giao diện hiện đại
     */
    private void showStatisticsDialog() {
        JDialog statsDialog = new JDialog(this, "📊 THỐNG KÊ CHI TIẾT MƯỢN/TRẢ SÁCH", true);
        statsDialog.setSize(1200, 850);
        statsDialog.setLocationRelativeTo(this);
        
        // Modern dialog styling
        statsDialog.setUndecorated(false);
        statsDialog.getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 3),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Main panel with modern styling and shadow effect
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Modern gradient background
                Color startColor = new Color(248, 251, 255);
                Color midColor = new Color(240, 248, 255);
                Color endColor = new Color(232, 245, 255);
                
                GradientPaint gradient1 = new GradientPaint(0, 0, startColor, 0, getHeight()/2, midColor);
                GradientPaint gradient2 = new GradientPaint(0, getHeight()/2, midColor, 0, getHeight(), endColor);
                
                g2d.setPaint(gradient1);
                g2d.fillRect(0, 0, getWidth(), getHeight()/2);
                g2d.setPaint(gradient2);
                g2d.fillRect(0, getHeight()/2, getWidth(), getHeight()/2);
                
                // Subtle inner shadow effect
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.drawRect(0, 0, getWidth()-1, getHeight()-1);
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Modern header with gradient and shadow
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Header gradient
                GradientPaint headerGradient = new GradientPaint(0, 0, new Color(70, 130, 180), 
                                                                 0, getHeight(), new Color(100, 149, 237));
                g2d.setPaint(headerGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Header shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth(), getHeight(), 15, 15);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel headerLabel = new JLabel("📊 THỐNG KÊ CHI TIẾT MƯỢN/TRẢ SÁCH", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(false);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        
        // Statistics content panel
        JPanel contentPanel = createStatisticsContent();
        
        // Modern close button with hover effects
        JButton closeBtn = new JButton("✖ Đóng") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = getModel().isPressed() ? new Color(220, 53, 69) : 
                               getModel().isRollover() ? new Color(255, 82, 82) : new Color(108, 117, 125);
                
                GradientPaint buttonGradient = new GradientPaint(0, 0, bgColor.brighter(), 
                                                                0, getHeight(), bgColor.darker());
                g2d.setPaint(buttonGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(getText(), x, y);
            }
        };
        closeBtn.setPreferredSize(new Dimension(120, 45));
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> statsDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeBtn);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        statsDialog.add(mainPanel);
        statsDialog.setVisible(true);
    }
    
    /**
     * Tạo nội dung thống kê chi tiết
     */
    private JPanel createStatisticsContent() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setOpaque(false);
        
        // Statistics cards panel
        JPanel cardsPanel = createStatisticsCards();
        
        // Charts and detailed stats panel
        JPanel chartsPanel = createChartsPanel();
        
        // Recent activities panel
        JPanel activitiesPanel = createRecentActivitiesPanel();
        
        // Top panel with cards
        contentPanel.add(cardsPanel, BorderLayout.NORTH);
        
        // Center panel with charts and activities
        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(chartsPanel, BorderLayout.CENTER);
        centerPanel.add(activitiesPanel, BorderLayout.EAST);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    /**
     * Tạo các thẻ thống kê tổng quan
     */
    private JPanel createStatisticsCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            // Tổng số sách đang được mượn
            PreparedStatement ps1 = conn.prepareStatement(
                "SELECT COUNT(*) FROM borrows WHERE return_date IS NULL OR return_date = '' OR return_date = 'null'");
            ResultSet rs1 = ps1.executeQuery();
            int totalBorrowed = rs1.next() ? rs1.getInt(1) : 0;
            
            // Số sách quá hạn
            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT COUNT(*) FROM borrows WHERE (return_date IS NULL OR return_date = '' OR return_date = 'null') " +
                "AND date(borrow_date, '+30 days') < date('now')");
            ResultSet rs2 = ps2.executeQuery();
            int overdueBooks = rs2.next() ? rs2.getInt(1) : 0;
            
            // Số sách sắp hết hạn (trong vòng 3 ngày)
            PreparedStatement ps3 = conn.prepareStatement(
                "SELECT COUNT(*) FROM borrows WHERE (return_date IS NULL OR return_date = '' OR return_date = 'null') " +
                "AND date(borrow_date, '+30 days') BETWEEN date('now') AND date('now', '+3 days')");
            ResultSet rs3 = ps3.executeQuery();
            int dueSoonBooks = rs3.next() ? rs3.getInt(1) : 0;
            
            // Tổng số người dùng đang mượn sách
            PreparedStatement ps4 = conn.prepareStatement(
                "SELECT COUNT(DISTINCT user_id) FROM borrows WHERE return_date IS NULL OR return_date = '' OR return_date = 'null'");
            ResultSet rs4 = ps4.executeQuery();
            int activeBorrowers = rs4.next() ? rs4.getInt(1) : 0;
            
            cardsPanel.add(createStatCard("📚 Đang mượn", String.valueOf(totalBorrowed), new Color(0, 123, 255), "Tổng số sách đang được mượn"));
            cardsPanel.add(createStatCard("⚠️ Quá hạn", String.valueOf(overdueBooks), new Color(220, 53, 69), "Số sách đã quá hạn trả"));
            cardsPanel.add(createStatCard("🔔 Sắp hết hạn", String.valueOf(dueSoonBooks), new Color(255, 193, 7), "Sách sắp hết hạn trong 3 ngày"));
            cardsPanel.add(createStatCard("👥 Người mượn", String.valueOf(activeBorrowers), new Color(40, 167, 69), "Số người đang mượn sách"));
            
        } catch (Exception e) {
            cardsPanel.add(createStatCard("❌ Lỗi", "N/A", new Color(220, 53, 69), "Không thể tải thống kê"));
        }
        
        return cardsPanel;
    }
    
    /**
     * Tạo một thẻ thống kê hiện đại với hiệu ứng hover
     */
    private JPanel createStatCard(String title, String value, Color color, String tooltip) {
        JPanel card = new JPanel(new BorderLayout()) {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Shadow effect
                if (isHovered) {
                    g2d.setColor(new Color(0, 0, 0, 30));
                    g2d.fillRoundRect(4, 4, getWidth()-4, getHeight()-4, 18, 18);
                }
                
                // Main card background with sophisticated gradient
                Color startColor = Color.WHITE;
                Color midColor = new Color(255, 255, 255, 250);
                Color endColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 25);
                
                GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, 15, 15);
                
                // Modern border with glow effect
                g2d.setColor(isHovered ? color.brighter() : color);
                g2d.setStroke(new BasicStroke(isHovered ? 3 : 2));
                g2d.drawRoundRect(1, 1, getWidth()-6, getHeight()-6, 15, 15);
                
                // Subtle inner highlight
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(3, 3, getWidth()-10, getHeight()-10, 12, 12);
            }
        };
        
        // Add hover effects
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                ((JPanel)e.getSource()).putClientProperty("isHovered", true);
                card.repaint();
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                ((JPanel)e.getSource()).putClientProperty("isHovered", false);
                card.repaint();
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setToolTipText(tooltip);
        card.setPreferredSize(new Dimension(250, 140));
        
        // Modern title label with icon
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(108, 117, 125));
        
        // Enhanced value label with modern typography
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        
        // Add subtle animation effect to value
        javax.swing.Timer timer = new javax.swing.Timer(50, null);
        timer.addActionListener(e -> {
            if (card.getClientProperty("isHovered") == Boolean.TRUE) {
                valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
            } else {
                valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            }
        });
        timer.start();
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Tạo panel biểu đồ hiện đại với styling đẹp
     */
    private JPanel createChartsPanel() {
        JPanel chartsPanel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern card background
                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), 
                                                          new Color(248, 249, 250));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Subtle border
                g2d.setColor(new Color(233, 236, 239));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
            }
        };
        chartsPanel.setOpaque(false);
        chartsPanel.setPreferredSize(new Dimension(580, 450));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Modern chart title with icon and styling
        JPanel titlePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Title background gradient
                GradientPaint titleGradient = new GradientPaint(0, 0, new Color(70, 130, 180, 20), 
                                                               0, getHeight(), new Color(70, 130, 180, 5));
                g2d.setPaint(titleGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel chartTitle = new JLabel("📈 THỐNG KÊ MƯỢN SÁCH THEO THÁNG", SwingConstants.CENTER);
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chartTitle.setForeground(new Color(70, 130, 180));
        titlePanel.add(chartTitle, BorderLayout.CENTER);
        
        // Enhanced chart area with modern styling
        JTextArea chartArea = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Chart background
                g2d.setColor(new Color(252, 253, 254));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                super.paintComponent(g);
            }
        };
        chartArea.setEditable(false);
        chartArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        chartArea.setForeground(new Color(33, 37, 41));
        chartArea.setOpaque(false);
        chartArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            StringBuilder chartData = new StringBuilder();
            chartData.append("╭─────────────────────────────────────────────────────────────────╮\n");
            chartData.append("│                    📊 BIỂU ĐỒ MƯỢN SÁCH THEO THÁNG               │\n");
            chartData.append("│                      (6 tháng gần nhất)                         │\n");
            chartData.append("╰─────────────────────────────────────────────────────────────────╯\n\n");
            
            PreparedStatement ps = conn.prepareStatement(
                "SELECT strftime('%Y-%m', borrow_date) as month, COUNT(*) as count " +
                "FROM borrows WHERE borrow_date >= date('now', '-6 months') " +
                "GROUP BY strftime('%Y-%m', borrow_date) ORDER BY month DESC");
            
            ResultSet rs = ps.executeQuery();
            
            // Find max count for scaling
            int maxCount = 0;
            java.util.List<String[]> chartDataList = new java.util.ArrayList<String[]>();
            while (rs.next()) {
                String month = rs.getString("month");
                int count = rs.getInt("count");
                maxCount = Math.max(maxCount, count);
                chartDataList.add(new String[]{month, String.valueOf(count)});
            }
            
            // Create the chart
            chartData.append("Tháng     │ Biểu đồ                                    │ Số lượng\n");
            chartData.append("──────────┼────────────────────────────────────────────┼─────────\n");
            
            for (String[] data : chartDataList) {
                String month = data[0];
                int count = Integer.parseInt(data[1]);
                int barLen = maxCount > 0 ? (count * 40) / maxCount : 0; // Scale to 40 chars max
                if (barLen < 1 && count > 0) barLen = 1; // Minimum 1 char for non-zero values
                
                StringBuilder barSb = new StringBuilder(40);
                // Different colors/patterns for different ranges
                char barChar;
                if (count >= maxCount * 0.8) barChar = '█';      // High
                else if (count >= maxCount * 0.5) barChar = '▓'; // Medium
                else if (count >= maxCount * 0.2) barChar = '▒'; // Low
                else barChar = '░';                              // Very low
                
                for (int i = 0; i < barLen; i++) {
                    barSb.append(barChar);
                }
                for (int i = barLen; i < 40; i++) {
                    barSb.append(' ');
                }
                
                chartData.append(String.format("%-9s │ %s │ %6d\n", month, barSb.toString(), count));
            }
            
            chartData.append("──────────┴────────────────────────────────────────────┴─────────\n");
            chartData.append(String.format("Tổng cộng: %d lượt mượn • Trung bình: %.1f lượt/tháng\n", 
                chartDataList.stream().mapToInt(d -> Integer.parseInt(d[1])).sum(),
                chartDataList.stream().mapToInt(d -> Integer.parseInt(d[1])).average().orElse(0.0)));
            
            chartArea.setText(chartData.toString());
            
        } catch (Exception e) {
            chartArea.setText("❌ Không thể tải dữ liệu biểu đồ\n\nLỗi: " + e.getMessage() + 
                "\n\nVui lòng kiểm tra kết nối cơ sở dữ liệu.");
        }
        
        JScrollPane chartScroll = new JScrollPane(chartArea);
        chartScroll.setBorder(BorderFactory.createEmptyBorder());
        chartScroll.setOpaque(false);
        chartScroll.getViewport().setOpaque(false);
        chartScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chartScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        chartsPanel.add(titlePanel, BorderLayout.NORTH);
        chartsPanel.add(chartScroll, BorderLayout.CENTER);
        
        return chartsPanel;
    }
    
    /**
     * Tạo panel hoạt động gần đây với styling hiện đại
     */
    private JPanel createRecentActivitiesPanel() {
        JPanel activitiesPanel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern card background
                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), 
                                                          new Color(248, 249, 250));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Subtle border
                g2d.setColor(new Color(233, 236, 239));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
            }
        };
        activitiesPanel.setOpaque(false);
        activitiesPanel.setPreferredSize(new Dimension(480, 450));
        activitiesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Modern title panel
        JPanel titlePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Title background gradient
                GradientPaint titleGradient = new GradientPaint(0, 0, new Color(40, 167, 69, 20), 
                                                               0, getHeight(), new Color(40, 167, 69, 5));
                g2d.setPaint(titleGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel activityTitle = new JLabel("🕒 HOẠT ĐỘNG GẦN ĐÂY", SwingConstants.CENTER);
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        activityTitle.setForeground(new Color(40, 167, 69));
        titlePanel.add(activityTitle, BorderLayout.CENTER);
        
    DefaultListModel<String> listModel = new DefaultListModel<String>();
    JList<String> activityList = new JList<String>(listModel) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // List background
                g2d.setColor(new Color(252, 253, 254));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                super.paintComponent(g);
            }
        };
        activityList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        activityList.setOpaque(false);
        activityList.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        activityList.setSelectionBackground(new Color(70, 130, 180, 50));
        activityList.setSelectionForeground(new Color(33, 37, 41));
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT u.username, b.title, br.borrow_date, br.return_date " +
                "FROM borrows br " +
                "JOIN users u ON br.user_id = u.id " +
                "JOIN books b ON br.book_id = b.id " +
                "ORDER BY br.borrow_date DESC LIMIT 15");
            
            ResultSet rs = ps.executeQuery();
            
            if (!rs.next()) {
                listModel.addElement("📋 Chưa có hoạt động nào gần đây");
            } else {
                do {
                    String username = rs.getString("username");
                    String bookTitle = rs.getString("title");
                    String borrowDate = rs.getString("borrow_date");
                    String returnDate = rs.getString("return_date");
                    
                    String activity;
                    String shortTitle = bookTitle.length() > 25 ? bookTitle.substring(0, 25) + "..." : bookTitle;
                    String shortUser = username.length() > 15 ? username.substring(0, 15) + "..." : username;
                    
                    if (returnDate != null && !returnDate.isEmpty() && !returnDate.equals("null")) {
                        activity = String.format("✅ %-15s trả 📖 \"%s\"", shortUser, shortTitle);
                        listModel.addElement(activity);
                        listModel.addElement(String.format("    📅 Ngày trả: %s", returnDate.substring(0, 10)));
                    } else {
                        activity = String.format("📚 %-15s mượn 📖 \"%s\"", shortUser, shortTitle);
                        listModel.addElement(activity);
                        listModel.addElement(String.format("    📅 Ngày mượn: %s", borrowDate.substring(0, 10)));
                    }
                    listModel.addElement("─────────────────────────────────────────────");
                } while (rs.next());
            }
            
        } catch (Exception e) {
            listModel.addElement("❌ Không thể tải hoạt động");
            listModel.addElement("📋 Lỗi: " + e.getMessage());
        }
        
        JScrollPane activityScroll = new JScrollPane(activityList);
        activityScroll.setBorder(BorderFactory.createEmptyBorder());
        activityScroll.setOpaque(false);
        activityScroll.getViewport().setOpaque(false);
        activityScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        activityScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        activitiesPanel.add(titlePanel, BorderLayout.NORTH);
        activitiesPanel.add(activityScroll, BorderLayout.CENTER);
        
        return activitiesPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BorrowManagementUI().setVisible(true);
        });
    }
}