package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    private Connection getConn() throws Exception { 
        // Sửa lại đường dẫn cho đúng với file CSDL đã tạo
        return DriverManager.getConnection("jdbc:sqlite:C:/data/library.db"); 
    }

    // Sửa lại để nhận thêm phone và email
    public int createUser(String username, String password, String role, String phone, String email) throws Exception {
        try (Connection c = getConn()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO users(username,password,role,phone,email) VALUES(?,?,?,?,?)");
            ps.setString(1, username); 
            ps.setString(2, password); 
            ps.setString(3, role);
            ps.setString(4, phone);
            ps.setString(5, email);
            return ps.executeUpdate();
        }
    }

    public ResultSet findUser(String username, String password) throws Exception {
        Connection c = getConn();
        PreparedStatement ps = c.prepareStatement("SELECT id, role FROM users WHERE username=? AND password=?");
        ps.setString(1, username); ps.setString(2, password);
        return ps.executeQuery();
    }
}