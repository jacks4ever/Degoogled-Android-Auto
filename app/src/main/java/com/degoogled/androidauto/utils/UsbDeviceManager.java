package com.degoogled.androidauto.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages USB device connections and handles Nissan-specific USB device IDs
 * for improved compatibility with Nissan Pathfinder and other vehicles.
 */
public class UsbDeviceManager {
    private static final String TAG = "UsbDeviceManager";
    private static final String ACTION_USB_PERMISSION = "com.degoogled.androidauto.USB_PERMISSION";
    
    // Known Nissan head unit USB IDs (to be expanded based on testing)
    private static final int[] NISSAN_VENDOR_IDS = {
        0x2B24,  // Nissan standard vendor ID
        0x0FCE,  // Sony (used in some Nissan models)
        0x054C,  // Sony alternate
        0x0BDA,  // Realtek (used in some infotainment systems)
        0x18D1   // Google/Android (fallback for generic Android Auto)
    };
    
    private final Context context;
    private final UsbManager usbManager;
    private ParcelFileDescriptor accessoryFd;
    private FileInputStream accessoryInputStream;
    private FileOutputStream accessoryOutputStream;
    private UsbConnectionListener connectionListener;
    private boolean isConnected = false;
    
    public interface UsbConnectionListener {
        void onUsbConnected(boolean success);
        void onUsbDisconnected();
        void onUsbData(byte[] data, int length);
        void onUsbError(String error);
    }
    
    public UsbDeviceManager(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        
        // Register for USB accessory events
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);
        
