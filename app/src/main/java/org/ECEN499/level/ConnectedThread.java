package org.ECEN499.level;


import static org.ECEN499.level.ConnectThread.handler;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//Class that given an open BT Socket will
//Open, manage and close the data Stream from the Arduino BT device
public class ConnectedThread extends Thread {

    private static final String TAG = "ConnectedThread";
    private static final int MESSAGE_READ = 1;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String valueRead;

    private static ConnectedThread instance = null; // Added 9:35

    public static ConnectedThread getInstance(BluetoothSocket socket) {
        if (instance == null) {
            instance = new ConnectedThread(socket);
        }
        return instance;
    }
    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        //Input and Output streams members of the class
        //We wont use the Output stream of this project
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public synchronized String getValueRead(){
        return valueRead;
    }


    public synchronized void write(byte[] bytes) {
        if (mmSocket.isConnected()) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        } else {
            Log.e(TAG, "Cannot write to a closed socket");
        }
    }
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes = 0; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                buffer[bytes] = (byte) mmInStream.read();
                String readMessage;
                // If I detect a "\n" means I already read a full measurement
                if (buffer[bytes] == '\n') {
                    readMessage = new String(buffer, 0, bytes);
                    Log.e(TAG, readMessage);
                    //Value to be read by the Observer streamed by the Obervable
                    valueRead=readMessage;
                    bytes = 0;

                    // Parse distance and temp from readMessage
                    Pattern pattern = Pattern.compile("Distance in feet: (.*): Thermister Value: (.*)");
                    Matcher matcher = pattern.matcher(readMessage);
                    if (matcher.find()) {
                        String distance = matcher.group(1);
                        String temp = matcher.group(2);

                        // Update distance and temp in DataHolder
                        DataHolder.getInstance().setDistance(distance);
                        DataHolder.getInstance().setTemp(temp);
                    }

                    // Send a message to the Handler
                    Message msg = handler.obtainMessage(MESSAGE_READ, readMessage);
                    msg.sendToTarget();
                } else {
                    bytes++;
                }

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }
    public boolean isConnected() {
        return mmSocket.isConnected();
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
