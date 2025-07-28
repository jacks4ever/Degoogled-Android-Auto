package com.degoogled.androidauto.protocol;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.content.Context;

import com.degoogled.androidauto.logging.ConnectionLogger;
import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageFactory;
import com.degoogled.androidauto.utils.LogManager;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles USB accessory communication for Android Auto.
 * This class manages the low-level USB connection and provides methods for sending and receiving messages.
 */
public class UsbConnection {
    private static final String TAG = "UsbConnection";
    private static final int BUFFER_SIZE = 16384;
    
    private final Context context;
    private final UsbManager usbManager;
    private final ConnectionLogger logger;
    private final BlockingQueue<Message> messageQueue;
    
    private UsbAccessory accessory;
    private ParcelFileDescriptor fileDescriptor;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    
    private ReadThread readThread;
    private WriteThread writeThread;
    
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    private UsbConnectionListener listener;
    
    /**
     * Create a new USB connection
     */
    public UsbConnection(Context context, UsbManager usbManager, ConnectionLogger logger) {
        this.context = context;
        this.usbManager = usbManager;
        this.logger = logger;
        this.messageQueue = new LinkedBlockingQueue<>();
    }
    
    /**
     * Set the connection listener
     */
    public void setListener(UsbConnectionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Connect to the USB accessory
     */
    public boolean connect(UsbAccessory accessory) {
        if (isConnected.get()) {
            LogManager.w(TAG, "Already connected to an accessory");
            return false;
        }
        
        this.accessory = accessory;
        
        try {
            fileDescriptor = usbManager.openAccessory(accessory);
            if (fileDescriptor == null) {
                LogManager.e(TAG, "Failed to open accessory");
                return false;
            }
            
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            inputStream = new FileInputStream(fd);
            outputStream = new FileOutputStream(fd);
            
            isConnected.set(true);
            isRunning.set(true);
            
            readThread = new ReadThread();
            writeThread = new WriteThread();
            
            readThread.start();
            writeThread.start();
            
            LogManager.i(TAG, "Connected to accessory: " + accessory.getModel());
            logger.logConnection("Connected to " + accessory.getManufacturer() + " " + accessory.getModel());
            
            if (listener != null) {
                listener.onConnected(accessory);
            }
            
            return true;
        } catch (IOException e) {
            LogManager.e(TAG, "Error connecting to accessory", e);
            logger.logError("Connection error: " + e.getMessage());
            closeConnection();
            return false;
        }
    }
    
    /**
     * Disconnect from the USB accessory
     */
    public void disconnect() {
        if (!isConnected.get()) {
            return;
        }
        
        LogManager.i(TAG, "Disconnecting from accessory");
        logger.logConnection("Disconnecting from accessory");
        
        isRunning.set(false);
        
        if (readThread != null) {
            readThread.interrupt();
        }
        
        if (writeThread != null) {
            writeThread.interrupt();
        }
        
        closeConnection();
        
        if (listener != null) {
            listener.onDisconnected();
        }
    }
    
    /**
     * Close the connection resources
     */
    private void closeConnection() {
        isConnected.set(false);
        
        try {
            if (fileDescriptor != null) {
                fileDescriptor.close();
            }
        } catch (IOException e) {
            LogManager.e(TAG, "Error closing file descriptor", e);
        }
        
        fileDescriptor = null;
        inputStream = null;
        outputStream = null;
        accessory = null;
    }
    
    /**
     * Send a message to the USB accessory
     */
    public boolean sendMessage(Message message) {
        if (!isConnected.get()) {
            LogManager.w(TAG, "Not connected to an accessory");
            return false;
        }
        
        return messageQueue.offer(message);
    }
    
    /**
     * Check if connected to an accessory
     */
    public boolean isConnected() {
        return isConnected.get();
    }
    
    /**
     * Get the connected accessory
     */
    public UsbAccessory getAccessory() {
        return accessory;
    }
    
    /**
     * Thread for reading data from the USB accessory
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[BUFFER_SIZE];
            
            while (isRunning.get()) {
                try {
                    int bytesRead = inputStream.read(buffer);
                    
                    if (bytesRead > 0) {
                        byte[] data = Arrays.copyOf(buffer, bytesRead);
                        logger.logRawData("Received", data);
                        
                        try {
                            Message message = Message.deserialize(data);
                            LogManager.d(TAG, "Received message: " + message);
                            logger.logMessage("Received", message);
                            
                            if (listener != null) {
                                listener.onMessageReceived(message);
                            }
                        } catch (Exception e) {
                            LogManager.e(TAG, "Error parsing message", e);
                            logger.logError("Parse error: " + e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    if (isRunning.get()) {
                        LogManager.e(TAG, "Error reading from accessory", e);
                        logger.logError("Read error: " + e.getMessage());
                        
                        if (listener != null) {
                            listener.onError(e);
                        }
                        
                        disconnect();
                    }
                    break;
                }
            }
            
            LogManager.d(TAG, "Read thread exiting");
        }
    }
    
    /**
     * Thread for writing data to the USB accessory
     */
    private class WriteThread extends Thread {
        @Override
        public void run() {
            while (isRunning.get()) {
                try {
                    Message message = messageQueue.take();
                    
                    if (outputStream != null) {
                        byte[] data = message.serialize();
                        LogManager.d(TAG, "Sending message: " + message);
                        logger.logMessage("Sending", message);
                        logger.logRawData("Sent", data);
                        
                        outputStream.write(data);
                        outputStream.flush();
                    }
                } catch (InterruptedException e) {
                    if (isRunning.get()) {
                        LogManager.w(TAG, "Write thread interrupted", e);
                    }
                    break;
                } catch (IOException e) {
                    if (isRunning.get()) {
                        LogManager.e(TAG, "Error writing to accessory", e);
                        logger.logError("Write error: " + e.getMessage());
                        
                        if (listener != null) {
                            listener.onError(e);
                        }
                        
                        disconnect();
                    }
                    break;
                }
            }
            
            LogManager.d(TAG, "Write thread exiting");
        }
    }
    
    /**
     * Interface for USB connection events
     */
    public interface UsbConnectionListener {
        void onConnected(UsbAccessory accessory);
        void onDisconnected();
        void onMessageReceived(Message message);
        void onError(Exception e);
    }
}