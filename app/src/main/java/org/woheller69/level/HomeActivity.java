package org.woheller69.level;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Intent;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements BluetoothManager.PermissionRequester{

    private static final String TAG = "HomeActivity";
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 1;
    private BluetoothManager bluetoothManager;
    private TextView dataView;

    //private static final String SPECIFIC_DEVICE_NAME = "Pico"; // replace with actual device name
    //private static final String SPECIFIC_DEVICE_ADDRESS = "28:CD:C1:10:B7:5E"; // replace with actual MAC address
    private static final String SPECIFIC_DEVICE_NAME = "TREKZ Titanium by AfterShokz"; // replace with actual device name
    private static final String SPECIFIC_DEVICE_ADDRESS = "20:74:CF:1F:52:99"; // replace with actual MAC address

    @Override
    public void requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                REQUEST_CODE_BLUETOOTH_CONNECT);
    }


    // Handler to handle incoming messages from the BluetoothManager
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == BluetoothManager.MessageConstants.MESSAGE_READ) {
                byte[] readBuf = (byte[]) msg.obj;
                // Construct a string from the valid bytes in the buffer
                String receivedText = new String(readBuf, 0, msg.arg1);
                dataView.setText(receivedText); // Update TextView with the received text
                return true;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize BluetoothManager
        bluetoothManager = new BluetoothManager(this, handler);

        if (!bluetoothManager.initialize()) {
            Log.e(TAG, "Bluetooth not supported on this device");
        } else {
            // Optionally, you can start Bluetooth discovery or perform other Bluetooth operations here
        }

        // Set the PermissionRequester for the BluetoothManager
        bluetoothManager.setPermissionRequester(this);


        // Initialize TextView and other UI elements
        dataView = findViewById(R.id.dataView); // Make sure you have a TextView with this ID in your layout


        // Attempt to connect to the specific device
        bluetoothManager.connectToSpecificDevice(SPECIFIC_DEVICE_NAME, SPECIFIC_DEVICE_ADDRESS);

        // Set an OnClickListener to define what should happen when the Button is clicked
        Button buttonLevel = findViewById(R.id.button_level);
        buttonLevel.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Level.class);
            startActivity(intent);
        });

        //RangeFinder Button
        Button buttonRangeFinder = findViewById(R.id.button_range_finder);
        buttonRangeFinder.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, RangeFinder.class);
                    startActivity(intent);
        });

        //Thermometer Button
        Button thermometer = findViewById(R.id.button_temperature);
        thermometer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Thermometer.class);
            startActivity(intent);
        });

        //Discover Devices Button
        Button discoverButton = findViewById(R.id.discoverButton);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothManager.startDiscovery();
                bluetoothManager.connectToSpecificDevice(SPECIFIC_DEVICE_NAME, SPECIFIC_DEVICE_ADDRESS);
            }
        });

        // Request necessary permissions
        requestPermissions();
    }

    private void requestPermissions() {
        // Example permission request for ACCESS_FINE_LOCATION
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothManager.stopDiscovery(); // Stop discovery and clean up when activity is destroyed
    }
}
