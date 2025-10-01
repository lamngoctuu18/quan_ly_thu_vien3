package client;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * DarkModeManager - Quản lý Dark Mode cho toàn bộ ứng dụng
 * Hỗ trợ cả Admin UI và Client UI
 */
public class DarkModeManager {
    private static volatile DarkModeManager instance;
    private boolean isDarkMode = false;
    private List<DarkModeListener> listeners = new ArrayList<>();
    private Preferences prefs;
    
    // Dark Mode Color Palette - Modern và đẹp mắt
    public static class DarkTheme {
        // Background colors - Gradient tối hiện đại
        public static final Color PRIMARY_BG = new Color(15, 17, 21);          // Nền chính tối đẹp
        public static final Color SECONDARY_BG = new Color(24, 28, 34);        // Nền phụ
        public static final Color TERTIARY_BG = new Color(34, 40, 49);         // Cards, panels
        public static final Color SURFACE_BG = new Color(44, 51, 61);          // Elevated surfaces
        public static final Color CARD_BG = new Color(28, 33, 40);             // Card background
        
        // Text colors - Contrast cao, dễ đọc
        public static final Color PRIMARY_TEXT = new Color(251, 252, 253);     // Text chính
        public static final Color SECONDARY_TEXT = new Color(195, 202, 210);   // Text phụ  
        public static final Color MUTED_TEXT = new Color(158, 168, 179);       // Text mờ
        
        // Accent colors - Màu sắc sinh động
        public static final Color PRIMARY_ACCENT = new Color(88, 166, 255);    // Blue chủ đạo
        public static final Color SUCCESS_ACCENT = new Color(64, 186, 106);    // Green thành công
        public static final Color WARNING_ACCENT = new Color(255, 183, 77);    // Yellow cảnh báo
        public static final Color DANGER_ACCENT = new Color(255, 107, 107);    // Red nguy hiểm
        public static final Color INFO_ACCENT = new Color(84, 199, 236);       // Cyan thông tin
        
        // Border colors - Viền tinh tế
        public static final Color BORDER_COLOR = new Color(54, 63, 77);        // Viền mặc định
        public static final Color FOCUS_BORDER = new Color(88, 166, 255);      // Viền focus
        public static final Color HOVER_BORDER = new Color(74, 144, 226);      // Viền hover
        
        // Hover states - Hiệu ứng hover mượt
        public static final Color HOVER_BG = new Color(54, 63, 77);            // Nền hover
        public static final Color SELECTED_BG = new Color(88, 166, 255, 25);   // Nền selected
        public static final Color ACTIVE_BG = new Color(88, 166, 255, 40);     // Nền active
    }
    
    // Light Mode Color Palette - Sáng và tinh tế
    public static class LightTheme {
        // Background colors
        public static final Color PRIMARY_BG = new Color(255, 255, 255);       // Nền chính trắng
        public static final Color SECONDARY_BG = new Color(248, 250, 252);     // Nền phụ
        public static final Color TERTIARY_BG = new Color(241, 245, 249);      // Cards, panels
        public static final Color SURFACE_BG = new Color(255, 255, 255);       // Elevated surfaces
        public static final Color CARD_BG = new Color(251, 252, 253);          // Card background
        
        // Text colors
        public static final Color PRIMARY_TEXT = new Color(15, 23, 42);        // Text chính
        public static final Color SECONDARY_TEXT = new Color(71, 85, 105);     // Text phụ
        public static final Color MUTED_TEXT = new Color(100, 116, 139);       // Text mờ
        
        // Accent colors - Giữ nguyên màu đẹp
        public static final Color PRIMARY_ACCENT = new Color(59, 130, 246);    // Blue chủ đạo
        public static final Color SUCCESS_ACCENT = new Color(34, 197, 94);     // Green thành công
        public static final Color WARNING_ACCENT = new Color(251, 191, 36);    // Yellow cảnh báo
        public static final Color DANGER_ACCENT = new Color(239, 68, 68);      // Red nguy hiểm
        public static final Color INFO_ACCENT = new Color(14, 165, 233);       // Cyan thông tin
        
