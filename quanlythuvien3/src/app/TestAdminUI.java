package app;

import client.AdminUI;
import javax.swing.SwingUtilities;

public class TestAdminUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminUI adminUI = new AdminUI();
            adminUI.setVisible(true);
        });
    }
}