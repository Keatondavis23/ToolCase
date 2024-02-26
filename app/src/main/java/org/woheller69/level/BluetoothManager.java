package org.woheller69.level;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import androidx.core.content.ContextCompat;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private BluetoothDeviceReceiver deviceReceiver;
    private PermissionRequester permissionRequester;

    private DeviceDiscoveryListener discoveryListener;

    // Define the UUID for SPP (Serial Port Profile)
    private static final UUID SPP_UUID = UUID.fromString("00002A00-0000-1000-8000-00805F9B34FB");
    private Handler handler; // Handler to manage UI updates or data processing
    private PackageManager.OnChecksumsReadyListener deviceDiscoveryListener;


    public interface PermissionRequester {
        void requestBluetoothPermissions();
    }

    public interface DeviceDiscoveryListener {
        void onDeviceDiscovered(BluetoothDevice device);
    }

    public void setDeviceDiscoveryListener(DeviceDiscoveryListener listener) {
        this.discoveryListener = listener;
    }

    public void setPermissionRequester(BluetoothManager.PermissionRequester permissionRequester) {
        this.permissionRequester = permissionRequester;
    }


    public BluetoothManager(Context context, Handler handler) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
    }

    public boolean initialize() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return false;
        }
        return true;
    }

    public void startDiscovery() {
        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            // You may want to prompt the user to enable Bluetooth here.
            return;
        }
        // Check for the location permission before starting discovery
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: You need to request permissions from the activity.
            // You can implement a callback interface to inform the activity that permission is needed.
            Log.e(TAG, "Location permission not granted");
            return;
        }

        // If permissions are granted, start Bluetooth discovery

        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        deviceReceiver = new BluetoothDeviceReceiver();
        context.registerReceiver(deviceReceiver, filter);
    }


    public void stopDiscovery() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, handle accordingly
            Log.e(TAG, "Bluetooth scan permission not granted");
            return;
        }

        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        if (deviceReceiver != null) {
            context.unregisterReceiver(deviceReceiver);
        }
    }


    // Method to connect to a Bluetooth device
    public void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            BluetoothSocket socket = null;
            try {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: You need to request permissions from the activity.
                    // You can implement a callback interface to inform the activity that permission is needed.
                    Log.e(TAG, "Location permission not granted in connection");
                    return;
                }
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                // Connection established, manage the socket
                Log.d(TAG, "Socket connected");
                manageConnectedSocket(socket);
            } catch (IOException e) {
                Log.e(TAG, "Unable to connect to device", e);
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Could not close the client socket", ex);
                }
            }
        }).start();
    }

    public void connectToSpecificDevice(String deviceName, String deviceAddress) {
        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (permissionRequester != null) {
                permissionRequester.requestBluetoothPermissions();
            } else {
                Log.e(TAG, "PermissionRequester is null when trying to connect to Pico");
            }
        }

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // Try to find the specific device in the list of paired devices
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (deviceName.equals(device.getName()) || deviceAddress.equals(device.getAddress())) {
                    Log.d(TAG, "Found device and trying to connect");
                    connectToDevice(device);
                    return;
                }
            }
        }

        // If the device is not found in paired devices, start discovery
        //bluetoothAdapter.startDiscovery();
        //deviceDiscoveryListener = device -> {
            //if (deviceName.equals(device.getName()) && deviceAddress.equals(device.getAddress())) {
                //bluetoothAdapter.cancelDiscovery();
                //connectToDevice(device);
           // }
        //};
    }


    // Manage the BluetoothSocket connection
    private void manageConnectedSocket(BluetoothSocket socket) {
        ConnectedThread connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public interface PermissionRequestListener {
        void onRequestBluetoothConnectPermission();
    }
    private PermissionRequestListener permissionRequestListener;
    // Method to set the listener
    public void setPermissionRequestListener(PermissionRequestListener listener) {
        this.permissionRequestListener = listener;
    }

    private class BluetoothDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (permissionRequester != null) {
                        permissionRequester.requestBluetoothPermissions();
                    } else {
                        Log.e(TAG, "PermissionRequester is null");
                    }
                }
                Log.d(TAG, "On Receive permission granted" );
                Log.d(TAG, "Found device: " + device.getName());
                Log.d(TAG, "Found device: " + device.getAddress());
                // Optionally, automatically connect to the device here
                if (discoveryListener != null) {
                    discoveryListener.onDeviceDiscovered(device);
                }
            }
        }
    }

    // Thread for managing connected BluetoothSocket
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }

            inStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inStream.read(buffer);
                    // Send the obtained bytes to the main thread
                    handler.obtainMessage(MessageConstants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this method to shut down the connection
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    interface MessageConstants {
        int MESSAGE_READ = 0;
    }
}
