package client;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Loading Utilities
 */
public class LoadingUtils {
    
    /**
     * Show a simple loading dialog
     */
    public static JDialog showLoadingDialog(JFrame parent, String message) {
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
        JDialog loadingDialog = showLoadingDialog(parent, message);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }
            
            @Override
            protected void done() {
                loadingDialog.setVisible(false);
                loadingDialog.dispose();
            }
        };
        
        // Show dialog and start task
        SwingUtilities.invokeLater(() -> {
            worker.execute();
            loadingDialog.setVisible(true);
        });
    }
}