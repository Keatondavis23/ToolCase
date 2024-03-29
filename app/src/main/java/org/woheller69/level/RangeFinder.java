package org.woheller69.level;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
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
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private TextView areaTextView;
    private static final String TAG = "RangeFinder";
    private Handler handler;

    private double area = 0.0;
    private String selectedUnit = "";
    private TextView rangeData;

    private double width = 0.0;
    private double length = 0.0;

    private double height = 0.0;

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

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_range_finder);

        rangeData = findViewById(R.id.rangeFinderJSONData);
        Button widthButton = findViewById(R.id.widthButton);
        Button lengthButton = findViewById(R.id.lengthButton);
        Button heightButton = findViewById(R.id.heightButton);
        areaTextView = findViewById(R.id.areaTextView);
        Button calcSqFeet = findViewById(R.id.calculateSquareFeet);
        Button calcCubicFeet = findViewById(R.id.calculateCubicFeet);
        Button calcCubicYard = findViewById(R.id.calculateCubicYard);
//        RadioGroup unitRadioGroup = findViewById(R.id.unitRadioGroup);

        widthButton.setOnClickListener(v -> storeWidth());
        lengthButton.setOnClickListener(v -> storeLength());
        heightButton.setOnClickListener(v -> storeHeight());
        calcSqFeet.setOnClickListener(v -> calculateSquareFeet());
        calcCubicFeet.setOnClickListener(v -> calculateCubicFeet());
        calcCubicYard.setOnClickListener(v -> calculateCubicYard());
//        unitRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            switch (checkedId) {
//                case R.id.squareFeetRadioButton:
//                    selectedUnit = "Square Feet";
//                    break;
//                case R.id.cubicFeetRadioButton:
//                    selectedUnit = "Cubic Feet";
//                    break;
//                case R.id.yardRadioButton:
//                    selectedUnit = "Yard";
//                    break;
//            }
//            updateAreaTextView(); // Update area TextView when unit is selected
//        });

        handler = new Handler(message -> {
            if (message.what == MessageConstants.MESSAGE_READ) {
                byte[] readBuf = (byte[]) message.obj;
                String receivedData = new String(readBuf, 0, message.arg1);
            }
            return true;
        });

        handler.post(fetchDataRunnable);
    }

    @SuppressLint("SetTextI18n")
    private void storeWidth() {
        width = Double.parseDouble(rangeData.getText().toString().split(":")[1].trim());
        TextView widthTextView = findViewById(R.id.widthView);
        widthTextView.setText("Width: " + width);
    }

    @SuppressLint("SetTextI18n")
    private void storeLength() {
        length = Double.parseDouble(rangeData.getText().toString().split(":")[1].trim());
        TextView lengthTextView = findViewById(R.id.lengthView);
        lengthTextView.setText("Length: " + length);
    }

    @SuppressLint("SetTextI18n")
    private void storeHeight() {
        height = Double.parseDouble(rangeData.getText().toString().split(":")[1].trim());
        TextView lengthTextView = findViewById(R.id.heightView);
        lengthTextView.setText("Height: " + height);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void calculateSquareFeet(){
        if (width == 0.0 || length == 0.0 || height == 0.0) {
            areaTextView.setText("You must provide width and length to do calculations");
        }
        double calculate = width * length; // Calculate area in square feet
        area = Double.parseDouble(String.format("%.2f", calculate));
        areaTextView.setText("Area: " + area + " " + "ft" + getResources().getString(R.string.square_symbol));
    }
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void calculateCubicFeet(){
        if (width == 0.0 || length == 0.0 || height == 0.0) {
            areaTextView.setText("You must provide width, length, and height to do calculations");
        }
        double calculate = width * length * height;
        area = Double.parseDouble(String.format("%.2f", calculate)); // Calculate area in square feet
        areaTextView.setText("Area: " + area + " " + "ft" + getResources().getString(R.string.cubic_symbol));
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void calculateCubicYard(){
        if (width == 0.0 || length == 0.0 || height == 0.0) {
            areaTextView.setText("You must provide width, length, and height to do calculations");
        }
        double calculate = (width * length * height) / 27;
        area = Double.parseDouble(String.format("%.2f", calculate)); // Calculate area in square feet
        areaTextView.setText("Area: " + area + " " + "yd" + getResources().getString(R.string.cubic_symbol));
    }



//    @SuppressLint("SetTextI18n")
//    private void updateAreaTextView() {
//        // Check if width and length (and height for cubic feet or yard) are available
//        if (width != 0 && length != 0 && (selectedUnit.equals("Square Feet") || (selectedUnit.equals("Cubic Feet") || selectedUnit.equals("Yard")) && height != 0)) {
//            // Calculate area based on selected unit
//            switch (selectedUnit) {
//                case "Square Feet":
//                    area = width * length; // Calculate area in square feet
//                    areaTextView.setText("Area: " + area + " " + getResources().getString(R.string.square_feet_symbol)); // Update area TextView with square feet symbol
//                    break;
//                case "Cubic Feet":
//                    area = width * length * height; // Calculate area in cubic feet
//                    areaTextView.setText("Area: " + area + " " + getResources().getString(R.string.cubic_feet_symbol));
//                    break;
//                case "Yard":
//                    area = width * length * height / 27; // Calculate area in yards (assuming height in feet)
//                    areaTextView.setText("Area: " + area + " " + getResources().getString(R.string.yard_symbol)); // Update area TextView with yard symbol
//                    break;
//            }
//        } else {
//            // Display a message indicating that all dimensions are required
//            areaTextView.setText("Please provide width, length" + (selectedUnit.equals("Cubic Feet") || selectedUnit.equals("Yard") ? ", and height" : "") + " to calculate area.");
//        }
//    }

    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }
}

