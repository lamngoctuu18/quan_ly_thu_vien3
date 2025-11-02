package client;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Avatar Manager - Quản lý upload và update avatar cho user
 * 
 * Features:
 * - Upload avatar từ local file
 * - Auto crop & resize
 * - Save to local storage
 * - Update database
 * - Validate image format
 * 
 * @author GitHub Copilot
 * @date November 2, 2025
 */
public class AvatarManager {
    
    private static final String AVATARS_DIR = "C:/data/avatars/";
    private static final int AVATAR_SIZE = 200; // Original size before resize
    private static final String[] VALID_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    
    /**
     * Upload avatar từ file chooser dialog
     * @param parentFrame Parent frame để show dialog
     * @param userId User ID
     * @param callback Callback sau khi upload thành công
     */
    public static void uploadAvatar(JFrame parentFrame, int userId, AvatarUploadCallback callback) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh đại diện");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image files (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(parentFrame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Validate file
            if (!isValidImageFile(selectedFile)) {
                JOptionPane.showMessageDialog(parentFrame,
                    "File không hợp lệ! Vui lòng chọn file ảnh (.jpg, .jpeg, .png, .gif)",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Show loading
            JDialog loadingDialog = createLoadingDialog(parentFrame, "Đang upload avatar...");
            loadingDialog.setVisible(true);
            
            // Process in background
            new Thread(() -> {
                try {
                    // Create avatars directory if not exists
                    File avatarsDir = new File(AVATARS_DIR);
                    if (!avatarsDir.exists()) {
                        avatarsDir.mkdirs();
                    }
                    
                    // Generate unique filename
                    String extension = getFileExtension(selectedFile);
                    String newFileName = userId + "_" + System.currentTimeMillis() + extension;
                    File destFile = new File(AVATARS_DIR + newFileName);
                    
                    // Process image (crop & resize)
                    BufferedImage processedImage = processImage(selectedFile);
                    
                    // Save processed image
                    javax.imageio.ImageIO.write(processedImage, "jpg", destFile);
                    
                    // Update database
                    String avatarPath = destFile.getAbsolutePath();
                    updateAvatarInDatabase(userId, avatarPath);
                    
                    // Callback on success
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        
                        if (callback != null) {
                            callback.onSuccess(avatarPath);
                        }
                        
                        JOptionPane.showMessageDialog(parentFrame,
                            "Upload avatar thành công!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    });
                    
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        loadingDialog.dispose();
                        
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                        
                        JOptionPane.showMessageDialog(parentFrame,
                            "Lỗi khi upload avatar: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Process image: crop to square and resize
     */
    private static BufferedImage processImage(File file) throws Exception {
        BufferedImage original = javax.imageio.ImageIO.read(file);
        
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Crop to square (center)
        int cropSize = Math.min(width, height);
        int x = (width - cropSize) / 2;
        int y = (height - cropSize) / 2;
        
        BufferedImage cropped = original.getSubimage(x, y, cropSize, cropSize);
        
        // Resize to AVATAR_SIZE
        Image scaled = cropped.getScaledInstance(AVATAR_SIZE, AVATAR_SIZE, Image.SCALE_SMOOTH);
        
        // Convert to BufferedImage
        BufferedImage result = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        
        return result;
    }
    
    /**
     * Update avatar path in database
     */
    private static void updateAvatarInDatabase(int userId, String avatarPath) throws Exception {
        String sql = "UPDATE users SET avatar = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, avatarPath);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Validate if file is valid image
     */
    private static boolean isValidImageFile(File file) {
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        for (String ext : VALID_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get file extension
     */
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot) : ".jpg";
    }
    
    /**
     * Create loading dialog
     */
    private static JDialog createLoadingDialog(JFrame parent, String message) {
        JDialog dialog = new JDialog(parent, "Loading...", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        dialog.add(panel);
        dialog.setSize(300, 120);
        dialog.setLocationRelativeTo(parent);
        
        return dialog;
    }
    
    /**
     * Callback interface
     */
    public interface AvatarUploadCallback {
        void onSuccess(String avatarPath);
        void onError(String errorMessage);
    }
}
