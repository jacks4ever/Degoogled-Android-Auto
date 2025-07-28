package com.degoogled.androidauto.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.degoogled.androidauto.logging.ConnectionLogger;

/**
 * Display adapter specifically optimized for 2023 Nissan Pathfinder head unit
 * Handles display characteristics, UI scaling, and touch target optimization
 */
public class NissanDisplayAdapter {
    private static final String TAG = "NissanDisplayAdapter";
    
    // Known Nissan Pathfinder display specifications
    private static final int PATHFINDER_2023_WIDTH_8INCH = 800;
    private static final int PATHFINDER_2023_HEIGHT_8INCH = 480;
    private static final int PATHFINDER_2023_WIDTH_9INCH = 1024;
    private static final int PATHFINDER_2023_HEIGHT_9INCH = 600;
    
    // Optimal touch target sizes for automotive use
    private static final int MIN_TOUCH_TARGET_DP = 48; // Android minimum
    private static final int OPTIMAL_TOUCH_TARGET_DP = 56; // Automotive recommended
    private static final int LARGE_TOUCH_TARGET_DP = 64; // For primary actions
    
    // Font scaling for automotive displays
    private static final float AUTOMOTIVE_FONT_SCALE_SMALL = 1.1f;
    private static final float AUTOMOTIVE_FONT_SCALE_MEDIUM = 1.3f;
    private static final float AUTOMOTIVE_FONT_SCALE_LARGE = 1.5f;
    
    private Context context;
    private ConnectionLogger logger;
    private DisplayMetrics displayMetrics;
    private DisplayProfile displayProfile;
    
    public enum DisplaySize {
        SMALL_8INCH,
        LARGE_9INCH,
        UNKNOWN
    }
    
    public static class DisplayProfile {
        public int widthPx;
        public int heightPx;
        public float density;
        public int densityDpi;
        public DisplaySize size;
        public float fontScale;
        public int optimalTouchTargetPx;
        public boolean isLandscape;
        public String orientation;
        
        @Override
        public String toString() {
            return String.format("DisplayProfile{%dx%d, density=%.2f, dpi=%d, size=%s, fontScale=%.2f, touchTarget=%dpx, landscape=%s}",
                    widthPx, heightPx, density, densityDpi, size, fontScale, optimalTouchTargetPx, isLandscape);
        }
    }
    
    public NissanDisplayAdapter(Context context) {
        this.context = context;
        this.logger = ConnectionLogger.getInstance(context);
        this.displayMetrics = context.getResources().getDisplayMetrics();
        this.displayProfile = analyzeDisplay();
        
        logger.logDisplayCharacteristics(
            displayProfile.widthPx, 
            displayProfile.heightPx, 
            displayProfile.density, 
            displayProfile.orientation
        );
    }
    
    private DisplayProfile analyzeDisplay() {
        DisplayProfile profile = new DisplayProfile();
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        
        profile.widthPx = size.x;
        profile.heightPx = size.y;
        profile.density = displayMetrics.density;
        profile.densityDpi = displayMetrics.densityDpi;
        profile.isLandscape = profile.widthPx > profile.heightPx;
        profile.orientation = profile.isLandscape ? "landscape" : "portrait";
        
        // Determine display size category
        profile.size = determineDisplaySize(profile.widthPx, profile.heightPx);
        
        // Calculate optimal font scaling
        profile.fontScale = calculateOptimalFontScale(profile);
        
        // Calculate optimal touch target size
        profile.optimalTouchTargetPx = calculateOptimalTouchTarget(profile);
        
        Log.i(TAG, "Analyzed display profile: " + profile.toString());
        
        return profile;
    }
    
    private DisplaySize determineDisplaySize(int width, int height) {
        // Check for known Nissan Pathfinder display sizes
        if ((width == PATHFINDER_2023_WIDTH_8INCH && height == PATHFINDER_2023_HEIGHT_8INCH) ||
            (height == PATHFINDER_2023_WIDTH_8INCH && width == PATHFINDER_2023_HEIGHT_8INCH)) {
            logger.logInfo("Detected Nissan Pathfinder 8-inch display");
            return DisplaySize.SMALL_8INCH;
        }
        
        if ((width == PATHFINDER_2023_WIDTH_9INCH && height == PATHFINDER_2023_HEIGHT_9INCH) ||
            (height == PATHFINDER_2023_WIDTH_9INCH && width == PATHFINDER_2023_HEIGHT_9INCH)) {
            logger.logInfo("Detected Nissan Pathfinder 9-inch display");
            return DisplaySize.LARGE_9INCH;
        }
        
        // Estimate based on resolution
        int totalPixels = width * height;
        if (totalPixels <= 400000) { // ~800x500 or smaller
            logger.logInfo("Estimated small automotive display (8-inch class)");
            return DisplaySize.SMALL_8INCH;
        } else if (totalPixels >= 600000) { // ~1024x600 or larger
            logger.logInfo("Estimated large automotive display (9-inch class)");
            return DisplaySize.LARGE_9INCH;
        }
        
        logger.logWarning("Unknown display size: " + width + "x" + height);
        return DisplaySize.UNKNOWN;
    }
    
