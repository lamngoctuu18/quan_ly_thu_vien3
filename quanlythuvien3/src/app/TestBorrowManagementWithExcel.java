package app;

import client.BorrowManagementUI;
import javax.swing.SwingUtilities;

public class TestBorrowManagementWithExcel {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BorrowManagementUI ui = new BorrowManagementUI();
            ui.setVisible(true);
        });
    }
}