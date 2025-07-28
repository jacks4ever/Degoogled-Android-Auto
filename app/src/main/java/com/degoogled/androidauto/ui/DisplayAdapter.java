package com.degoogled.androidauto.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import com.degoogled.androidauto.utils.LogManager;

/**
 * Handles display adaptation for different head unit screens,
 * with specific optimizations for Nissan Pathfinder HUD.
 */
public class DisplayAdapter {
    private static final String TAG = "DisplayAdapter";
    
    // Nissan Pathfinder 2023 display characteristics (to be confirmed with testing)
    private static final int NISSAN_PATHFINDER_WIDTH = 1280;
    private static final int NISSAN_PATHFINDER_HEIGHT = 720;
    private static final float NISSAN_PATHFINDER_DENSITY = 1.5f;
    
    // Display size categories
    public enum DisplaySize {
        SMALL,      // < 7 inch
        MEDIUM,     // 7-8 inch
        LARGE,      // 8-10 inch
        EXTRA_LARGE // > 10 inch
    }
    
    private final Context context;
    private DisplaySize displaySize;
    private float scaleFactor = 1.0f;
    private boolean isNissanPathfinder = false;
    private int displayWidth;
    private int displayHeight;
    private float displayDensity;
    
    public DisplayAdapter(Context context) {
        this.context = context;
        detectDisplayCharacteristics();
    }
    
    /**
     * Detect the display characteristics of the device
     */
    private void detectDisplayCharacteristics() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        
        Point size = new Point();
        display.getRealSize(size);
        
        displayWidth = size.x;
        displayHeight = size.y;
        displayDensity = metrics.density;
        
        LogManager.i(TAG, "Display characteristics: " + displayWidth + "x" + displayHeight + 
                ", density: " + displayDensity);
        
        // Check if this might be a Nissan Pathfinder
        if (Math.abs(displayWidth - NISSAN_PATHFINDER_WIDTH) < 100 && 
                Math.abs(displayHeight - NISSAN_PATHFINDER_HEIGHT) < 100) {
            LogManager.i(TAG, "Display matches Nissan Pathfinder characteristics");
            isNissanPathfinder = true;
        }
        
        // Determine display size category
        float diagonalInches = calculateScreenDiagonalInches(metrics);
        LogManager.i(TAG, "Estimated screen diagonal: " + diagonalInches + " inches");
        
        if (diagonalInches < 7.0f) {
            displaySize = DisplaySize.SMALL;
        } else if (diagonalInches < 8.0f) {
            displaySize = DisplaySize.MEDIUM;
        } else if (diagonalInches < 10.0f) {
            displaySize = DisplaySize.LARGE;
        } else {
            displaySize = DisplaySize.EXTRA_LARGE;
        }
        
        // Calculate scale factor based on display size
        calculateScaleFactor();
        
        LogManager.i(TAG, "Display size category: " + displaySize + ", scale factor: " + scaleFactor);
    }
    
    /**
     * Calculate the diagonal screen size in inches
     */
    private float calculateScreenDiagonalInches(DisplayMetrics metrics) {
        float widthInches = (float) displayWidth / metrics.xdpi;
        float heightInches = (float) displayHeight / metrics.ydpi;
        return (float) Math.sqrt(widthInches * widthInches + heightInches * heightInches);
    }
    
    /**
     * Calculate the scale factor based on display characteristics
     */
    private void calculateScaleFactor() {
        // Base scale factor on display size
        switch (displaySize) {
            case SMALL:
                scaleFactor = 0.85f;
                break;
            case MEDIUM:
                scaleFactor = 1.0f;
                break;
            case LARGE:
                scaleFactor = 1.15f;
                break;
            case EXTRA_LARGE:
                scaleFactor = 1.3f;
                break;
        }
        
        // Adjust for Nissan Pathfinder if detected
        if (isNissanPathfinder) {
            // Fine-tune based on Nissan Pathfinder characteristics
            scaleFactor *= 1.1f;  // Slightly larger for better visibility
        }
        
        // Adjust for display density
        if (displayDensity < 1.0f) {
            scaleFactor *= 1.2f;  // Increase for low-density displays
        } else if (displayDensity > 2.0f) {
            scaleFactor *= 0.9f;  // Decrease for high-density displays
        }
        
        // Adjust for orientation
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape is the expected orientation for Android Auto
            // No adjustment needed
        } else {
            // In portrait, we might need different scaling
            scaleFactor *= 0.9f;
        }
    }
    
    /**
     * Apply the appropriate text size to a TextView
     */
    public void applyTextSize(TextView textView, TextSize size) {
        float baseSize;
        
        switch (size) {
            case SMALL:
                baseSize = 12.0f;
                break;
            case MEDIUM:
                baseSize = 16.0f;
                break;
            case LARGE:
                baseSize = 20.0f;
                break;
            case EXTRA_LARGE:
                baseSize = 24.0f;
                break;
            case HEADER:
                baseSize = 28.0f;
                break;
            default:
                baseSize = 16.0f;
                break;
        }
        
        float scaledSize = baseSize * scaleFactor;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);
    }
    
    /**
     * Get the appropriate dimension value scaled for the current display
     */
    public int getDimension(Dimension dimension) {
        int baseValue;
        
        switch (dimension) {
            case TOUCH_TARGET:
                baseValue = 48;  // Minimum touch target size (dp)
                break;
            case PADDING_SMALL:
                baseValue = 4;
                break;
            case PADDING_MEDIUM:
                baseValue = 8;
                break;
            case PADDING_LARGE:
                baseValue = 16;
                break;
            case MARGIN_SMALL:
                baseValue = 8;
                break;
            case MARGIN_MEDIUM:
                baseValue = 16;
                break;
            case MARGIN_LARGE:
                baseValue = 24;
                break;
            case ICON_SMALL:
                baseValue = 24;
                break;
            case ICON_MEDIUM:
                baseValue = 36;
                break;
            case ICON_LARGE:
                baseValue = 48;
                break;
            default:
                baseValue = 8;
                break;
        }
        
        float scaledValue = baseValue * scaleFactor;
        return Math.round(scaledValue);
    }
    
    /**
     * Get the scale factor for the current display
     */
    public float getScaleFactor() {
        return scaleFactor;
    }
    
    /**
     * Check if the current display is likely a Nissan Pathfinder
     */
    public boolean isNissanPathfinder() {
        return isNissanPathfinder;
    }
    
    /**
     * Get the display size category
     */
    public DisplaySize getDisplaySize() {
        return displaySize;
    }
    
    /**
     * Get the display width in pixels
     */
    public int getDisplayWidth() {
        return displayWidth;
    }
    
    /**
     * Get the display height in pixels
     */
    public int getDisplayHeight() {
        return displayHeight;
    }
    
    /**
     * Update the display characteristics (call this if the configuration changes)
     */
    public void updateDisplayCharacteristics() {
        detectDisplayCharacteristics();
    }
    
    /**
     * Text size categories
     */
    public enum TextSize {
        SMALL,
        MEDIUM,
        LARGE,
        EXTRA_LARGE,
        HEADER
    }
    
    /**
     * Dimension categories
     */
    public enum Dimension {
        TOUCH_TARGET,
        PADDING_SMALL,
        PADDING_MEDIUM,
        PADDING_LARGE,
        MARGIN_SMALL,
        MARGIN_MEDIUM,
        MARGIN_LARGE,
        ICON_SMALL,
        ICON_MEDIUM,
        ICON_LARGE
    }
}