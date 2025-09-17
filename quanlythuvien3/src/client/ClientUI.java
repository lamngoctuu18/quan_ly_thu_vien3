package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ClientUI extends JFrame {
    private JTextField txtUser, txtPass, txtSearch;
    private JTextArea txtResult;
    private JButton btnLogin, btnRegister, btnSearch, btnBorrow, btnReturn;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int userId = -1;

    public ClientUI() {
        setTitle("Library Client - User");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Login Panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Username:"));
        txtUser = new JTextField();
        loginPanel.add(txtUser);

        loginPanel.add(new JLabel("Password:"));
        txtPass = new JPasswordField();
        loginPanel.add(txtPass);

        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register");
        loginPanel.add(btnLogin);
        loginPanel.add(btnRegister);

        add(loginPanel, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        txtSearch = new JTextField(20);
        btnSearch = new JButton("Search");
        btnBorrow = new JButton("Borrow");
        btnReturn = new JButton("Return");
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnBorrow);
        searchPanel.add(btnReturn);

        add(searchPanel, BorderLayout.SOUTH);

        // Result Area
        txtResult = new JTextArea();
        add(new JScrollPane(txtResult), BorderLayout.CENTER);

        // Connect server
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server");
            System.exit(0);
        }

        // Event
        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> register());
        btnSearch.addActionListener(e -> search());
        btnBorrow.addActionListener(e -> borrow());
        btnReturn.addActionListener(e -> returnBook());
    }

    private void login() {
        out.println("LOGIN|" + txtUser.getText() + "|" + txtPass.getText());
        try {
            String resp = in.readLine();
            if (resp.startsWith("LOGIN_SUCCESS")) {
                userId = Integer.parseInt(resp.split("\\|")[1]);
                JOptionPane.showMessageDialog(this, "Login thành công! UserID=" + userId);
            } else {
                JOptionPane.showMessageDialog(this, "Login thất bại!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void register() {
        out.println("REGISTER|" + txtUser.getText() + "|" + txtPass.getText());
        try {
            String resp = in.readLine();
            JOptionPane.showMessageDialog(this, resp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void search() {
        out.println("SEARCH|" + txtSearch.getText());
        try {
            String resp = in.readLine();
            txtResult.setText(resp.replace("SEARCH_RESULT|", "").replace(";", "\n"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void borrow() {
        String bookId = JOptionPane.showInputDialog(this, "Nhập ID sách cần mượn:");
        out.println("BORROW|" + userId + "|" + bookId);
        try {
            JOptionPane.showMessageDialog(this, in.readLine());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void returnBook() {
        String bookId = JOptionPane.showInputDialog(this, "Nhập ID sách cần trả:");
        out.println("RETURN|" + userId + "|" + bookId);
        try {
            JOptionPane.showMessageDialog(this, in.readLine());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
    }
}
