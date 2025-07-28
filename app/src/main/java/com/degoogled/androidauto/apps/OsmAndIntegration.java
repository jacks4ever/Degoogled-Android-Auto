package com.degoogled.androidauto.apps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.degoogled.androidauto.logging.ConnectionLogger;

import java.util.List;

/**
 * OsmAnd Navigation Integration for Degoogled Android Auto
 * Provides navigation services using OsmAnd offline maps
 */
public class OsmAndIntegration {
    private static final String TAG = "OsmAndIntegration";
    
    // OsmAnd package names (try multiple variants)
    private static final String[] OSMAND_PACKAGES = {
        "net.osmand.plus",           // OsmAnd+
        "net.osmand",                // OsmAnd free
        "net.osmand.dev",            // OsmAnd development
        "net.osmand.srtmPlugin.paid" // OsmAnd with SRTM
    };
    
    // OsmAnd intents and actions
    private static final String OSMAND_NAVIGATE_ACTION = "net.osmand.plus.navigate";
    private static final String OSMAND_SHOW_LOCATION_ACTION = "net.osmand.plus.show_location";
    private static final String OSMAND_ADD_FAVORITE_ACTION = "net.osmand.plus.add_favorite";
    
    private Context context;
    private ConnectionLogger logger;
    private String installedOsmAndPackage;
    private NavigationListener navigationListener;
    
    // Navigation state
    private boolean isNavigating = false;
    private String currentDestination;
    private double currentLatitude;
    private double currentLongitude;
    
    public interface NavigationListener {
        void onNavigationStarted(String destination);
        void onNavigationStopped();
        void onNavigationUpdate(NavigationInfo info);
        void onNavigationError(String error);
    }
    
    public static class NavigationInfo {
        public final String instruction;
        public final int distanceToNextTurn; // meters
        public final int totalDistance; // meters
        public final int estimatedTime; // seconds
        public final String streetName;
        public final int turnType;
        
        public NavigationInfo(String instruction, int distanceToNextTurn, int totalDistance, 
                            int estimatedTime, String streetName, int turnType) {
            this.instruction = instruction;
            this.distanceToNextTurn = distanceToNextTurn;
            this.totalDistance = totalDistance;
            this.estimatedTime = estimatedTime;
            this.streetName = streetName;
            this.turnType = turnType;
        }
    }
    
