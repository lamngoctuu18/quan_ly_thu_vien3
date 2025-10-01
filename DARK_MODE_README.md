# 🌙 Dark Mode System - Hệ thống quản lý thư viện

## Tổng quan
Hệ thống Dark Mode toàn diện cho ứng dụng quản lý thư viện, hỗ trợ cả giao diện Admin và Client với khả năng chuyển đổi mượt mà giữa Light Mode và Dark Mode.

## ✨ Tính năng chính

### 🔄 Dark Mode Manager
- **Quản lý trạng thái**: Tự động lưu và khôi phục cài đặt Dark Mode
- **Color Palette**: Bộ màu được thiết kế chuyên nghiệp cho cả Light và Dark theme
- **Event System**: Hệ thống listener để cập nhật theme real-time
- **Persistent Settings**: Lưu trữ cài đặt qua các phiên làm việc

### 🎨 Theme Colors

#### Dark Theme
- **Primary Background**: `#121212` - Nền chính tối
- **Secondary Background**: `#212529` - Nền phụ
- **Tertiary Background**: `#343A40` - Cards, panels
- **Primary Text**: `#F8F9FA` - Text chính
- **Accent Color**: `#0D6EFD` - Màu nhấn xanh

#### Light Theme  
- **Primary Background**: `#FFFFFF` - Nền chính sáng
- **Secondary Background**: `#F8F9FA` - Nền phụ
- **Tertiary Background**: `#E9ECEF` - Cards, panels
- **Primary Text**: `#212529` - Text chính
- **Accent Color**: `#0D6EFD` - Màu nhấn xanh

### 🖱️ Dark Mode Toggle Button
- **Biểu tượng động**: 🌙 Dark / ☀️ Light
- **Hover effects**: Hiệu ứng khi di chuột
- **Instant feedback**: Chuyển đổi ngay lập tức

## 🏗️ Kiến trúc hệ thống

### Core Components

1. **DarkModeManager.java**
   - Singleton pattern cho quản lý global
   - Color palette management
   - Event listener system
   - Settings persistence

2. **DarkModeAwareComponent.java**
   - Base class cho components hỗ trợ Dark Mode
   - Automatic theme application
   - Resource cleanup

3. **ModernSidebarButton.java** (Updated)
   - 60 FPS smooth animations
   - Dark Mode integration
   - Dynamic color adaptation

## 📱 Giao diện được hỗ trợ

### 🔧 Admin Interface (AdminUI)
- **Header**: Gradient background thích ứng với theme
- **Sidebar**: Modern buttons với Dark Mode colors
- **Main Content**: Tự động cập nhật background
- **Dark Mode Toggle**: Nút chuyển đổi ở header

### 👤 Client Interface (ClientUI)  
- **Search Section**: Form tìm kiếm với Dark Mode styling
- **Books Grid**: Lưới sách với màu nền thích ứng
- **User Profile**: Avatar và thông tin với theme colors
- **Dark Mode Toggle**: Nút chuyển đổi bên cạnh thông báo

## 🚀 Cách sử dụng

### Khởi chạy với Dark Mode Support

#### Admin Interface
```bash
# Chạy với AdminLauncher mới (có Dark Mode)
java client.AdminLauncherNew

# Hoặc demo Dark Mode
java client.DarkModeDemo
```

#### Client Interface  
```bash
# ClientUI đã tích hợp sẵn Dark Mode
java client.ClientUI
```

### Sử dụng trong code

#### Tích hợp Dark Mode Manager
```java
// Khởi tạo
DarkModeManager darkModeManager = DarkModeManager.getInstance();

// Toggle Dark Mode
darkModeManager.toggleDarkMode();

// Kiểm tra trạng thái
boolean isDark = darkModeManager.isDarkMode();

// Lấy màu theme hiện tại
Color bgColor = darkModeManager.getBackgroundColor();
Color textColor = darkModeManager.getTextColor();
```

#### Tạo component với Dark Mode support
```java
public class MyComponent extends DarkModeAwareComponent {
    @Override
    protected void updateTheme(boolean isDarkMode) {
        // Custom theme update logic
        if (isDarkMode) {
            setBackground(DarkModeManager.DarkTheme.PRIMARY_BG);
        } else {
            setBackground(DarkModeManager.LightTheme.PRIMARY_BG);
        }
    }
}
```

#### Listener cho theme changes
```java
darkModeManager.addDarkModeListener(isDarkMode -> {
    // Cập nhật UI khi theme thay đổi
    updateComponentColors(isDarkMode);
});
```

## 🎯 Demo và Test

### DarkModeDemo.java
Chương trình demo đầy đủ để test Dark Mode:
- Form tìm kiếm với text fields, combo boxes
- Bảng dữ liệu mẫu 
- Buttons với different colors
- Real-time theme switching

```bash
java client.DarkModeDemo
```

## 🔄 Animation System

### Smooth Transitions
- **60 FPS animations** cho sidebar buttons
- **Color interpolation** mượt mà
- **Progressive effects** với glow và shadow
- **Responsive hover states**

### ModernSidebarButton Features
- Dynamic icon và text colors
- Animated background transitions  
- Left accent bar cho selected state
- Glow effects on hover/selection

## 💾 Settings Persistence

Dark Mode settings được lưu tự động qua:
- **Java Preferences API**
- **User-specific storage** 
- **Automatic restore** khi khởi động lại

## 🎨 Customization Options

### Thêm theme colors mới
```java
// Trong DarkModeManager.java
public static class CustomTheme {
    public static final Color NEW_BG = new Color(r, g, b);
    // ... other colors
}
```

### Tạo custom Dark Mode button
```java
JButton customToggle = darkModeManager.createDarkModeToggleButton();
customToggle.setText("🌓 Theme");
customToggle.setPreferredSize(new Dimension(100, 35));
```

## 📂 File Structure
```
src/client/
├── DarkModeManager.java          # Core Dark Mode management
├── DarkModeAwareComponent.java   # Base class for Dark Mode components  
├── ModernSidebarButton.java      # Updated with Dark Mode support
├── AdminUI.java                  # Admin interface with Dark Mode
├── ClientUI.java                 # Client interface with Dark Mode
├── AdminLauncherNew.java         # Launcher with Dark Mode loading
└── DarkModeDemo.java             # Full feature demo
```

## 🐛 Troubleshooting

### Theme not applying?
- Kiểm tra xem component có implement `DarkModeListener` không
- Đảm bảo `darkModeManager.applyDarkMode()` được gọi
- Verify listener được register properly

### Colors not updating?
- Gọi `repaint()` sau khi update colors
- Sử dụng `SwingUtilities.invokeLater()` cho UI updates
- Check color constants trong DarkModeManager

### Performance issues?
- Cleanup listeners trong `finalize()` methods
- Stop animations khi component bị dispose
- Limit số lượng concurrent theme updates

## 🚀 Future Enhancements

- [ ] Multiple theme presets (Blue, Purple, Green, etc.)
- [ ] System theme detection (Windows/Mac)
- [ ] Theme scheduling (auto dark at night)  
- [ ] Custom theme editor
- [ ] Theme import/export functionality
- [ ] Accessibility improvements
- [ ] High contrast mode support

## 👥 Contributing

Để contribute cho Dark Mode system:

1. Test với cả Light và Dark themes
2. Đảm bảo animations mượt mà (60 FPS)
3. Follow color palette guidelines
4. Add proper cleanup cho listeners
5. Test memory leaks với long-running sessions

---

**Phát triển bởi**: GitHub Copilot  
**Ngày cập nhật**: 2024  
**Version**: 1.0.0 - Full Dark Mode Support ✨