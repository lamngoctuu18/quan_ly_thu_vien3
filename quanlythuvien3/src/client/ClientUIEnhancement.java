package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Simple timeout prevention and loading enhancement for ClientUI
 * Add this to your existing ClientUI without breaking current code
 */
public class ClientUIEnhancement {
    
    private static ClientUIEnhancement instance;
    private Timer keepAliveTimer;
    private boolean isActive = false;
    
    public static ClientUIEnhancement getInstance() {
        if (instance == null) {
            instance = new ClientUIEnhancement();
        }
        return instance;
    }
    
    /**
     * Initialize keep-alive system
     * Call this in ClientUI constructor
     */
    public void initializeKeepAlive(JFrame parentFrame) {
        if (isActive) return;
        
        isActive = true;
        System.out.println("Initializing ClientUI enhancements...");
        
        // Keep-alive timer - ping database every 5 minutes
        keepAliveTimer = new Timer(300000, e -> { // 5 minutes
            SwingUtilities.invokeLater(() -> {
                try {
                    // Simple database ping
                    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
                        conn.createStatement().executeQuery("SELECT 1").close();
                        System.out.println("Keep-alive ping successful");
                    }
                } catch (Exception ex) {
                    System.err.println("Keep-alive ping failed: " + ex.getMessage());
                }
            });
        });
        keepAliveTimer.start();
        
        // Memory cleanup timer - every 10 minutes
        Timer cleanupTimer = new Timer(600000, e -> {
            System.gc(); // Suggest garbage collection
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            long freeMemory = runtime.freeMemory() / 1024 / 1024;
            System.out.println(String.format("Memory: %dMB used, %dMB free", 
                             totalMemory - freeMemory, freeMemory));
        });
        cleanupTimer.start();
        
        // Add window close listener for cleanup
        parentFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
        
        System.out.println("ClientUI enhancements initialized successfully");
    }
    
    /**
     * Show loading dialog and execute task
     * Use this to wrap heavy operations
     */
    public static void executeWithLoading(JFrame parent, String message, Runnable task) {
        // Create loading dialog
        JDialog loadingDialog = new JDialog(parent, "Đang xử lý...", true);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Loading components
        JLabel iconLabel = new JLabel("⏳", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
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
        
        // Execute task in background
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
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (!isActive) return;
        
        System.out.println("Cleaning up ClientUI enhancements...");
        
        if (keepAliveTimer != null && keepAliveTimer.isRunning()) {
            keepAliveTimer.stop();
        }
        
        isActive = false;
        System.out.println("ClientUI enhancements cleanup completed");
    }
    
    /**
     * Check if enhancement is active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Get status information
     */
    public String getStatus() {
        if (!isActive) {
            return "ClientUI enhancements are inactive";
        }
        
        boolean timerActive = keepAliveTimer != null && keepAliveTimer.isRunning();
        return String.format("ClientUI enhancements are %s", 
                           timerActive ? "running" : "stopped");
    }
}