        LogManager.i(TAG, "UsbDeviceManager initialized");
    }
    
    /**
     * Set a listener for USB connection events
     */
    public void setConnectionListener(UsbConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    /**
     * Check if a USB accessory is connected and request permission if needed
     */
    public void checkForConnectedAccessory() {
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if (accessories != null && accessories.length > 0) {
            UsbAccessory accessory = accessories[0];
            LogManager.i(TAG, "USB accessory found: " + accessory.getModel() + " by " + accessory.getManufacturer());
            
            if (!usbManager.hasPermission(accessory)) {
                LogManager.i(TAG, "Requesting USB accessory permission");
                PendingIntent permissionIntent = PendingIntent.getBroadcast(
                        context, 0, new Intent(ACTION_USB_PERMISSION), 
                        PendingIntent.FLAG_IMMUTABLE);
                usbManager.requestPermission(accessory, permissionIntent);
            } else {
                openAccessory(accessory);
            }
        } else {
            LogManager.i(TAG, "No USB accessories found, checking for USB devices");
            checkForConnectedDevices();
        }
    }
    
    /**
     * Check for connected USB devices that might be head units
     */
    public void checkForConnectedDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        LogManager.i(TAG, "Found " + deviceList.size() + " USB devices");
        
        for (UsbDevice device : deviceList.values()) {
            LogManager.v(TAG, "USB device: " + device.getDeviceName() + 
                    " VendorID: " + device.getVendorId() + 
                    " ProductID: " + device.getProductId());
            
            if (isNissanHeadUnit(device)) {
                LogManager.i(TAG, "Potential Nissan head unit detected: " + 
                        device.getDeviceName() + " VendorID: 0x" + 
                        Integer.toHexString(device.getVendorId()).toUpperCase());
                
                if (!usbManager.hasPermission(device)) {
                    LogManager.i(TAG, "Requesting USB device permission");
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(
                            context, 0, new Intent(ACTION_USB_PERMISSION), 
                            PendingIntent.FLAG_IMMUTABLE);
                    usbManager.requestPermission(device, permissionIntent);
                } else {
                    connectToDevice(device);
                }
                return;
            }
        }
        
        LogManager.i(TAG, "No compatible head unit devices found");
        if (connectionListener != null) {
            connectionListener.onUsbError("No compatible head unit devices found");
        }
    }
    
    /**
     * Check if the device is likely a Nissan head unit
     */
    private boolean isNissanHeadUnit(UsbDevice device) {
        int vendorId = device.getVendorId();
        for (int nissanVendorId : NISSAN_VENDOR_IDS) {
            if (vendorId == nissanVendorId) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Open a connection to a USB accessory
     */
    private void openAccessory(UsbAccessory accessory) {
        LogManager.i(TAG, "Opening USB accessory connection");
        try {
            accessoryFd = usbManager.openAccessory(accessory);
            if (accessoryFd != null) {
                FileDescriptor fd = accessoryFd.getFileDescriptor();
                accessoryInputStream = new FileInputStream(fd);
                accessoryOutputStream = new FileOutputStream(fd);
                isConnected = true;
                
                LogManager.i(TAG, "USB accessory connected successfully");
                if (connectionListener != null) {
                    connectionListener.onUsbConnected(true);
                }
                
                // Start reading data from the accessory
                startAccessoryReader();
            } else {
                LogManager.e(TAG, "Failed to open USB accessory");
                if (connectionListener != null) {
                    connectionListener.onUsbError("Failed to open USB accessory");
                }
            }
        } catch (Exception e) {
            LogManager.e(TAG, "Error opening USB accessory: " + e.getMessage());
            if (connectionListener != null) {
                connectionListener.onUsbError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Connect to a USB device
     */
    private void connectToDevice(UsbDevice device) {
        LogManager.i(TAG, "Connecting to USB device: " + device.getDeviceName());
        UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection != null) {
            LogManager.i(TAG, "USB device connected successfully");
            isConnected = true;
            
            // Store device info for future reference
            String serialNumber = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    serialNumber = connection.getSerial();
                } catch (SecurityException e) {
                    LogManager.w(TAG, "Unable to get device serial: " + e.getMessage());
                }
            }
            
            LogManager.i(TAG, "Device info - Manufacturer: " + 
                    connection.getManufacturerName() + 
                    ", Product: " + connection.getProductName() + 
                    ", Serial: " + serialNumber);
            
            if (connectionListener != null) {
                connectionListener.onUsbConnected(true);
            }
            
            // Close the connection as we just needed the info
            // The actual communication will happen through the accessory mode
            connection.close();
            
            // Try to put the device into accessory mode
            // This is a simplified version and may need to be expanded
            checkForConnectedAccessory();
        } else {
            LogManager.e(TAG, "Failed to open USB device connection");
            if (connectionListener != null) {
                connectionListener.onUsbError("Failed to open USB device connection");
            }
        }
    }
    
    /**
     * Start a thread to read data from the USB accessory
     */
    private void startAccessoryReader() {
        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[16384];
            while (isConnected) {
                try {
                    int len = accessoryInputStream.read(buffer);
                    if (len > 0) {
                        LogManager.v(TAG, "Received " + len + " bytes from USB accessory");
                        if (connectionListener != null) {
                            connectionListener.onUsbData(buffer, len);
                        }
                    }
                } catch (IOException e) {
                    LogManager.e(TAG, "Error reading from accessory: " + e.getMessage());
                    isConnected = false;
                    closeAccessory();
                    if (connectionListener != null) {
                        connectionListener.onUsbError("Read error: " + e.getMessage());
                        connectionListener.onUsbDisconnected();
                    }
                    break;
                }
            }
        });
        thread.start();
    }
    
    /**
     * Send data to the USB accessory
     */
    public boolean sendData(byte[] data) {
        if (!isConnected || accessoryOutputStream == null) {
            LogManager.e(TAG, "Cannot send data - not connected");
            return false;
        }
        
        try {
            accessoryOutputStream.write(data);
            LogManager.v(TAG, "Sent " + data.length + " bytes to USB accessory");
            return true;
        } catch (IOException e) {
            LogManager.e(TAG, "Error sending data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Close the USB accessory connection
     */
    public void closeAccessory() {
        isConnected = false;
        
        try {
            if (accessoryFd != null) {
                accessoryFd.close();
            }
        } catch (IOException e) {
            LogManager.e(TAG, "Error closing accessory: " + e.getMessage());
        } finally {
            accessoryFd = null;
            accessoryInputStream = null;
            accessoryOutputStream = null;
        }
        
        LogManager.i(TAG, "USB accessory disconnected");
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        try {
            context.unregisterReceiver(usbReceiver);
        } catch (Exception e) {
            LogManager.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
        closeAccessory();
    }
    
    /**
     * Get a list of all connected USB devices for logging/debugging
     */
    public List<String> getConnectedDeviceInfo() {
        List<String> deviceInfo = new ArrayList<>();
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        
        for (UsbDevice device : deviceList.values()) {
            deviceInfo.add(String.format("Device: %s, VendorID: 0x%04X, ProductID: 0x%04X",
                    device.getDeviceName(),
                    device.getVendorId(),
                    device.getProductId()));
        }
        
        return deviceInfo;
    }
    
    /**
     * BroadcastReceiver for USB events
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogManager.d(TAG, "USB broadcast received: " + action);
            
            if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null) {
                    LogManager.i(TAG, "USB accessory attached: " + accessory.getModel());
                    if (usbManager.hasPermission(accessory)) {
                        openAccessory(accessory);
                    } else {
                        PendingIntent permissionIntent = PendingIntent.getBroadcast(
                                context, 0, new Intent(ACTION_USB_PERMISSION), 
                                PendingIntent.FLAG_IMMUTABLE);
                        usbManager.requestPermission(accessory, permissionIntent);
                    }
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null) {
                    LogManager.i(TAG, "USB accessory detached: " + accessory.getModel());
                    closeAccessory();
                    if (connectionListener != null) {
                        connectionListener.onUsbDisconnected();
                    }
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                LogManager.i(TAG, "USB permission " + (granted ? "granted" : "denied"));
                
                if (granted) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (accessory != null) {
                        openAccessory(accessory);
                    } else {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device != null) {
                            connectToDevice(device);
                        }
                    }
                } else {
                    if (connectionListener != null) {
                        connectionListener.onUsbError("USB permission denied");
                    }
                }
            }
        }
    };
}