    public OsmAndIntegration(Context context, ConnectionLogger logger) {
        this.context = context;
        this.logger = logger;
        this.installedOsmAndPackage = findInstalledOsmAnd();
    }
    
    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }
    
    /**
     * Find installed OsmAnd package
     */
    private String findInstalledOsmAnd() {
        PackageManager pm = context.getPackageManager();
        
        for (String packageName : OSMAND_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                logger.logInfo("Found OsmAnd package: " + packageName);
                return packageName;
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found, try next
            }
        }
        
        logger.logWarning("No OsmAnd package found");
        return null;
    }
    
    /**
     * Check if OsmAnd is available
     */
    public boolean isOsmAndAvailable() {
        return installedOsmAndPackage != null;
    }
    
    /**
     * Start navigation to coordinates
     */
    public boolean navigateToCoordinates(double latitude, double longitude, String name) {
        if (!isOsmAndAvailable()) {
            logger.logError("OsmAnd not available for navigation");
            if (navigationListener != null) {
                navigationListener.onNavigationError("OsmAnd not installed");
            }
            return false;
        }
        
        logger.logInfo("Starting navigation to: " + name + " (" + latitude + ", " + longitude + ")");
        
        try {
            Intent intent = new Intent(OSMAND_NAVIGATE_ACTION);
            intent.setPackage(installedOsmAndPackage);
            
            // Add coordinates and destination name
            intent.putExtra("lat", latitude);
            intent.putExtra("lon", longitude);
            intent.putExtra("name", name);
            intent.putExtra("start_navigation", true);
            
            // Set flags for external app launch
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            context.startActivity(intent);
            
            // Update state
            isNavigating = true;
            currentDestination = name;
            currentLatitude = latitude;
            currentLongitude = longitude;
            
            if (navigationListener != null) {
                navigationListener.onNavigationStarted(name);
            }
            
            logger.logInfo("Navigation started successfully");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to start navigation: " + e.getMessage());
            if (navigationListener != null) {
                navigationListener.onNavigationError("Failed to start navigation: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Navigate to address
     */
    public boolean navigateToAddress(String address) {
        if (!isOsmAndAvailable()) {
            logger.logError("OsmAnd not available for navigation");
            if (navigationListener != null) {
                navigationListener.onNavigationError("OsmAnd not installed");
            }
            return false;
        }
        
        logger.logInfo("Starting navigation to address: " + address);
        
        try {
            // Use geo intent for address-based navigation
            Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Intent intent = new Intent(Intent.ACTION_VIEW, geoUri);
            intent.setPackage(installedOsmAndPackage);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(intent);
            
            // Update state
            isNavigating = true;
            currentDestination = address;
            
            if (navigationListener != null) {
                navigationListener.onNavigationStarted(address);
            }
            
            logger.logInfo("Address navigation started successfully");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to start address navigation: " + e.getMessage());
            if (navigationListener != null) {
                navigationListener.onNavigationError("Failed to start navigation: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Show location on map without navigation
     */
    public boolean showLocation(double latitude, double longitude, String name) {
        if (!isOsmAndAvailable()) {
            logger.logError("OsmAnd not available");
            return false;
        }
        
        logger.logInfo("Showing location: " + name + " (" + latitude + ", " + longitude + ")");
        
        try {
            Intent intent = new Intent(OSMAND_SHOW_LOCATION_ACTION);
            intent.setPackage(installedOsmAndPackage);
            
            intent.putExtra("lat", latitude);
            intent.putExtra("lon", longitude);
            intent.putExtra("name", name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(intent);
            
            logger.logInfo("Location shown successfully");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to show location: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Add favorite location
     */
    public boolean addFavorite(double latitude, double longitude, String name, String category) {
        if (!isOsmAndAvailable()) {
            logger.logError("OsmAnd not available");
            return false;
        }
        
        logger.logInfo("Adding favorite: " + name);
        
        try {
            Intent intent = new Intent(OSMAND_ADD_FAVORITE_ACTION);
            intent.setPackage(installedOsmAndPackage);
            
            intent.putExtra("lat", latitude);
            intent.putExtra("lon", longitude);
            intent.putExtra("name", name);
            intent.putExtra("category", category != null ? category : "Favorites");
            
            context.startActivity(intent);
            
            logger.logInfo("Favorite added successfully");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to add favorite: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Stop current navigation
     */
    public boolean stopNavigation() {
        if (!isNavigating) {
            logger.logInfo("No active navigation to stop");
            return true;
        }
        
        logger.logInfo("Stopping navigation");
        
        try {
            // Try to send stop navigation intent
            Intent intent = new Intent();
            intent.setPackage(installedOsmAndPackage);
            intent.setAction("net.osmand.plus.stop_navigation");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(intent);
            
            // Update state
            isNavigating = false;
            currentDestination = null;
            
            if (navigationListener != null) {
                navigationListener.onNavigationStopped();
            }
            
            logger.logInfo("Navigation stopped");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to stop navigation: " + e.getMessage());
            // Still update state even if intent failed
            isNavigating = false;
            currentDestination = null;
            
            if (navigationListener != null) {
                navigationListener.onNavigationStopped();
            }
            
            return false;
        }
    }
    
    /**
     * Launch OsmAnd main activity
     */
    public boolean launchOsmAnd() {
        if (!isOsmAndAvailable()) {
            logger.logError("OsmAnd not available");
            return false;
        }
        
        logger.logInfo("Launching OsmAnd");
        
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(installedOsmAndPackage);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                
                logger.logInfo("OsmAnd launched successfully");
                return true;
            } else {
                logger.logError("Could not create launch intent for OsmAnd");
                return false;
            }
            
        } catch (Exception e) {
            logger.logError("Failed to launch OsmAnd: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Search for places using OsmAnd
     */
    public boolean searchPlaces(String query) {
        if (!isOsmAndAvailable()) {
            logger.logError("OsmAnd not available");
            return false;
        }
        
        logger.logInfo("Searching places: " + query);
        
        try {
            // Use geo intent for search
            Uri searchUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
            Intent intent = new Intent(Intent.ACTION_VIEW, searchUri);
            intent.setPackage(installedOsmAndPackage);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(intent);
            
            logger.logInfo("Place search initiated");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to search places: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get navigation status for Android Auto display
     */
    public NavigationStatus getNavigationStatus() {
        return new NavigationStatus(
            isNavigating,
            currentDestination,
            currentLatitude,
            currentLongitude,
            installedOsmAndPackage
        );
    }
    
    /**
     * Simulate navigation updates (since we can't get real-time data from OsmAnd easily)
     */
    public void simulateNavigationUpdate() {
        if (!isNavigating || navigationListener == null) {
            return;
        }
        
        // Simulate navigation instruction
        NavigationInfo info = new NavigationInfo(
            "Continue straight for 500m",
            500,
            2500,
            180, // 3 minutes
            "Main Street",
            0 // Straight
        );
        
        navigationListener.onNavigationUpdate(info);
    }
    
    /**
     * Check if OsmAnd supports Android Auto integration
     */
    public boolean supportsAndroidAuto() {
        if (!isOsmAndAvailable()) {
            return false;
        }
        
        // Check if OsmAnd has Android Auto support
        PackageManager pm = context.getPackageManager();
        Intent autoIntent = new Intent("android.intent.action.MAIN");
        autoIntent.addCategory("android.intent.category.CAR_MODE");
        autoIntent.setPackage(installedOsmAndPackage);
        
        List<ResolveInfo> activities = pm.queryIntentActivities(autoIntent, 0);
        boolean hasAutoSupport = !activities.isEmpty();
        
        logger.logInfo("OsmAnd Android Auto support: " + hasAutoSupport);
        return hasAutoSupport;
    }
    
    /**
     * Navigation status container
     */
    public static class NavigationStatus {
        public final boolean isNavigating;
        public final String destination;
        public final double latitude;
        public final double longitude;
        public final String osmandPackage;
        
        public NavigationStatus(boolean isNavigating, String destination, 
                              double latitude, double longitude, String osmandPackage) {
            this.isNavigating = isNavigating;
            this.destination = destination;
            this.latitude = latitude;
            this.longitude = longitude;
            this.osmandPackage = osmandPackage;
        }
    }
    
    // Getters
    public boolean isNavigating() {
        return isNavigating;
    }
    
    public String getCurrentDestination() {
        return currentDestination;
    }
    
    public String getInstalledOsmAndPackage() {
        return installedOsmAndPackage;
    }
}