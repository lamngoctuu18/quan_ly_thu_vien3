package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý cache ảnh thông minh - Tối ưu hiệu suất
 * 
 * Cơ chế hoạt động:
 * 1. Memory Cache (RAM): Lưu ảnh đã load vào bộ nhớ để truy xuất siêu nhanh
 * 2. Disk Cache (Ổ đĩa): Lưu ảnh đã tải về để không cần load lại từ internet
 * 
 * Luồng xử lý:
 * - Khi cần ảnh: Kiểm tra RAM → Ổ đĩa → Internet (theo thứ tự)
 * - Tự động tải ảnh về khi có URL mới
 * - Tự động xóa ảnh khi xóa sách
 */
public class ImageCacheManager {
    
    // Singleton instance
    private static ImageCacheManager instance;
    
    // Thư mục lưu ảnh trên ổ đĩa
    private static final String CACHE_DIR = "C:/data/library_images/";
    
    // Memory cache - Lưu ảnh trong RAM để truy xuất nhanh
    private final Map<String, ImageIcon> memoryCache;
    
    // Giới hạn số lượng ảnh trong RAM (tránh tràn bộ nhớ)
    private static final int MAX_MEMORY_CACHE = 100;
    
    private ImageCacheManager() {
        this.memoryCache = new HashMap<>();
        initializeCacheDirectory();
    }
    
    /**
     * Lấy instance của ImageCacheManager (Singleton pattern)
     */
    public static synchronized ImageCacheManager getInstance() {
        if (instance == null) {
            instance = new ImageCacheManager();
        }
        return instance;
    }
    
    /**
     * Khởi tạo thư mục cache nếu chưa tồn tại
     */
    private void initializeCacheDirectory() {
        try {
            Path cachePath = Paths.get(CACHE_DIR);
            if (!Files.exists(cachePath)) {
                Files.createDirectories(cachePath);
                System.out.println("✅ Đã tạo thư mục cache ảnh: " + CACHE_DIR);
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi tạo thư mục cache: " + e.getMessage());
        }
    }
    
    /**
     * Tải ảnh từ URL và lưu vào cache
     * 
     * @param imageUrl URL của ảnh cần tải
     * @param bookId ID của sách (để đặt tên file)
     * @return Đường dẫn local của ảnh đã tải về, hoặc null nếu lỗi
     */
    public String downloadAndCacheImage(String imageUrl, String bookId) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Tạo tên file duy nhất dựa trên URL và bookId
            String fileName = generateFileName(imageUrl, bookId);
            String localPath = CACHE_DIR + fileName;
            
            // Kiểm tra xem ảnh đã tồn tại chưa
            File localFile = new File(localPath);
            if (localFile.exists()) {
                System.out.println("📁 Ảnh đã tồn tại trong cache: " + fileName);
                return localPath;
            }
            
            // Tải ảnh từ URL
            System.out.println("⬇️ Đang tải ảnh từ: " + imageUrl);
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            
            if (image == null) {
                System.err.println("❌ Không thể đọc ảnh từ URL: " + imageUrl);
                return null;
            }
            
            // Lưu ảnh vào ổ đĩa
            String fileExtension = getFileExtension(imageUrl);
            ImageIO.write(image, fileExtension, localFile);
            
            System.out.println("✅ Đã lưu ảnh vào: " + localPath);
            return localPath;
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi tải ảnh: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy ảnh từ cache (RAM hoặc ổ đĩa) hoặc tải từ URL
     * 
     * Thứ tự ưu tiên:
     * 1. Kiểm tra Memory Cache (RAM)
     * 2. Kiểm tra Disk Cache (Ổ đĩa)
     * 3. Tải từ URL nếu chưa có
     * 
     * @param imagePath Đường dẫn ảnh (local hoặc URL)
     * @param bookId ID sách
     * @param maxWidth Chiều rộng tối đa
     * @param maxHeight Chiều cao tối đa
     * @return ImageIcon đã được scale, hoặc null nếu lỗi
     */
    public ImageIcon getImage(String imagePath, String bookId, int maxWidth, int maxHeight) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }
        
        // Tạo cache key duy nhất
        String cacheKey = imagePath + "_" + maxWidth + "x" + maxHeight;
        
        // 1. Kiểm tra Memory Cache (nhanh nhất)
        if (memoryCache.containsKey(cacheKey)) {
            System.out.println("🚀 Load ảnh từ RAM cache");
            return memoryCache.get(cacheKey);
        }
        
        ImageIcon icon = null;
        
