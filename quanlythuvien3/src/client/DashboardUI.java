package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardUI extends JPanel {
    private JPanel statsPanel;
    private JPanel chartsPanel;
    private JPanel widgetsPanel;
    private Map<String, Integer> realTimeStats;
    
    // Stat card components for real-time updates
    private JLabel lblBooksValue, lblUsersValue, lblBorrowsValue, lblOverdueValue;
    
    // Chart data
    private List<Integer> monthlyBorrowData;
    private Map<String, Integer> categoryData;
    private List<TopBook> topBooks;
    private List<ActiveUser> activeUsers;
    
    // Modern color palette
    private final Color PRIMARY_BLUE = new Color(52, 152, 219);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color WARNING_ORANGE = new Color(241, 196, 15);
    private final Color DANGER_RED = new Color(231, 76, 60);
    private final Color PURPLE = new Color(155, 89, 182);
    private final Color DARK_GRAY = new Color(52, 73, 94);
    private final Color LIGHT_GRAY = new Color(236, 240, 241);

    // Inner classes for data structures
    private static class TopBook {
        String title, author;
        int borrowCount;
        TopBook(String title, String author, int borrowCount) {
            this.title = title;
            this.author = author;
            this.borrowCount = borrowCount;
        }
    }
    
    private static class ActiveUser {
        String username;
        int activityCount;
        ActiveUser(String username, int activityCount) {
            this.username = username;
            this.activityCount = activityCount;
        }
    }

    public DashboardUI() {
        initializeData();
        initializeComponents();
        loadRealTimeData();
        
        // Auto-refresh data every 30 seconds
        Timer refreshTimer = new Timer(30000, e -> loadRealTimeData());
        refreshTimer.start();
    }
    
    private void initializeData() {
        realTimeStats = new HashMap<>();
        monthlyBorrowData = new ArrayList<>();
        categoryData = new HashMap<>();
        topBooks = new ArrayList<>();
        activeUsers = new ArrayList<>();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Create main sections
        createStatsSection();
        createChartsSection();
        createWidgetsSection();
        
        // Layout sections
        JPanel topSection = new JPanel(new BorderLayout(0, 20));
        topSection.setOpaque(false);
        topSection.add(statsPanel, BorderLayout.NORTH);
        topSection.add(chartsPanel, BorderLayout.CENTER);
        
        add(topSection, BorderLayout.CENTER);
        add(widgetsPanel, BorderLayout.SOUTH);
    }

    private void createStatsSection() {
        statsPanel = new JPanel(new GridLayout(1, 4, 25, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Create modern stat cards with shadows and gradients
        JPanel booksCard = createModernStatCard("Tổng số sách", "0", "quyển", PRIMARY_BLUE, "📚");
        JPanel usersCard = createModernStatCard("Người dùng", "0", "thành viên", SUCCESS_GREEN, "👥");
        JPanel borrowsCard = createModernStatCard("Đang mượn", "0", "cuốn sách", WARNING_ORANGE, "📖");
        JPanel overdueCard = createModernStatCard("Quá hạn", "0", "cuốn sách", DANGER_RED, "⚠️");

        // Store value labels for updates
        lblBooksValue = (JLabel) booksCard.getClientProperty("valueLabel");
        lblUsersValue = (JLabel) usersCard.getClientProperty("valueLabel");
        lblBorrowsValue = (JLabel) borrowsCard.getClientProperty("valueLabel");
        lblOverdueValue = (JLabel) overdueCard.getClientProperty("valueLabel");

        statsPanel.add(booksCard);
        statsPanel.add(usersCard);
        statsPanel.add(borrowsCard);
        statsPanel.add(overdueCard);
    }

    private JPanel createModernStatCard(String title, String value, String unit, Color color, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 12, 12);
                
                // Draw card background with gradient
                GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(250, 250, 250));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 12, 12);
                
                // Draw colored left border
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, 4, getHeight()-2, 12, 12);
            }
        };
        
        card.setLayout(new BorderLayout(15, 10));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(280, 120));
        
        // Icon section
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setPreferredSize(new Dimension(50, 50));
        
        // Content section
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(new Color(33, 37, 41));
        
        JLabel unitLabel = new JLabel(unit);
        unitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        unitLabel.setForeground(new Color(134, 142, 150));
        
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(valueLabel, BorderLayout.CENTER);
        contentPanel.add(unitLabel, BorderLayout.SOUTH);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Store value label for updates
        card.putClientProperty("valueLabel", valueLabel);
        
        // Add hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return card;
    }

    private void createChartsSection() {
        chartsPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        chartsPanel.setOpaque(false);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Modern bar chart with gradient
        JPanel borrowChart = createModernBarChart();
        
        // Modern pie chart with hover effects
        JPanel categoryChart = createModernPieChart();
        
        chartsPanel.add(borrowChart);
        chartsPanel.add(categoryChart);

    }

    private JPanel createModernBarChart() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw card background
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 12, 12);

                // Chart title
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2d.setColor(DARK_GRAY);
                g2d.drawString("Thống kê mượn sách theo tháng", 25, 35);

                // Draw chart
                int chartX = 40, chartY = 60, chartWidth = getWidth() - 80, chartHeight = getHeight() - 120;
                
                // Axes
                g2d.setColor(LIGHT_GRAY);
                g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight);
                g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight);

                // Sample data (replace with real data)
                int[] data = {15, 25, 20, 30, 22, 28};
                String[] months = {"T7", "T8", "T9", "T10", "T11", "T12"};
                int maxValue = 35;
                int barWidth = chartWidth / data.length - 10;

                for (int i = 0; i < data.length; i++) {
                    int barHeight = (int)((data[i] * chartHeight) / maxValue);
                    int x = chartX + i * (chartWidth / data.length) + 5;
                    int y = chartY + chartHeight - barHeight;
                    
                    // Gradient bar
                    GradientPaint gradient = new GradientPaint(x, y, PRIMARY_BLUE.brighter(), 
                                                             x, y + barHeight, PRIMARY_BLUE);
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(x, y, barWidth, barHeight, 4, 4);
                    
                    // Value on top of bar
                    g2d.setColor(DARK_GRAY);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String valueStr = String.valueOf(data[i]);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = x + (barWidth - fm.stringWidth(valueStr)) / 2;
                    g2d.drawString(valueStr, textX, y - 5);
                    
                    // Month label
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    fm = g2d.getFontMetrics();
                    textX = x + (barWidth - fm.stringWidth(months[i])) / 2;
                    g2d.drawString(months[i], textX, chartY + chartHeight + 15);
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 320));
        return chartPanel;
    }

    private JPanel createSimplePieChart(String title) {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw title
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString(title, 50, 30);

                // Example data - replace with real data
                Map<String, Double> data = new HashMap<>();
                data.put("Văn học", 25.0);
                data.put("Khoa học", 20.0);
                data.put("Kinh tế", 15.0);

                double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
                double currentAngle = 0;
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = Math.min(getWidth(), getHeight()) / 3;

                // Draw pie segments
                int y = 60;
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    double angle = 360 * (entry.getValue() / total);
                    g2.setColor(getRandomColor());
                    g2.fillArc(centerX - radius, centerY - radius, 
                              radius * 2, radius * 2, 
                              (int) currentAngle, (int) angle);
                    
                    // Draw legend
                    g2.fillRect(50, y, 10, 10);
                    g2.setColor(Color.BLACK);
                    g2.drawString(entry.getKey() + ": " + entry.getValue() + "%", 
                                70, y + 10);
                    
                    currentAngle += angle;
                    y += 20;
                }
            }

            private Color getRandomColor() {
                return new Color(
                    (int)(Math.random() * 128) + 128,
                    (int)(Math.random() * 128) + 128,
                    (int)(Math.random() * 128) + 128
                );
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 300));
        return chartPanel;
    }

    private void createWidgetsSection() {
        widgetsPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        widgetsPanel.setOpaque(false);
        widgetsPanel.setPreferredSize(new Dimension(0, 280));

        // Top borrowed books widget
        JPanel topBooksWidget = createTopBooksWidget();
        
        // Most active users widget
        JPanel activeUsersWidget = createActiveUsersWidget();
        
        // Quick notifications widget
        JPanel notificationsWidget = createNotificationsWidget();

        widgetsPanel.add(topBooksWidget);
        widgetsPanel.add(activeUsersWidget);
        widgetsPanel.add(notificationsWidget);
    }

    private JPanel createTopBooksWidget() {
        JPanel widget = createWidgetCard(" Top 5 sách mượn nhiều nhất", SUCCESS_GREEN);
        JPanel contentPanel = (JPanel) widget.getClientProperty("contentPanel");
        
        // Load real data
        loadTopBooksData(contentPanel);
        
        return widget;
    }

    private JPanel createActiveUsersWidget() {
        JPanel widget = createWidgetCard(" Người dùng hoạt động nhiều nhất", PRIMARY_BLUE);
        JPanel contentPanel = (JPanel) widget.getClientProperty("contentPanel");
        
        // Load real data
        loadActiveUsersData(contentPanel);
        
        return widget;
    }

    private JPanel createNotificationsWidget() {
        JPanel widget = createWidgetCard(" Thông báo nhanh", WARNING_ORANGE);
        JPanel contentPanel = (JPanel) widget.getClientProperty("contentPanel");
        
        // Load real notifications
        loadNotificationsData(contentPanel);
        
        return widget;
    }

    private JPanel createWidgetCard(String title, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 12, 12);
                
                // Draw card background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 12, 12);
                
                // Draw accent top border
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, getWidth()-2, 4, 12, 12);
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(DARK_GRAY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Store content panel for adding items
        card.putClientProperty("contentPanel", contentPanel);
        
        return card;
    }

    private void loadRealTimeData() {
        SwingUtilities.invokeLater(() -> {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
                // Load total books
                PreparedStatement ps = conn.prepareStatement("SELECT SUM(quantity) FROM books");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    lblBooksValue.setText(String.valueOf(rs.getInt(1)));
                }

                // Load total users (excluding admin)
                ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role != 'admin'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    lblUsersValue.setText(String.valueOf(rs.getInt(1)));
                }

                // Load active borrows
                ps = conn.prepareStatement("SELECT COUNT(*) FROM borrows WHERE return_date IS NULL OR return_date = ''");
                rs = ps.executeQuery();
                if (rs.next()) {
                    lblBorrowsValue.setText(String.valueOf(rs.getInt(1)));
                }

                // Load overdue books (more than 30 days)
                String today = LocalDate.now().toString();
                ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM borrows WHERE (return_date IS NULL OR return_date = '') " +
                    "AND julianday(?) - julianday(borrow_date) > 30"
                );
                ps.setString(1, today);
                rs = ps.executeQuery();
                if (rs.next()) {
                    lblOverdueValue.setText(String.valueOf(rs.getInt(1)));
                }
                
                // Refresh charts
                repaint();
                
            } catch (Exception e) {
                e.printStackTrace();
                // Set default values on error
                lblBooksValue.setText("0");
                lblUsersValue.setText("0");
                lblBorrowsValue.setText("0");
                lblOverdueValue.setText("0");
            }
        });
    }

    private void loadTopBooksData(JPanel contentPanel) {
        contentPanel.removeAll();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT b.title, b.author, COUNT(*) as borrow_count " +
                "FROM borrows br JOIN books b ON br.book_id = b.id " +
                "GROUP BY b.id ORDER BY borrow_count DESC LIMIT 5"
            );
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next() && rank <= 5) {
                JPanel bookItem = createBookItem(rank, rs.getString(1), 
                                               rs.getString(2), rs.getInt(3) + " lượt");
                contentPanel.add(bookItem);
                if (rank < 5) contentPanel.add(Box.createVerticalStrut(10));
                rank++;
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Không có dữ liệu");
            errorLabel.setForeground(new Color(108, 117, 125));
            contentPanel.add(errorLabel);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadActiveUsersData(JPanel contentPanel) {
        contentPanel.removeAll();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT u.username, COUNT(*) as activity_count " +
                "FROM users u JOIN borrows b ON u.id = b.user_id " +
                "WHERE u.role != 'admin' " +
                "GROUP BY u.id ORDER BY activity_count DESC LIMIT 5"
            );
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next() && rank <= 5) {
                JPanel userItem = createUserItem(rank, rs.getString(1), 
                                               rs.getInt(2) + " hoạt động");
                contentPanel.add(userItem);
                if (rank < 5) contentPanel.add(Box.createVerticalStrut(10));
                rank++;
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Không có dữ liệu");
            errorLabel.setForeground(new Color(108, 117, 125));
            contentPanel.add(errorLabel);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadNotificationsData(JPanel contentPanel) {
        contentPanel.removeAll();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            List<String> notifications = new ArrayList<>();
            List<Color> colors = new ArrayList<>();
            
            // Check overdue books
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM borrows WHERE (return_date IS NULL OR return_date = '') " +
                "AND julianday('now') - julianday(borrow_date) > 30"
            );
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                notifications.add(rs.getInt(1) + " sách quá hạn cần thu hồi");
                colors.add(DANGER_RED);
            }
            
            // Check pending requests
            ps = conn.prepareStatement("SELECT COUNT(*) FROM borrow_requests WHERE status = 'PENDING'");
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                notifications.add(rs.getInt(1) + " yêu cầu mượn sách mới");
                colors.add(PRIMARY_BLUE);
            }
            
            // Check low stock books
            ps = conn.prepareStatement("SELECT COUNT(*) FROM books WHERE quantity < 3");
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                notifications.add(rs.getInt(1) + " sách sắp hết trong kho");
                colors.add(WARNING_ORANGE);
            }
            
            // Add sample notifications if none
            if (notifications.isEmpty()) {
                notifications.add("Hệ thống hoạt động bình thường");
                colors.add(SUCCESS_GREEN);
                notifications.add("Backup dữ liệu thành công");
                colors.add(DARK_GRAY);
            }
            
            // Display notifications
            for (int i = 0; i < Math.min(notifications.size(), 5); i++) {
                JPanel notifItem = createNotificationItem(notifications.get(i), colors.get(i));
                contentPanel.add(notifItem);
                if (i < Math.min(notifications.size(), 5) - 1) {
                    contentPanel.add(Box.createVerticalStrut(8));
                }
            }
            
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Không thể tải thông báo");
            errorLabel.setForeground(new Color(108, 117, 125));
            contentPanel.add(errorLabel);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createBookItem(int rank, String title, String author, String count) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JLabel rankLabel = new JLabel(String.valueOf(rank));
        rankLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rankLabel.setForeground(SUCCESS_GREEN);
        rankLabel.setPreferredSize(new Dimension(20, 20));
        
        JPanel bookInfo = new JPanel(new BorderLayout());
        bookInfo.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title.length() > 20 ? title.substring(0, 20) + "..." : title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(DARK_GRAY);
        
        JLabel authorLabel = new JLabel(author.length() > 15 ? author.substring(0, 15) + "..." : author);
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        authorLabel.setForeground(new Color(108, 117, 125));
        
        JLabel countLabel = new JLabel(count);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        countLabel.setForeground(SUCCESS_GREEN);
        
        bookInfo.add(titleLabel, BorderLayout.NORTH);
        bookInfo.add(authorLabel, BorderLayout.SOUTH);
        
        item.add(rankLabel, BorderLayout.WEST);
        item.add(bookInfo, BorderLayout.CENTER);
        item.add(countLabel, BorderLayout.EAST);
        
        return item;
    }

    private JPanel createUserItem(int rank, String username, String activity) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel rankLabel = new JLabel(String.valueOf(rank));
        rankLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rankLabel.setForeground(PRIMARY_BLUE);
        rankLabel.setPreferredSize(new Dimension(20, 20));
        
        JLabel userLabel = new JLabel(username);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(DARK_GRAY);
        
        JLabel activityLabel = new JLabel(activity);
        activityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        activityLabel.setForeground(PRIMARY_BLUE);
        
        item.add(rankLabel, BorderLayout.WEST);
        item.add(userLabel, BorderLayout.CENTER);
        item.add(activityLabel, BorderLayout.EAST);
        
        return item;
    }

    private JPanel createNotificationItem(String message, Color color) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JPanel indicator = new JPanel();
        indicator.setBackground(color);
        indicator.setPreferredSize(new Dimension(4, 20));
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        messageLabel.setForeground(DARK_GRAY);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        
        item.add(indicator, BorderLayout.WEST);
        item.add(messageLabel, BorderLayout.CENTER);
        
        return item;
    }

    private JPanel createModernPieChart() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw card background
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth()-2, getHeight()-2, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 12, 12);

                // Chart title
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2d.setColor(DARK_GRAY);
                g2d.drawString("Phân bố sách theo thể loại", 25, 35);

                // Sample data (replace with real data)
                String[] categories = {"Văn học", "Khoa học", "Kinh tế", "Tâm lý", "Khác"};
                double[] values = {30, 25, 20, 15, 10};
                Color[] colors = {PRIMARY_BLUE, SUCCESS_GREEN, WARNING_ORANGE, PURPLE, DANGER_RED};
                
                double total = 0;
                for (double v : values) total += v;
                
                int centerX = getWidth() / 2 - 50;
                int centerY = getHeight() / 2 + 10;
                int radius = 80;
                double currentAngle = 0;

                // Draw pie slices
                for (int i = 0; i < categories.length; i++) {
                    double angle = 360 * (values[i] / total);
                    g2d.setColor(colors[i]);
                    g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                              (int) currentAngle, (int) angle);
                              
                    // Draw percentage labels
                    double midAngle = Math.toRadians(currentAngle + angle / 2);
                    int labelX = (int) (centerX + Math.cos(midAngle) * radius * 0.7);
                    int labelY = (int) (centerY + Math.sin(midAngle) * radius * 0.7);
                    
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String percentage = String.format("%.0f%%", values[i]);
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(percentage, labelX - fm.stringWidth(percentage) / 2, labelY);
                    
                    currentAngle += angle;
                }
                
                // Legend
                int legendX = centerX + radius + 30;
                int legendY = 80;
                for (int i = 0; i < categories.length; i++) {
                    g2d.setColor(colors[i]);
                    g2d.fillRoundRect(legendX, legendY + i * 25, 12, 12, 2, 2);
                    
                    g2d.setColor(DARK_GRAY);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.drawString(categories[i], legendX + 18, legendY + i * 25 + 10);
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 320));
        return chartPanel;
    }
}
