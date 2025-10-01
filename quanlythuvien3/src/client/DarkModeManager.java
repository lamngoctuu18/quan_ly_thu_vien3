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
    
    // Dark Mode Color Palette
    public static class DarkTheme {
        // Background colors
        public static final Color PRIMARY_BG = new Color(18, 18, 18);           // Main background
        public static final Color SECONDARY_BG = new Color(33, 37, 41);        // Secondary background
        public static final Color TERTIARY_BG = new Color(52, 58, 64);         // Cards, panels
        public static final Color SURFACE_BG = new Color(73, 80, 87);          // Elevated surfaces
        
        // Text colors
        public static final Color PRIMARY_TEXT = new Color(248, 249, 250);     // Primary text
        public static final Color SECONDARY_TEXT = new Color(206, 212, 218);   // Secondary text
        public static final Color MUTED_TEXT = new Color(173, 181, 189);       // Muted text
        
        // Accent colors
        public static final Color PRIMARY_ACCENT = new Color(13, 110, 253);    // Primary blue
        public static final Color SUCCESS_ACCENT = new Color(25, 135, 84);     // Success green
        public static final Color WARNING_ACCENT = new Color(255, 193, 7);     // Warning yellow
        public static final Color DANGER_ACCENT = new Color(220, 53, 69);      // Danger red
        public static final Color INFO_ACCENT = new Color(13, 202, 240);       // Info cyan
        
        // Border colors
        public static final Color BORDER_COLOR = new Color(73, 80, 87);        // Default border
        public static final Color FOCUS_BORDER = new Color(13, 110, 253);      // Focus border
        
        // Hover states
        public static final Color HOVER_BG = new Color(73, 80, 87);            // Hover background
        public static final Color SELECTED_BG = new Color(13, 110, 253, 30);   // Selected background
    }
    
    // Light Mode Color Palette
    public static class LightTheme {
        // Background colors
        public static final Color PRIMARY_BG = new Color(255, 255, 255);       // Main background
        public static final Color SECONDARY_BG = new Color(248, 249, 250);     // Secondary background
        public static final Color TERTIARY_BG = new Color(233, 236, 239);      // Cards, panels
        public static final Color SURFACE_BG = new Color(255, 255, 255);       // Elevated surfaces
        
        // Text colors
        public static final Color PRIMARY_TEXT = new Color(33, 37, 41);        // Primary text
        public static final Color SECONDARY_TEXT = new Color(108, 117, 125);   // Secondary text
        public static final Color MUTED_TEXT = new Color(134, 142, 150);       // Muted text
        
        // Accent colors
        public static final Color PRIMARY_ACCENT = new Color(13, 110, 253);    // Primary blue
        public static final Color SUCCESS_ACCENT = new Color(25, 135, 84);     // Success green
        public static final Color WARNING_ACCENT = new Color(255, 193, 7);     // Warning yellow
        public static final Color DANGER_ACCENT = new Color(220, 53, 69);      // Danger red
        public static final Color INFO_ACCENT = new Color(13, 202, 240);       // Info cyan
        
        // Border colors
        public static final Color BORDER_COLOR = new Color(206, 212, 218);     // Default border
        public static final Color FOCUS_BORDER = new Color(13, 110, 253);      // Focus border
        
        // Hover states
        public static final Color HOVER_BG = new Color(248, 249, 250);         // Hover background
        public static final Color SELECTED_BG = new Color(13, 110, 253, 30);   // Selected background
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
     * Apply dark theme to component
     */
    private void applyDarkTheme(JComponent component) {
        component.setBackground(DarkTheme.PRIMARY_BG);
        component.setForeground(DarkTheme.PRIMARY_TEXT);
        
        if (component instanceof JPanel) {
            component.setBackground(DarkTheme.SECONDARY_BG);
        } else if (component instanceof JButton) {
            component.setBackground(DarkTheme.TERTIARY_BG);
            component.setForeground(DarkTheme.PRIMARY_TEXT);
        } else if (component instanceof JTextField || component instanceof JTextArea) {
            component.setBackground(DarkTheme.SURFACE_BG);
            component.setForeground(DarkTheme.PRIMARY_TEXT);
            component.setBorder(BorderFactory.createLineBorder(DarkTheme.BORDER_COLOR));
        } else if (component instanceof JLabel) {
            component.setForeground(DarkTheme.PRIMARY_TEXT);
        } else if (component instanceof JTable) {
            component.setBackground(DarkTheme.SECONDARY_BG);
            component.setForeground(DarkTheme.PRIMARY_TEXT);
            ((JTable) component).setGridColor(DarkTheme.BORDER_COLOR);
        }
    }
    
    /**
     * Apply light theme to component
     */
    private void applyLightTheme(JComponent component) {
        component.setBackground(LightTheme.PRIMARY_BG);
        component.setForeground(LightTheme.PRIMARY_TEXT);
        
        if (component instanceof JPanel) {
            component.setBackground(LightTheme.SECONDARY_BG);
        } else if (component instanceof JButton) {
            component.setBackground(LightTheme.TERTIARY_BG);
            component.setForeground(LightTheme.PRIMARY_TEXT);
        } else if (component instanceof JTextField || component instanceof JTextArea) {
            component.setBackground(LightTheme.SURFACE_BG);
            component.setForeground(LightTheme.PRIMARY_TEXT);
            component.setBorder(BorderFactory.createLineBorder(LightTheme.BORDER_COLOR));
        } else if (component instanceof JLabel) {
            component.setForeground(LightTheme.PRIMARY_TEXT);
        } else if (component instanceof JTable) {
            component.setBackground(LightTheme.SECONDARY_BG);
            component.setForeground(LightTheme.PRIMARY_TEXT);
            ((JTable) component).setGridColor(LightTheme.BORDER_COLOR);
        }
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