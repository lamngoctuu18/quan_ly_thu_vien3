package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class AddSampleBooks {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db")) {
            
            // Xóa dữ liệu cũ
            conn.createStatement().execute("DELETE FROM books");
            
            // Thêm sách mẫu với ảnh bìa
            String sql = "INSERT INTO books (title, author, publisher, year, quantity, category, cover_image) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // Sách 1
            ps.setString(1, "Tôi thấy hoa vàng trên cỏ xanh");
            ps.setString(2, "Nguyễn Nhật Ánh");
            ps.setString(3, "NXB Trẻ");
            ps.setString(4, "2010");
            ps.setInt(5, 15);
            ps.setString(6, "Văn học – Tiểu thuyết");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/5e/18/24/2a6154ba08df6ce6161c13f4303fa19e.jpg");
            ps.executeUpdate();
            
            // Sách 2
            ps.setString(1, "Đắc Nhân Tâm");
            ps.setString(2, "Dale Carnegie");
            ps.setString(3, "NXB Tổng hợp TP.HCM");
            ps.setString(4, "2017");
            ps.setInt(5, 20);
            ps.setString(6, "Tâm lý – Kỹ năng sống");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/fe/6d/2b/384d96baa1534b8887c6f7de6b4c7442.jpg");
            ps.executeUpdate();
            
            // Sách 3
            ps.setString(1, "Clean Code");
            ps.setString(2, "Robert C. Martin");
            ps.setString(3, "Prentice Hall");
            ps.setString(4, "2008");
            ps.setInt(5, 10);
            ps.setString(6, "Khoa học – Công nghệ");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/29/82/1dc5c2d6db8e52c5e60f527b8ac8f2a3.jpg");
            ps.executeUpdate();
            
            // Sách 4
            ps.setString(1, "Sapiens: Lược sử loài người");
            ps.setString(2, "Yuval Noah Harari");
            ps.setString(3, "NXB Thế Giới");
            ps.setString(4, "2018");
            ps.setInt(5, 12);
            ps.setString(6, "Lịch sử – Địa lý");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/29/28/8b/a60ccc8d548d3bda57a5e4d1d3dd1a93.jpg");
            ps.executeUpdate();
            
            // Sách 5
            ps.setString(1, "Nghĩ giàu làm giàu");
            ps.setString(2, "Napoleon Hill");
            ps.setString(3, "NXB Lao Động");
            ps.setString(4, "2016");
            ps.setInt(5, 18);
            ps.setString(6, "Kinh tế – Quản trị");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/eb/b8/43c2b823dfaaa3e14d069ac9d108d4e2.jpg");
            ps.executeUpdate();
            
            // Sách 6
            ps.setString(1, "Doraemon - Tập 1");
            ps.setString(2, "Fujiko F. Fujio");
            ps.setString(3, "NXB Kim Đồng");
            ps.setString(4, "2020");
            ps.setInt(5, 25);
            ps.setString(6, "Trẻ em – Thiếu nhi");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/35/ea/a6/b75271e2c18c7a19b96fd8c5a0f57b6d.jpg");
            ps.executeUpdate();
            
            // Sách 7
            ps.setString(1, "Từ điển Anh - Việt");
            ps.setString(2, "Lê Bá Khánh");
            ps.setString(3, "NXB Thế Giới");
            ps.setString(4, "2019");
            ps.setInt(5, 8);
            ps.setString(6, "Ngoại ngữ – Từ điển");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/67/50/a6/87d6c0b5bfb9bfb5b29ecce8b7a37260.jpg");
            ps.executeUpdate();
            
            // Sách 8
            ps.setString(1, "Lịch sử Việt Nam");
            ps.setString(2, "Trần Trọng Kim");
            ps.setString(3, "NXB Văn Học");
            ps.setString(4, "2015");
            ps.setInt(5, 14);
            ps.setString(6, "Lịch sử – Địa lý");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/15/c9/8b/d99b0a3ae4cfb9b5b56d5e8b4f3e92eb.jpg");
            ps.executeUpdate();
            
            // Sách 9
            ps.setString(1, "Atomic Habits");
            ps.setString(2, "James Clear");
            ps.setString(3, "NXB Thế Giới");
            ps.setString(4, "2021");
            ps.setInt(5, 16);
            ps.setString(6, "Tâm lý – Kỹ năng sống");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/eb/b8/43c2b823dfaaa3e14d069ac9d108d4e2.jpg");
            ps.executeUpdate();
            
            // Sách 10
            ps.setString(1, "7 Thói quen hiệu quả");
            ps.setString(2, "Stephen R. Covey");
            ps.setString(3, "NXB Tổng hợp TP.HCM");
            ps.setString(4, "2019");
            ps.setInt(5, 13);
            ps.setString(6, "Tâm lý – Kỹ năng sống");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/29/28/8b/a60ccc8d548d3bda57a5e4d1d3dd1a93.jpg");
            ps.executeUpdate();
            
            // Sách 11
            ps.setString(1, "Nhà Giả Kim");
            ps.setString(2, "Paulo Coelho");
            ps.setString(3, "NXB Hội Nhà Văn");
            ps.setString(4, "2020");
            ps.setInt(5, 22);
            ps.setString(6, "Văn học – Tiểu thuyết");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/5e/18/24/2a6154ba08df6ce6161c13f4303fa19e.jpg");
            ps.executeUpdate();
            
            // Sách 12
            ps.setString(1, "Java: The Complete Reference");
            ps.setString(2, "Herbert Schildt");
            ps.setString(3, "McGraw-Hill");
            ps.setString(4, "2022");
            ps.setInt(5, 8);
            ps.setString(6, "Khoa học – Công nghệ");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/29/82/1dc5c2d6db8e52c5e60f527b8ac8f2a3.jpg");
            ps.executeUpdate();
            
            // Sách 13
            ps.setString(1, "Thiên tài bên trái, kẻ điên bên phải");
            ps.setString(2, "Cao Minh");
            ps.setString(3, "NXB Thế Giới");
            ps.setString(4, "2018");
            ps.setInt(5, 19);
            ps.setString(6, "Tâm lý – Kỹ năng sống");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/fe/6d/2b/384d96baa1534b8887c6f7de6b4c7442.jpg");
            ps.executeUpdate();
            
            // Sách 14
            ps.setString(1, "Tuổi trẻ đáng giá bao nhiêu");
            ps.setString(2, "Rosie Nguyễn");
            ps.setString(3, "NXB Hội Nhà Văn");
            ps.setString(4, "2017");
            ps.setInt(5, 24);
            ps.setString(6, "Tâm lý – Kỹ năng sống");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/35/ea/a6/b75271e2c18c7a19b96fd8c5a0f57b6d.jpg");
            ps.executeUpdate();
            
            // Sách 15
            ps.setString(1, "Sherlock Holmes - Bộ sưu tập");
            ps.setString(2, "Arthur Conan Doyle");
            ps.setString(3, "NXB Văn Học");
            ps.setString(4, "2016");
            ps.setInt(5, 11);
            ps.setString(6, "Văn học – Tiểu thuyết");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/67/50/a6/87d6c0b5bfb9bfb5b29ecce8b7a37260.jpg");
            ps.executeUpdate();
            
            // Sách 16
            ps.setString(1, "Toán học cao cấp");
            ps.setString(2, "Nguyễn Đình Trí");
            ps.setString(3, "NXB Giáo Dục");
            ps.setString(4, "2021");
            ps.setInt(5, 7);
            ps.setString(6, "Giáo trình – Học thuật");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/15/c9/8b/d99b0a3ae4cfb9b5b56d5e8b4f3e92eb.jpg");
            ps.executeUpdate();
            
            // Sách 17
            ps.setString(1, "Python Crash Course");
            ps.setString(2, "Eric Matthes");
            ps.setString(3, "No Starch Press");
            ps.setString(4, "2023");
            ps.setInt(5, 12);
            ps.setString(6, "Khoa học – Công nghệ");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/29/82/1dc5c2d6db8e52c5e60f527b8ac8f2a3.jpg");
            ps.executeUpdate();
            
            // Sách 18
            ps.setString(1, "Marketing 4.0");
            ps.setString(2, "Philip Kotler");
            ps.setString(3, "NXB Thế Giới");
            ps.setString(4, "2019");
            ps.setInt(5, 15);
            ps.setString(6, "Kinh tế – Quản trị");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/eb/b8/43c2b823dfaaa3e14d069ac9d108d4e2.jpg");
            ps.executeUpdate();
            
            // Sách 19
            ps.setString(1, "Chiến tranh Việt Nam");
            ps.setString(2, "Stanley Karnow");
            ps.setString(3, "NXB Chính Trị Quốc Gia");
            ps.setString(4, "2018");
            ps.setInt(5, 9);
            ps.setString(6, "Lịch sử – Địa lý");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/29/28/8b/a60ccc8d548d3bda57a5e4d1d3dd1a93.jpg");
            ps.executeUpdate();
            
            // Sách 20
            ps.setString(1, "Conan - Thám tử lừng danh");
            ps.setString(2, "Gosho Aoyama");
            ps.setString(3, "NXB Kim Đồng");
            ps.setString(4, "2022");
            ps.setInt(5, 30);
            ps.setString(6, "Trẻ em – Thiếu nhi");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/35/ea/a6/b75271e2c18c7a19b96fd8c5a0f57b6d.jpg");
            ps.executeUpdate();
            
            // Sách 21
            ps.setString(1, "Tiếng Anh giao tiếp cơ bản");
            ps.setString(2, "Lê Văn Sự");
            ps.setString(3, "NXB Đại Học Quốc Gia");
            ps.setString(4, "2020");
            ps.setInt(5, 18);
            ps.setString(6, "Ngoại ngữ – Từ điển");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/67/50/a6/87d6c0b5bfb9bfb5b29ecce8b7a37260.jpg");
            ps.executeUpdate();
            
            // Sách 22
            ps.setString(1, "Vật lý đại cương");
            ps.setString(2, "Halliday Resnick");
            ps.setString(3, "NXB Giáo Dục");
            ps.setString(4, "2019");
            ps.setInt(5, 6);
            ps.setString(6, "Giáo trình – Học thuật");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/15/c9/8b/d99b0a3ae4cfb9b5b56d5e8b4f3e92eb.jpg");
            ps.executeUpdate();
            
            // Sách 23
            ps.setString(1, "Cẩm nang khởi nghiệp");
            ps.setString(2, "Steve Blank");
            ps.setString(3, "NXB Lao Động");
            ps.setString(4, "2021");
            ps.setInt(5, 14);
            ps.setString(6, "Kinh tế – Quản trị");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/eb/b8/43c2b823dfaaa3e14d069ac9d108d4e2.jpg");
            ps.executeUpdate();
            
            // Sách 24
            ps.setString(1, "Tâm lý học tội phạm");
            ps.setString(2, "David Canter");
            ps.setString(3, "NXB Công An Nhân Dân");
            ps.setString(4, "2020");
            ps.setInt(5, 8);
            ps.setString(6, "Tâm lý – Kỹ năng sống");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/fe/6d/2b/384d96baa1534b8887c6f7de6b4c7442.jpg");
            ps.executeUpdate();
            
            // Sách 25
            ps.setString(1, "Hóa học hữu cơ");
            ps.setString(2, "Morrison & Boyd");
            ps.setString(3, "NXB Khoa Học Kỹ Thuật");
            ps.setString(4, "2018");
            ps.setInt(5, 5);
            ps.setString(6, "Giáo trình – Học thuật");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/15/c9/8b/d99b0a3ae4cfb9b5b56d5e8b4f3e92eb.jpg");
            ps.executeUpdate();
            
            // Sách 26
            ps.setString(1, "Naruto - Tập 1");
            ps.setString(2, "Masashi Kishimoto");
            ps.setString(3, "NXB Kim Đồng");
            ps.setString(4, "2021");
            ps.setInt(5, 28);
            ps.setString(6, "Trẻ em – Thiếu nhi");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/35/ea/a6/b75271e2c18c7a19b96fd8c5a0f57b6d.jpg");
            ps.executeUpdate();
            
            // Sách 27
            ps.setString(1, "Địa lý Việt Nam");
            ps.setString(2, "Viện Địa Lý");
            ps.setString(3, "NXB Khoa Học Xã Hội");
            ps.setString(4, "2019");
            ps.setInt(5, 12);
            ps.setString(6, "Lịch sử – Địa lý");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/29/28/8b/a60ccc8d548d3bda57a5e4d1d3dd1a93.jpg");
            ps.executeUpdate();
            
            // Sách 28
            ps.setString(1, "JavaScript: The Good Parts");
            ps.setString(2, "Douglas Crockford");
            ps.setString(3, "O'Reilly Media");
            ps.setString(4, "2020");
            ps.setInt(5, 10);
            ps.setString(6, "Khoa học – Công nghệ");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/29/82/1dc5c2d6db8e52c5e60f527b8ac8f2a3.jpg");
            ps.executeUpdate();
            
            // Sách 29
            ps.setString(1, "Tư duy nhanh và chậm");
            ps.setString(2, "Daniel Kahneman");
            ps.setString(3, "NXB Thế Giới");
            ps.setString(4, "2017");
            ps.setInt(5, 17);
            ps.setString(6, "Tâm lý – Kỹ năng sống");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/fe/6d/2b/384d96baa1534b8887c6f7de6b4c7442.jpg");
            ps.executeUpdate();
            
            // Sách 30
            ps.setString(1, "Kinh tế vĩ mô");
            ps.setString(2, "N. Gregory Mankiw");
            ps.setString(3, "NXB Thống Kê");
            ps.setString(4, "2022");
            ps.setInt(5, 9);
            ps.setString(6, "Kinh tế – Quản trị");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/eb/b8/43c2b823dfaaa3e14d069ac9d108d4e2.jpg");
            ps.executeUpdate();
            
            // Sách 31
            ps.setString(1, "Truyện Kiều");
            ps.setString(2, "Nguyễn Du");
            ps.setString(3, "NXB Văn Học");
            ps.setString(4, "2015");
            ps.setInt(5, 20);
            ps.setString(6, "Văn học – Tiểu thuyết");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/5e/18/24/2a6154ba08df6ce6161c13f4303fa19e.jpg");
            ps.executeUpdate();
            
            // Sách 32
            ps.setString(1, "Từ điển tiếng Hàn");
            ps.setString(2, "Kim Min-jung");
            ps.setString(3, "NXB Đại Học Quốc Gia");
            ps.setString(4, "2021");
            ps.setInt(5, 11);
            ps.setString(6, "Ngoại ngữ – Từ điển");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/67/50/a6/87d6c0b5bfb9bfb5b29ecce8b7a37260.jpg");
            ps.executeUpdate();
            
            // Sách 33
            ps.setString(1, "One Piece - Tập 1");
            ps.setString(2, "Eiichiro Oda");
            ps.setString(3, "NXB Kim Đồng");
            ps.setString(4, "2020");
            ps.setInt(5, 35);
            ps.setString(6, "Trẻ em – Thiếu nhi");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/35/ea/a6/b75271e2c18c7a19b96fd8c5a0f57b6d.jpg");
            ps.executeUpdate();
            
            // Sách 34
            ps.setString(1, "Design Patterns");
            ps.setString(2, "Gang of Four");
            ps.setString(3, "Addison-Wesley");
            ps.setString(4, "2019");
            ps.setInt(5, 7);
            ps.setString(6, "Khoa học – Công nghệ");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/29/82/1dc5c2d6db8e52c5e60f527b8ac8f2a3.jpg");
            ps.executeUpdate();
            
            // Sách 35
            ps.setString(1, "Quản trị nhân sự");
            ps.setString(2, "Gary Dessler");
            ps.setString(3, "NXB Lao Động");
            ps.setString(4, "2020");
            ps.setInt(5, 13);
            ps.setString(6, "Kinh tế – Quản trị");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/eb/b8/43c2b823dfaaa3e14d069ac9d108d4e2.jpg");
            ps.executeUpdate();
            
            // Sách 36
            ps.setString(1, "Sinh học phân tử");
            ps.setString(2, "Watson & Crick");
            ps.setString(3, "NXB Khoa Học Tự Nhiên");
            ps.setString(4, "2018");
            ps.setInt(5, 6);
            ps.setString(6, "Giáo trình – Học thuật");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/15/c9/8b/d99b0a3ae4cfb9b5b56d5e8b4f3e92eb.jpg");
            ps.executeUpdate();
            
            // Sách 37
            ps.setString(1, "Triết học Mác-Lênin");
            ps.setString(2, "Bộ Giáo Dục");
            ps.setString(3, "NXB Chính Trị Quốc Gia");
            ps.setString(4, "2019");
            ps.setInt(5, 15);
            ps.setString(6, "Giáo trình – Học thuật");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/15/c9/8b/d99b0a3ae4cfb9b5b56d5e8b4f3e92eb.jpg");
            ps.executeUpdate();
            
            // Sách 38
            ps.setString(1, "Nghệ thuật bán hàng");
            ps.setString(2, "Brian Tracy");
            ps.setString(3, "NXB Thế Giới");
            ps.setString(4, "2021");
            ps.setInt(5, 16);
            ps.setString(6, "Kinh tế – Quản trị");
            ps.setString(7, "https://salt.tikicdn.com/cache/280x280/ts/product/ca/eb/b8/43c2b823dfaaa3e14d069ac9d108d4e2.jpg");
            ps.executeUpdate();
            
            System.out.println("Đã thêm 38 sách mẫu với ảnh bìa thành công!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}