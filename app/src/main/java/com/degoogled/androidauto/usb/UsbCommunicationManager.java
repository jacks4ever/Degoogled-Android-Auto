package com.degoogled.androidauto.usb;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.degoogled.androidauto.logging.ConnectionLogger;
import com.degoogled.androidauto.protocol.AndroidAutoProtocol;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * USB Communication Manager for Android Auto Protocol
 * Handles low-level USB communication with head unit
 */
public class UsbCommunicationManager {
    private static final String TAG = "UsbCommunicationManager";
    
    // USB transfer constants
    private static final int USB_TIMEOUT_MS = 5000;
    private static final int MAX_PACKET_SIZE = 16384; // 16KB
    private static final int BUFFER_SIZE = 65536; // 64KB
    
    // AOAP constants
    private static final int AOAP_VENDOR_ID = 0x18D1;
    private static final int AOAP_PRODUCT_ID_MIN = 0x2D00;
    private static final int AOAP_PRODUCT_ID_MAX = 0x2D05;
    
    private Context context;
    private ConnectionLogger logger;
    private UsbManager usbManager;
    private AndroidAutoProtocol protocol;
    
    // USB connection state
    private UsbDevice currentDevice;
    private UsbAccessory currentAccessory;
    private UsbDeviceConnection deviceConnection;
    private ParcelFileDescriptor accessoryFileDescriptor;
    
    // USB endpoints for device mode
    private UsbInterface usbInterface;
    private UsbEndpoint endpointIn;
    private UsbEndpoint endpointOut;
    
    // File streams for accessory mode
    private FileInputStream accessoryInputStream;
    private FileOutputStream accessoryOutputStream;
    
    // Communication threads
    private Thread readerThread;
    private Thread writerThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    // Message queues
    private final BlockingQueue<byte[]> outgoingMessages = new LinkedBlockingQueue<>();
    private final BlockingQueue<byte[]> incomingMessages = new LinkedBlockingQueue<>();
    
    // Listeners
    private UsbCommunicationListener listener;
    
    public interface UsbCommunicationListener {
        void onConnected();
        void onDisconnected();
        void onMessageReceived(byte[] message);
        void onError(String error);
    }
    
    public UsbCommunicationManager(Context context, ConnectionLogger logger) {
        this.context = context;
        this.logger = logger;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.protocol = new AndroidAutoProtocol(logger);
    }
    
    public void setListener(UsbCommunicationListener listener) {
        this.listener = listener;
    }
    
    /**
     * Connect to USB device (host mode)
     */
    public boolean connectToDevice(UsbDevice device) {
        if (device == null) {
            logger.logError("Cannot connect to null device");
            return false;
        }
        
        logger.logInfo("Attempting to connect to USB device: " + device.getDeviceName());
        
        // Check if device is AOAP compatible
        if (!isAOAPDevice(device)) {
            logger.logWarning("Device is not AOAP compatible");
            return false;
        }
        
        // Request permission if needed
        if (!usbManager.hasPermission(device)) {
            logger.logError("No permission for USB device");
            return false;
        }
        
        // Open connection
        deviceConnection = usbManager.openDevice(device);
        if (deviceConnection == null) {
            logger.logError("Failed to open USB device connection");
            return false;
        }
        
        // Find and claim interface
        if (!claimInterface(device)) {
            deviceConnection.close();
            deviceConnection = null;
            return false;
        }
        
        currentDevice = device;
        startCommunication();
        
        logger.logInfo("Successfully connected to USB device");
        if (listener != null) {
            listener.onConnected();
        }
        
        return true;
    }
    
    /**
     * Connect to USB accessory (accessory mode)
     */
    public boolean connectToAccessory(UsbAccessory accessory) {
        if (accessory == null) {
            logger.logError("Cannot connect to null accessory");
            return false;
        }
        
        logger.logInfo("Attempting to connect to USB accessory: " + accessory.getModel());
        
        // Request permission if needed
        if (!usbManager.hasPermission(accessory)) {
            logger.logError("No permission for USB accessory");
            return false;
        }
        
        // Open accessory
        accessoryFileDescriptor = usbManager.openAccessory(accessory);
        if (accessoryFileDescriptor == null) {
            logger.logError("Failed to open USB accessory");
            return false;
        }
        
        // Create file streams
        FileDescriptor fd = accessoryFileDescriptor.getFileDescriptor();
        accessoryInputStream = new FileInputStream(fd);
        accessoryOutputStream = new FileOutputStream(fd);
        
        currentAccessory = accessory;
        startCommunication();
        
        logger.logInfo("Successfully connected to USB accessory");
        if (listener != null) {
            listener.onConnected();
        }
        
        return true;
    }
    
