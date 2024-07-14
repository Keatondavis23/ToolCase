package org.ECEN499.level;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static ConnectedThread connectedThread; // Make this public static

    // Global variables
    private static final String TAG = "FrugalLogs";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MESSAGE_READ = 1;

    private static final int ERROR_READ = 0;
    private BluetoothDevice arduinoBTModule = null;
    private UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Default UUID

    // UI elements
    private TextView btReadings;
    private TextView btDevices;

    // Handler to update UI from Bluetooth threads
    public static Handler handler;

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Existing code for MainActivity setup

        // Initialize UI elements
        btReadings = findViewById(R.id.btReadings);
        btDevices = findViewById(R.id.btDevices);

        // Bluetooth initialization
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Button setup
        Button connectToDevice = findViewById(R.id.connectToDevice);
        Button seachDevices = findViewById(R.id.seachDevices);
        Button clearValues = findViewById(R.id.refresh);
        Button dashboard = findViewById(R.id.dashboard);

        // Handle dashboard button click
        dashboard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Handler to update UI with Bluetooth data
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        btReadings.setText(arduinoMsg);

                        // Update btDevices TextView
                        btDevices.setText(arduinoMsg);
                        break;
                }
            }
        };

        // Button to clear text values
        clearValues.setOnClickListener(v -> {
            btDevices.setText("");
            btReadings.setText("");
            if (connectedThread != null) {
                connectedThread.cancel();
            }
        });

        // Observable to connect to Bluetooth and read data
        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run();

            if (connectThread.getMmSocket().isConnected()) {
                // Initialize the ConnectedThread instance here
                connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();
                if (connectedThread.getValueRead() != null) {
                    emitter.onNext(connectedThread.getValueRead());
                }
            }
            emitter.onComplete();
        });

        // Button to connect to selected BT device and read data
        connectToDevice.setOnClickListener(v -> {
            btReadings.setText("");
            connectToDevice.setEnabled(false); // Disable button

            if (arduinoBTModule != null) {
                connectToBTObservable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(valueRead -> {
                            String distance = extractDistance(valueRead);
                            String temp = extractTemp(valueRead);

                            DataHolder.getInstance().setDistance(distance);
                            DataHolder.getInstance().setTemp(temp);

                            btReadings.setText("Distance: " + distance + ", Temp: " + temp);

                            // Re-enable button after 1 second
                            new Handler().postDelayed(() -> connectToDevice.setEnabled(true), 1000);
                        });
            }
        });

        // Button to search for paired BT devices
        seachDevices.setOnClickListener(v -> {
            connectToDevice.setEnabled(false); // Disable the connect button

            if (bluetoothAdapter == null) {
                Log.d(TAG, "Device doesn't support Bluetooth");
            } else {
                Log.d(TAG, "Device supports Bluetooth");

                if (!bluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Bluetooth is disabled");
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "We don't have BT Permissions");
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        Log.d(TAG, "We have BT Permissions");
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }

                } else {
                    Log.d(TAG, "Bluetooth is enabled");
                }

                StringBuilder btDevicesString = new StringBuilder();
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress();
                        btDevicesString.append(deviceName).append(" || ").append(deviceHardwareAddress).append("\n");

                        if (deviceName.equals("DSD TECH HC-05")) {
                            Log.d(TAG, "HC-05 found");
                            arduinoUUID = device.getUuids()[0].getUuid();
                            arduinoBTModule = device;
                            connectToDevice.setEnabled(true);
                        }
                        btDevices.setText(btDevicesString.toString());
                    }
                }
            }

            // Re-enable the connect button after 1 second
            new Handler().postDelayed(() -> connectToDevice.setEnabled(true), 1000);
        });

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_laser:
                    startActivity(new Intent(MainActivity.this, Laser.class));
                    return true;
                case R.id.nav_thermometer:
                    startActivity(new Intent(MainActivity.this, Thermometer.class));
                    return true;
                case R.id.nav_range_finder:
                    startActivity(new Intent(MainActivity.this, RangeFinder.class));
                    return true;
                case R.id.nav_menu:
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    return true;
            }
            return false;
        });
    }


    // Method to extract distance from Arduino data string
    private String extractDistance(String data) {
        String distance = "";

        // Split the data based on the separator ": "
        String[] parts = data.split(":");

        // Loop through the parts to find the distance part
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("Distance in feet")) {
                distance = parts[i + 1];
                break;
            }
        }
        return distance;
    }

    private String extractTemp(String data) {
        String temp = "";

        // Split the data based on the separator ": "
        String[] parts = data.split(":");

        // Loop through the parts to find the temperature part
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("Distance in feet")) {
                temp = parts[i + 3];
                break;
            }
        }

        return temp;
    }
}
