package org.ECEN499.level;

import static org.ECEN499.level.MainActivity.connectedThread;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Thermometer extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private static final String TAG = "Thermometer";
    private Handler handler;
    private TextView tempData;
    private static final int ERROR_READ = 0; // Used to identify message updates in Bluetooth handler
    private String currentUnit = "Fahrenheit"; // Default temperature unit
    private double Jsontemp = 0.0; // Initialize with a default value

    // Runnable to fetch data from Arduino
    private final Runnable fetchDataRunnable = new Runnable() {
        @Override
        public void run() {
            TextView temperatureJSONData = findViewById(R.id.temperatureJSONData);

            // Handler to read messages from Arduino
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                }
            };

            // Handler and Runnable to update temperature data every second
            Handler tempUpdateHandler = new Handler(Looper.getMainLooper());
            Runnable tempUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    if (connectedThread != null && connectedThread.isConnected()) {
                        String temp = DataHolder.getInstance().getTemp();
                        double tempValue = Double.parseDouble(temp); // Convert temp string to double
                        String formattedTemp = formatTemperature(tempValue); // Format temp with correct unit
                        runOnUiThread(() -> temperatureJSONData.setText(formattedTemp)); // Update TextView on main thread
                        tempUpdateHandler.postDelayed(this, 1000); // Update temp every second
                    }
                }
            };

            // Start the repeating temperature update task
            tempUpdateHandler.post(tempUpdateRunnable);

            // Fetch data from Arduino and update UI
            executorService.execute(() -> {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://192.168.4.1/data") // Replace with your Arduino endpoint
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    assert response.body() != null;
                    final String result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);
                    double temperature = jsonObject.getDouble("temperature");
                    String formattedTemperature = formatTemperature(temperature);

                    // Update Jsontemp with the received temperature
                    Jsontemp = temperature;

                    mainThreadHandler.post(() -> tempData.setText(formattedTemperature));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            });

            handler.postDelayed(this, 3000); // Fetch data every 3 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermometer);

        tempData = findViewById(R.id.temperatureJSONData);
        handler = new Handler();

        handler.post(fetchDataRunnable); // Start fetching data

        Button celsiusButton = findViewById(R.id.celsiusButton);
        Button fahrenheitButton = findViewById(R.id.fahrenheitButton);

        celsiusButton.setOnClickListener(v -> setCurrentUnit("Celsius"));
        fahrenheitButton.setOnClickListener(v -> setCurrentUnit("Fahrenheit"));

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_laser:
                    startActivity(new Intent(Thermometer.this, Laser.class));
                    return true;
                case R.id.nav_thermometer:
                    startActivity(new Intent(Thermometer.this, Thermometer.class));
                    return true;
                case R.id.nav_range_finder:
                    startActivity(new Intent(Thermometer.this, RangeFinder.class));
                    return true;
                case R.id.nav_menu:
                    startActivity(new Intent(Thermometer.this, HomeActivity.class));
                    return true;
            }
            return false;
        });
    }

    @SuppressLint("SetTextI18n")
    private void setCurrentUnit(@NonNull String unit) {
        currentUnit = unit;
        fetchDataRunnable.run(); // Re-run fetchDataRunnable to update temperature with new unit
    }

    private String formatTemperature(double temperature) {
        switch (currentUnit) {
            case "Celsius":
                return "Temperature: " + String.format("%.2f", fahrenheitToCelsius(temperature)) + " °C";
            case "Fahrenheit":
                return "Temperature: " + String.format("%.2f", temperature) + " °F";
            default:
                return "Unknown temperature unit";
        }
    }

    private double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }
}
