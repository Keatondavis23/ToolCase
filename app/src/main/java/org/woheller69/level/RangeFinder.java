package org.woheller69.level;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


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

public class RangeFinder extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private TextView areaTextView;
    private static final String TAG = "RangeFinder";
    private Handler handler;

    private String currentUnit = "Ft"; // Default temperature unit
    private TextView rangeData;
    private double width = 0.0;
    private double length = 0.0;
    private double height = 0.0;
    private boolean isManualInput = false;


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
                    String formattedRange = formatRange(range);

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
        Button calcArea = findViewById(R.id.calculateArea);
        Button calcVolume = findViewById(R.id.calculateVolume);
        Button feetUnitButton = findViewById(R.id.newButton1);
        Button yardUnitButton = findViewById(R.id.newButton2);
//        RadioGroup unitRadioGroup = findViewById(R.id.unitRadioGroup);

        widthButton.setOnClickListener(v -> storeWidth());
        lengthButton.setOnClickListener(v -> storeLength());
        heightButton.setOnClickListener(v -> storeHeight());
        calcArea.setOnClickListener(v -> calculateArea());
        calcVolume.setOnClickListener(v -> calculateVolume());
        feetUnitButton.setOnClickListener(v -> setCurrentUnit("ft"));
        yardUnitButton.setOnClickListener(v -> setCurrentUnit("yds"));

        Switch manualInputSwitch = findViewById(R.id.manualInputSwitch);
        manualInputSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isManualInput = isChecked;
            if (isChecked) {
                // Code to disable automatic fetching, if necessary
            } else {
                // Optionally, re-enable automatic fetching
            }
        });

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
        if (isManualInput) {
            promptForManualInput("Width");
        } else {
            String unparsedWidth = rangeData.getText().toString();

            Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
            Matcher matcher = pattern.matcher(unparsedWidth);

            if (matcher.find()) {
                String parsedWidth = matcher.group(0);
                width = Double.parseDouble(parsedWidth);

                TextView widthTextView = findViewById(R.id.widthView);
                widthTextView.setText("Width: " + width + " " + currentUnit);
            } else {
                Log.e("storeWidth", "No number found in the string: " + unparsedWidth);
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void storeLength() {
        if (isManualInput) {
            promptForManualInput("Length");
        } else {
            String unparsedLength = rangeData.getText().toString();

            Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
            Matcher matcher = pattern.matcher(unparsedLength);

            if (matcher.find()) {
                String parsedLength = matcher.group(0);
                length = Double.parseDouble(parsedLength);

                TextView lengthTextView = findViewById(R.id.lengthView);
                lengthTextView.setText("Length: " + length + " " + currentUnit);
            } else {
                Log.e("storeLength", "No number found in the string: " + unparsedLength);
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private void storeHeight() {
            if (isManualInput) {
                promptForManualInput("Height");
            } else {
                String unparsedHeight = rangeData.getText().toString();

                // Use a regex to find numbers in the string, including decimal numbers
                Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
                Matcher matcher = pattern.matcher(unparsedHeight);

                // Check if a number is found
                if (matcher.find()) {
                    // Extract the first match which should be your number
                    String parsedHeight = matcher.group(0);

                    // Convert the extracted string to double
                    height = Double.parseDouble(parsedHeight);

                    // Set the extracted number to the TextView
                    TextView lengthTextView = findViewById(R.id.heightView);
                    lengthTextView.setText("Height: " + height + " " + currentUnit);
                } else {
                    // Handle the case where the number is not found
                    Log.e("storeHeight", "No number found in the string: " + unparsedHeight);
                }
            }
    }

    private void updateDimensionView(int textViewId, String dimension, double value) {
        TextView dimensionView = findViewById(textViewId);
        dimensionView.setText(String.format("%s: %.2f %s", dimension, value, currentUnit));
    }


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void calculateArea() {
        if (width == 0.0 || length == 0.0 || height == 0.0) {
            areaTextView.setText("You must provide width and length to do calculations");
        }
        double calculate = width * length; // Calculate area
        double area = Double.parseDouble(String.format("%.2f", calculate));
        Log.d(TAG, "Area calculated" + area);

        areaTextView.setText("Area: " + area + " " + currentUnit + getResources().getString(R.string.square_symbol));
    }
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void calculateVolume(){
        if (width == 0.0 || length == 0.0 || height == 0.0) {
            areaTextView.setText("You must provide width, length, and height to do calculations");
        }
        double calculate = width * length * height;
        double volume = Double.parseDouble(String.format("%.2f", calculate)); // Calculate area in square feet
        areaTextView.setText("Volume: " + volume + " " + currentUnit + getResources().getString(R.string.cubic_symbol));
    }

    @SuppressLint("SetTextI18n")
    private void setCurrentUnit(@NonNull String unit) {
        currentUnit = unit;
        fetchDataRunnable.run(); // Re-run fetchDataRunnable to update temperature with new unit
    }

    private String formatRange(double range) {
        if (currentUnit.equals("yds")) {
            return String.format("Range: %.2f yds", feetToYards(range));
        }
        return String.format("Range: %.2f ft", range);
        //if (currentUnit.equals("yds")) {
        //    return "Range: " + feetToYards(range) + " yds";
        //}
        //return "Range: " + range + " ft";
    }

    private double feetToYards(double range) {
        return (range / 3);
    }

    private void promptForManualInput(String dimension) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter " + dimension);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            double value = Double.parseDouble(input.getText().toString());
            switch (dimension) {
                case "Width":
                    width = value;
                    updateDimensionView(R.id.widthView, "Width", width);
                    break;
                case "Length":
                    length = value;
                    updateDimensionView(R.id.lengthView, "Length", length);
                    break;
                case "Height":
                    height = value;
                    updateDimensionView(R.id.heightView, "Height", height);
                    break;
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }
}

