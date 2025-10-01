package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ModernSidebarButton - Nút sidebar với hiệu ứng animation mượt mà và hỗ trợ Dark Mode
 */
public class ModernSidebarButton extends JButton implements DarkModeManager.DarkModeListener {
    private boolean isSelected = false;
    private boolean isHovered = false;
    private Timer animationTimer;
    private float animationProgress = 0.0f;
    private float targetProgress = 0.0f;
    
    // Theme management
    private DarkModeManager darkModeManager;
    
    private String iconText;
    private String buttonText;
    
    public ModernSidebarButton(String icon, String text) {
        this.iconText = icon;
        this.buttonText = text;
        this.darkModeManager = DarkModeManager.getInstance();
        
        // Register for dark mode changes
        darkModeManager.addDarkModeListener(this);
        
        setupButton();
        setupAnimation();
        setupMouseListeners();
    }
    
    private void setupButton() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(230, 50));
        setMaximumSize(new Dimension(230, 50));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        
        updateButtonContent();
    }
    
    private void updateButtonContent() {
        removeAll();
        
        // Get current colors based on dark mode
        Color iconColor = getCurrentIconColor();
        Color textColor = getCurrentTextColor();
        
        // Icon label
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconLabel.setForeground(iconColor);
        iconLabel.setPreferredSize(new Dimension(25, 20));
        
        // Text label
        JLabel textLabel = new JLabel(buttonText);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        textLabel.setForeground(textColor);
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        add(iconLabel, BorderLayout.WEST);
        add(textLabel, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
    
    private Color getCurrentIconColor() {
        if (darkModeManager.isDarkMode()) {
            return isSelected ? DarkModeManager.DarkTheme.PRIMARY_ACCENT : DarkModeManager.DarkTheme.SECONDARY_TEXT;
        } else {
            return isSelected ? new Color(52, 152, 219) : new Color(108, 117, 125);
        }
    }
    
    private Color getCurrentTextColor() {
        if (darkModeManager.isDarkMode()) {
            return isSelected ? DarkModeManager.DarkTheme.PRIMARY_TEXT : DarkModeManager.DarkTheme.SECONDARY_TEXT;
        } else {
            return isSelected ? Color.WHITE : new Color(52, 73, 94);
        }
    }
    
    private Color getCurrentNormalBg() {
        return new Color(0, 0, 0, 0); // Transparent
    }
    
    private Color getCurrentHoverBg() {
        if (darkModeManager.isDarkMode()) {
            return new Color(73, 80, 87, 100);
        } else {
            return new Color(52, 152, 219, 50);
        }
    }
    
    private Color getCurrentSelectedBg() {
        if (darkModeManager.isDarkMode()) {
            return new Color(13, 110, 253, 80);
        } else {
            return new Color(52, 152, 219, 120);
        }
    }
    
    private Color getCurrentAccentColor() {
        if (darkModeManager.isDarkMode()) {
            return DarkModeManager.DarkTheme.PRIMARY_ACCENT;
        } else {
            return new Color(52, 152, 219);
        }
    }
    
    private void setupAnimation() {
        animationTimer = new Timer(16, e -> { // ~60 FPS
            float speed = 0.15f;
            
            if (animationProgress < targetProgress) {
                animationProgress = Math.min(targetProgress, animationProgress + speed);
            } else if (animationProgress > targetProgress) {
                animationProgress = Math.max(targetProgress, animationProgress - speed);
            }
            
            repaint();
            
            // Stop timer when animation complete
            if (Math.abs(animationProgress - targetProgress) < 0.01f) {
                animationProgress = targetProgress;
                animationTimer.stop();
            }
        });
    }
    
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelected) {
                    isHovered = true;
                    animateToHover();
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isSelected) {
                    isHovered = false;
                    animateToNormal();
                }
            }
        });
    }
    
    private void animateToNormal() {
        targetProgress = 0.0f;
        startAnimation();
    }
    
    private void animateToHover() {
        targetProgress = 0.6f;
        startAnimation();
    }
    
    private void animateToSelected() {
        targetProgress = 1.0f;
        startAnimation();
    }
    
    private void startAnimation() {
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }
    
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateButtonContent(); // Update colors when selection changes
        
        if (selected) {
            animateToSelected();
        } else if (isHovered) {
            animateToHover();
        } else {
            animateToNormal();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // Calculate colors based on animation progress and current theme
        Color bgColor = interpolateColor(getCurrentNormalBg(), 
                                       isSelected ? getCurrentSelectedBg() : getCurrentHoverBg(), 
                                       animationProgress);
        
        // Draw rounded background
        int cornerRadius = 8;
        if (animationProgress > 0.01f) {
            g2d.setColor(bgColor);
            g2d.fillRoundRect(5, 2, width - 10, height - 4, cornerRadius, cornerRadius);
        }
        
        // Draw left accent bar for selected state
        if (isSelected && animationProgress > 0.5f) {
            int barHeight = (int) ((height - 10) * (animationProgress - 0.5f) * 2);
            int barY = (height - barHeight) / 2;
            
            g2d.setColor(getCurrentAccentColor());
            g2d.fillRoundRect(2, barY, 4, barHeight, 2, 2);
        }
        
        // Draw subtle glow effect on hover/selected
        if (animationProgress > 0.2f) {
            float glowOpacity = (animationProgress - 0.2f) * 0.3f;
            Color accentColor = getCurrentAccentColor();
            Color glowColor = new Color(accentColor.getRed(), accentColor.getGreen(), 
                                      accentColor.getBlue(), (int)(glowOpacity * 255));
            
            g2d.setColor(glowColor);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(4, 1, width - 8, height - 2, cornerRadius + 1, cornerRadius + 1);
        }
        
        g2d.dispose();
        super.paintComponent(g);
    }
    
    /**
     * Interpolate between two colors
     */
    private Color interpolateColor(Color start, Color end, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        
        int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
        int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
        int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);
        int a = (int) (start.getAlpha() + (end.getAlpha() - start.getAlpha()) * progress);
        
        return new Color(r, g, b, a);
    }
    
    /**
     * Dark mode change callback
     */
    @Override
    public void onDarkModeChanged(boolean isDarkMode) {
        SwingUtilities.invokeLater(() -> {
            updateButtonContent();
            repaint();
        });
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (darkModeManager != null) {
            darkModeManager.removeDarkModeListener(this);
        }
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }
}