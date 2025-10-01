package client;

import javax.swing.*;
import java.awt.*;

/**
 * DarkModeAwareComponent - Base class cho các component hỗ trợ Dark Mode
 * Tự động cập nhật theme khi Dark Mode thay đổi
 */
public abstract class DarkModeAwareComponent extends JPanel implements DarkModeManager.DarkModeListener {
    protected DarkModeManager darkModeManager;
    private boolean isListenerRegistered = false;
    
    public DarkModeAwareComponent() {
        super();
        initializeDarkMode();
    }
    
    public DarkModeAwareComponent(LayoutManager layout) {
        super(layout);
        initializeDarkMode();
    }
    
    /**
     * Initialize dark mode support
     */
    private void initializeDarkMode() {
        darkModeManager = DarkModeManager.getInstance();
        registerDarkModeListener();
        applyCurrentTheme();
    }
    
    /**
     * Register this component as dark mode listener
     */
    private void registerDarkModeListener() {
        if (!isListenerRegistered) {
            darkModeManager.addDarkModeListener(this);
            isListenerRegistered = true;
        }
    }
    
    /**
     * Apply current theme to this component
     */
    protected void applyCurrentTheme() {
        SwingUtilities.invokeLater(() -> {
            updateTheme(darkModeManager.isDarkMode());
            darkModeManager.applyDarkMode(this);
        });
    }
    
    /**
     * Dark mode change callback
     */
    @Override
    public void onDarkModeChanged(boolean isDarkMode) {
        SwingUtilities.invokeLater(() -> {
            updateTheme(isDarkMode);
            darkModeManager.applyDarkMode(this);
        });
    }
    
    /**
     * Abstract method for subclasses to implement theme updates
     */
    protected abstract void updateTheme(boolean isDarkMode);
    
    /**
     * Cleanup resources when component is disposed
     */
    public void cleanup() {
        if (isListenerRegistered) {
            darkModeManager.removeDarkModeListener(this);
            isListenerRegistered = false;
        }
    }
    
    /**
     * Override finalize to ensure cleanup
     */
    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }
    
    /**
     * Get current background color based on theme
     */
    protected Color getCurrentBackgroundColor() {
        return darkModeManager.getBackgroundColor();
    }
    
    /**
     * Get current text color based on theme
     */
    protected Color getCurrentTextColor() {
        return darkModeManager.getTextColor();
    }
    
    /**
     * Get current secondary background color based on theme
     */
    protected Color getCurrentSecondaryBackgroundColor() {
        return darkModeManager.getSecondaryBackgroundColor();
    }
    
    /**
     * Get current border color based on theme
     */
    protected Color getCurrentBorderColor() {
        return darkModeManager.getBorderColor();
    }
    
    /**
     * Create a dark mode aware button
     */
    protected JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        applyButtonTheme(button);
        return button;
    }
    
    /**
     * Apply theme to button
     */
    protected void applyButtonTheme(JButton button) {
        if (darkModeManager.isDarkMode()) {
            button.setBackground(DarkModeManager.DarkTheme.TERTIARY_BG);
            button.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
            button.setBorder(BorderFactory.createLineBorder(DarkModeManager.DarkTheme.BORDER_COLOR));
        } else {
            button.setBackground(DarkModeManager.LightTheme.TERTIARY_BG);
            button.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
            button.setBorder(BorderFactory.createLineBorder(DarkModeManager.LightTheme.BORDER_COLOR));
        }
        button.setFocusPainted(false);
    }
    
    /**
     * Create a dark mode aware label
     */
    protected JLabel createThemedLabel(String text) {
        JLabel label = new JLabel(text);
        applyLabelTheme(label);
        return label;
    }
    
    /**
     * Apply theme to label
     */
    protected void applyLabelTheme(JLabel label) {
        if (darkModeManager.isDarkMode()) {
            label.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
        } else {
            label.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
        }
    }
    
    /**
     * Create a dark mode aware text field
     */
    protected JTextField createThemedTextField() {
        JTextField textField = new JTextField();
        applyTextFieldTheme(textField);
        return textField;
    }
    
    /**
     * Apply theme to text field
     */
    protected void applyTextFieldTheme(JTextField textField) {
        if (darkModeManager.isDarkMode()) {
            textField.setBackground(DarkModeManager.DarkTheme.SURFACE_BG);
            textField.setForeground(DarkModeManager.DarkTheme.PRIMARY_TEXT);
            textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DarkModeManager.DarkTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            textField.setCaretColor(DarkModeManager.DarkTheme.PRIMARY_TEXT);
        } else {
            textField.setBackground(DarkModeManager.LightTheme.SURFACE_BG);
            textField.setForeground(DarkModeManager.LightTheme.PRIMARY_TEXT);
            textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DarkModeManager.LightTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            textField.setCaretColor(DarkModeManager.LightTheme.PRIMARY_TEXT);
        }
    }
    
    /**
     * Create a dark mode aware panel
     */
    protected JPanel createThemedPanel() {
        JPanel panel = new JPanel();
        applyPanelTheme(panel);
        return panel;
    }
    
    /**
     * Apply theme to panel
     */
    protected void applyPanelTheme(JPanel panel) {
        if (darkModeManager.isDarkMode()) {
            panel.setBackground(DarkModeManager.DarkTheme.SECONDARY_BG);
        } else {
            panel.setBackground(DarkModeManager.LightTheme.SECONDARY_BG);
        }
    }
}