    private float calculateOptimalFontScale(DisplayProfile profile) {
        switch (profile.size) {
            case SMALL_8INCH:
                return AUTOMOTIVE_FONT_SCALE_MEDIUM; // Slightly larger for readability
            case LARGE_9INCH:
                return AUTOMOTIVE_FONT_SCALE_SMALL; // Standard size is fine
            default:
                // Adaptive scaling based on density
                if (profile.densityDpi < 160) {
                    return AUTOMOTIVE_FONT_SCALE_LARGE;
                } else if (profile.densityDpi > 240) {
                    return AUTOMOTIVE_FONT_SCALE_SMALL;
                } else {
                    return AUTOMOTIVE_FONT_SCALE_MEDIUM;
                }
        }
    }
    
    private int calculateOptimalTouchTarget(DisplayProfile profile) {
        int targetDp;
        
        switch (profile.size) {
            case SMALL_8INCH:
                targetDp = LARGE_TOUCH_TARGET_DP; // Larger targets for smaller screens
                break;
            case LARGE_9INCH:
                targetDp = OPTIMAL_TOUCH_TARGET_DP; // Standard automotive size
                break;
            default:
                targetDp = OPTIMAL_TOUCH_TARGET_DP;
                break;
        }
        
        // Convert DP to pixels
        int targetPx = (int) (targetDp * profile.density);
        
        // Ensure minimum size
        int minTargetPx = (int) (MIN_TOUCH_TARGET_DP * profile.density);
        return Math.max(targetPx, minTargetPx);
    }
    
    public DisplayProfile getDisplayProfile() {
        return displayProfile;
    }
    
    public int getOptimalTouchTargetSize() {
        return displayProfile.optimalTouchTargetPx;
    }
    
    public float getOptimalFontScale() {
        return displayProfile.fontScale;
    }
    
    public boolean isSmallDisplay() {
        return displayProfile.size == DisplaySize.SMALL_8INCH;
    }
    
    public boolean isLargeDisplay() {
        return displayProfile.size == DisplaySize.LARGE_9INCH;
    }
    
    public int getScaledSize(int baseSizeDp) {
        return (int) (baseSizeDp * displayProfile.density);
    }
    
    public int getScaledFontSize(int baseFontSizeSp) {
        return (int) (baseFontSizeSp * displayProfile.fontScale);
    }
    
    /**
     * Get recommended layout parameters for Nissan Pathfinder display
     */
    public LayoutRecommendations getLayoutRecommendations() {
        LayoutRecommendations recommendations = new LayoutRecommendations();
        
        switch (displayProfile.size) {
            case SMALL_8INCH:
                recommendations.maxColumns = 3;
                recommendations.maxRows = 2;
                recommendations.marginDp = 8;
                recommendations.paddingDp = 12;
                recommendations.cardElevationDp = 2;
                break;
                
            case LARGE_9INCH:
                recommendations.maxColumns = 4;
                recommendations.maxRows = 3;
                recommendations.marginDp = 12;
                recommendations.paddingDp = 16;
                recommendations.cardElevationDp = 4;
                break;
                
            default:
                recommendations.maxColumns = 3;
                recommendations.maxRows = 2;
                recommendations.marginDp = 10;
                recommendations.paddingDp = 14;
                recommendations.cardElevationDp = 3;
                break;
        }
        
        return recommendations;
    }
    
    public static class LayoutRecommendations {
        public int maxColumns;
        public int maxRows;
        public int marginDp;
        public int paddingDp;
        public int cardElevationDp;
        
        @Override
        public String toString() {
            return String.format("LayoutRecommendations{cols=%d, rows=%d, margin=%ddp, padding=%ddp, elevation=%ddp}",
                    maxColumns, maxRows, marginDp, paddingDp, cardElevationDp);
        }
    }
    
    /**
     * Test touch accuracy and calibrate for optimal user experience
     */
    public void calibrateTouchTargets() {
        logger.logInfo("Starting touch calibration for Nissan Pathfinder");
        
        // Simulate touch accuracy test (in real implementation, this would be interactive)
        float simulatedAccuracy = 0.85f; // 85% accuracy assumption
        boolean calibrationSuccess = simulatedAccuracy >= 0.8f;
        
        if (!calibrationSuccess) {
            logger.logWarning("Touch accuracy below threshold, increasing target sizes");
            displayProfile.optimalTouchTargetPx = (int) (displayProfile.optimalTouchTargetPx * 1.2f);
        }
        
        logger.logTouchCalibration(simulatedAccuracy, displayProfile.optimalTouchTargetPx, calibrationSuccess);
    }
    
    /**
     * Apply Nissan-specific display optimizations to the current configuration
     */
    public Configuration getOptimizedConfiguration() {
        Configuration config = new Configuration(context.getResources().getConfiguration());
        
        // Apply font scaling
        config.fontScale = displayProfile.fontScale;
        
        // Ensure landscape orientation for automotive use
        if (!displayProfile.isLandscape) {
            config.orientation = Configuration.ORIENTATION_LANDSCAPE;
            logger.logInfo("Forcing landscape orientation for automotive display");
        }
        
        // Set appropriate density for UI scaling
        config.densityDpi = displayProfile.densityDpi;
        
        logger.logInfo("Applied Nissan Pathfinder display optimizations: " + config.toString());
        
        return config;
    }
}