package com.degoogled.androidauto.integration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;

import com.degoogled.androidauto.logging.ConnectionLogger;
import com.degoogled.androidauto.protocol.UsbConnection;
import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;
import com.degoogled.androidauto.protocol.messages.nav.NavTurnEvent;
import com.degoogled.androidauto.utils.LogManager;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Integration with OsmAnd for navigation.
 * Uses OsmAnd's API to get navigation information and send it to the head unit.
 */
public class OsmAndIntegration {
    private static final String TAG = "OsmAndIntegration";
    
    // OsmAnd package and service information
    private static final String OSMAND_PACKAGE = "net.osmand";
    private static final String OSMAND_PLUS_PACKAGE = "net.osmand.plus";
    private static final String OSMAND_API_SERVICE = "net.osmand.aidl.OsmandAidlService";
    
    // Turn types mapping from OsmAnd to Android Auto
    private static final Map<Integer, Integer> TURN_TYPE_MAP = new HashMap<>();
    static {
        // Initialize the turn type mapping
        TURN_TYPE_MAP.put(1, NavTurnEvent.TURN_TYPE_STRAIGHT);
        TURN_TYPE_MAP.put(2, NavTurnEvent.TURN_TYPE_RIGHT);
        TURN_TYPE_MAP.put(3, NavTurnEvent.TURN_TYPE_LEFT);
        TURN_TYPE_MAP.put(4, NavTurnEvent.TURN_TYPE_SLIGHT_RIGHT);
        TURN_TYPE_MAP.put(5, NavTurnEvent.TURN_TYPE_SLIGHT_LEFT);
        TURN_TYPE_MAP.put(6, NavTurnEvent.TURN_TYPE_SHARP_RIGHT);
        TURN_TYPE_MAP.put(7, NavTurnEvent.TURN_TYPE_SHARP_LEFT);
        TURN_TYPE_MAP.put(8, NavTurnEvent.TURN_TYPE_U_TURN);
        TURN_TYPE_MAP.put(9, NavTurnEvent.TURN_TYPE_ROUNDABOUT);
        TURN_TYPE_MAP.put(10, NavTurnEvent.TURN_TYPE_EXIT);
        TURN_TYPE_MAP.put(11, NavTurnEvent.TURN_TYPE_DESTINATION);
    }
    
    private final Context context;
    private final ConnectionLogger logger;
    private final UsbConnection usbConnection;
    
    private IOsmAndAidlInterface osmAndService;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isNavigating = new AtomicBoolean(false);
    
    private OsmAndNavigationListener navigationListener;
    
    /**
     * Create a new OsmAnd integration
     */
    public OsmAndIntegration(Context context, ConnectionLogger logger, UsbConnection usbConnection) {
        this.context = context;
        this.logger = logger;
        this.usbConnection = usbConnection;
    }
    
