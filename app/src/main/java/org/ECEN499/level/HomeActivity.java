package org.ECEN499.level;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomePage";
    private final BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBluetoothStatus(); // Method to update WiFi status in the UI
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the title bar
        setContentView(R.layout.activity_home);

        updateBluetoothStatus();

        // Setup click listeners for your ImageViews as before
        ImageView cardLevel = findViewById(R.id.button_level);
        cardLevel.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Level.class);
            startActivity(intent);
        });

        ImageView btconnect = findViewById(R.id.btconnect);
        btconnect.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageView buttonRangeFinder = findViewById(R.id.button_range_finder);
        buttonRangeFinder.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RangeFinder.class);
            startActivity(intent);
        });

        ImageView laserbutton = findViewById(R.id.laserbutton);
        laserbutton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, Laser.class);
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

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_laser:
                        startActivity(new Intent(HomeActivity.this, Laser.class));
                        return true;
                    case R.id.nav_thermometer:
                        startActivity(new Intent(HomeActivity.this, Thermometer.class));
                        return true;
                    case R.id.nav_range_finder:
                        startActivity(new Intent(HomeActivity.this, RangeFinder.class));
                        return true;
                    case R.id.nav_menu:
                        startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                        return true;
                }
                return false;
            }
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

        updateBluetoothStatus(); // Also, update the status immediately when the activity resumes
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(wifiStateReceiver);
    }

    private void updateBluetoothStatus() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
