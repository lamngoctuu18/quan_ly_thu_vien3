package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import dao.UserDAO;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("WELCOME|Library TCP Server");

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("REQ: " + request);
                String[] parts = request.split("\\|", -1);
                String cmd = parts[0];

                switch (cmd) {
                    case "LOGIN":
                        handleLogin(parts);
                        break;
                    case "REGISTER":
                        handleRegister(parts);
                        break;
                    case "SEARCH":
                        handleSearch(parts);
                        break;
                    case "BORROW":
                        handleBorrow(parts);
                        break;
                    case "RETURN":
                        handleReturn(parts);
                        break;
                    case "ADD_BOOK":
                        handleAddBook(parts);
                        break;
                    case "DELETE_BOOK":
                        handleDeleteBook(parts);
                        break;
                    case "LIST_BORROWS":
                        handleListBorrows();
                        break;
                    case "EXIT":
                        out.println("BYE");
                        socket.close();
                        return;
                    default:
                        out.println("ERROR|Unknown command");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception e) {}
        }
    }

    private Connection getConnection() throws Exception {
        // Sửa lại đường dẫn cho đúng với file CSDL đã tạo
        return DriverManager.getConnection("jdbc:sqlite:C:/data/library.db");
    }

    private void handleLogin(String[] parts) {
        if (parts.length < 3) { out.println("LOGIN_FAIL|Missing params"); return; }
        String username = parts[1];
        String password = parts[2];
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, role, username, password FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                String dbUser = rs.getString("username");
                String dbPass = rs.getString("password");
                System.out.println("LOGIN DB: " + dbUser + " | " + dbPass + " | " + role);
                out.println("LOGIN_SUCCESS|" + id + "|" + role);
            } else {
                // Thêm log chi tiết để kiểm tra nguyên nhân
                PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM users WHERE username=?");
                ps2.setString(1, username);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) {
                    System.out.println("LOGIN_FAIL: Tên đúng nhưng sai mật khẩu. DB pass=" + rs2.getString("password"));
                } else {
                    System.out.println("LOGIN_FAIL: Không tìm thấy username=" + username);
                }
                out.println("LOGIN_FAIL|Invalid credentials");
            }
        } catch (Exception e) {
            out.println("LOGIN_FAIL|" + e.getMessage());
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 5) { out.println("REGISTER_FAIL|Missing params"); return; }
        String username = parts[1];
        String password = parts[2];
        String phone = parts[3];
        String email = parts[4];
        String role = "user";
        try {
            UserDAO dao = new UserDAO();
            int result = dao.createUser(username, password, role, phone, email);
            if (result > 0) {
                out.println("REGISTER_SUCCESS");
            } else {
                out.println("REGISTER_FAIL|Could not create user");
            }
        } catch (Exception e) {
            out.println("REGISTER_FAIL|" + e.getMessage());
        }
    }

    private void handleSearch(String[] parts) {
        String keyword = parts.length > 1 ? parts[1] : "";
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, title, author, publisher, year, quantity FROM books WHERE title LIKE ? OR author LIKE ?");
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder("SEARCH_RESULT|");
            while (rs.next()) {
                sb.append(rs.getInt("id")).append(",")
                  .append(rs.getString("title")).append(",")
                  .append(rs.getString("author")).append(",")
                  .append(rs.getString("publisher")).append(",")
                  .append(rs.getString("year")).append(",")
                  .append(rs.getInt("quantity")).append(";");
            }
            out.println(sb.toString());
        } catch (Exception e) {
            out.println("SEARCH_FAIL|" + e.getMessage());
        }
    }

    private void handleBorrow(String[] parts) {
        if (parts.length < 3) { out.println("BORROW_FAIL|Missing params"); return; }
        int userId = Integer.parseInt(parts[1]);
        int bookId = Integer.parseInt(parts[2]);
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement check = conn.prepareStatement("SELECT quantity FROM books WHERE id=?");
            check.setInt(1, bookId);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                conn.rollback();
                out.println("BORROW_FAIL|Book not found");
                return;
            }
            int qty = rs.getInt("quantity");
            if (qty <= 0) {
                conn.rollback();
                out.println("BORROW_FAIL|Not available");
                return;
            }
            PreparedStatement borrow = conn.prepareStatement("INSERT INTO borrows(user_id, book_id, borrow_date) VALUES(?,?,date('now'))");
            borrow.setInt(1, userId);
            borrow.setInt(2, bookId);
            borrow.executeUpdate();

            PreparedStatement update = conn.prepareStatement("UPDATE books SET quantity=quantity-1 WHERE id=?");
            update.setInt(1, bookId);
            update.executeUpdate();

            conn.commit();
            out.println("BORROW_SUCCESS");
        } catch (Exception e) {
            out.println("BORROW_FAIL|" + e.getMessage());
        }
    }

    private void handleReturn(String[] parts) {
        if (parts.length < 3) { out.println("RETURN_FAIL|Missing params"); return; }
        int userId = Integer.parseInt(parts[1]);
        int bookId = Integer.parseInt(parts[2]);
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement ret = conn.prepareStatement(
                "UPDATE borrows SET return_date=date('now') WHERE user_id=? AND book_id=? AND return_date IS NULL");
            ret.setInt(1, userId);
            ret.setInt(2, bookId);
            int rows = ret.executeUpdate();
            if (rows > 0) {
                PreparedStatement update = conn.prepareStatement("UPDATE books SET quantity=quantity+1 WHERE id=?");
                update.setInt(1, bookId);
                update.executeUpdate();
                conn.commit();
                out.println("RETURN_SUCCESS");
            } else {
                conn.rollback();
                out.println("RETURN_FAIL|No active borrow");
            }
        } catch (Exception e) {
            out.println("RETURN_FAIL|" + e.getMessage());
        }
    }

    private void handleAddBook(String[] parts) {
        if (parts.length < 4) { out.println("ADD_BOOK_FAIL|Missing params"); return; }
        String title = parts[1];
        String author = parts[2];
        int qty = Integer.parseInt(parts[3]);
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO books(title, author, quantity) VALUES(?,?,?)");
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, qty);
            ps.executeUpdate();
            out.println("ADD_BOOK_SUCCESS");
        } catch (Exception e) {
            out.println("ADD_BOOK_FAIL|" + e.getMessage());
        }
    }

    private void handleDeleteBook(String[] parts) {
        if (parts.length < 2) { out.println("DELETE_BOOK_FAIL|Missing params"); return; }
        int id = Integer.parseInt(parts[1]);
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            out.println("DELETE_BOOK_SUCCESS");
        } catch (Exception e) {
            out.println("DELETE_BOOK_FAIL|" + e.getMessage());
        }
    }

    private void handleListBorrows() {
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM borrows");
            StringBuilder sb = new StringBuilder("BORROW_LIST|");
            while (rs.next()) {
                sb.append(rs.getInt("id")).append(",")
                  .append(rs.getInt("user_id")).append(",")
                  .append(rs.getInt("book_id")).append(",")
                  .append(rs.getString("borrow_date")).append(",")
                  .append(rs.getString("return_date")).append(";");
            }
            out.println(sb.toString());
        } catch (Exception e) {
            out.println("LIST_BORROWS_FAIL|" + e.getMessage());
        }
    }
}
