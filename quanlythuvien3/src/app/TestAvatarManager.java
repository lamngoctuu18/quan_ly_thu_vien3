package app;

import javax.swing.*;
import java.awt.*;
import client.AvatarManager;

/**
 * Test Avatar Manager with upload functionality
 * 
 * How to use:
 * 1. Run this test
 * 2. Click "Upload Avatar" button
 * 3. Select an image file
 * 4. Image will be cropped, resized and saved
 * 
 * @author GitHub Copilot
 */
public class TestAvatarManager extends JFrame {
    
    private JLabel lblAvatar;
    private int testUserId = 1; // User ID để test
    
    public TestAvatarManager() {
        setTitle("Test Avatar Upload");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));
        
        // Panel chứa avatar
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        avatarPanel.setBorder(BorderFactory.createTitledBorder("Avatar Preview"));
        
        // Default avatar
        lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(150, 150));
        lblAvatar.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setText("No Avatar");
        
        avatarPanel.add(lblAvatar);
        add(avatarPanel, BorderLayout.CENTER);
        
        // Panel chứa buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnUpload = new JButton("Upload Avatar");
        btnUpload.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUpload.setBackground(new Color(88, 166, 255));
        btnUpload.setForeground(Color.WHITE);
        btnUpload.setFocusPainted(false);
        btnUpload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnUpload.addActionListener(e -> {
            AvatarManager.uploadAvatar(this, testUserId, new AvatarManager.AvatarUploadCallback() {
                @Override
                public void onSuccess(String avatarPath) {
                    System.out.println("✅ Upload thành công: " + avatarPath);
                    
                    // Load and display avatar
                    ImageIcon icon = new ImageIcon(avatarPath);
                    Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    lblAvatar.setIcon(new ImageIcon(scaled));
                    lblAvatar.setText("");
                }
                
                @Override
                public void onError(String errorMessage) {
                    System.err.println("❌ Upload thất bại: " + errorMessage);
                }
            });
        });
        
        buttonPanel.add(btnUpload);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel("✓ Upload ảnh từ file local"));
        infoPanel.add(new JLabel("✓ Auto crop & resize to 200x200"));
        infoPanel.add(new JLabel("✓ Lưu vào C:/data/avatars/"));
        add(infoPanel, BorderLayout.NORTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TestAvatarManager().setVisible(true);
        });
    }
}
