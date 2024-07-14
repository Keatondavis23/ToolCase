package org.ECEN499.level;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Laser extends AppCompatActivity {

    ConnectedThread connectedThread = MainActivity.connectedThread;
    private static final String TAG = "LaserLogs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser);

        // Existing code for button setup
        Button laserOnButton = findViewById(R.id.laserON);
        laserOnButton.setOnClickListener(v -> sendSignal("1"));

        Button laserOFFButton = findViewById(R.id.laserOFF);
        laserOFFButton.setOnClickListener(v -> sendSignal("0"));

        Button laserLeftButton = findViewById(R.id.laserLeft);
        laserLeftButton.setOnClickListener(v -> sendSignal("4"));

        Button laserFaceButton = findViewById(R.id.laserFace1);
        laserFaceButton.setOnClickListener(v -> sendSignal("2"));

        Button laserBottomButton = findViewById(R.id.laserBottom);
        laserBottomButton.setOnClickListener(v -> sendSignal("5"));

        Button laserTopButton = findViewById(R.id.laserTop);
        laserTopButton.setOnClickListener(v -> sendSignal("3"));

        Button laserRightButton = findViewById(R.id.laserRight);
        laserRightButton.setOnClickListener(v -> sendSignal("6"));

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {

                case R.id.nav_thermometer:
                    startActivity(new Intent(Laser.this, Thermometer.class));
                    return true;
                case R.id.nav_laser:
                    startActivity(new Intent(Laser.this, Laser.class));
                    return true;
                case R.id.nav_range_finder:
                    startActivity(new Intent(Laser.this, RangeFinder.class));
                    return true;
                case R.id.nav_menu:
                    startActivity(new Intent(Laser.this, HomeActivity.class));
                    return true;
            }
            return false;
        });
    }


    private void sendSignal(String signal) {
        try {
            byte[] messageBytes = signal.getBytes();
            if (connectedThread != null) {
                connectedThread.write(messageBytes);
            } else {
                Log.e(TAG, "connectedThread is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred when trying to send data", e);
        }
    }
}



