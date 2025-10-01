package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SystemTimeoutManager - Quản lý timeout cho toàn hệ thống
 * Ngăn chặn timeout và giữ cho ứng dụng luôn hoạt động
 */
public class SystemTimeoutManager {
    private static SystemTimeoutManager instance;
    private Timer heartbeatTimer;
    private AtomicLong lastActivity;
    private static final long TIMEOUT_THRESHOLD = 30 * 60 * 1000; // 30 phút
    private static final int HEARTBEAT_INTERVAL = 5 * 60 * 1000; // 5 phút
    
    private SystemTimeoutManager() {
        lastActivity = new AtomicLong(System.currentTimeMillis());
        initializeHeartbeat();
    }
    
    public static synchronized SystemTimeoutManager getInstance() {
        if (instance == null) {
            instance = new SystemTimeoutManager();
        }
        return instance;
    }
    
    /**
     * Khởi tạo heartbeat system
     */
    private void initializeHeartbeat() {
        heartbeatTimer = new Timer(HEARTBEAT_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performHeartbeat();
            }
        });
        heartbeatTimer.start();
        System.out.println("🔄 System heartbeat initialized - interval: " + HEARTBEAT_INTERVAL/1000 + "s");
    }
    
    /**
     * Thực hiện heartbeat để giữ system alive
     */
    private void performHeartbeat() {
        try {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastActivity = currentTime - lastActivity.get();
            
            System.out.println("💓 System heartbeat - Active for: " + (timeSinceLastActivity/1000) + "s");
            
            // Nếu không có activity trong thời gian dài, thực hiện keep-alive actions
            if (timeSinceLastActivity > TIMEOUT_THRESHOLD / 2) {
                performKeepAliveActions();
            }
            
            // Update activity time
            updateActivity();
            
        } catch (Exception e) {
            System.err.println("❌ Error in system heartbeat: " + e.getMessage());
        }
    }
    
    /**
     * Thực hiện các action để giữ system alive
     */
    private void performKeepAliveActions() {
        try {
            // Ping database
            DatabaseManager.getInstance().executeWithConnection(conn -> {
                conn.createStatement().executeQuery("SELECT 1").close();
                return null;
            });
            
            // Trigger minor UI update để giữ EDT active
            SwingUtilities.invokeLater(() -> {
                // Invisible update
                System.setProperty("system.keepalive", String.valueOf(System.currentTimeMillis()));
            });
            
            System.out.println("🔧 Keep-alive actions performed");
            
        } catch (Exception e) {
            System.err.println("❌ Error in keep-alive actions: " + e.getMessage());
        }
    }
    
    /**
     * Update last activity time
     */
    public void updateActivity() {
        lastActivity.set(System.currentTimeMillis());
    }
    
    /**
     * Start monitoring user activity
     */
    public void startActivityMonitoring() {
        // Monitor mouse và keyboard activity
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
            event -> updateActivity(),
            java.awt.AWTEvent.MOUSE_EVENT_MASK | 
            java.awt.AWTEvent.KEY_EVENT_MASK |
            java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK
        );
        
        System.out.println("🖱️ User activity monitoring started");
    }
    
    /**
     * Stop timeout manager
     */
    public void stop() {
        if (heartbeatTimer != null && heartbeatTimer.isRunning()) {
            heartbeatTimer.stop();
            System.out.println("🔄 System heartbeat stopped");
        }
    }
    
    /**
     * Get system status
     */
    public String getStatus() {
        long timeSinceActivity = System.currentTimeMillis() - lastActivity.get();
        boolean isActive = heartbeatTimer != null && heartbeatTimer.isRunning();
        
        return String.format("SystemTimeout - Active: %s, Last activity: %ds ago", 
                           isActive, timeSinceActivity/1000);
    }
    
    /**
     * Check if system is at risk of timeout
     */
    public boolean isAtRiskOfTimeout() {
        long timeSinceActivity = System.currentTimeMillis() - lastActivity.get();
        return timeSinceActivity > (TIMEOUT_THRESHOLD * 0.8); // 80% của threshold
    }
    
    // Shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.stop();
            }
        }));
    }
}