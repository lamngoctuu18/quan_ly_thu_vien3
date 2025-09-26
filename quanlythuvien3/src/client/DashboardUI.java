package client;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DashboardUI extends JPanel {
    private JPanel lblTotalBooks, lblTotalUsers, lblActiveBorrows, lblOverdueBooks;
    private JPanel borrowChart;
    private JPanel categoryChart;

    public DashboardUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);

        // Charts panel
        JPanel chartsPanel = createChartsPanel();
        add(chartsPanel, BorderLayout.CENTER);

        loadData();
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create stat cards
        lblTotalBooks = createStatCard("Tổng số sách", "0", new Color(0, 102, 204));
        lblTotalUsers = createStatCard("Tổng người dùng", "0", new Color(0, 153, 76));
        lblActiveBorrows = createStatCard("Đang mượn", "0", new Color(255, 153, 51));
        lblOverdueBooks = createStatCard("Sách quá hạn", "0", new Color(204, 0, 0));

        // Add cards to panel
        panel.add(lblTotalBooks);
        panel.add(lblTotalUsers);
        panel.add(lblActiveBorrows);
        panel.add(lblOverdueBooks);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(color);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(new Color(51, 51, 51));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(lblValue);
        
        // Store the value label as a client property so we can update it later
        card.putClientProperty("valueLabel", lblValue);

        return card;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        // Borrow Trend Chart
        borrowChart = createSimpleBarChart("Thống kê mượn sách theo tháng");
        borrowChart.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Category Distribution Chart
        categoryChart = createSimplePieChart("Phân bố sách theo thể loại");
        categoryChart.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panel.add(borrowChart);
        panel.add(categoryChart);

        return panel;
    }

    private JPanel createSimpleBarChart(String title) {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw title
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString(title, 50, 30);

                // Draw axes
                g2.drawLine(50, getHeight() - 50, getWidth() - 50, getHeight() - 50); // X axis
                g2.drawLine(50, 50, 50, getHeight() - 50); // Y axis

                // Example data - replace with real data
                int[] data = {15, 25, 20, 30, 22, 28};
                String[] labels = {"T1", "T2", "T3", "T4", "T5", "T6"};
                int maxValue = 30;
                int barWidth = (getWidth() - 100) / data.length;

                // Draw bars
                for (int i = 0; i < data.length; i++) {
                    int height = (int)((data[i] * (getHeight() - 100)) / maxValue);
                    g2.setColor(new Color(0, 102, 204));
                    g2.fillRect(50 + i * barWidth, getHeight() - 50 - height, 
                              barWidth - 10, height);
                    g2.setColor(Color.BLACK);
                    g2.drawString(labels[i], 50 + i * barWidth + barWidth/2 - 10, 
                                getHeight() - 30);
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 300));
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

    private void loadData() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            // Load total books
            ResultSet rs = conn.createStatement().executeQuery("SELECT SUM(quantity) FROM books");
            ((JLabel)lblTotalBooks.getClientProperty("valueLabel")).setText(rs.getString(1));

            // Load total users
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users WHERE role != 'admin'");
            ((JLabel)lblTotalUsers.getClientProperty("valueLabel")).setText(rs.getString(1));

            // Load active borrows
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM borrows WHERE return_date IS NULL");
            ((JLabel)lblActiveBorrows.getClientProperty("valueLabel")).setText(rs.getString(1));

            // Load overdue books
            String today = LocalDate.now().toString();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM borrows WHERE return_date IS NULL AND julianday(?) - julianday(borrow_date) > 30"
            );
            ps.setString(1, today);
            rs = ps.executeQuery();
            ((JLabel)lblOverdueBooks.getClientProperty("valueLabel")).setText(rs.getString(1));

            // Update charts
            updateBorrowTrendChart();
            updateCategoryDistributionChart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBorrowTrendChart() {
        // Update with real data from database
    }

    private void updateCategoryDistributionChart() {
        // Update with real data from database
    }
}
