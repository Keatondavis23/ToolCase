package org.woheller69.level;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;

public class RangeFinder extends AppCompatActivity {
    private static final String TAG = "RangeFinder";
    private Handler handler; // Handler to send and process messages and runnable objects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_range_finder); // Ensure you have a layout file named activity_range_finder.xml

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
    }

    // Method to update UI with received data
    private void updateUI(String data) {
        // Update your UI elements here with the received data
        // Example: textView.setText(data);
    }

    // Define the ConnectedThread inner class
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }

            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    handler.obtainMessage(MessageConstants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }
}