    /**
     * Check if OsmAnd is installed
     */
    public boolean isOsmAndInstalled() {
        PackageManager pm = context.getPackageManager();
        
        try {
            pm.getPackageInfo(OSMAND_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            try {
                pm.getPackageInfo(OSMAND_PLUS_PACKAGE, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e2) {
                return false;
            }
        }
    }
    
    /**
     * Connect to the OsmAnd service
     */
    public boolean connect() {
        if (isConnected.get()) {
            return true;
        }
        
        LogManager.i(TAG, "Connecting to OsmAnd service");
        logger.logConnection("Connecting to OsmAnd service");
        
        Intent intent = new Intent();
        
        // Try to connect to OsmAnd or OsmAnd+
        String packageName = OSMAND_PACKAGE;
        try {
            context.getPackageManager().getPackageInfo(OSMAND_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            try {
                context.getPackageManager().getPackageInfo(OSMAND_PLUS_PACKAGE, 0);
                packageName = OSMAND_PLUS_PACKAGE;
            } catch (PackageManager.NameNotFoundException e2) {
                LogManager.e(TAG, "OsmAnd not installed");
                logger.logError("OsmAnd not installed");
                return false;
            }
        }
        
        intent.setComponent(new ComponentName(packageName, OSMAND_API_SERVICE));
        
        boolean bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        if (!bound) {
            LogManager.e(TAG, "Failed to bind to OsmAnd service");
            logger.logError("Failed to bind to OsmAnd service");
        }
        
        return bound;
    }
    
    /**
     * Disconnect from the OsmAnd service
     */
    public void disconnect() {
        if (!isConnected.get()) {
            return;
        }
        
        LogManager.i(TAG, "Disconnecting from OsmAnd service");
        logger.logConnection("Disconnecting from OsmAnd service");
        
        try {
            if (navigationListener != null) {
                osmAndService.removeNavigationListener(navigationListener);
            }
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error removing navigation listener", e);
        }
        
        context.unbindService(serviceConnection);
        osmAndService = null;
        isConnected.set(false);
    }
    
    /**
     * Start navigation to a destination
     */
    public boolean startNavigation(double latitude, double longitude, String destinationName) {
        if (!isConnected.get()) {
            LogManager.w(TAG, "Not connected to OsmAnd service");
            return false;
        }
        
        LogManager.i(TAG, "Starting navigation to " + destinationName);
        logger.logConnection("Starting navigation to " + destinationName);
        
        try {
            // Create a navigation params object
            NavigationParams params = new NavigationParams();
            params.setLatitude(latitude);
            params.setLongitude(longitude);
            params.setDestinationName(destinationName);
            params.setForce(true);
            
            // Start navigation
            boolean result = osmAndService.startNavigation(params);
            
            if (result) {
                isNavigating.set(true);
            }
            
            return result;
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error starting navigation", e);
            logger.logError("Error starting navigation", e);
            return false;
        }
    }
    
    /**
     * Stop navigation
     */
    public boolean stopNavigation() {
        if (!isConnected.get() || !isNavigating.get()) {
            return false;
        }
        
        LogManager.i(TAG, "Stopping navigation");
        logger.logConnection("Stopping navigation");
        
        try {
            boolean result = osmAndService.stopNavigation();
            
            if (result) {
                isNavigating.set(false);
            }
            
            return result;
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error stopping navigation", e);
            logger.logError("Error stopping navigation", e);
            return false;
        }
    }
    
    /**
     * Check if navigation is active
     */
    public boolean isNavigating() {
        return isNavigating.get();
    }
    
    /**
     * Service connection for OsmAnd
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogManager.i(TAG, "Connected to OsmAnd service");
            logger.logConnection("Connected to OsmAnd service");
            
            osmAndService = IOsmAndAidlInterface.Stub.asInterface(service);
            isConnected.set(true);
            
            try {
                // Register a navigation listener
                navigationListener = new OsmAndNavigationListener();
                osmAndService.addNavigationListener(navigationListener);
            } catch (RemoteException e) {
                LogManager.e(TAG, "Error adding navigation listener", e);
                logger.logError("Error adding navigation listener", e);
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogManager.i(TAG, "Disconnected from OsmAnd service");
            logger.logConnection("Disconnected from OsmAnd service");
            
            osmAndService = null;
            isConnected.set(false);
            isNavigating.set(false);
        }
    };
    
    /**
     * Navigation listener for OsmAnd
     */
    private class OsmAndNavigationListener extends IOsmAndNavigationListener.Stub {
        @Override
        public void onNavigationStateChanged(NavigationState state) throws RemoteException {
            LogManager.d(TAG, "Navigation state changed: " + state.isNavigating());
            
            isNavigating.set(state.isNavigating());
            
            if (!state.isNavigating()) {
                // Navigation stopped
                LogManager.i(TAG, "Navigation stopped");
                logger.logConnection("Navigation stopped");
            }
        }
        
        @Override
        public void onTurnEvent(TurnEvent event) throws RemoteException {
            LogManager.d(TAG, "Turn event: " + event.getTurnType() + ", " + event.getTurnName());
            
            // Map OsmAnd turn type to Android Auto turn type
            int turnType = TURN_TYPE_MAP.getOrDefault(event.getTurnType(), NavTurnEvent.TURN_TYPE_STRAIGHT);
            
            // Get the turn icon
            byte[] turnIcon = getTurnIcon(event.getTurnType());
            
            // Create and send a turn event message
            NavTurnEvent turnEvent = new NavTurnEvent(
                    turnType,
                    event.getDistanceToTurn(),
                    event.getTurnName(),
                    event.getRoadName(),
                    turnIcon
            );
            
            usbConnection.sendMessage(turnEvent);
        }
    }
    
    /**
     * Get a turn icon for the given turn type
     */
    private byte[] getTurnIcon(int osmandTurnType) {
        // In a real implementation, this would load the appropriate icon from resources
        // For now, we'll return a placeholder
        
        // Create a simple bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_directions);
        
        if (bitmap == null) {
            return new byte[0];
        }
        
        // Convert the bitmap to a byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    
    /**
     * OsmAnd AIDL interface
     */
    public interface IOsmAndAidlInterface {
        boolean startNavigation(NavigationParams params) throws RemoteException;
        boolean stopNavigation() throws RemoteException;
        void addNavigationListener(OsmAndNavigationListener listener) throws RemoteException;
        void removeNavigationListener(OsmAndNavigationListener listener) throws RemoteException;
        
        abstract class Stub implements IOsmAndAidlInterface {
            public static IOsmAndAidlInterface asInterface(IBinder binder) {
                // In a real implementation, this would use the actual AIDL-generated code
                return null;
            }
        }
    }
    
    /**
     * OsmAnd navigation listener interface
     */
    public interface IOsmAndNavigationListener {
        void onNavigationStateChanged(NavigationState state) throws RemoteException;
        void onTurnEvent(TurnEvent event) throws RemoteException;
        
        abstract class Stub implements IOsmAndNavigationListener {
            // In a real implementation, this would use the actual AIDL-generated code
        }
    }
    
    /**
     * Navigation parameters for OsmAnd
     */
    public static class NavigationParams {
        private double latitude;
        private double longitude;
        private String destinationName;
        private boolean force;
        
        public double getLatitude() {
            return latitude;
        }
        
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
        
        public String getDestinationName() {
            return destinationName;
        }
        
        public void setDestinationName(String destinationName) {
            this.destinationName = destinationName;
        }
        
        public boolean isForce() {
            return force;
        }
        
        public void setForce(boolean force) {
            this.force = force;
        }
    }
    
    /**
     * Navigation state from OsmAnd
     */
    public static class NavigationState {
        private boolean isNavigating;
        
        public boolean isNavigating() {
            return isNavigating;
        }
        
        public void setNavigating(boolean navigating) {
            isNavigating = navigating;
        }
    }
    
    /**
     * Turn event from OsmAnd
     */
    public static class TurnEvent {
        private int turnType;
        private String turnName;
        private String roadName;
        private int distanceToTurn;
        
        public int getTurnType() {
            return turnType;
        }
        
        public void setTurnType(int turnType) {
            this.turnType = turnType;
        }
        
        public String getTurnName() {
            return turnName;
        }
        
        public void setTurnName(String turnName) {
            this.turnName = turnName;
        }
        
        public String getRoadName() {
            return roadName;
        }
        
        public void setRoadName(String roadName) {
            this.roadName = roadName;
        }
        
        public int getDistanceToTurn() {
            return distanceToTurn;
        }
        
        public void setDistanceToTurn(int distanceToTurn) {
            this.distanceToTurn = distanceToTurn;
        }
    }
}