package org.woheller69.level;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomePage";
    private final BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWifiStatus(); // Method to update WiFi status in the UI
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup click listeners for your ImageViews as before
        ImageView cardLevel = findViewById(R.id.button_level);
        cardLevel.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Level.class);
            startActivity(intent);
        });

        ImageView buttonRangeFinder = findViewById(R.id.button_range_finder);
        buttonRangeFinder.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RangeFinder.class);
            startActivity(intent);
        });

        ImageView thermometer = findViewById(R.id.button_temperature);
        thermometer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Thermometer.class);
            startActivity(intent);
        });

        ImageView messagesButton = findViewById(R.id.messageButton); // Make sure ID corresponds to your Messages button
        messagesButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MessagesActivity.class);
            startActivity(intent);
        });

        // Request fine location permission which is required for accessing WiFi SSID on newer Android versions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register receiver with necessary intent filters
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);

        updateWifiStatus(); // Also, update the status immediately when the activity resumes
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(wifiStateReceiver);
    }

    private void updateWifiStatus() {
        TextView wifiStatusTextView = findViewById(R.id.wifiStatusTextView);
        CardView wifiStatusTextViewBox = findViewById(R.id.wifiStatusTextViewBox);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            String connectedSSID = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
            //Log.d(TAG, "Result: " + connectedSSID);
            if ("Pico-W-AP".equals(connectedSSID)) {
                wifiStatusTextView.setText("Status: Connected to PhoneCase");
                wifiStatusTextViewBox.setBackgroundColor(getResources().getColor(R.color.green));
            } else {
                wifiStatusTextView.setText("Status: Not Connected to PhoneCase");
                wifiStatusTextViewBox.setBackgroundColor(getResources().getColor(R.color.red));
            }
        } else {
            wifiStatusTextView.setText("WiFi Status: WiFi is turned off");
            wifiStatusTextViewBox.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

