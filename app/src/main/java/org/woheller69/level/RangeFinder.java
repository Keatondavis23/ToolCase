package org.woheller69.level;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import android.os.Looper;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
public class RangeFinder extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Handler for posting results back to the main thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private static final String TAG = "RangeFinder";
    private Handler handler; // Handler to send and process messages and runnable objects

    private TextView rangeData;

    private final Runnable fetchDataRunnable = new Runnable() {
        @Override
        public void run() {
            // Execute the network request on a background thread
            executorService.execute(() -> {
                // Your network request code...

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://192.168.4.1/data")
                        .build();

                try (Response response = client.newCall(request).execute()) {

                    final String result = response.body().string();

                    JSONObject jsonObject = new JSONObject(result);
                    double range = jsonObject.getDouble("range");
                    //Log.d(TAG, "Result: " + result);
                    String formattedRange = "Range: " + range;
                    //Log.d(TAG, "Range: " + range);

                    // Post the result back to the main thread
                    mainThreadHandler.post(() -> {
                        if (formattedRange != null && !formattedRange.isEmpty()) {
                            rangeData.setText(formattedRange); // Update TextView with the result
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "IOException while making HTTP request", e);
                    // Additional logging to help diagnose the state when the exception is thrown
                    Log.e(TAG, "Request URL: " + request.url());
                    e.printStackTrace();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });

            // Schedule the next update
            handler.postDelayed(this, 3000); // 3000ms (3 seconds) delay for the next update
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_range_finder); // Ensure you have a layout file named activity_range_finder.xml

        rangeData = findViewById(R.id.rangeFinderJSONData);

        // Initialize the handler to handle messages from the ConnectedThread
        handler = new Handler(message -> {
            switch (message.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) message.obj;
                    String receivedData = new String(readBuf, 0, message.arg1);
                    updateUI(receivedData); // Method to update UI with received data
                    break;
            }
            return true;
        });

        handler.post(fetchDataRunnable);
    }

    // Method to update UI with received data
    private void updateUI(String data) {
        // Update your UI elements here with the received data
        // Example: textView.setText(data);
    }

    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }
}
