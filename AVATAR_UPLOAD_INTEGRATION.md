# AVATAR UPLOAD INTEGRATION GUIDE

## 📋 Tổng quan

Hướng dẫn tích hợp **AvatarManager** vào `ClientUI.java` để cho phép user upload avatar từ giao diện chính.

---

## ✅ Điều kiện tiên quyết

- ✓ `AvatarManager.java` đã được compile
- ✓ Database có column `avatar` trong table `users`
- ✓ Thư mục `C:/data/avatars/` sẽ được tạo tự động

---

## 🔧 Tích hợp vào ClientUI

### **Bước 1: Import AvatarManager**

```java
import client.AvatarManager;
```

### **Bước 2: Thêm Upload Button vào User Section**

Trong method `createModernUserSection()`, thêm button để upload avatar:

```java
private JPanel createModernUserSection() {
    // ... existing code ...
    
    // Avatar với click để upload
    lblAvatar = new JLabel();
    lblAvatar.setPreferredSize(new Dimension(32, 32));
    lblAvatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    lblAvatar.setToolTipText("Click để đổi avatar");
    
    // Add click listener
    lblAvatar.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            handleAvatarUpload();
        }
    });
    
    // ... rest of code ...
}
```

### **Bước 3: Implement handleAvatarUpload()**

Thêm method mới vào `ClientUI.java`:

```java
/**
 * Xử lý upload avatar
 */
private void handleAvatarUpload() {
    if (currentUser == null) {
        JOptionPane.showMessageDialog(this,
            "Vui lòng đăng nhập để đổi avatar!",
            "Thông báo",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    AvatarManager.uploadAvatar(this, currentUser.getId(), new AvatarManager.AvatarUploadCallback() {
        @Override
        public void onSuccess(String avatarPath) {
            System.out.println("✅ Upload avatar thành công: " + avatarPath);
            
            // Reload avatar
            loadUserAvatar(avatarPath);
            
            // Broadcast update to other components
            if (userProfileUI != null) {
                userProfileUI.refreshAvatar();
            }
        }
        
        @Override
        public void onError(String errorMessage) {
            System.err.println("❌ Lỗi upload avatar: " + errorMessage);
            
            JOptionPane.showMessageDialog(ClientUI.this,
                "Không thể upload avatar: " + errorMessage,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    });
}
```

---

## 🎯 Cải tiến giao diện

### **Option 1: Thêm nút "Đổi Avatar" riêng**

```java
JButton btnChangeAvatar = new JButton("📷");
btnChangeAvatar.setPreferredSize(new Dimension(40, 40));
btnChangeAvatar.setToolTipText("Đổi avatar");
btnChangeAvatar.addActionListener(e -> handleAvatarUpload());

userSection.add(btnChangeAvatar);
```

### **Option 2: Show menu khi click vào avatar**

```java
private void showAvatarMenu() {
    JPopupMenu menu = new JPopupMenu();
    
    JMenuItem itemUpload = new JMenuItem("📤 Upload avatar mới");
    itemUpload.addActionListener(e -> handleAvatarUpload());
    
    JMenuItem itemRemove = new JMenuItem("🗑️ Xóa avatar");
    itemRemove.addActionListener(e -> removeAvatar());
    
    menu.add(itemUpload);
    menu.add(itemRemove);
    menu.show(lblAvatar, 0, lblAvatar.getHeight());
}

lblAvatar.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        showAvatarMenu();
    }
});
```

### **Option 3: Hover effect**

```java
lblAvatar.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
        lblAvatar.setBorder(BorderFactory.createLineBorder(
            new Color(88, 166, 255), 3));
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
        lblAvatar.setBorder(BorderFactory.createLineBorder(
            new Color(88, 166, 255, 200), 2));
    }
});
```

---

## 📂 File Structure

