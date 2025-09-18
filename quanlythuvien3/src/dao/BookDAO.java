package dao;

public class BookDAO {
    // Nếu có phương thức kết nối, hãy sửa như sau:
    private java.sql.Connection getConn() throws Exception {
        return java.sql.DriverManager.getConnection("jdbc:sqlite:C:/data/library.db");
    }
}