        try {
            // 2. Xác định nguồn ảnh
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                // Là URL - cần tải về
                String localPath = downloadAndCacheImage(imagePath, bookId);
                if (localPath != null) {
                    icon = loadImageFromFile(localPath);
                }
            } else {
                // Là đường dẫn local
                icon = loadImageFromFile(imagePath);
            }
            
            // 3. Scale ảnh nếu cần
            if (icon != null && icon.getIconWidth() > 0) {
                icon = scaleImage(icon, maxWidth, maxHeight);
                
                // 4. Lưu vào Memory Cache
                addToMemoryCache(cacheKey, icon);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi load ảnh: " + e.getMessage());
        }
        
        return icon;
    }
    
    /**
     * Load ảnh từ file local
     */
    private ImageIcon loadImageFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("❌ File không tồn tại: " + filePath);
                return null;
            }
            
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                return null;
            }
            
            System.out.println("📂 Load ảnh từ ổ đĩa: " + filePath);
            return new ImageIcon(img);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi đọc file ảnh: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Scale ảnh theo tỷ lệ (giữ nguyên tỷ lệ khung hình)
     */
    private ImageIcon scaleImage(ImageIcon original, int maxWidth, int maxHeight) {
        int originalWidth = original.getIconWidth();
        int originalHeight = original.getIconHeight();
        
        // Tính tỷ lệ scale
        double scaleWidth = (double) maxWidth / originalWidth;
        double scaleHeight = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleWidth, scaleHeight);
        
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        
        Image scaledImage = original.getImage().getScaledInstance(
            scaledWidth, scaledHeight, Image.SCALE_SMOOTH
        );
        
        return new ImageIcon(scaledImage);
    }
    
    /**
     * Thêm ảnh vào Memory Cache với giới hạn số lượng
     */
    private void addToMemoryCache(String key, ImageIcon icon) {
        // Nếu vượt quá giới hạn, xóa ảnh cũ nhất
        if (memoryCache.size() >= MAX_MEMORY_CACHE) {
            String oldestKey = memoryCache.keySet().iterator().next();
            memoryCache.remove(oldestKey);
            System.out.println("🗑️ Đã xóa ảnh cũ khỏi RAM cache");
        }
        
        memoryCache.put(key, icon);
    }
    
    /**
     * Xóa ảnh khỏi cache khi xóa sách
     */
    public void removeImageFromCache(String bookId) {
        try {
            // Xóa từ Memory Cache
            memoryCache.entrySet().removeIf(entry -> 
                entry.getKey().contains("book_" + bookId)
            );
            
            // Xóa từ Disk Cache
            File cacheDir = new File(CACHE_DIR);
            File[] files = cacheDir.listFiles((dir, name) -> 
                name.startsWith("book_" + bookId)
            );
            
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        System.out.println("🗑️ Đã xóa ảnh: " + file.getName());
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi xóa cache: " + e.getMessage());
        }
    }
    
    /**
     * Xóa toàn bộ Memory Cache
     */
    public void clearMemoryCache() {
        memoryCache.clear();
        System.out.println("🗑️ Đã xóa toàn bộ RAM cache");
    }
    
    /**
     * Tạo tên file duy nhất từ URL và bookId
     */
    private String generateFileName(String url, String bookId) {
        try {
            // Tạo hash từ URL để tên file ngắn gọn và duy nhất
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(url.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            String extension = getFileExtension(url);
            return "book_" + bookId + "_" + hexString.substring(0, 8) + "." + extension;
            
        } catch (Exception e) {
            // Fallback: dùng timestamp
            return "book_" + bookId + "_" + System.currentTimeMillis() + ".jpg";
        }
    }
    
    /**
     * Lấy phần mở rộng file từ URL
     */
    private String getFileExtension(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".png")) return "png";
        if (lower.contains(".gif")) return "gif";
        if (lower.contains(".bmp")) return "bmp";
        return "jpg"; // Mặc định
    }
    
    /**
     * Lấy thông tin cache
     */
    public String getCacheInfo() {
        File cacheDir = new File(CACHE_DIR);
        int diskCacheCount = 0;
        long totalSize = 0;
        
        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                diskCacheCount = files.length;
                for (File file : files) {
                    totalSize += file.length();
                }
            }
        }
        
        return String.format(
            "💾 Cache Info:\n" +
            "- Ảnh trong RAM: %d/%d\n" +
            "- Ảnh trên ổ đĩa: %d\n" +
            "- Dung lượng: %.2f MB",
            memoryCache.size(), MAX_MEMORY_CACHE,
            diskCacheCount,
            totalSize / 1024.0 / 1024.0
        );
    }
}
