package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BorrowRequestManagerUI extends JPanel {
    private JTable requestsTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbStatus;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    private static final String[] STATUS_OPTIONS = {"Tất cả", "PENDING", "APPROVED", "REJECTED"};
    
    public BorrowRequestManagerUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        connectToServer();
        createComponents();
        loadBorrowRequests();
    }
    
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            in.readLine(); // Read welcome message
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server: " + e.getMessage());
        }
    }
    
    private void createComponents() {
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 15));
        headerPanel.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("📋 Quản lý đăng ký mượn sách");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 123, 255));
        
        // Search and filter panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        searchPanel.setOpaque(false);
        
        // Search field
        JLabel searchLabel = new JLabel("🔍 Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Status filter
        JLabel statusLabel = new JLabel("📊 Trạng thái:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        cbStatus = new JComboBox<>(STATUS_OPTIONS);
        cbStatus.setPreferredSize(new Dimension(120, 35));
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbStatus.setBackground(Color.WHITE);
        
        // Search button
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 35));
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> loadBorrowRequests());
        
        // Refresh button
        JButton btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.setPreferredSize(new Dimension(120, 35));
        btnRefresh.setBackground(new Color(40, 167, 69));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadBorrowRequests());
        
        searchPanel.add(searchLabel);
        searchPanel.add(txtSearch);
        searchPanel.add(statusLabel);
        searchPanel.add(cbStatus);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        
        // Create table model
        String[] columnNames = {
            "ID", "👤 Người dùng", "📖 Tên sách", "✍️ Tác giả", 
            "📅 Ngày đăng ký", "📊 Trạng thái", "📝 Ghi chú"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        requestsTable = new JTable(tableModel);
        requestsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        requestsTable.setRowHeight(45);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.setSelectionBackground(new Color(0, 123, 255, 50));
        requestsTable.setSelectionForeground(new Color(0, 123, 255));
        requestsTable.setGridColor(new Color(222, 226, 230));
        requestsTable.setShowGrid(true);
        
        // Style table header
        JTableHeader header = requestsTable.getTableHeader();
        header.setBackground(new Color(0, 123, 255));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 50));
        
        // Set column widths
        requestsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        requestsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        requestsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        requestsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        requestsTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        requestsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        requestsTable.getColumnModel().getColumn(6).setPreferredWidth(200);
        
        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setOpaque(false);
        
        // Approve button
        JButton btnApprove = new JButton("✅ Duyệt đăng ký");
        btnApprove.setPreferredSize(new Dimension(150, 40));
        btnApprove.setBackground(new Color(40, 167, 69));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnApprove.setFocusPainted(false);
        btnApprove.addActionListener(e -> approveRequest());
        
        // Reject button
        JButton btnReject = new JButton("❌ Từ chối");
        btnReject.setPreferredSize(new Dimension(120, 40));
        btnReject.setBackground(new Color(220, 53, 69));
        btnReject.setForeground(Color.WHITE);
        btnReject.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReject.setFocusPainted(false);
        btnReject.addActionListener(e -> rejectRequest());
        
        // View details button
        JButton btnDetails = new JButton("📄 Chi tiết");
        btnDetails.setPreferredSize(new Dimension(120, 40));
        btnDetails.setBackground(new Color(255, 193, 7));
        btnDetails.setForeground(Color.WHITE);
        btnDetails.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDetails.setFocusPainted(false);
        btnDetails.addActionListener(e -> viewRequestDetails());
        
        buttonPanel.add(btnDetails);
        buttonPanel.add(btnApprove);
        buttonPanel.add(btnReject);
        
        return buttonPanel;
    }
    
    private void loadBorrowRequests() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            StringBuilder query = new StringBuilder(
                "SELECT br.id, u.username, b.title, b.author, br.request_date, br.status, br.admin_notes " +
                "FROM borrow_requests br " +
                "JOIN users u ON br.user_id = u.id " +
                "JOIN books b ON br.book_id = b.id " +
                "WHERE 1=1"
            );
            
            // Add search filter
            String searchText = txtSearch.getText().trim();
            if (!searchText.isEmpty()) {
                query.append(" AND (u.username LIKE ? OR b.title LIKE ? OR b.author LIKE ?)");
            }
            
            // Add status filter
            String statusFilter = cbStatus.getSelectedItem().toString();
            if (!"Tất cả".equals(statusFilter)) {
                query.append(" AND br.status = ?");
            }
            
            query.append(" ORDER BY br.request_date DESC");
            
            PreparedStatement ps = conn.prepareStatement(query.toString());
            int paramIndex = 1;
            
            if (!searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
            }
            
            if (!"Tất cả".equals(statusFilter)) {
                ps.setString(paramIndex++, statusFilter);
            }
            
            ResultSet rs = ps.executeQuery();
            
            // Clear existing data
            tableModel.setRowCount(0);
            
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
                    rs.getString("username"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("request_date"),
                    statusDisplay,
                    rs.getString("admin_notes") != null ? rs.getString("admin_notes") : ""
                };
                tableModel.addRow(row);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }
    
    private void approveRequest() {
        int selectedRows = requestsTable.getSelectedRow();
        if (selectedRows == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đăng ký để duyệt!");
            return;
        }
        
        int requestId = (Integer) tableModel.getValueAt(selectedRows, 0);
        String username = (String) tableModel.getValueAt(selectedRows, 1);
        String bookTitle = (String) tableModel.getValueAt(selectedRows, 2);
        
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn duyệt đăng ký mượn sách:\n" +
            "Người dùng: " + username + "\n" +
            "Sách: " + bookTitle,
            "Xác nhận duyệt",
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            String notes = JOptionPane.showInputDialog(this, 
                "Ghi chú (tùy chọn):", 
                "Ghi chú duyệt đăng ký", 
                JOptionPane.PLAIN_MESSAGE);
                
            updateRequestStatus(requestId, "APPROVED", notes != null ? notes : "Đã được duyệt");
        }
    }
    
    private void rejectRequest() {
        int selectedRows = requestsTable.getSelectedRow();
        if (selectedRows == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đăng ký để từ chối!");
            return;
        }
        
        int requestId = (Integer) tableModel.getValueAt(selectedRows, 0);
        String username = (String) tableModel.getValueAt(selectedRows, 1);
        String bookTitle = (String) tableModel.getValueAt(selectedRows, 2);
        
        String reason = JOptionPane.showInputDialog(this,
            "Lý do từ chối đăng ký của " + username + " cho sách '" + bookTitle + "':",
            "Lý do từ chối",
            JOptionPane.PLAIN_MESSAGE);
            
        if (reason != null && !reason.trim().isEmpty()) {
            updateRequestStatus(requestId, "REJECTED", reason);
        }
    }
    
    private void updateRequestStatus(int requestId, String status, String notes) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            String updateQuery = "UPDATE borrow_requests " +
                "SET status = ?, admin_notes = ?, approved_date = ? " +
                "WHERE id = ?";
            
            PreparedStatement ps = conn.prepareStatement(updateQuery);
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setString(3, LocalDateTime.now().toString());
            ps.setInt(4, requestId);
            
            int rowsUpdated = ps.executeUpdate();
            
            if (rowsUpdated > 0) {
                // If approved, actually create the borrow record
                if ("APPROVED".equals(status)) {
                    createBorrowRecord(requestId);
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Đã cập nhật trạng thái đăng ký thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                loadBorrowRequests(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái!");
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + e.getMessage());
        }
    }
    
    private void createBorrowRecord(int requestId) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            // Get request details
            String getRequestQuery = "SELECT user_id, book_id FROM borrow_requests WHERE id = ?";
            PreparedStatement ps1 = conn.prepareStatement(getRequestQuery);
            ps1.setInt(1, requestId);
            ResultSet rs = ps1.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                int bookId = rs.getInt("book_id");
                
                // Create actual borrow record (only use columns that exist in borrows table)
                String insertBorrowQuery = "INSERT INTO borrows (user_id, book_id, borrow_date) " +
                    "VALUES (?, ?, ?)";
                
                PreparedStatement ps2 = conn.prepareStatement(insertBorrowQuery);
                ps2.setInt(1, userId);
                ps2.setInt(2, bookId);
                ps2.setString(3, LocalDateTime.now().toString());
                ps2.executeUpdate();
                
                // Update book quantity
                String updateBookQuery = "UPDATE books SET quantity = quantity - 1 WHERE id = ? AND quantity > 0";
                PreparedStatement ps3 = conn.prepareStatement(updateBookQuery);
                ps3.setInt(1, bookId);
                ps3.executeUpdate();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo bản ghi mượn sách: " + e.getMessage());
        }
    }
    
    private void viewRequestDetails() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đăng ký để xem chi tiết!");
            return;
        }
        
        int requestId = (Integer) tableModel.getValueAt(selectedRow, 0);
        showRequestDetailsDialog(requestId);
    }
    
    private void showRequestDetailsDialog(int requestId) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            String query = "SELECT br.*, u.username, u.email, b.title, b.author, b.category, b.quantity " +
                "FROM borrow_requests br " +
                "JOIN users u ON br.user_id = u.id " +
                "JOIN books b ON br.book_id = b.id " +
                "WHERE br.id = ?";
            
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                    "Chi tiết đăng ký mượn sách", true);
                dialog.setSize(600, 500);
                dialog.setLocationRelativeTo(this);
                
                JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
                mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                mainPanel.setBackground(new Color(248, 249, 250));
                
                // Header
                JLabel headerLabel = new JLabel("📋 Chi tiết đăng ký mượn sách", SwingConstants.CENTER);
                headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                headerLabel.setForeground(new Color(0, 123, 255));
                
                // Content
                JPanel contentPanel = new JPanel();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setOpaque(false);
                
                addDetailRow(contentPanel, "ID đăng ký:", String.valueOf(rs.getInt("id")));
                addDetailRow(contentPanel, "Người dùng:", rs.getString("username"));
                addDetailRow(contentPanel, "Email:", rs.getString("email"));
                addDetailRow(contentPanel, "Tên sách:", rs.getString("title"));
                addDetailRow(contentPanel, "Tác giả:", rs.getString("author"));
                addDetailRow(contentPanel, "Thể loại:", rs.getString("category"));
                addDetailRow(contentPanel, "Số lượng còn lại:", String.valueOf(rs.getInt("quantity")));
                addDetailRow(contentPanel, "Ngày đăng ký:", rs.getString("request_date"));
                addDetailRow(contentPanel, "Trạng thái:", rs.getString("status"));
                addDetailRow(contentPanel, "Ghi chú admin:", 
                    rs.getString("admin_notes") != null ? rs.getString("admin_notes") : "Chưa có");
                
                // Close button
                JButton closeBtn = new JButton("Đóng");
                closeBtn.setPreferredSize(new Dimension(100, 40));
                closeBtn.setBackground(new Color(108, 117, 125));
                closeBtn.setForeground(Color.WHITE);
                closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                closeBtn.setFocusPainted(false);
                closeBtn.addActionListener(e -> dialog.dispose());
                
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setOpaque(false);
                buttonPanel.add(closeBtn);
                
                mainPanel.add(headerLabel, BorderLayout.NORTH);
                mainPanel.add(contentPanel, BorderLayout.CENTER);
                mainPanel.add(buttonPanel, BorderLayout.SOUTH);
                
                dialog.add(mainPanel);
                dialog.setVisible(true);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết: " + e.getMessage());
        }
    }
    
    private void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        row.setOpaque(false);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLabel.setPreferredSize(new Dimension(150, 25));
        lblLabel.setForeground(new Color(52, 58, 64));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblValue.setForeground(new Color(73, 80, 87));
        
        row.add(lblLabel);
        row.add(lblValue);
        
        parent.add(row);
    }
}