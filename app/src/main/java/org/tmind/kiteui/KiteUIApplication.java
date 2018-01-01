package org.tmind.kiteui;

import android.app.Application;

import org.tmind.kiteui.crash.CrashHandler;

/**
 * Created by vali on 1/1/2018.
 */

public class KiteUIApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
