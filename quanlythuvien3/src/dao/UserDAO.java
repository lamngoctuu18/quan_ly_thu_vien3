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

    // Sửa lại để nhận thêm phone, email và avatar
    public int createUser(String username, String password, String role, String phone, String email, String avatar) throws Exception {
        try (Connection c = getConn()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO users(username,password,role,phone,email,avatar) VALUES(?,?,?,?,?,?)");
            ps.setString(1, username); 
            ps.setString(2, password); 
            ps.setString(3, role);
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.setString(6, avatar);
            return ps.executeUpdate();
        }
    }

    // Overload for backward compatibility
    public int createUser(String username, String password, String role, String phone, String email) throws Exception {
        return createUser(username, password, role, phone, email, "");
    }

    public ResultSet findUser(String username, String password) throws Exception {
        Connection c = getConn();
        PreparedStatement ps = c.prepareStatement("SELECT id, role FROM users WHERE username=? AND password=?");
        ps.setString(1, username); ps.setString(2, password);
        return ps.executeQuery();
    }
}