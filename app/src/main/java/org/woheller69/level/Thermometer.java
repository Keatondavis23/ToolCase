package org.woheller69.level;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    private String currentUnit = "Celsius"; // Default temperature unit

    private final Runnable fetchDataRunnable = new Runnable() {
        @Override
        public void run() {
            executorService.execute(() -> {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://192.168.4.1/data")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    assert response.body() != null;
                    final String result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);
                    double temperature = jsonObject.getDouble("temperature");
                    String formattedTemperature = formatTemperature(temperature);

                    mainThreadHandler.post(() -> {
                        tempData.setText(formattedTemperature);
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            });
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermometer);

        tempData = findViewById(R.id.temperatureJSONData);
        handler = new Handler(message -> {
            if (message.what == MessageConstants.MESSAGE_READ) {
                byte[] readBuf = (byte[]) message.obj;
                String receivedData = new String(readBuf, 0, message.arg1);
                updateUI(receivedData);
            }
            return true;
        });

        handler.post(fetchDataRunnable);

        Button celsiusButton = findViewById(R.id.celsiusButton);
        Button fahrenheitButton = findViewById(R.id.fahrenheitButton);
        Button kelvinButton = findViewById(R.id.kelvinButton);

        celsiusButton.setOnClickListener(v -> setCurrentUnit("Celsius"));
        fahrenheitButton.setOnClickListener(v -> setCurrentUnit("Fahrenheit"));
        kelvinButton.setOnClickListener(v -> setCurrentUnit("Kelvin"));
    }

    @SuppressLint("SetTextI18n")
    private void setCurrentUnit(@NonNull String unit) {
        currentUnit = unit;
        fetchDataRunnable.run(); // Re-run fetchDataRunnable to update temperature with new unit
    }

    private String formatTemperature(double temperature) {
        switch (currentUnit) {
            case "Celsius":
                return "Temperature: " + temperature + " °C";
            case "Fahrenheit":
                return "Temperature: " + celsiusToFahrenheit(temperature) + " °F";
            case "Kelvin":
                return "Temperature: " + celsiusToKelvin(temperature) + " K";
            default:
                return "Unknown temperature unit";
        }
    }

    private void updateUI(String data) {
        // Update your UI elements here with the received data
        // Example: textView.setText(data);
    }

    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    private double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }
}
