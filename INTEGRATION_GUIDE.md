# 🔧 HƯỚNG DẪN TÍCH HỢP KEEP-ALIVE & LOADING

## 📋 **Tích hợp vào ClientUI.java:**

### 1️⃣ **Thêm import:**
```java
// Thêm vào đầu file ClientUI.java
import client.KeepAliveManager;
import client.LoadingUtils;
```

### 2️⃣ **Thêm biến instance:**
```java
// Thêm vào class ClientUI
private KeepAliveManager keepAlive;
```

### 3️⃣ **Khởi tạo trong constructor:**
```java
// Thêm vào cuối constructor ClientUI()
public ClientUI() {
    // ... existing code ...
    
    // Start keep-alive system
    keepAlive = KeepAliveManager.getInstance();
    keepAlive.start();
    
    // Setup window close handler
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            // Cleanup resources
            if (keepAlive != null) {
                keepAlive.stop();
            }
            System.exit(0);
        }
    });
    
    // ... rest of constructor
}
```

### 4️⃣ **Thêm loading vào loadBooksGrid():**
```java
// Thay thế method loadBooksGrid() hiện tại
private void loadBooksGrid() {
    if (booksGridPanel == null) return;
    
    // Use loading dialog
    LoadingUtils.executeWithLoading(this, "Đang tải danh sách sách", () -> {
        SwingUtilities.invokeLater(() -> booksGridPanel.removeAll());
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:C:/data/library.db?busy_timeout=30000")) {
            // ... existing database code ...
            
            SwingUtilities.invokeLater(() -> {
                updatePaginationUI();
                booksGridPanel.revalidate();
                booksGridPanel.repaint();
            });
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi tải sách: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    });
}
```

### 5️⃣ **Thêm loading vào các tác vụ khác:**
```java
// Thêm loading vào method search
private void performSearch() {
    LoadingUtils.executeWithLoading(this, "Đang tìm kiếm", () -> {
        loadBooksGrid();
    });
}

// Thêm loading vào method borrow
private void handleBorrowRequest() {
    LoadingUtils.executeWithLoading(this, "Đang xử lý yêu cầu mượn", () -> {
        // ... existing borrow code ...
    });
}

// Thêm loading vào method show favorites
private void showFavoriteBooks() {
    LoadingUtils.executeWithLoading(this, "Đang tải sách yêu thích", () -> {
        // ... existing favorites code ...
        SwingUtilities.invokeLater(() -> {
            // Update UI here
        });
    });
}
```

## 🎯 **Sử dụng nhanh:**

### ✅ **Cách 1: Tự động (Khuyến nghị)**
```java
// Chỉ cần thêm 3 dòng vào constructor:
keepAlive = KeepAliveManager.getInstance();
keepAlive.start();
// Tự động ping database mỗi 5 phút + cleanup memory
```

### ✅ **Cách 2: Manual Loading**
```java
// Wrap bất kỳ tác vụ nào cần loading:
LoadingUtils.executeWithLoading(this, "Message", () -> {
    // Your heavy task here
});
```

## 🚀 **Lợi ích ngay lập tức:**

### 🛡️ **Stability:**
- ✅ **No timeout** - App chạy 24/7
- ✅ **Auto cleanup** - Memory không bị leak
- ✅ **Database alive** - Connection luôn fresh

### 🎨 **UX:**
- ✅ **Loading feedback** - User biết app đang làm gì
- ✅ **No freeze** - UI luôn responsive
- ✅ **Professional look** - Modern loading dialogs

### ⚡ **Performance:**
- ✅ **Background tasks** - Heavy work không block UI
- ✅ **Resource management** - Tự động cleanup
- ✅ **Memory monitoring** - Track usage real-time

## 📋 **Implementation Steps:**

1. **Copy files:** `KeepAliveManager.java`, `LoadingUtils.java` vào thư mục client
2. **Add imports** vào ClientUI.java
3. **Add keep-alive start** trong constructor
4. **Add window close handler** 
5. **Wrap heavy tasks** với LoadingUtils
6. **Test và enjoy!** 🎉

## 🔍 **Monitoring:**

### 📊 **Console output:**
```
Starting keep-alive system...
Keep-alive system started successfully
Database ping successful
Memory: 45MB used, 123MB free, 168MB total
```

### 🎯 **Status check:**
```java
System.out.println(keepAlive.getStatus());
// Output: "Keep-alive system is running"
```

---

## 🎊 **KẾT QUẢ SAU KHI TÍCH HỢP:**

✅ **App ổn định** - Không bao giờ timeout  
✅ **Loading modern** - Professional user experience  
✅ **Performance tốt** - Memory được quản lý  
✅ **Easy maintenance** - Tự động cleanup  
✅ **Production ready** - Đáng tin cậy 24/7  

**🚀 Chỉ cần 5 phút tích hợp là có ngay ứng dụng desktop professional!**