    /**
     * Check if device is AOAP compatible
     */
    private boolean isAOAPDevice(UsbDevice device) {
        int vendorId = device.getVendorId();
        int productId = device.getProductId();
        
        return vendorId == AOAP_VENDOR_ID && 
               productId >= AOAP_PRODUCT_ID_MIN && 
               productId <= AOAP_PRODUCT_ID_MAX;
    }
    
    /**
     * Claim USB interface and find endpoints
     */
    private boolean claimInterface(UsbDevice device) {
        // Find the first interface (usually the only one for AOAP)
        if (device.getInterfaceCount() == 0) {
            logger.logError("Device has no interfaces");
            return false;
        }
        
        usbInterface = device.getInterface(0);
        
        // Claim the interface
        if (!deviceConnection.claimInterface(usbInterface, true)) {
            logger.logError("Failed to claim USB interface");
            return false;
        }
        
        // Find bulk endpoints
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            
            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    endpointIn = endpoint;
                    logger.logInfo("Found bulk IN endpoint");
                } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpointOut = endpoint;
                    logger.logInfo("Found bulk OUT endpoint");
                }
            }
        }
        
        if (endpointIn == null || endpointOut == null) {
            logger.logError("Could not find required bulk endpoints");
            deviceConnection.releaseInterface(usbInterface);
            return false;
        }
        
        return true;
    }
    
    /**
     * Start communication threads
     */
    private void startCommunication() {
        isRunning.set(true);
        
        // Start reader thread
        readerThread = new Thread(this::readerLoop, "USB-Reader");
        readerThread.start();
        
        // Start writer thread
        writerThread = new Thread(this::writerLoop, "USB-Writer");
        writerThread.start();
        
        // Start protocol handshake
        initiateHandshake();
    }
    
    /**
     * Reader thread loop
     */
    private void readerLoop() {
        byte[] buffer = new byte[BUFFER_SIZE];
        
        while (isRunning.get()) {
            try {
                int bytesRead = readData(buffer);
                if (bytesRead > 0) {
                    byte[] message = new byte[bytesRead];
                    System.arraycopy(buffer, 0, message, 0, bytesRead);
                    
                    // Process the message
                    processIncomingMessage(message);
                }
            } catch (IOException e) {
                if (isRunning.get()) {
                    logger.logError("USB read error: " + e.getMessage());
                    handleConnectionError();
                }
                break;
            }
        }
        
        logger.logInfo("USB reader thread stopped");
    }
    
    /**
     * Writer thread loop
     */
    private void writerLoop() {
        while (isRunning.get()) {
            try {
                byte[] message = outgoingMessages.take(); // Blocking call
                writeData(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                if (isRunning.get()) {
                    logger.logError("USB write error: " + e.getMessage());
                    handleConnectionError();
                }
                break;
            }
        }
        
        logger.logInfo("USB writer thread stopped");
    }
    
    /**
     * Read data from USB connection
     */
    private int readData(byte[] buffer) throws IOException {
        if (currentDevice != null) {
            // Device mode - use bulk transfer
            return deviceConnection.bulkTransfer(endpointIn, buffer, buffer.length, USB_TIMEOUT_MS);
        } else if (currentAccessory != null) {
            // Accessory mode - use file stream
            return accessoryInputStream.read(buffer);
        }
        
        throw new IOException("No active USB connection");
    }
    
    /**
     * Write data to USB connection
     */
    private void writeData(byte[] data) throws IOException {
        if (currentDevice != null) {
            // Device mode - use bulk transfer
            int result = deviceConnection.bulkTransfer(endpointOut, data, data.length, USB_TIMEOUT_MS);
            if (result < 0) {
                throw new IOException("Bulk transfer failed: " + result);
            }
        } else if (currentAccessory != null) {
            // Accessory mode - use file stream
            accessoryOutputStream.write(data);
            accessoryOutputStream.flush();
        } else {
            throw new IOException("No active USB connection");
        }
    }
    
    /**
     * Process incoming message
     */
    private void processIncomingMessage(byte[] data) {
        try {
            // Parse protocol message
            AndroidAutoProtocol.ProtocolMessage message = protocol.parseMessage(data);
            if (message != null) {
                // Handle the message
                AndroidAutoProtocol.ProtocolResponse response = protocol.handleMessage(message);
                
                if (response.isSuccess() && response.getResponseMessage() != null) {
                    // Send response
                    sendMessage(response.getResponseMessage());
                }
                
                // Notify listener
                if (listener != null) {
                    listener.onMessageReceived(data);
                }
            }
        } catch (Exception e) {
            logger.logError("Error processing incoming message: " + e.getMessage());
        }
    }
    
    /**
     * Send message through USB
     */
    public void sendMessage(byte[] message) {
        if (!isRunning.get()) {
            logger.logError("Cannot send message: USB communication not running");
            return;
        }
        
        try {
            outgoingMessages.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.logError("Interrupted while queuing message");
        }
    }
    
    /**
     * Initiate protocol handshake
     */
    private void initiateHandshake() {
        logger.logInfo("Initiating Android Auto protocol handshake");
        
        // Send version request
        byte[] versionRequest = protocol.createVersionRequest();
        sendMessage(versionRequest);
        
        // Send service discovery request
        byte[] serviceDiscovery = protocol.createServiceDiscoveryRequest();
        sendMessage(serviceDiscovery);
    }
    
    /**
     * Handle connection error
     */
    private void handleConnectionError() {
        logger.logError("USB connection error occurred");
        disconnect();
        
        if (listener != null) {
            listener.onError("USB connection lost");
        }
    }
    
    /**
     * Disconnect from USB
     */
    public void disconnect() {
        logger.logInfo("Disconnecting USB communication");
        
        isRunning.set(false);
        
        // Interrupt threads
        if (readerThread != null) {
            readerThread.interrupt();
        }
        if (writerThread != null) {
            writerThread.interrupt();
        }
        
        // Close device connection
        if (deviceConnection != null) {
            if (usbInterface != null) {
                deviceConnection.releaseInterface(usbInterface);
            }
            deviceConnection.close();
            deviceConnection = null;
        }
        
        // Close accessory connection
        if (accessoryInputStream != null) {
            try {
                accessoryInputStream.close();
            } catch (IOException e) {
                logger.logError("Error closing accessory input stream: " + e.getMessage());
            }
            accessoryInputStream = null;
        }
        
        if (accessoryOutputStream != null) {
            try {
                accessoryOutputStream.close();
            } catch (IOException e) {
                logger.logError("Error closing accessory output stream: " + e.getMessage());
            }
            accessoryOutputStream = null;
        }
        
        if (accessoryFileDescriptor != null) {
            try {
                accessoryFileDescriptor.close();
            } catch (IOException e) {
                logger.logError("Error closing accessory file descriptor: " + e.getMessage());
            }
            accessoryFileDescriptor = null;
        }
        
        // Clear state
        currentDevice = null;
        currentAccessory = null;
        usbInterface = null;
        endpointIn = null;
        endpointOut = null;
        
        // Clear queues
        outgoingMessages.clear();
        incomingMessages.clear();
        
        if (listener != null) {
            listener.onDisconnected();
        }
        
        logger.logInfo("USB communication disconnected");
    }
    
    /**
     * Send ping message
     */
    public void sendPing() {
        long timestamp = System.currentTimeMillis();
        byte[] ping = protocol.createPing(timestamp);
        sendMessage(ping);
    }
    
    /**
     * Request navigation focus
     */
    public void requestNavigationFocus() {
        byte[] navFocusRequest = protocol.createNavFocusRequest();
        sendMessage(navFocusRequest);
    }
    
    /**
     * Request audio focus
     */
    public void requestAudioFocus(int focusType) {
        byte[] audioFocusRequest = protocol.createAudioFocusRequest(focusType);
        sendMessage(audioFocusRequest);
    }
    
    /**
     * Request video focus
     */
    public void requestVideoFocus() {
        byte[] videoFocusRequest = protocol.createVideoFocusRequest();
        sendMessage(videoFocusRequest);
    }
    
    // Getters
    public boolean isConnected() {
        return isRunning.get() && (currentDevice != null || currentAccessory != null);
    }
    
    public UsbDevice getCurrentDevice() {
        return currentDevice;
    }
    
    public UsbAccessory getCurrentAccessory() {
        return currentAccessory;
    }
    
    public AndroidAutoProtocol getProtocol() {
        return protocol;
    }
}