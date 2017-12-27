package org.tmind.kiteui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.TelephonyManager;

import org.tmind.kiteui.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by vali on 12/16/2017.
 */

public class PhoneUtil {

    private final static String TAG = "PhoneUtil";

    public static String getImei(Context context) {
        String imei = "unknown";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = tm.getSimSerialNumber();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }
        return imei;
    }

    public static String getPhoneNo(Context context) {
        String tel = "unknown";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            tel = tm.getLine1Number();//手机号
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }
        return tel;

    }

    public static String getLocationInfo(LocationManager locationManager, LocationListener locationListener) {
        // 设置位置服务信息
        // 设置位置服务信息
        String locationStr = "unknown+unknown";
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // 取得效果最好的位置服务
        String provider = locationManager.getBestProvider(criteria,
                true);
        Location location = null;
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,
                    locationListener);
            location = locationManager
                    .getLastKnownLocation(provider);
        } catch (SecurityException e1) {
            LogUtil.w(TAG, e1.getMessage());
        } catch (Exception e) {
            LogUtil.w(TAG, e.getMessage());
        }
        //lng+lat
        if (location != null) {
            locationStr = location.getLongitude() + "+" + location.getLatitude();
        }
        return locationStr;
    }

    public static boolean isFirstStart(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("count", MODE_PRIVATE);
        int count = sharedPreferences.getInt("count", 0);
        if (count == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //存入数据
            editor.putInt("count", 1);
            //提交修改
            editor.commit();
            return true;
        }
        return false;

    }

    private static final String formatStr = "HH:mm";
    private static SimpleDateFormat sdf=new SimpleDateFormat(formatStr);

    public static long getTimeHHMM2Long(String timeStr) throws ParseException {
        return sdf.parse(timeStr).getTime();
    }

    public static boolean isApplicationAvaiableTimeInZone(long tStart,long tEnd,long t) throws ParseException {
        return tStart <= t && t <= tEnd;
    }

    public static long getCurrentTime() throws ParseException {
        return PhoneUtil.getTimeHHMM2Long(sdf.format(new Date()));
    }

}
