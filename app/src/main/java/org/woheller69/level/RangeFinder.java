package org.woheller69.level;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RangeFinder extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(getMainLooper());

    private static final String TAG = "RangeFinder";
    private Handler handler;

    private TextView rangeData;

    private double width = 0.0;
    private double length = 0.0;

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
                    String result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);
                    double range = jsonObject.getDouble("range");
                    String formattedRange = "Range: " + range;

                    mainThreadHandler.post(() -> {
                        rangeData.setText(formattedRange);
                    });
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Exception while making HTTP request", e);
                }
            });

            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_range_finder);

        rangeData = findViewById(R.id.rangeFinderJSONData);
        Button widthButton = findViewById(R.id.widthButton);
        Button lengthButton = findViewById(R.id.lengthButton);
        Button calcAreaButton = findViewById(R.id.calculateButton);

        widthButton.setOnClickListener(v -> storeWidth());
        lengthButton.setOnClickListener(v -> storeLength());
        calcAreaButton.setOnClickListener(v -> calculateArea());

        handler = new Handler(message -> {
            if (message.what == MessageConstants.MESSAGE_READ) {
                byte[] readBuf = (byte[]) message.obj;
                String receivedData = new String(readBuf, 0, message.arg1);
                updateUI(receivedData);
            }
            return true;
        });

        handler.post(fetchDataRunnable);
    }

    @SuppressLint("SetTextI18n")
    private void storeWidth() {
        width = Double.parseDouble(rangeData.getText().toString().split(":")[1].trim());
        TextView widthTextView = findViewById(R.id.width);
        widthTextView.setText("Width: " + width);
    }

    @SuppressLint("SetTextI18n")
    private void storeLength() {
        length = Double.parseDouble(rangeData.getText().toString().split(":")[1].trim());
        TextView lengthTextView = findViewById(R.id.length);
        lengthTextView.setText("Length: " + length);
    }

    @SuppressLint("SetTextI18n")
    private void calculateArea() {
        double area = width * length;
        TextView areaTextView = findViewById(R.id.area);
        areaTextView.setText("Area: " + area);
    }

    private void updateUI(String data) {
        // Update UI elements with received data
    }

    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }
}

