package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class AdminUI extends JFrame {
    private JTextField txtTitle, txtAuthor, txtQty;
    private JTextArea txtResult;
    private JButton btnAdd, btnDelete, btnList;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public AdminUI() {
        setTitle("Library Admin");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Form thêm sách
        JPanel formPanel = new JPanel(new GridLayout(4, 2));
        formPanel.add(new JLabel("Title:"));
        txtTitle = new JTextField();
        formPanel.add(txtTitle);

        formPanel.add(new JLabel("Author:"));
        txtAuthor = new JTextField();
        formPanel.add(txtAuthor);

        formPanel.add(new JLabel("Quantity:"));
        txtQty = new JTextField();
        formPanel.add(txtQty);

        btnAdd = new JButton("Add Book");
        btnDelete = new JButton("Delete Book");
        btnList = new JButton("List Borrows");
        formPanel.add(btnAdd);
        formPanel.add(btnDelete);
        formPanel.add(btnList);

        add(formPanel, BorderLayout.NORTH);

        // Kết quả
        txtResult = new JTextArea();
        add(new JScrollPane(txtResult), BorderLayout.CENTER);

        // Kết nối server
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server");
            System.exit(0);
        }

        // Event
        btnAdd.addActionListener(e -> addBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnList.addActionListener(e -> listBorrows());
    }

    private void addBook() {
        out.println("ADD_BOOK|" + txtTitle.getText() + "|" + txtAuthor.getText() + "|" + txtQty.getText());
        try {
            JOptionPane.showMessageDialog(this, in.readLine());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteBook() {
        String id = JOptionPane.showInputDialog(this, "Nhập ID sách cần xóa:");
        out.println("DELETE_BOOK|" + id);
        try {
            JOptionPane.showMessageDialog(this, in.readLine());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void listBorrows() {
        out.println("LIST_BORROWS");
        try {
            String resp = in.readLine();
            txtResult.setText(resp.replace("BORROW_LIST|", "").replace(";", "\n"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminUI().setVisible(true));
    }
}
