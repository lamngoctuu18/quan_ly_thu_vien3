package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    private Connection getConn() throws Exception { return DriverManager.getConnection("jdbc:sqlite:library.db"); }

    public int createUser(String username, String password, String role) throws Exception {
        try (Connection c = getConn()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO users(username,password,role) VALUES(?,?,?)");
            ps.setString(1, username); ps.setString(2, password); ps.setString(3, role);
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