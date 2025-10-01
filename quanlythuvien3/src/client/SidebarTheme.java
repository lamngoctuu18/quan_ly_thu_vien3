package client;

import java.awt.Color;

/**
 * SidebarTheme - Quản lý theme cho sidebar buttons
 */
public class SidebarTheme {
    public static class Theme {
        public final Color normalBg;
        public final Color hoverBg;
        public final Color selectedBg;
        public final Color textColor;
        public final Color iconColor;
        public final Color accentColor;
        public final Color glowColor;
        
        public Theme(Color normalBg, Color hoverBg, Color selectedBg, 
                    Color textColor, Color iconColor, Color accentColor, Color glowColor) {
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.selectedBg = selectedBg;
            this.textColor = textColor;
            this.iconColor = iconColor;
            this.accentColor = accentColor;
            this.glowColor = glowColor;
        }
    }
    
    // Available themes
    public static final Theme BLUE_THEME = new Theme(
        new Color(52, 73, 94, 0),           // Normal background
        new Color(52, 152, 219, 25),        // Hover background  
        new Color(52, 152, 219, 80),        // Selected background
        Color.WHITE,                         // Text color
        new Color(236, 240, 241),           // Icon color
        new Color(52, 152, 219),            // Accent color
        new Color(52, 152, 219, 60)         // Glow color
    );
    
    public static final Theme PURPLE_THEME = new Theme(
        new Color(44, 62, 80, 0),           // Normal background
        new Color(155, 89, 182, 25),        // Hover background
        new Color(155, 89, 182, 80),        // Selected background
        Color.WHITE,                         // Text color
        new Color(236, 240, 241),           // Icon color
        new Color(155, 89, 182),            // Accent color
        new Color(155, 89, 182, 60)         // Glow color
    );
    
    public static final Theme GREEN_THEME = new Theme(
        new Color(44, 62, 80, 0),           // Normal background
        new Color(46, 204, 113, 25),        // Hover background
        new Color(46, 204, 113, 80),        // Selected background
        Color.WHITE,                         // Text color
        new Color(236, 240, 241),           // Icon color
        new Color(46, 204, 113),            // Accent color
        new Color(46, 204, 113, 60)         // Glow color
    );
    
    public static final Theme DARK_THEME = new Theme(
        new Color(33, 37, 41, 0),           // Normal background
        new Color(73, 80, 87, 40),          // Hover background
        new Color(108, 117, 125, 80),       // Selected background
        new Color(248, 249, 250),           // Text color
        new Color(206, 212, 218),           // Icon color
        new Color(108, 117, 125),           // Accent color
        new Color(108, 117, 125, 60)        // Glow color
    );
    
    public static final Theme ORANGE_THEME = new Theme(
        new Color(44, 62, 80, 0),           // Normal background
        new Color(230, 126, 34, 25),        // Hover background
        new Color(230, 126, 34, 80),        // Selected background
        Color.WHITE,                         // Text color
        new Color(236, 240, 241),           // Icon color
        new Color(230, 126, 34),            // Accent color
        new Color(230, 126, 34, 60)         // Glow color
    );
    
    // Current active theme
    private static Theme currentTheme = BLUE_THEME;
    
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }
    
    // Quick theme switchers
    public static void useBlueTheme() { setTheme(BLUE_THEME); }
    public static void usePurpleTheme() { setTheme(PURPLE_THEME); }
    public static void useGreenTheme() { setTheme(GREEN_THEME); }
    public static void useDarkTheme() { setTheme(DARK_THEME); }
    public static void useOrangeTheme() { setTheme(ORANGE_THEME); }
}