        // Border colors
        public static final Color BORDER_COLOR = new Color(226, 232, 240);     // Viền mặc định
        public static final Color FOCUS_BORDER = new Color(59, 130, 246);      // Viền focus
        public static final Color HOVER_BORDER = new Color(37, 99, 235);       // Viền hover
        
        // Hover states
        public static final Color HOVER_BG = new Color(248, 250, 252);         // Nền hover
        public static final Color SELECTED_BG = new Color(59, 130, 246, 20);   // Nền selected
        public static final Color ACTIVE_BG = new Color(59, 130, 246, 35);     // Nền active
    }
    
    private DarkModeManager() {
        prefs = Preferences.userNodeForPackage(DarkModeManager.class);
        loadSettings();
    }
    
    public static DarkModeManager getInstance() {
        if (instance == null) {
            synchronized (DarkModeManager.class) {
                if (instance == null) {
                    instance = new DarkModeManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Toggle Dark Mode on/off
     */
    public void toggleDarkMode() {
        setDarkMode(!isDarkMode);
    }
    
    /**
     * Set Dark Mode state
     */
    public void setDarkMode(boolean enabled) {
        if (this.isDarkMode != enabled) {
            this.isDarkMode = enabled;
            saveSettings();
            notifyListeners();
            updateLookAndFeel();
        }
    }
    
    /**
     * Check if Dark Mode is enabled
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    /**
     * Get current background color
     */
    public Color getBackgroundColor() {
        return isDarkMode ? DarkTheme.PRIMARY_BG : LightTheme.PRIMARY_BG;
    }
    
    /**
     * Get current secondary background color
     */
    public Color getSecondaryBackgroundColor() {
        return isDarkMode ? DarkTheme.SECONDARY_BG : LightTheme.SECONDARY_BG;
    }
    
    /**
     * Get current tertiary background color
     */
    public Color getTertiaryBackgroundColor() {
        return isDarkMode ? DarkTheme.TERTIARY_BG : LightTheme.TERTIARY_BG;
    }
    
    /**
     * Get current text color
     */
    public Color getTextColor() {
        return isDarkMode ? DarkTheme.PRIMARY_TEXT : LightTheme.PRIMARY_TEXT;
    }
    
    /**
     * Get current secondary text color
     */
    public Color getSecondaryTextColor() {
        return isDarkMode ? DarkTheme.SECONDARY_TEXT : LightTheme.SECONDARY_TEXT;
    }
    
    /**
     * Get current border color
     */
    public Color getBorderColor() {
        return isDarkMode ? DarkTheme.BORDER_COLOR : LightTheme.BORDER_COLOR;
    }
    
    /**
     * Get current hover background color
     */
    public Color getHoverBackgroundColor() {
        return isDarkMode ? DarkTheme.HOVER_BG : LightTheme.HOVER_BG;
    }
    
    /**
     * Get current selected background color
     */
    public Color getSelectedBackgroundColor() {
        return isDarkMode ? DarkTheme.SELECTED_BG : LightTheme.SELECTED_BG;
    }
    
    /**
     * Update Look and Feel based on current mode
     */
    private void updateLookAndFeel() {
        try {
            if (isDarkMode) {
                // Set dark theme properties
                UIManager.put("Panel.background", DarkTheme.PRIMARY_BG);
                UIManager.put("OptionPane.background", DarkTheme.SECONDARY_BG);
                UIManager.put("Button.background", DarkTheme.TERTIARY_BG);
                UIManager.put("Button.foreground", DarkTheme.PRIMARY_TEXT);
                UIManager.put("Label.foreground", DarkTheme.PRIMARY_TEXT);
                UIManager.put("TextField.background", DarkTheme.SURFACE_BG);
                UIManager.put("TextField.foreground", DarkTheme.PRIMARY_TEXT);
                UIManager.put("TextField.border", BorderFactory.createLineBorder(DarkTheme.BORDER_COLOR));
                UIManager.put("ComboBox.background", DarkTheme.SURFACE_BG);
                UIManager.put("ComboBox.foreground", DarkTheme.PRIMARY_TEXT);
                UIManager.put("Table.background", DarkTheme.SECONDARY_BG);
                UIManager.put("Table.foreground", DarkTheme.PRIMARY_TEXT);
                UIManager.put("Table.gridColor", DarkTheme.BORDER_COLOR);
                UIManager.put("TableHeader.background", DarkTheme.TERTIARY_BG);
                UIManager.put("TableHeader.foreground", DarkTheme.PRIMARY_TEXT);
                UIManager.put("ScrollPane.background", DarkTheme.PRIMARY_BG);
                UIManager.put("Viewport.background", DarkTheme.PRIMARY_BG);
            } else {
                // Reset to light theme
                UIManager.put("Panel.background", LightTheme.PRIMARY_BG);
                UIManager.put("OptionPane.background", LightTheme.SECONDARY_BG);
                UIManager.put("Button.background", LightTheme.TERTIARY_BG);
                UIManager.put("Button.foreground", LightTheme.PRIMARY_TEXT);
                UIManager.put("Label.foreground", LightTheme.PRIMARY_TEXT);
                UIManager.put("TextField.background", LightTheme.SURFACE_BG);
                UIManager.put("TextField.foreground", LightTheme.PRIMARY_TEXT);
                UIManager.put("TextField.border", BorderFactory.createLineBorder(LightTheme.BORDER_COLOR));
                UIManager.put("ComboBox.background", LightTheme.SURFACE_BG);
                UIManager.put("ComboBox.foreground", LightTheme.PRIMARY_TEXT);
                UIManager.put("Table.background", LightTheme.SECONDARY_BG);
                UIManager.put("Table.foreground", LightTheme.PRIMARY_TEXT);
                UIManager.put("Table.gridColor", LightTheme.BORDER_COLOR);
                UIManager.put("TableHeader.background", LightTheme.TERTIARY_BG);
                UIManager.put("TableHeader.foreground", LightTheme.PRIMARY_TEXT);
                UIManager.put("ScrollPane.background", LightTheme.PRIMARY_BG);
                UIManager.put("Viewport.background", LightTheme.PRIMARY_BG);
            }
            
        } catch (Exception e) {
            System.err.println("Error updating Look and Feel: " + e.getMessage());
        }
    }
    
    /**
     * Apply dark mode to a specific component
     */
    public void applyDarkMode(JComponent component) {
        if (component == null) return;
        
        if (isDarkMode) {
            applyDarkTheme(component);
        } else {
            applyLightTheme(component);
        }
        
        // Recursively apply to children
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                applyDarkMode((JComponent) child);
            }
        }
        
        component.repaint();
    }
    
    /**
     * Apply dark theme to component - Thông minh hơn, giữ màu đặc trưng
     */
    private void applyDarkTheme(JComponent component) {
        if (component instanceof JPanel) {
            // Panels - nền tối hiện đại
            component.setBackground(DarkTheme.SECONDARY_BG);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            // Chỉ cập nhật nút nếu chúng có màu mặc định hoặc không đặc biệt
            Color currentBg = button.getBackground();
            
            // Kiểm tra xem có phải nút đặc biệt không (có màu riêng)
            if (isSpecialColoredButton(currentBg)) {
                // Nút có màu đặc biệt - chỉ điều chỉnh cho Dark Mode
                adjustButtonForDarkMode(button, currentBg);
            } else {
                // Nút bình thường - áp dụng theme mặc định
                button.setBackground(DarkTheme.TERTIARY_BG);
                button.setForeground(DarkTheme.PRIMARY_TEXT);
                button.setBorder(BorderFactory.createLineBorder(DarkTheme.BORDER_COLOR, 1));
            }
        } else if (component instanceof JTextField || component instanceof JTextArea) {
            component.setBackground(DarkTheme.SURFACE_BG);
            component.setForeground(DarkTheme.PRIMARY_TEXT);
            component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DarkTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
        } else if (component instanceof JLabel) {
            component.setForeground(DarkTheme.PRIMARY_TEXT);
        } else if (component instanceof JTable) {
            component.setBackground(DarkTheme.CARD_BG);
            component.setForeground(DarkTheme.PRIMARY_TEXT);
            ((JTable) component).setGridColor(DarkTheme.BORDER_COLOR);
            // Header của table
            if (((JTable) component).getTableHeader() != null) {
                ((JTable) component).getTableHeader().setBackground(DarkTheme.TERTIARY_BG);
                ((JTable) component).getTableHeader().setForeground(DarkTheme.PRIMARY_TEXT);
            }
        } else if (component instanceof JComboBox) {
            component.setBackground(DarkTheme.SURFACE_BG);
            component.setForeground(DarkTheme.PRIMARY_TEXT);
        } else {
            // Component khác
            component.setBackground(DarkTheme.PRIMARY_BG);
            component.setForeground(DarkTheme.PRIMARY_TEXT);
        }
    }
    
    /**
     * Apply light theme to component - Thông minh hơn, giữ màu đặc trưng
     */
    private void applyLightTheme(JComponent component) {
        if (component instanceof JPanel) {
            // Panels - nền sáng tinh tế
            component.setBackground(LightTheme.SECONDARY_BG);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            // Chỉ cập nhật nút nếu chúng có màu mặc định hoặc không đặc biệt
            Color currentBg = button.getBackground();
            
            // Kiểm tra xem có phải nút đặc biệt không (có màu riêng)
            if (isSpecialColoredButton(currentBg)) {
                // Nút có màu đặc biệt - khôi phục màu gốc
                restoreButtonOriginalColors(button, currentBg);
            } else {
                // Nút bình thường - áp dụng theme mặc định
                button.setBackground(LightTheme.TERTIARY_BG);
                button.setForeground(LightTheme.PRIMARY_TEXT);
                button.setBorder(BorderFactory.createLineBorder(LightTheme.BORDER_COLOR, 1));
            }
        } else if (component instanceof JTextField || component instanceof JTextArea) {
            component.setBackground(LightTheme.SURFACE_BG);
            component.setForeground(LightTheme.PRIMARY_TEXT);
            component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LightTheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
        } else if (component instanceof JLabel) {
            component.setForeground(LightTheme.PRIMARY_TEXT);
        } else if (component instanceof JTable) {
            component.setBackground(LightTheme.CARD_BG);
            component.setForeground(LightTheme.PRIMARY_TEXT);
            ((JTable) component).setGridColor(LightTheme.BORDER_COLOR);
            // Header của table
            if (((JTable) component).getTableHeader() != null) {
                ((JTable) component).getTableHeader().setBackground(LightTheme.TERTIARY_BG);
                ((JTable) component).getTableHeader().setForeground(LightTheme.PRIMARY_TEXT);
            }
        } else if (component instanceof JComboBox) {
            component.setBackground(LightTheme.SURFACE_BG);
            component.setForeground(LightTheme.PRIMARY_TEXT);
        } else {
            // Component khác
            component.setBackground(LightTheme.PRIMARY_BG);
            component.setForeground(LightTheme.PRIMARY_TEXT);
        }
    }
    
    /**
     * Kiểm tra xem nút có màu đặc biệt không
     */
    private boolean isSpecialColoredButton(Color currentColor) {
        if (currentColor == null) return false;
        
        // Các màu mặc định của Swing - không coi là đặc biệt
        if (currentColor.equals(UIManager.getColor("Button.background")) ||
            currentColor.equals(new Color(238, 238, 238)) ||
            currentColor.equals(new Color(240, 240, 240)) ||
            currentColor.equals(Color.LIGHT_GRAY) ||
            currentColor.equals(Color.WHITE)) {
            return false;
        }
        
        // Nút có màu đặc biệt (không phải xám/trắng mặc định)
        return true;
    }
    
    /**
     * Điều chỉnh nút có màu đặc biệt cho Dark Mode
     */
    private void adjustButtonForDarkMode(JButton button, Color originalColor) {
        // Làm tối màu nền một chút để phù hợp với Dark Mode
        Color adjustedColor = darkenColor(originalColor, 0.3f);
        button.setBackground(adjustedColor);
        
        // Text màu trắng cho dễ đọc
        button.setForeground(DarkTheme.PRIMARY_TEXT);
        
        // Border tối hơn
        Color borderColor = darkenColor(originalColor, 0.5f);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    /**
     * Khôi phục màu gốc của nút cho Light Mode
     */
    private void restoreButtonOriginalColors(JButton button, Color currentColor) {
        // Khôi phục màu sáng hơn
        Color restoredColor = lightenColor(currentColor, 0.2f);
        button.setBackground(restoredColor);
        
        // Text màu trắng để contrast
        button.setForeground(Color.WHITE);
        
        // Border sáng hơn
        Color borderColor = lightenColor(restoredColor, 0.1f);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    /**
     * Làm tối màu theo tỷ lệ
     */
    private Color darkenColor(Color color, float factor) {
        return new Color(
            Math.max(0, (int) (color.getRed() * (1 - factor))),
            Math.max(0, (int) (color.getGreen() * (1 - factor))),
            Math.max(0, (int) (color.getBlue() * (1 - factor))),
            color.getAlpha()
        );
    }
    
    /**
     * Làm sáng màu theo tỷ lệ
     */
    private Color lightenColor(Color color, float factor) {
        return new Color(
            Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * factor)),
            Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * factor)),
            Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * factor)),
            color.getAlpha()
        );
    }
    
    /**
     * Add listener for dark mode changes
     */
    public void addDarkModeListener(DarkModeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove dark mode listener
     */
    public void removeDarkModeListener(DarkModeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners of dark mode change
     */
    private void notifyListeners() {
        for (DarkModeListener listener : listeners) {
            try {
                listener.onDarkModeChanged(isDarkMode);
            } catch (Exception e) {
                System.err.println("Error notifying dark mode listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Save settings to preferences
     */
    private void saveSettings() {
        prefs.putBoolean("darkMode", isDarkMode);
    }
    
    /**
     * Load settings from preferences
     */
    private void loadSettings() {
        isDarkMode = prefs.getBoolean("darkMode", false);
    }
    
    /**
     * Interface for dark mode change listeners
     */
    public interface DarkModeListener {
        void onDarkModeChanged(boolean isDarkMode);
    }
    
    /**
     * Create dark mode toggle button
     */
    public JButton createDarkModeToggleButton() {
        JButton toggleButton = new JButton();
        updateToggleButtonAppearance(toggleButton);
        
        toggleButton.addActionListener(e -> {
            toggleDarkMode();
            updateToggleButtonAppearance(toggleButton);
        });
        
        toggleButton.setFocusPainted(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        toggleButton.setToolTipText(isDarkMode ? "Chuyển sang Light Mode" : "Chuyển sang Dark Mode");
        
        return toggleButton;
    }
    
    /**
     * Update toggle button appearance
     */
    private void updateToggleButtonAppearance(JButton button) {
        if (isDarkMode) {
            button.setText("☀️ Light");
            button.setBackground(new Color(255, 193, 7));
            button.setForeground(Color.BLACK);
            button.setToolTipText("Chuyển sang Light Mode");
        } else {
            button.setText("🌙 Dark");
            button.setBackground(new Color(52, 58, 64));
            button.setForeground(Color.WHITE);
            button.setToolTipText("Chuyển sang Dark Mode");
        }
    }
}