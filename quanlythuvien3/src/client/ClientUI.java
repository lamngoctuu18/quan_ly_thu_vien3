package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClientUI extends JFrame {
    private JTextField txtSearch;
    private JButton btnSearch, btnBorrow, btnLogout;
    private JLabel lblUser;
    private JTable table;
    private DefaultTableModel tableModel;
    private int userId = -1;
    private String username = "";

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JPopupMenu suggestPopup; // Popup gợi ý

    public ClientUI() {
        setTitle("Giao diện người dùng");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tiêu đề
        JLabel lblTitle = new JLabel("Giao diện người dùng");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitle, BorderLayout.NORTH);

        // Panel tài khoản + đăng xuất
        JPanel topPanel = new JPanel(new BorderLayout());
        lblUser = new JLabel("Tên tài khoản VD: ");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 14));
        btnLogout = new JButton("Đăng xuất");
        topPanel.add(lblUser, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);
        add(topPanel, BorderLayout.BEFORE_FIRST_LINE);

        // Panel tìm kiếm + list sách đang mượn
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        suggestPopup = new JPopupMenu();

        btnSearch = new JButton("Tìm kiếm");
        btnBorrow = new JButton("List sách đang mượn");
        searchPanel.add(new JLabel("Ô tìm kiếm sách"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnBorrow);
        add(searchPanel, BorderLayout.AFTER_LAST_LINE);

        // Bảng sách
        String[] cols = {"ID sách", "Tên sách", "Tác giả", "NXB", "Số lượng"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Kết nối server
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Đọc dòng WELCOME ngay sau khi kết nối
            String welcome = in.readLine();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server");
            System.exit(0);
        }

        // Sự kiện
        btnSearch.addActionListener(e -> searchBooks());
        btnBorrow.addActionListener(e -> {
            // Mở giao diện danh sách sách đang mượn
            BorrowListUI borrowListUI = new BorrowListUI(userId);
            borrowListUI.setVisible(true);
        });
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> app.MainApp.main(null));
        });

        // Gợi ý khi nhập vào txtSearch
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String kw = txtSearch.getText().trim();
                suggestPopup.setVisible(false);
                if (kw.length() < 1) return;

                suggestPopup.removeAll();
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT title, author, publisher FROM books WHERE title LIKE ? OR author LIKE ? OR publisher LIKE ? LIMIT 10");
                    String likeKw = "%" + kw + "%";
                    ps.setString(1, likeKw);
                    ps.setString(2, likeKw);
                    ps.setString(3, likeKw);
                    ResultSet rs = ps.executeQuery();
                    java.util.Set<String> suggestions = new java.util.LinkedHashSet<>();
                    while (rs.next()) {
                        String t = rs.getString("title");
                        String a = rs.getString("author");
                        String p = rs.getString("publisher");
                        if (t != null && t.toLowerCase().contains(kw.toLowerCase())) suggestions.add(t);
                        if (a != null && a.toLowerCase().contains(kw.toLowerCase())) suggestions.add(a);
                        if (p != null && p.toLowerCase().contains(kw.toLowerCase())) suggestions.add(p);
                    }
                    if (!suggestions.isEmpty()) {
                        for (String s : suggestions) {
                            JMenuItem item = new JMenuItem(s);
                            item.addActionListener(ev -> {
                                txtSearch.setText(s);
                                suggestPopup.setVisible(false);
                            });
                            suggestPopup.add(item);
                        }
                        suggestPopup.show(txtSearch, 0, txtSearch.getHeight());
                    }
                } catch (Exception ex) {
                    suggestPopup.setVisible(false);
                }
            }
        });

        // Ẩn popup khi mất focus
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                suggestPopup.setVisible(false);
            }
        });

        setLocationRelativeTo(null);
    }

    // Đặt tên tài khoản khi đăng nhập thành công
    public void setUserInfo(int id, String username) {
        this.userId = id;
        this.username = username;
        lblUser.setText("Tên tài khoản " + username);
    }

    private void searchBooks() {
        String kw = txtSearch.getText().trim();
        out.println("SEARCH|" + kw);
        try {
            String resp = in.readLine();
            // Nếu dòng đầu là WELCOME, đọc tiếp dòng kết quả
            if (resp != null && resp.startsWith("WELCOME|")) {
                resp = in.readLine();
            }
            tableModel.setRowCount(0);
            if (resp != null && resp.startsWith("SEARCH_RESULT|")) {
                String[] rows = resp.substring("SEARCH_RESULT|".length()).split(";");
                for (String row : rows) {
                    if (row.trim().isEmpty()) continue;
                    String[] vals = row.split(",");
                    Object[] data = new Object[5];
                    data[0] = vals.length > 0 ? vals[0] : ""; // ID sách
                    data[1] = vals.length > 1 ? vals[1] : ""; // Tên sách
                    data[2] = vals.length > 2 ? vals[2] : ""; // Tác giả
                    data[3] = vals.length > 3 ? vals[3] : ""; // NXB
                    data[4] = vals.length > 5 ? vals[5] : ""; // Số lượng (cột thứ 6 trong dữ liệu server)
                    tableModel.addRow(data);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu sách.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi nhận dữ liệu từ server");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            ui.setVisible(true);
        });
    }
}