package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.io.BufferedReader;
import java.io.FileReader;

public class BorrowClientUI extends JFrame {
    private DefaultTableModel returnModel;
    private JTable returnTable;
    private JTextField txtSearchUser, txtSearchPhone;
    private JLabel lblBorrowCount;
    private TableRowSorter<DefaultTableModel> sorter;

    public BorrowClientUI() {
        setTitle("Quản lý mượn/trả");
        setMinimumSize(new Dimension(900, 500));
        setPreferredSize(new Dimension(1200, 700));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(232, 242, 255));
        setContentPane(mainPanel);

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Bộ lọc tìm kiếm
        JLabel lblSearchUser = new JLabel("Tên người mượn:");
        lblSearchUser.setForeground(new Color(0, 51, 102));
        txtSearchUser = new JTextField(15);
        txtSearchUser.setBackground(Color.WHITE);
        txtSearchUser.setForeground(new Color(0, 51, 102));
        JLabel lblSearchPhone = new JLabel("Số điện thoại:");
        lblSearchPhone.setForeground(new Color(0, 51, 102));
        txtSearchPhone = new JTextField(12);
        txtSearchPhone.setBackground(Color.WHITE);
        txtSearchPhone.setForeground(new Color(0, 51, 102));

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(232, 242, 255));
        GroupLayout searchLayout = new GroupLayout(searchPanel);
        searchPanel.setLayout(searchLayout);
        searchLayout.setAutoCreateGaps(true);
        searchLayout.setAutoCreateContainerGaps(true);
        searchLayout.setHorizontalGroup(
            searchLayout.createSequentialGroup()
                .addComponent(lblSearchUser)
                .addComponent(txtSearchUser, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblSearchPhone)
                .addComponent(txtSearchPhone, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
        );
        searchLayout.setVerticalGroup(
            searchLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblSearchUser)
                .addComponent(txtSearchUser)
                .addComponent(lblSearchPhone)
                .addComponent(txtSearchPhone)
        );

        // Bảng danh sách mượn/trả
        String[] cols = {"ID sách", "Tên sách", "Tên người mượn", "Số điện thoại", "Ngày mượn", "Ngày trả", "Hạn trả", "Tình trạng"};
        returnModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        returnTable = new JTable(returnModel);
        returnTable.setRowHeight(28);
        returnTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        returnTable.getTableHeader().setBackground(new Color(0, 102, 204));
        returnTable.getTableHeader().setForeground(Color.WHITE);

        // Renderer màu cho cột Hạn trả và Tình trạng
        returnTable.getColumn("Hạn trả").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String hanTra = value == null ? "" : value.toString();
                if (hanTra.matches("\\d+ ngày")) {
                    int days = Integer.parseInt(hanTra.replaceAll("[^0-9]", ""));
                    if (days > 1) {
                        c.setForeground(new Color(0, 128, 0)); // xanh
                    } else {
                        c.setForeground(Color.RED);
                    }
                } else if (hanTra.startsWith("Quá hạn")) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        returnTable.getColumn("Tình trạng").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setForeground(new Color(0, 128, 0)); // luôn xanh cho "Đang mượn"
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(returnTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        // Bottom: Số lượng và nút trả sách
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(232, 242, 255));
        GroupLayout bottomLayout = new GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomLayout);
        bottomLayout.setAutoCreateGaps(true);
        bottomLayout.setAutoCreateContainerGaps(true);

        lblBorrowCount = new JLabel("Số lượng người đang mượn:");
        lblBorrowCount.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblBorrowCount.setForeground(new Color(0, 102, 204));
        JButton btnReturn = new JButton("Trả sách");
        btnReturn.setBackground(new Color(255, 153, 51));
        btnReturn.setForeground(Color.WHITE);
        btnReturn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        bottomLayout.setHorizontalGroup(
            bottomLayout.createSequentialGroup()
                .addComponent(lblBorrowCount)
                .addGap(0, 800, Short.MAX_VALUE)
                .addComponent(btnReturn, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
        );
        bottomLayout.setVerticalGroup(
            bottomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblBorrowCount)
                .addComponent(btnReturn)
        );

        // Responsive layout cho mainPanel
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(searchPanel)
                .addComponent(scroll)
                .addComponent(bottomPanel)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(18)
                .addComponent(searchPanel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                .addComponent(scroll)
                .addGap(10)
                .addComponent(bottomPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
        );

        // Bộ lọc realtime
        sorter = new TableRowSorter<>(returnModel);
        returnTable.setRowSorter(sorter);

        txtSearchUser.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterReturnTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterReturnTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterReturnTable(); }
        });
        txtSearchPhone.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterReturnTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterReturnTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterReturnTable(); }
        });

        btnReturn.addActionListener(e -> {
            int row = returnTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu mượn để trả!");
                return;
            }
            int modelRow = returnTable.convertRowIndexToModel(row);
            String id = returnModel.getValueAt(modelRow, 0).toString();
            String returnDate = java.time.LocalDate.now().toString();
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
                PreparedStatement psGetBook = conn.prepareStatement("SELECT book_id FROM borrows WHERE id=?");
                psGetBook.setInt(1, Integer.parseInt(id));
                ResultSet rsBook = psGetBook.executeQuery();
                int realBookId = -1;
                if (rsBook.next()) {
                    realBookId = rsBook.getInt("book_id");
                }
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE borrows SET return_date=? WHERE id=?");
                ps.setString(1, returnDate);
                ps.setInt(2, Integer.parseInt(id));
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    if (realBookId != -1) {
                        PreparedStatement psUpdateQty = conn.prepareStatement("UPDATE books SET quantity = quantity + 1 WHERE id=?");
                        psUpdateQty.setInt(1, realBookId);
                        psUpdateQty.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(this, "Trả sách thành công!\nNgày trả: " + returnDate);
                    returnModel.removeRow(modelRow);
                    updateBorrowCount();
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu mượn với ID này!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi trả sách: " + ex.getMessage());
            }
        });

        loadReturnList();
        pack();
        setLocationRelativeTo(null);
    }

    private void filterReturnTable() {
        String userText = txtSearchUser.getText().trim();
        String phoneText = txtSearchPhone.getText().trim();
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String user = entry.getStringValue(2).toLowerCase();
                String phone = entry.getStringValue(3).toLowerCase();
                return user.contains(userText.toLowerCase()) && phone.contains(phoneText.toLowerCase());
            }
        };
        sorter.setRowFilter(rf);
        updateBorrowCount();
    }

    private void loadReturnList() {
        returnModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader("hoadon.csv"))) {
            String header = br.readLine(); // Bỏ qua dòng tiêu đề
            String line;
            LocalDate now = LocalDate.now();
            while ((line = br.readLine()) != null) {
                String[] vals = line.split(",", -1);
                if (vals.length < 10) continue;
                String tenNguoiMuon = vals[0];
                String sdt = vals[1];
                String email = vals[2];
                String tenSach = vals[3];
                String tacGia = vals[4];
                String nxb = vals[5];
                String namXB = vals[6];
                String theLoai = vals[7];
                String ngayMuon = vals[8];
                String ngayTra = vals[9];

                // Lấy ID sách từ database theo tên sách (nếu có)
                String idSach = "";
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
                    PreparedStatement ps = conn.prepareStatement("SELECT id FROM books WHERE title=? LIMIT 1");
                    ps.setString(1, tenSach);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        idSach = String.valueOf(rs.getInt("id"));
                    }
                } catch (Exception ignore) {}

                // Tính hạn trả
                LocalDate borrowDate = null, returnDate = null;
                try {
                    borrowDate = LocalDate.parse(ngayMuon);
                    returnDate = LocalDate.parse(ngayTra);
                } catch (Exception ignore) {}
                String hanTra = "";
                String tinhTrang = "";
                if (borrowDate != null && returnDate != null) {
                    long daysLeft = ChronoUnit.DAYS.between(now, returnDate);
                    if (now.isAfter(returnDate)) {
                        hanTra = "Quá hạn " + Math.abs(daysLeft) + " ngày";
                        tinhTrang = "Đã trả";
                    } else if (daysLeft > 1) {
                        hanTra = daysLeft + " ngày";
                        tinhTrang = "Đang mượn";
                    } else if (daysLeft == 1) {
                        hanTra = "1 ngày";
                        tinhTrang = "Đang mượn";
                    } else if (daysLeft == 0) {
                        hanTra = "Hết hạn";
                        tinhTrang = "Đang mượn";
                    }
                }
                returnModel.addRow(new Object[]{
                    idSach,
                    tenSach,
                    tenNguoiMuon,
                    sdt,
                    ngayMuon,
                    ngayTra,
                    hanTra,
                    tinhTrang
                });
            }
            updateBorrowCount();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc hóa đơn: " + ex.getMessage());
        }
    }

    private void updateBorrowCount() {
        lblBorrowCount.setText("Số lượng người đang mượn: " + returnModel.getRowCount());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BorrowClientUI().setVisible(true));
    }
}
