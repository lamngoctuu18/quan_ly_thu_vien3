package app;

import client.ClientUI;
import javax.swing.SwingUtilities;

public class TestAdminModeWithCheckbox {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            ui.setVisible(true);
            
            System.out.println("ClientUI with Admin Mode and Checkbox Selection is running!");
            System.out.println("\nFeatures:");
            System.out.println("✅ Toggle Admin Mode với nút 'Quản lý'");
            System.out.println("✅ Checkbox selection cho từng sách");
            System.out.println("✅ Chọn tất cả / Bỏ chọn tất cả");
            System.out.println("✅ Xóa hàng loạt sách đã chọn");
            System.out.println("✅ Hiển thị số lượng sách đã chọn");
            System.out.println("✅ Phân trang 16 sách/trang");
            System.out.println("\nInstructions:");
            System.out.println("1. Click nút 'Quản lý' để bật chế độ admin");
            System.out.println("2. Checkbox sẽ xuất hiện trên mỗi sách");
            System.out.println("3. Chọn sách cần xóa bằng checkbox");
            System.out.println("4. Dùng 'Chọn tất cả' hoặc 'Bỏ chọn'");
            System.out.println("5. Click 'Xóa đã chọn' để xóa sách");
        });
    }
}