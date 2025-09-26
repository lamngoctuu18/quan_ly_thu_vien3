package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.net.URL;

public class BookManagerUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtTitle, txtAuthor, txtPublisher, txtYear, txtQuantity, txtCoverImage;
    private JTextArea txtDescription;
    private JComboBox<String> cbCategory;
    private JLabel lblCount, lblImagePreview;

    private static final String[] CATEGORIES = {
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

    public BookManagerUI() {
        setTitle("Quản lý sách");
        setMinimumSize(new Dimension(750, 450));
        setPreferredSize(new Dimension(950, 650));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panel quản lý sách (bảng)
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(232, 242, 255));
        JLabel lblTitle = new JLabel("Quản lý sách");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 102));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 0));
        listPanel.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"ID sách", "Ảnh bìa", "Tên sách", "Tác giả", "Nhà XB", "Năm XB", "Thể loại", "SL", "Thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 8; // Chỉ cột thao tác
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        // Renderer và Editor cho cột Thao tác
        table.getColumn("Thao tác").setCellRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
                JButton btnEdit = new JButton("Sửa");
                btnEdit.setBackground(new Color(255, 153, 51));
                btnEdit.setForeground(Color.WHITE);
                btnEdit.setPreferredSize(new Dimension(60, 22));
                btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                JButton btnDelete = new JButton("Xóa");
                btnDelete.setBackground(new Color(204, 0, 0));
                btnDelete.setForeground(Color.WHITE);
                btnDelete.setPreferredSize(new Dimension(60, 22));
                btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                panel.add(btnEdit);
                panel.add(btnDelete);
                return panel;
            }
        });
        
        table.getColumn("Thao tác").setCellEditor(new javax.swing.DefaultCellEditor(new JCheckBox()) {
            private JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
            private JButton btnEdit = new JButton("Sửa");
            private JButton btnDelete = new JButton("Xóa");
            {
                btnEdit.setBackground(new Color(255, 153, 51));
                btnEdit.setForeground(Color.WHITE);
                btnEdit.setPreferredSize(new Dimension(60, 22));
                btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnDelete.setBackground(new Color(204, 0, 0));
                btnDelete.setForeground(Color.WHITE);
                btnDelete.setPreferredSize(new Dimension(60, 22));
                btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                
                btnEdit.addActionListener(e -> {
                    int row = table.getEditingRow();
                    fireEditingStopped();
                    BookManagerUI.this.editBook(row);
                });
                
                btnDelete.addActionListener(e -> {
                    int row = table.getEditingRow();
                    fireEditingStopped();
                    BookManagerUI.this.deleteBook(row);
                });
                
                panel.add(btnEdit);
                panel.add(btnDelete);
            }
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                return panel;
            }
        });
        
        // Đặt kích thước cột "Thao tác" rộng hơn
        table.getColumn("Thao tác").setPreferredWidth(130);
        table.getColumn("Thao tác").setMinWidth(130);
        table.getColumn("Thao tác").setMaxWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        listPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(232, 242, 255));
        lblCount = new JLabel("Số lượng sách hiện tại:");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblCount.setForeground(new Color(0, 102, 204));
        bottomPanel.add(lblCount, BorderLayout.WEST);
        JButton btnAddNew = new JButton("Thêm sách mới");
        btnAddNew.setBackground(new Color(0, 153, 76));
        btnAddNew.setForeground(Color.WHITE);
        btnAddNew.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bottomPanel.add(btnAddNew, BorderLayout.EAST);

        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        listPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Panel thêm sách mới (form) với GridBagLayout
        JPanel addPanel = createAddPanel();

        // Thêm panel vào CardLayout
        mainPanel.add(listPanel, "LIST");
        mainPanel.add(addPanel, "ADD");
        add(mainPanel);

        // Sự kiện chuyển đổi giao diện
        btnAddNew.addActionListener(e -> {
            clearAddForm();
            cardLayout.show(mainPanel, "ADD");
        });

        loadBooks();
        pack();
        setLocationRelativeTo(null);
    }

    private Connection getConn() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:C:/data/library.db");
    }

    private JPanel createAddPanel() {
        JPanel addPanel = new JPanel();
        addPanel.setBackground(new Color(255, 245, 230));
        GridBagLayout gbl = new GridBagLayout();
        addPanel.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();

        // Preview panel (ảnh bìa)
        JPanel previewPanel = new JPanel();
        previewPanel.setBackground(Color.WHITE);
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        previewPanel.setPreferredSize(new Dimension(150, 200));
        lblImagePreview = new JLabel("Xem trước ảnh bìa");
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        previewPanel.add(lblImagePreview);

        // URL input + preview button với style đẹp và kích thước cố định
        JPanel urlPanel = new JPanel(new BorderLayout(5, 0));
        urlPanel.setBackground(new Color(255, 245, 230));
        urlPanel.setPreferredSize(new Dimension(280, 40));
        urlPanel.setMinimumSize(new Dimension(280, 40));
        urlPanel.setMaximumSize(new Dimension(280, 40));
        
        txtCoverImage = createStyledTextField("", 20);
        // Override kích thước cho txtCoverImage trong panel
        txtCoverImage.setPreferredSize(new Dimension(200, 40));
        
        JButton btnPreview = new JButton("👁️ Xem");
        btnPreview.setBackground(new Color(103, 58, 183));
        btnPreview.setForeground(Color.WHITE);
        btnPreview.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPreview.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnPreview.setFocusPainted(false);
        btnPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPreview.setPreferredSize(new Dimension(75, 40));
        btnPreview.addActionListener(e -> previewImage());
        
        urlPanel.add(txtCoverImage, BorderLayout.CENTER);
        urlPanel.add(btnPreview, BorderLayout.EAST);

        // Các trường nhập liệu với style đẹp
        txtTitle = createStyledTextField("", 18);
        txtAuthor = createStyledTextField("", 18);
        txtPublisher = createStyledTextField("", 18);
        txtYear = createStyledTextField("", 18);
        txtQuantity = createStyledTextField("", 18);
        
        // Thêm trường mô tả sách với kích thước cố định
        txtDescription = new JTextArea(3, 18);
        txtDescription.setBackground(Color.WHITE);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JScrollPane scrollDescription = new JScrollPane(txtDescription);
        scrollDescription.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDescription.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        // Đặt kích thước giống với textfield nhưng cao hơn
        scrollDescription.setPreferredSize(new Dimension(280, 80));
        scrollDescription.setMinimumSize(new Dimension(280, 80));
        scrollDescription.setMaximumSize(new Dimension(280, 80));
        
        cbCategory = new JComboBox<>(CATEGORIES);
        cbCategory.setBackground(Color.WHITE);
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbCategory.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        // Đặt kích thước giống với textfield
        cbCategory.setPreferredSize(new Dimension(280, 40));
        cbCategory.setMinimumSize(new Dimension(280, 40));
        cbCategory.setMaximumSize(new Dimension(280, 40));

        // Nút với style đẹp hơn
        JButton btnAddBook = new JButton("➕ Thêm sách");
        btnAddBook.setBackground(new Color(76, 175, 80));
        btnAddBook.setForeground(Color.WHITE);
        btnAddBook.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAddBook.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnAddBook.setFocusPainted(false);
        btnAddBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton btnBack = new JButton("🔙 Quay lại");
        btnBack.setBackground(new Color(96, 125, 139));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnBack.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btnPanel.setBackground(new Color(255, 245, 230));
        btnPanel.add(btnAddBook);
        btnPanel.add(btnBack);

        // Bố cục 2 cột: trái là form, phải là ảnh
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblFormTitle = new JLabel("📚 Thêm sách mới vào thư viện");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFormTitle.setForeground(new Color(25, 118, 210));
        addPanel.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        addPanel.add(createStyledLabel("📖 Tên sách:"), gbc);
        gbc.gridx = 1;
        addPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(createStyledLabel("✍️ Tác giả:"), gbc);
        gbc.gridx = 1;
        addPanel.add(txtAuthor, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(createStyledLabel("🏢 Nhà xuất bản:"), gbc);
        gbc.gridx = 1;
        addPanel.add(txtPublisher, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(createStyledLabel("📅 Năm xuất bản:"), gbc);
        gbc.gridx = 1;
        addPanel.add(txtYear, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(createStyledLabel("📊 Số lượng:"), gbc);
        gbc.gridx = 1;
        addPanel.add(txtQuantity, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(createStyledLabel("🏷️ Thể loại:"), gbc);
        gbc.gridx = 1;
        addPanel.add(cbCategory, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(createStyledLabel("📝 Mô tả sách:"), gbc);
        gbc.gridx = 1;
        addPanel.add(scrollDescription, gbc);

        gbc.gridx = 0; gbc.gridy++;
        addPanel.add(createStyledLabel("🖼️ URL ảnh bìa:"), gbc);
        gbc.gridx = 1;
        addPanel.add(urlPanel, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        addPanel.add(btnPanel, gbc);

        // Ảnh bìa ở bên phải, chiếm nhiều dòng (tăng thêm 1 dòng cho mô tả)
        gbc.gridx = 2; gbc.gridy = 1; gbc.gridheight = 9;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 30, 10, 10);
        addPanel.add(previewPanel, gbc);

        // Sự kiện nút
        btnAddBook.addActionListener(e -> addBook());
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "LIST"));

        return addPanel;
    }

    private void previewImage() {
        String imageUrl = txtCoverImage.getText().trim();
        if (!imageUrl.isEmpty()) {
            loadImagePreview(imageUrl, lblImagePreview);
        }
    }
    
    private void loadImagePreview(String imageUrl, JLabel previewLabel) {
        if (!imageUrl.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(new URL(imageUrl));
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(160, 200, Image.SCALE_SMOOTH);
                previewLabel.setIcon(new ImageIcon(scaledImg));
                previewLabel.setText("");
            } catch (Exception ex) {
                previewLabel.setIcon(null);
                previewLabel.setText("❌ Không thể tải ảnh");
            }
        } else {
            previewLabel.setIcon(null);
            previewLabel.setText("🖼️ Xem trước ảnh bìa");
        }
    }
    
    private JTextField createStyledTextField(String text, int columns) {
        JTextField field = new JTextField(text, columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        // Đặt kích thước cố định cho tất cả textfield
        field.setPreferredSize(new Dimension(280, 40));
        field.setMinimumSize(new Dimension(280, 40));
        field.setMaximumSize(new Dimension(280, 40));
        return field;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(55, 71, 79));
        return label;
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try (Connection conn = getConn()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT id, title, author, publisher, year, category, quantity, cover_image FROM books");
            while (rs.next()) {
                String coverUrl = rs.getString("cover_image");
                ImageIcon coverIcon = null;
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    try {
                        ImageIcon originalIcon = new ImageIcon(new URL(coverUrl));
                        Image scaledImg = originalIcon.getImage().getScaledInstance(50, 70, Image.SCALE_SMOOTH);
                        coverIcon = new ImageIcon(scaledImg);
                    } catch (Exception ex) {
                        coverIcon = null;
                    }
                }
                
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    coverIcon, // Column for book cover
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("year"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    "Thao tác" // Column for actions
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
        updateBookCount();
    }

    private void updateBookCount() {
        int rowCount = tableModel.getRowCount();
        lblCount.setText("Số lượng sách hiện tại: " + rowCount);
    }

    private void addBook() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String year = txtYear.getText().trim();
        String quantity = txtQuantity.getText().trim();
        String description = txtDescription.getText().trim();
        String category = (String) cbCategory.getSelectedItem();
        String coverImage = txtCoverImage.getText().trim();
        
        if (title.isEmpty() || author.isEmpty() || publisher.isEmpty() || year.isEmpty() || quantity.isEmpty() || category == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books(title, author, publisher, year, quantity, category, cover_image, description) " +
                "VALUES(?,?,?,?,?,?,?,?)");
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, publisher);
            ps.setString(4, year);
            ps.setInt(5, Integer.parseInt(quantity));
            ps.setString(6, category);
            ps.setString(7, coverImage);
            ps.setString(8, description);
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm sách thành công!");
            loadBooks();
            clearAddForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm sách: " + ex.getMessage());
        }
    }

    private void clearAddForm() {
        txtTitle.setText("");
        txtAuthor.setText("");
        txtPublisher.setText("");
        txtYear.setText("");
        txtQuantity.setText("");
        txtDescription.setText("");
        txtCoverImage.setText("");
        cbCategory.setSelectedIndex(0);
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("Xem trước ảnh bìa");
    }
    
    private void editBook(int row) {
        int bookId = (Integer) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 2);
        String author = (String) tableModel.getValueAt(row, 3);
        String publisher = (String) tableModel.getValueAt(row, 4);
        String year = (String) tableModel.getValueAt(row, 5);
        String category = (String) tableModel.getValueAt(row, 6);
        int quantity = (Integer) tableModel.getValueAt(row, 7);
        
        // Lấy thông tin ảnh bìa và mô tả hiện tại
        String currentImageUrl = "";
        String currentDescription = "";
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement("SELECT cover_image, description FROM books WHERE id = ?");
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentImageUrl = rs.getString("cover_image");
                if (currentImageUrl == null) currentImageUrl = "";
                currentDescription = rs.getString("description");
                if (currentDescription == null) currentDescription = "";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // Tạo dialog sửa sách với giao diện đẹp và kích thước phù hợp
        JDialog editDialog = new JDialog(this, "✏️ Sửa thông tin sách", true);
        editDialog.setSize(750, 700);
        editDialog.setLocationRelativeTo(this);
        editDialog.setResizable(false);
        
        // Panel chính với gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 248, 255), 0, getHeight(), new Color(230, 240, 250));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        JLabel titleLabel = new JLabel("📚 Chỉnh sửa thông tin sách");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 118, 210));
        titlePanel.add(titleLabel);
        
        // Content panel với form
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Tạo các text fields với style đẹp
        JTextField editTitle = createStyledTextField(title, 22);
        JTextField editAuthor = createStyledTextField(author, 22);
        JTextField editPublisher = createStyledTextField(publisher, 22);
        JTextField editYear = createStyledTextField(year, 22);
        JTextField editQuantity = createStyledTextField(String.valueOf(quantity), 22);
        
        JComboBox<String> editCategory = new JComboBox<>(CATEGORIES);
        editCategory.setSelectedItem(category);
        editCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editCategory.setBackground(Color.WHITE);
        editCategory.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        // Đặt kích thước giống với textfield
        editCategory.setPreferredSize(new Dimension(280, 40));
        editCategory.setMinimumSize(new Dimension(280, 40));
        editCategory.setMaximumSize(new Dimension(280, 40));
        
        // Thêm trường mô tả sách
        JTextArea editDescription = new JTextArea(currentDescription, 3, 18);
        editDescription.setBackground(Color.WHITE);
        editDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editDescription.setLineWrap(true);
        editDescription.setWrapStyleWord(true);
        editDescription.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JScrollPane editScrollDescription = new JScrollPane(editDescription);
        editScrollDescription.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        editScrollDescription.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        editScrollDescription.setPreferredSize(new Dimension(280, 80));
        editScrollDescription.setMinimumSize(new Dimension(280, 80));
        editScrollDescription.setMaximumSize(new Dimension(280, 80));
        
        // Thêm trường ảnh bìa với style đẹp
        JTextField editImageUrl = createStyledTextField(currentImageUrl, 22);
        
        // Preview ảnh với border đẹp và shadow effect
        JLabel editImagePreview = new JLabel("🖼️ Xem trước ảnh bìa");
        editImagePreview.setPreferredSize(new Dimension(180, 240));
        editImagePreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        editImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        editImagePreview.setBackground(Color.WHITE);
        editImagePreview.setOpaque(true);
        
        // Load ảnh hiện tại nếu có
        if (!currentImageUrl.isEmpty()) {
            loadImagePreview(currentImageUrl, editImagePreview);
        }
        
        JButton editPreviewBtn = new JButton("👁️ Xem trước ảnh");
        editPreviewBtn.setBackground(new Color(103, 58, 183));
        editPreviewBtn.setForeground(Color.WHITE);
        editPreviewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editPreviewBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        editPreviewBtn.setFocusPainted(false);
        editPreviewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editPreviewBtn.setPreferredSize(new Dimension(140, 35));
        editPreviewBtn.addActionListener(e -> {
            String url = editImageUrl.getText().trim();
            if (!url.isEmpty()) {
                loadImagePreview(url, editImagePreview);
            }
        });
        
        // Layout form với labels đẹp
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(createStyledLabel("📖 Tên sách:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(editTitle, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(createStyledLabel("✍️ Tác giả:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(editAuthor, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(createStyledLabel("🏢 Nhà XB:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(editPublisher, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(createStyledLabel("📅 Năm XB:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(editYear, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(createStyledLabel("📊 Số lượng:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(editQuantity, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(createStyledLabel("🏷️ Thể loại:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(editCategory, gbc);
        
        // Thêm trường mô tả sách
        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(createStyledLabel("📝 Mô tả sách:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(editScrollDescription, gbc);
        
        // Thêm trường ảnh bìa
        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(createStyledLabel("🖼️ Ảnh bìa URL:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(editImageUrl, gbc);
        
        gbc.gridx = 1; gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(editPreviewBtn, gbc);
        
        // Thêm preview ảnh bên phải
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.gridheight = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 20, 8, 8);
        contentPanel.add(editImagePreview, gbc);
        
        // Reset gridheight cho các component khác
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Button panel với style đẹp
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        JButton btnSave = new JButton("💾 Lưu thay đổi");
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton btnCancel = new JButton("❌ Hủy bỏ");
        btnCancel.setBackground(new Color(244, 67, 54));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSave.addActionListener(e -> {
            try (Connection conn = getConn()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE books SET title=?, author=?, publisher=?, year=?, quantity=?, category=?, description=?, cover_image=? WHERE id=?");
                ps.setString(1, editTitle.getText().trim());
                ps.setString(2, editAuthor.getText().trim());
                ps.setString(3, editPublisher.getText().trim());
                ps.setString(4, editYear.getText().trim());
                ps.setInt(5, Integer.parseInt(editQuantity.getText().trim()));
                ps.setString(6, (String) editCategory.getSelectedItem());
                ps.setString(7, editDescription.getText().trim());
                ps.setString(8, editImageUrl.getText().trim());
                ps.setInt(9, bookId);
                
                ps.executeUpdate();
                JOptionPane.showMessageDialog(editDialog, "✅ Cập nhật sách thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
                editDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editDialog, "❌ Lỗi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancel.addActionListener(e -> editDialog.dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        // Lắp ráp dialog
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        editDialog.add(mainPanel);
        editDialog.setVisible(true);
    }
    
    private void deleteBook(int row) {
        int bookId = (Integer) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa sách '" + title + "'?", 
            "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = getConn()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id=?");
                ps.setInt(1, bookId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Xóa sách thành công!");
                loadBooks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa sách: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookManagerUI().setVisible(true));
    }
}