```
quanlythuvien3/
├── src/
│   ├── client/
│   │   ├── ClientUI.java         ← Thêm handleAvatarUpload()
│   │   └── AvatarManager.java    ← Class mới
│   └── app/
│       └── TestAvatarManager.java ← Test class
└── test_avatar_upload.bat        ← Test script
```

---

## 🧪 Testing

### **Test độc lập:**
```batch
test_avatar_upload.bat
```

### **Test tích hợp trong ClientUI:**
1. Chạy ClientUI
2. Đăng nhập
3. Click vào avatar
4. Chọn file ảnh
5. Kiểm tra avatar hiển thị đúng

---

## 📊 Flow Diagram

```
User Click Avatar
       ↓
handleAvatarUpload()
       ↓
AvatarManager.uploadAvatar()
       ↓
JFileChooser Dialog
       ↓
[User chọn file]
       ↓
Validate Image
       ↓
Process (Crop & Resize)
       ↓
Save to C:/data/avatars/
       ↓
Update Database
       ↓
Callback Success
       ↓
Reload Avatar in UI
```

---

## ⚙️ Configuration

Có thể customize các thông số trong `AvatarManager.java`:

```java
// Thư mục lưu avatar
private static final String AVATARS_DIR = "C:/data/avatars/";

// Kích thước avatar gốc (trước khi resize)
private static final int AVATAR_SIZE = 200;

// Các định dạng hợp lệ
private static final String[] VALID_EXTENSIONS = {
    ".jpg", ".jpeg", ".png", ".gif", ".webp"
};
```

---

## 🐛 Common Issues

### **Issue 1: "Permission denied" khi lưu file**

**Giải pháp:**
```java
File avatarsDir = new File(AVATARS_DIR);
if (!avatarsDir.exists()) {
    avatarsDir.mkdirs(); // Tạo thư mục nếu chưa có
}
```

### **Issue 2: Avatar không refresh sau upload**

**Giải pháp:**
```java
SwingUtilities.invokeLater(() -> {
    loadUserAvatar(avatarPath);
    lblAvatar.getParent().revalidate();
    lblAvatar.getParent().repaint();
});
```

### **Issue 3: OutOfMemoryError với ảnh lớn**

**Giải pháp:**
```java
// Thêm vào AvatarManager
private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

if (file.length() > MAX_FILE_SIZE) {
    throw new Exception("File quá lớn! Tối đa 5MB");
}
```

---

## 🚀 Improvements

### **1. Thêm Cache cho Avatar**

```java
private static Map<Integer, ImageIcon> avatarCache = new HashMap<>();

public static ImageIcon getCachedAvatar(int userId, String avatarPath) {
    if (!avatarCache.containsKey(userId)) {
        ImageIcon icon = loadAvatarFromPath(avatarPath);
        avatarCache.put(userId, icon);
    }
    return avatarCache.get(userId);
}
```

### **2. Thêm Avatar History**

Lưu các version avatar cũ:

```java
String historyFileName = userId + "_" + timestamp + "_backup" + extension;
```

### **3. Support Drag & Drop**

```java
lblAvatar.setTransferHandler(new TransferHandler() {
    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }
    
    @Override
    public boolean importData(TransferSupport support) {
        // Handle dropped file
    }
});
```

---

## 📝 Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | Nov 2, 2025 | Initial implementation |
| | | - Upload from local file |
| | | - Auto crop & resize |
| | | - Database integration |

---

## ✅ Checklist

Khi tích hợp xong:

- [ ] Import `AvatarManager`
- [ ] Thêm click listener vào `lblAvatar`
- [ ] Implement `handleAvatarUpload()`
- [ ] Test upload với file .jpg
- [ ] Test upload với file .png
- [ ] Test với file không hợp lệ
- [ ] Test với file quá lớn
- [ ] Verify database được update
- [ ] Verify avatar hiển thị đúng sau upload
- [ ] Test avatar refresh ở các component khác

---

**🎉 Done! Avatar upload feature ready to use!**
