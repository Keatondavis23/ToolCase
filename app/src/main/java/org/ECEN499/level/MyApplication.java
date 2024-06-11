package org.ECEN499.level;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication instance;
    private ConnectedThread connectedThread;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public void setupConnectedThread(ConnectedThread connectedThread) {
        this.connectedThread = connectedThread;
    }

    public ConnectedThread getConnectedThread() {
        return connectedThread;
    }
}
