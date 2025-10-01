package app;

import client.ClientUI;
import javax.swing.SwingUtilities;

public class TestScrollingWithPagination {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI ui = new ClientUI();
            ui.setVisible(true);
            
            System.out.println("ClientUI with scrolling and pagination is now running!");
            System.out.println("Features:");
            System.out.println("- Mouse wheel scrolling enabled");
            System.out.println("- 16 books per page");
            System.out.println("- Pagination controls at bottom");
            System.out.println("- Smooth scrolling within each page");
        });
    }
}