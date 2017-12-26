package org.tmind.kiteui.utils;

import android.util.Log;

/**
 * Created by vali on 12/26/2017.
 */

public class LogUtil{

    private static boolean DEBUG_V = true;
    private static boolean DEBUG_D = true;
    private static boolean DEBUG_I = true;
    private static boolean DEBUG_W = true;
    private static boolean DEBUG_E = true;

    public static void v(String tag, String msg) {
        if (DEBUG_V) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG_D) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG_I) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG_W) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG_E) {
            Log.e(tag, msg);
        }
    }

}
