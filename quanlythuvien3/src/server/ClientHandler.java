package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                    case "FAVORITE":
                        handleFavorite(parts);
                        break;
                    case "LIST_ACTIVITIES":
                        handleListActivities(parts);
                        break;
                    case "LIST_FAVORITES":
                        handleListFavorites(parts);
                        break;
                    case "LIST_BORROWED":
                        handleListBorrowed(parts);
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

        // Special handling for admin login
        if ("admin".equals(username) && "admin".equals(password)) {
            out.println("LOGIN_SUCCESS|1|admin");
            return;
        }

        try (Connection conn = getConnection()) {
            // Add status column if it doesn't exist
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("ALTER TABLE users ADD COLUMN status TEXT DEFAULT 'active'");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, role, status FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                String status = rs.getString("status");
                
                // Check if account is locked
                if ("locked".equals(status)) {
                    out.println("LOGIN_FAIL|ACCOUNT_LOCKED|Tài khoản của bạn đã bị khóa, vui lòng đến thư viện hoặc liên hệ số 1900 2004 để biết chi tiết");
                    return;
                }
                
                out.println("LOGIN_SUCCESS|" + id + "|" + role);
            } else {
                out.println("LOGIN_FAIL|Invalid credentials");
            }
        } catch (Exception e) {
            out.println("LOGIN_FAIL|" + e.getMessage());
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 6) { out.println("REGISTER_FAIL|Missing params"); return; }
        String username = parts[1];
        String password = parts[2];
        String phone = parts[3];
        String email = parts[4];
        String avatar = parts.length > 5 ? parts[5] : "";
        String role = "user";
        try {
            UserDAO dao = new UserDAO();
            int result = dao.createUser(username, password, role, phone, email, avatar);
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

            // Ghi lịch sử mượn
            PreparedStatement act = conn.prepareStatement("INSERT INTO activities(user_id, book_id, action, action_time) VALUES(?,?,?,datetime('now'))");
            act.setInt(1, userId);
            act.setInt(2, bookId);
            act.setString(3, "borrow");
            act.executeUpdate();

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
                // Ghi lịch sử trả
                PreparedStatement act = conn.prepareStatement("INSERT INTO activities(user_id, book_id, action, action_time) VALUES(?,?,?,datetime('now'))");
                act.setInt(1, userId);
                act.setInt(2, bookId);
                act.setString(3, "return");
                act.executeUpdate();
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

    private void handleFavorite(String[] parts) {
        // FAVORITE|userId|bookId
        if (parts.length < 3) { out.println("FAVORITE_FAIL|Missing params"); return; }
        int userId = Integer.parseInt(parts[1]);
        int bookId = Integer.parseInt(parts[2]);
        try (Connection conn = getConnection()) {
            // Kiểm tra bảng activities, nếu chưa có thì tạo bảng
            try {
                conn.createStatement().executeQuery("SELECT id FROM activities LIMIT 1");
            } catch (Exception e) {
                conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS activities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "book_id INTEGER," +
                    "action TEXT," +
                    "action_time TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(book_id) REFERENCES books(id))"
                );
            }
            // Kiểm tra cột favorite có tồn tại không
            try {
                conn.createStatement().executeQuery("SELECT favorite FROM books LIMIT 1");
            } catch (Exception e) {
                out.println("FAVORITE_FAIL|Cột favorite chưa tồn tại trong bảng books");
                return;
            }
            // Kiểm tra sách có tồn tại không
            PreparedStatement check = conn.prepareStatement("SELECT id FROM books WHERE id=?");
            check.setInt(1, bookId);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                out.println("FAVORITE_FAIL|Book not found");
                return;
            }
            // Cập nhật trường favorite
            PreparedStatement ps = conn.prepareStatement("UPDATE books SET favorite=1 WHERE id=?");
            ps.setInt(1, bookId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                out.println("FAVORITE_FAIL|Update failed");
                return;
            }
            // Ghi lịch sử yêu thích
            PreparedStatement act = conn.prepareStatement("INSERT INTO activities(user_id, book_id, action, action_time) VALUES(?,?,?,datetime('now'))");
            act.setInt(1, userId);
            act.setInt(2, bookId);
            act.setString(3, "favorite");
            act.executeUpdate();
            out.println("FAVORITE_SUCCESS");
        } catch (Exception e) {
            out.println("FAVORITE_FAIL|" + e.getMessage());
        }
    }

    private void handleListActivities(String[] parts) {
        // LIST_ACTIVITIES|userId
        if (parts.length < 2) { out.println("ACTIVITIES_FAIL|Missing params"); return; }
        int userId = Integer.parseInt(parts[1]);
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT a.id, a.action, a.action_time, b.title FROM activities a LEFT JOIN books b ON a.book_id=b.id WHERE a.user_id=? ORDER BY a.action_time DESC LIMIT 50");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                String id = rs.getString("id");
                String action = rs.getString("action");
                String time = rs.getString("action_time");
                String title = rs.getString("title");
                sb.append(id).append(" - ").append(title).append(" - ").append(action).append(" - ").append(time).append(";");
            }
            out.println("ACTIVITIES_LIST|" + sb.toString());
        } catch (Exception e) {
            out.println("ACTIVITIES_FAIL|" + e.getMessage());
        }
    }

    private void handleListFavorites(String[] parts) {
        // LIST_FAVORITES|userId
        if (parts.length < 2) { out.println("FAVORITES_FAIL|Missing params"); return; }
        int userId = Integer.parseInt(parts[1]);
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT b.id, b.title, b.author FROM books b INNER JOIN favorites f ON b.id = f.book_id WHERE f.user_id = ? ORDER BY f.added_date DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                sb.append(id).append(" - ").append(title).append(" - ").append(author).append(";");
            }
            out.println("FAVORITES_LIST|" + sb.toString());
        } catch (Exception e) {
            out.println("FAVORITES_FAIL|" + e.getMessage());
        }
    }

    private void handleListBorrowed(String[] parts) {
        // LIST_BORROWED|userId
        if (parts.length < 2) { out.println("BORROWED_FAIL|Missing params"); return; }
        int userId = Integer.parseInt(parts[1]);
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT b.title, b.author, br.borrow_date, datetime(br.borrow_date, '+30 days') as due_date FROM borrows br INNER JOIN books b ON br.book_id = b.id WHERE br.user_id = ? AND br.return_date IS NULL");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                String borrowDate = rs.getString("borrow_date");
                String dueDate = rs.getString("due_date");
                sb.append(title).append(",").append(author).append(",").append(borrowDate).append(",").append(dueDate).append(";");
            }
            out.println("BORROWED_LIST|" + sb.toString());
        } catch (Exception e) {
            out.println("BORROWED_FAIL|" + e.getMessage());
        }
    }
}

