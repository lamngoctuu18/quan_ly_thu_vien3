package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BorrowClientUI extends JFrame {
    private JTabbedPane tabPane;
    private DefaultTableModel borrowModel, returnModel;
    private JTable borrowTable, returnTable;

    // Các trường cho tab mượn
    private JTextField txtId, txtBookId, txtBook, txtUser, txtPhone, txtBorrowDate, txtReturnDate;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JLabel lblBorrowCount;
    private JButton btnBack; // Thêm biến nút quay lại

    // Các trường cho tab trả
    private JTextField txtSearchId, txtSearchUser;
    private JButton btnSearch, btnReturn;

    public BorrowClientUI() {
        setTitle("Quản lý Mượn/Trả sách");
        setSize(1000, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        tabPane = new JTabbedPane();

        // Tab Mượn sách
        JPanel borrowPanel = new JPanel(new BorderLayout());
        borrowPanel.add(createBorrowForm(), BorderLayout.NORTH);
        borrowPanel.add(createBorrowTable(), BorderLayout.CENTER);
        borrowPanel.add(createBorrowButtons(), BorderLayout.SOUTH);

        // Tab Trả sách
        JPanel returnPanel = new JPanel(new BorderLayout());
        returnPanel.add(createReturnSearchPanel(), BorderLayout.NORTH);
        returnPanel.add(createReturnTable(), BorderLayout.CENTER);
        returnPanel.add(createReturnButtonPanel(), BorderLayout.SOUTH);

        tabPane.addTab("Mượn sách", borrowPanel);
        tabPane.addTab("Trả sách", returnPanel);

        add(tabPane, BorderLayout.CENTER);
        setLocationRelativeTo(null);

        loadBorrowList();
        loadReturnList();
    }

    private JPanel createBorrowForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = new JTextField();
        txtBookId = new JTextField();
        txtBook = new JTextField();
        txtUser = new JTextField();
        txtPhone = new JTextField();
        txtBorrowDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        txtReturnDate = new JTextField(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE));

        // Đặt kích thước rộng hơn cho ô nhập ID sách và số điện thoại
        txtBookId.setPreferredSize(new Dimension(180, 28));
        txtPhone.setPreferredSize(new Dimension(180, 28));

        int row = 0;
        gbc.gridy = row++;
        gbc.gridx = 0; panel.add(new JLabel("ID (khi sửa/xóa/trả):"), gbc);
        gbc.gridx = 1; panel.add(txtId, gbc);
        gbc.gridx = 2; panel.add(new JLabel("ID sách:"), gbc);
        gbc.gridx = 3; panel.add(txtBookId, gbc);
        gbc.gridx = 4; panel.add(new JLabel("Chọn Sách:"), gbc);
        gbc.gridx = 5; panel.add(txtBook, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0; panel.add(new JLabel("Tên độc giả:"), gbc);
        gbc.gridx = 1; panel.add(txtUser, gbc);
        gbc.gridx = 2; panel.add(new JLabel("SDT:"), gbc);
        gbc.gridx = 3; panel.add(txtPhone, gbc);
        gbc.gridx = 4; panel.add(new JLabel("Ngày mượn (yyyy-MM-dd):"), gbc);
        gbc.gridx = 5; panel.add(txtBorrowDate, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0; panel.add(new JLabel("Ngày trả (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; panel.add(txtReturnDate, gbc);

        return panel;
    }

    private JScrollPane createBorrowTable() {
        String[] cols = {"ID", "Sách", "Độc giả", "Ngày mượn", "Ngày trả", "Trạng thái"};
        borrowModel = new DefaultTableModel(cols, 0);
        borrowTable = new JTable(borrowModel);
        return new JScrollPane(borrowTable);
    }

    private JPanel createBorrowButtons() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel trái chứa nút Quay lại
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnBack = new JButton("Quay lại");
        leftPanel.add(btnBack);

        // Panel phải chứa các nút chức năng
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAdd = new JButton("+ Mượn");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnRefresh = new JButton("Làm mới");
        lblBorrowCount = new JLabel("Số lượng người đang mượn: 0");
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDelete);
        rightPanel.add(btnRefresh);
        rightPanel.add(lblBorrowCount);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        // Xử lý sự kiện nút Quay lại
        btnBack.addActionListener(e -> {
            // Đóng cửa sổ này, quay lại giao diện chính admin
            dispose();
            // Nếu có giao diện chính admin, có thể mở lại ở đây
            // new AdminMainUI().setVisible(true);
        });

        // Thêm sự kiện cho nút "+ Mượn"
        btnAdd.addActionListener(e -> addBorrow());

        // Thêm sự kiện cho các nút khác nếu cần
        btnEdit.addActionListener(e -> editBorrow());
        btnDelete.addActionListener(e -> deleteBorrow());
        btnRefresh.addActionListener(e -> loadBorrowList());

        return panel;
    }

    private JPanel createReturnSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearchId = new JTextField(10);
        txtSearchUser = new JTextField(15);
        btnSearch = new JButton("Tìm kiếm");
        panel.add(new JLabel("Tìm kiếm ID:"));
        panel.add(txtSearchId);
        panel.add(new JLabel("Tìm kiếm tên khách hàng:"));
        panel.add(txtSearchUser);
        panel.add(btnSearch);

        btnSearch.addActionListener(e -> loadReturnList());

        return panel;
    }

    private JScrollPane createReturnTable() {
        String[] cols = {"ID", "Sách", "Độc giả", "Ngày mượn", "Ngày trả", "Trạng thái"};
        returnModel = new DefaultTableModel(cols, 0);
        returnTable = new JTable(returnModel);
        return new JScrollPane(returnTable);
    }

    private JPanel createReturnButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnReturn = new JButton("Trả sách");
        panel.add(btnReturn);

        btnReturn.addActionListener(e -> returnBook());

        return panel;
    }

    private void loadBorrowList() {
        borrowModel.setRowCount(0);
        int count = 0;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            String sql = "SELECT br.id, b.title, u.username, br.borrow_date, br.return_date " +
                         "FROM borrows br JOIN books b ON br.book_id = b.id JOIN users u ON br.user_id = u.id " +
                         "ORDER BY br.borrow_date DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String returnDate = rs.getString("return_date");
                String status = (returnDate == null || returnDate.trim().isEmpty()) ? "Đang mượn" : "Đã trả";
                String displayReturnDate;
                if ("Đang mượn".equals(status)) {
                    // Hiển thị ngày trả dự kiến từ txtReturnDate
                    displayReturnDate = txtReturnDate.getText().trim();
                } else {
                    // Hiển thị ngày trả thực tế từ CSDL
                    displayReturnDate = returnDate == null ? "" : returnDate;
                }
                borrowModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("username"),
                    rs.getString("borrow_date"),
                    displayReturnDate,
                    status
                });
                if ("Đang mượn".equals(status)) count++;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu mượn: " + ex.getMessage());
        }
        lblBorrowCount.setText("Số lượng người đang mượn: " + count);
    }

    private void addBorrow() {
        String bookIdStr = txtBookId.getText().trim();
        String book = txtBook.getText().trim();
        String user = txtUser.getText().trim();
        String borrowDate = txtBorrowDate.getText().trim();
        // String returnDate = txtReturnDate.getText().trim(); // Không dùng khi thêm mới

        int borrowId = -1;

        // Nếu nhập ID sách, tự động lấy tên sách từ CSDL
        if (!bookIdStr.isEmpty()) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
                PreparedStatement psBook = conn.prepareStatement("SELECT title FROM books WHERE id=?");
                psBook.setInt(1, Integer.parseInt(bookIdStr));
                ResultSet rsBook = psBook.executeQuery();
                if (rsBook.next()) {
                    book = rsBook.getString("title");
                    txtBook.setText(book); // Hiển thị tên sách vào ô chọn sách
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy sách với ID này!");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi lấy tên sách: " + ex.getMessage());
                return;
            }
        }

        if (book.isEmpty() || user.isEmpty() || borrowDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            // Lấy id sách và id user
            PreparedStatement psBook = conn.prepareStatement("SELECT id FROM books WHERE title=?");
            psBook.setString(1, book);
            ResultSet rsBook = psBook.executeQuery();
            if (!rsBook.next()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sách!");
                return;
            }
            int bookId = rsBook.getInt("id");

            PreparedStatement psUser = conn.prepareStatement("SELECT id FROM users WHERE username=?");
            psUser.setString(1, user);
            ResultSet rsUser = psUser.executeQuery();
            if (!rsUser.next()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng!");
                return;
            }
            int userId = rsUser.getInt("id");

            // Kiểm tra số lượng sách còn lại
            PreparedStatement psCheckQty = conn.prepareStatement("SELECT quantity FROM books WHERE id=?");
            psCheckQty.setInt(1, bookId);
            ResultSet rsQty = psCheckQty.executeQuery();
            if (rsQty.next()) {
                int qty = rsQty.getInt("quantity");
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(this, "Sách đã hết, không thể mượn!");
                    return;
                }
            }

            // Khi thêm mới, return_date phải là null
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO borrows(user_id, book_id, borrow_date, return_date) VALUES(?,?,?,NULL)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.setString(3, borrowDate);
            // ps.setString(4, null); // Không cần truyền vào vì đã để NULL trong câu lệnh SQL
            ps.executeUpdate();

            ResultSet rsGen = ps.getGeneratedKeys();
            if (rsGen.next()) {
                borrowId = rsGen.getInt(1);
            } else {
                PreparedStatement psLast = conn.prepareStatement(
                    "SELECT id FROM borrows WHERE user_id=? AND book_id=? AND borrow_date=? ORDER BY id DESC LIMIT 1");
                psLast.setInt(1, userId);
                psLast.setInt(2, bookId);
                psLast.setString(3, borrowDate);
                ResultSet rsLast = psLast.executeQuery();
                if (rsLast.next()) borrowId = rsLast.getInt("id");
            }

            // Giảm số lượng sách đi 1
            PreparedStatement psUpdateQty = conn.prepareStatement("UPDATE books SET quantity = quantity - 1 WHERE id=?");
            psUpdateQty.setInt(1, bookId);
            psUpdateQty.executeUpdate();

            JOptionPane.showMessageDialog(this, "Thêm phiếu mượn thành công!");
            // Hiển thị phiếu mượn, ngày trả chỉ là dự kiến
            showBorrowReceipt(borrowId, user, book, borrowDate, txtReturnDate.getText().trim());
            loadBorrowList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm phiếu mượn: " + ex.getMessage());
        }
    }

    // Hiển thị phiếu mượn và cho phép in ra file CSV
    private void showBorrowReceipt(int borrowId, String user, String book, String borrowDate, String returnDate) {
        JDialog dialog = new JDialog(this, "Phiếu mượn sách", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(6, 1, 10, 10));
        dialog.setLocationRelativeTo(this);

        JLabel lblId = new JLabel("ID phiếu mượn: " + borrowId);
        JLabel lblUser = new JLabel("Tên người mượn: " + user);
        JLabel lblBook = new JLabel("Tên sách mượn: " + book);
        JLabel lblBorrowDate = new JLabel("Ngày mượn: " + borrowDate);
        JLabel lblReturnDate = new JLabel("Ngày trả: " + returnDate);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnOk = new JButton("OK");
        JButton btnPrint = new JButton("In phiếu mượn");
        btnPanel.add(btnOk);
        btnPanel.add(btnPrint);

        dialog.add(lblId);
        dialog.add(lblUser);
        dialog.add(lblBook);
        dialog.add(lblBorrowDate);
        dialog.add(lblReturnDate);
        dialog.add(btnPanel);

        btnOk.addActionListener(e -> dialog.dispose());

        btnPrint.addActionListener(e -> {
            try {
                java.io.File file = new java.io.File("lichsuphieumuon.csv");
                boolean newFile = false;
                if (!file.exists()) {
                    file.createNewFile();
                    newFile = true;
                }
                try (java.io.FileWriter fw = new java.io.FileWriter(file, true)) {
                    if (newFile) {
                        fw.write("ID phiếu mượn,Tên người mượn,Tên sách mượn,Ngày mượn,Ngày trả\n");
                    }
                    fw.write(borrowId + "," + user + "," + book + "," + borrowDate + "," + returnDate + "\n");
                }
                JOptionPane.showMessageDialog(dialog, "Đã in phiếu mượn ra file lichsuphieumuon.csv");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi ghi file: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void editBorrow() {
        String id = txtId.getText().trim();
        String returnDate = txtReturnDate.getText().trim();
        if (id.isEmpty() || returnDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ID và ngày trả!");
            return;
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE borrows SET return_date=? WHERE id=?");
            ps.setString(1, returnDate);
            ps.setInt(2, Integer.parseInt(id));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Sửa phiếu mượn thành công!");
                loadBorrowList();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu mượn với ID này!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi sửa phiếu mượn: " + ex.getMessage());
        }
    }

    private void deleteBorrow() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ID phiếu mượn cần xóa!");
            return;
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM borrows WHERE id=?");
            ps.setInt(1, Integer.parseInt(id));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Xóa phiếu mượn thành công!");
                loadBorrowList();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu mượn với ID này!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa phiếu mượn: " + ex.getMessage());
        }
    }

    private void loadReturnList() {
        returnModel.setRowCount(0);
        String idSearch = txtSearchId.getText().trim();
        String userSearch = txtSearchUser.getText().trim();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            String sql = "SELECT br.id, b.title, u.username, br.borrow_date, br.return_date, " +
                         "CASE WHEN br.return_date IS NULL THEN 'Đang mượn' ELSE 'Đã trả' END AS status " +
                         "FROM borrows br JOIN books b ON br.book_id = b.id JOIN users u ON br.user_id = u.id WHERE 1=1 ";
            if (!idSearch.isEmpty()) sql += "AND br.id = " + idSearch + " ";
            if (!userSearch.isEmpty()) sql += "AND u.username LIKE '%" + userSearch + "%' ";
            sql += "ORDER BY br.borrow_date DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String status = rs.getString("status");
                String borrowDate = rs.getString("borrow_date");
                String returnDate = rs.getString("return_date");
                String displayReturnDate;
                if ("Đang mượn".equals(status)) {
                    // Nếu chưa có ngày trả, hiển thị ngày trả dự kiến (7 ngày sau ngày mượn)
                    if (returnDate == null || returnDate.trim().isEmpty()) {
                        try {
                            java.time.LocalDate bd = java.time.LocalDate.parse(borrowDate);
                            displayReturnDate = bd.plusDays(7).toString();
                        } catch (Exception ex) {
                            displayReturnDate = "";
                        }
                    } else {
                        displayReturnDate = returnDate;
                    }
                } else {
                    displayReturnDate = returnDate == null ? "" : returnDate;
                }
                returnModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("username"),
                    borrowDate,
                    displayReturnDate,
                    status
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu trả: " + ex.getMessage());
        }
    }

    private void returnBook() {
        int row = returnTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu mượn để trả!");
            return;
        }
        String id = returnModel.getValueAt(row, 0).toString();
        // Lấy ngày trả theo thời gian thực
        String returnDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            // Lấy book_id từ phiếu mượn
            PreparedStatement psGetBook = conn.prepareStatement("SELECT book_id FROM borrows WHERE id=?");
            psGetBook.setInt(1, Integer.parseInt(id));
            ResultSet rsBook = psGetBook.executeQuery();
            int bookId = -1;
            if (rsBook.next()) {
                bookId = rsBook.getInt("book_id");
            }

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE borrows SET return_date=? WHERE id=?");
            ps.setString(1, returnDate);
            ps.setInt(2, Integer.parseInt(id));
            int rows = ps.executeUpdate();

            if (rows > 0) {
                // Tăng số lượng sách lên 1
                if (bookId != -1) {
                    PreparedStatement psUpdateQty = conn.prepareStatement("UPDATE books SET quantity = quantity + 1 WHERE id=?");
                    psUpdateQty.setInt(1, bookId);
                    psUpdateQty.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Trả sách thành công!\nNgày trả: " + returnDate);
                loadReturnList();
                loadBorrowList();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu mượn với ID này!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi trả sách: " + ex.getMessage());
        }
    }
}

