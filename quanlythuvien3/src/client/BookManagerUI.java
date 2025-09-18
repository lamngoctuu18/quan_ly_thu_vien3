package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookManagerUI extends JFrame {
    private JTextField txtId, txtTitle, txtAuthor, txtPublisher, txtYear, txtQuantity;
    private DefaultTableModel tableModel;
    private JTable table;

    public BookManagerUI() {
        setTitle("Quản lý Sách");
        setSize(900, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel nhập liệu
        JPanel inputPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtId = new JTextField();
        txtTitle = new JTextField();
        txtAuthor = new JTextField();
        txtPublisher = new JTextField();
        txtYear = new JTextField();
        txtQuantity = new JTextField();

        inputPanel.add(new JLabel("ID (khi sửa/xóa):"));
        inputPanel.add(txtId);
        inputPanel.add(new JLabel("Tên sách:"));
        inputPanel.add(txtTitle);
        inputPanel.add(new JLabel("Tác giả:"));
        inputPanel.add(txtAuthor);
        inputPanel.add(new JLabel("NXB:"));
        inputPanel.add(txtPublisher);
        inputPanel.add(new JLabel("Năm XB:"));
        inputPanel.add(txtYear);
        inputPanel.add(new JLabel("Số lượng:"));
        inputPanel.add(txtQuantity);

        add(inputPanel, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] cols = {"ID", "Tên sách", "Tác giả", "NXB", "Năm", "SL"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel nút chức năng
        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("+ Thêm");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        JButton btnRefresh = new JButton("Làm mới");
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        // Sự kiện nút
        btnAdd.addActionListener(e -> addBook());
        btnEdit.addActionListener(e -> editBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnRefresh.addActionListener(e -> loadBooks());

        loadBooks();
        setLocationRelativeTo(null);
    }

    private Connection getConn() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:C:/data/library.db");
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM books")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getString("year"),
                    rs.getInt("quantity")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void addBook() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String year = txtYear.getText().trim();
        String quantity = txtQuantity.getText().trim();
        if (title.isEmpty() || author.isEmpty() || publisher.isEmpty() || year.isEmpty() || quantity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books(title, author, publisher, year, quantity) VALUES(?,?,?,?,?)");
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, publisher);
            ps.setString(4, year);
            ps.setInt(5, Integer.parseInt(quantity));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm sách thành công!");
            loadBooks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm sách: " + ex.getMessage());
        }
    }

    private void editBook() {
        String id = txtId.getText().trim();
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String year = txtYear.getText().trim();
        String quantity = txtQuantity.getText().trim();
        if (id.isEmpty() || title.isEmpty() || author.isEmpty() || publisher.isEmpty() || year.isEmpty() || quantity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET title=?, author=?, publisher=?, year=?, quantity=? WHERE id=?");
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, publisher);
            ps.setString(4, year);
            ps.setInt(5, Integer.parseInt(quantity));
            ps.setInt(6, Integer.parseInt(id));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Sửa sách thành công!");
                loadBooks();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sách với ID này!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi sửa sách: " + ex.getMessage());
        }
    }

    private void deleteBook() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ID sách cần xóa!");
            return;
        }
        try (Connection conn = getConn()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id=?");
            ps.setInt(1, Integer.parseInt(id));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Xóa sách thành công!");
                loadBooks();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sách với ID này!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa sách: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookManagerUI().setVisible(true));
    }
}
