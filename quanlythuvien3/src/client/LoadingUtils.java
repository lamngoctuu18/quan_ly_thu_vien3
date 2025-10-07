package client;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Loading Utilities
 */
public class LoadingUtils {
    // Global toggle to temporarily disable loading UI (useful for debugging/running faster)
    // Default false to temporarily turn off loading as requested
    public static volatile boolean ENABLE_LOADING = false;
    
    /**
     * Show a simple loading dialog
     */
    public static JDialog showLoadingDialog(JFrame parent, String message) {
        if (!ENABLE_LOADING) {
            // Return a dummy dialog that does nothing when loading is disabled
            JDialog dummy = new JDialog(parent, "", false);
            dummy.setSize(0, 0);
            return dummy;
        }

        JDialog loadingDialog = new JDialog(parent, "Đang xử lý...", true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Loading icon
        JLabel iconLabel = new JLabel("⏳");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Message
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);
        
        panel.add(iconLabel, BorderLayout.NORTH);
        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        loadingDialog.setContentPane(panel);
        loadingDialog.setSize(300, 120);
        loadingDialog.setLocationRelativeTo(parent);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        return loadingDialog;
    }
    
    /**
     * Execute task with loading dialog
     */
    public static void executeWithLoading(JFrame parent, String message, Runnable task) {
        if (!ENABLE_LOADING) {
            // Directly run task synchronously when loading is disabled
            task.run();
            return;
        }

        JDialog loadingDialog = showLoadingDialog(parent, message);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    loadingDialog.setVisible(false);
                    loadingDialog.dispose();
                } catch (Exception ignored) {
                }
            }
        };

        // Show dialog and start task
        SwingUtilities.invokeLater(() -> {
            worker.execute();
            loadingDialog.setVisible(true);
        });
    }
}