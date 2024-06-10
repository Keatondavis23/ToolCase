package org.ECEN499.level;

import android.app.Application;

import org.ECEN499.level.util.PreferenceHelper;

public class LevelApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceHelper.initPrefs(this);
    }
}
