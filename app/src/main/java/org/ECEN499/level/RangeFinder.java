package org.ECEN499.level;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RangeFinder extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private TextView areaTextView;
    private static final String TAG = "RangeFinder";
    private Handler handler;

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

                    mainThreadHandler.post(() -> rangeData.setText(formattedRange));
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Error fetching data", e);
                    mainThreadHandler.post(() -> rangeData.setText("Error fetching data"));
                }

                mainThreadHandler.postDelayed(this, 1000);
            });
        }
    };





    private String currentUnit = "Feet"; // Add a field to store the current unit

    private String formatRange(double range) {
        double convertedRange;
        if (currentUnit.equals("Feet")) {
            convertedRange = range;
        } else if (currentUnit.equals("Inches")) {
            convertedRange = range * 12;
        } else if (currentUnit.equals("Yards")) {
            convertedRange = range / 3;
        } else if (currentUnit.equals("Centimeters")) {
            convertedRange = range * 30.48;
        } else { // currentUnit is "Meters"
            convertedRange = range * 0.3048;
        }
        return "Range: " + String.format("%.2f", convertedRange) + " " + currentUnit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_range_finder);

        Spinner unitSpinner = findViewById(R.id.unitSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.unit_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);



        // Modify the onItemSelected method of the unitSpinner
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUnit = (String) parent.getItemAtPosition(position);
                if (!selectedUnit.equals(currentUnit)) {
                    convertMeasurements(selectedUnit);
                    currentUnit = selectedUnit;
                    calculateArea();
                    calculateVolume();
                }
                updateDimensionTextViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        areaTextView = findViewById(R.id.areaTextView);
        rangeData = findViewById(R.id.rangeFinderJSONData);

        handler = new Handler();

        Switch manualInputSwitch = findViewById(R.id.manualInputSwitch);
        manualInputSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> isManualInput = isChecked);

        setupDimensionButton(R.id.widthButton, "width");
        setupDimensionButton(R.id.lengthButton, "length");
        setupDimensionButton(R.id.heightButton, "height");

        Button calculateAreaButton = findViewById(R.id.calculateArea);
        Button calculateVolumeButton = findViewById(R.id.calculateVolume);

        calculateAreaButton.setOnClickListener(view -> calculateArea());
        calculateVolumeButton.setOnClickListener(view -> calculateVolume());

        Handler rangeUpdateHandler = new Handler(Looper.getMainLooper());
        Runnable rangeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                String distance = DataHolder.getInstance().getDistance();
                if (distance != null) {
                    double distanceValue = Double.parseDouble(distance); // Convert the distance string to a double
                    String formattedDistance = formatRange(distanceValue); // Format the distance value with the correct unit
                    // Update TextView on the main thread
                    runOnUiThread(() -> rangeData.setText(formattedDistance));
                } else {
                    runOnUiThread(() -> rangeData.setText("Bluetooth is not connected"));
                }
                rangeUpdateHandler.postDelayed(this, 500); // Update distance every 1 second
            }
        };

        // Start the repeating task
        rangeUpdateHandler.post(rangeUpdateRunnable);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_laser:
                    startActivity(new Intent(RangeFinder.this, Laser.class));
                    return true;
                case R.id.nav_thermometer:
                    startActivity(new Intent(RangeFinder.this, Thermometer.class));
                    return true;
                case R.id.nav_range_finder:
                    startActivity(new Intent(RangeFinder.this, RangeFinder.class));
                    return true;
                case R.id.nav_menu:
                    startActivity(new Intent(RangeFinder.this, HomeActivity.class));
                    return true;
            }
            return false;
        });
    }


    private void setupDimensionButton(int buttonId, String dimension) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(view -> {
            if (isManualInput) {
                showDimensionInputDialog(dimension);
            } else {
                fetchRangeForDimension(dimension);
            }
        });
    }

    private void showDimensionInputDialog(String dimension) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter " + dimension);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            double value = Double.parseDouble(input.getText().toString());
            setDimensionValue(dimension, value);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void fetchRangeForDimension(String dimension) {
        handler.post(() -> {
            String rangeText = rangeData.getText().toString();
            Pattern pattern = Pattern.compile("\\d+\\.\\d+");
            Matcher matcher = pattern.matcher(rangeText);
            if (matcher.find()) {
                double rangeValue = Double.parseDouble(matcher.group());
                setDimensionValue(dimension, rangeValue);
            }
        });
    }

    private void convertMeasurements(String newUnit) {
        if (!newUnit.equals(currentUnit)) {
            double conversionFactor;
            if (newUnit.equals("Feet")) {
                conversionFactor = currentUnit.equals("Inches") ? 1.0 / 12 : currentUnit.equals("Yards") ? 3 : currentUnit.equals("Centimeters") ? 1.0 / 30.48 : 1.0 / 0.3048;
            } else if (newUnit.equals("Inches")) {
                conversionFactor = 12;
            } else if (newUnit.equals("Yards")) {
                conversionFactor = currentUnit.equals("Inches") ? 1.0 / 36 : 1.0 / 3;
            } else if (newUnit.equals("Centimeters")) {
                conversionFactor = currentUnit.equals("Inches") ? 2.54 : currentUnit.equals("Yards") ? 91.44 : currentUnit.equals("Feet") ? 30.48 : 100;
            } else { // newUnit is "Meters"
                conversionFactor = currentUnit.equals("Inches") ? 0.0254 : currentUnit.equals("Yards") ? 0.9144 : currentUnit.equals("Feet") ? 0.3048 : 0.01;
            }
            width *= conversionFactor;
            length *= conversionFactor;
            height *= conversionFactor;
            currentUnit = newUnit;
        }
    }


    // Modify the setDimensionValue method to not convert the value
    private void setDimensionValue(String dimension, double value) {
        switch (dimension) {
            case "width":
                width = value;
                break;
            case "length":
                length = value;
                break;
            case "height":
                height = value;
                break;
        }
        updateDimensionTextViews();
    }

    private void updateDimensionTextViews() {
        ((TextView) findViewById(R.id.widthView)).setText("Width: " + String.format("%.2f", width) + " " + currentUnit);
        ((TextView) findViewById(R.id.lengthView)).setText("Length: " + String.format("%.2f", length) + " " + currentUnit);
        ((TextView) findViewById(R.id.heightView)).setText("Height: " + String.format("%.2f", height) + " " + currentUnit);
    }

    // Modify the calculateArea and calculateVolume methods to use the selected unit
    // Modify the calculateArea and calculateVolume methods to use the selected unit
    private void calculateArea() {
        if (width != 0.0 && length != 0.0) {
            double area = width * length;
            if (currentUnit.equals("inches")) {
                area *= 144; // Convert square feet to square inches
            }
            areaTextView.setText("Area: " + String.format("%.2f", area) + " Sq " + currentUnit);
        } else {
            areaTextView.setText("Set width and length first");
        }
    }

    private void calculateVolume() {
        if (width != 0.0 && length != 0.0 && height != 0.0) {
            double volume = width * length * height;
            if (currentUnit.equals("inches")) {
                volume *= 1728; // Convert cubic feet to cubic inches
            }
            areaTextView.setText("Volume: " + String.format("%.2f", volume) + " Cubic " + currentUnit);
        } else {
            areaTextView.setText("Set width, length, and height first");
        }
    }


    private double getCurrentRange() {
        String rangeText = rangeData.getText().toString();
        Pattern pattern = Pattern.compile("\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(rangeText);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }
        return 0.0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(fetchDataRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(fetchDataRunnable);
    }
}
