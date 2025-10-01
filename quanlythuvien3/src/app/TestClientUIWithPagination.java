package app;

import client.ClientUI;
import javax.swing.SwingUtilities;

public class TestClientUIWithPagination {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            ui.setVisible(true);
        });
    }
}