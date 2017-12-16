package org.tmind.kiteui.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

/**
 * Created by vali on 12/16/2017.
 */

public class PhoneUtil {

    private final static String TAG = "PhoneUtil";

    public static String getImei(Context context){
        String imei = "unknown";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = tm.getSimSerialNumber();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        return imei;
    }

    public static String getPhoneNo(Context context){
        String tel = "unknown";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            tel = tm.getLine1Number();//手机号
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return tel;

    }

    public static String getLocationInfo(LocationManager locationManager, LocationListener locationListener){
        // 设置位置服务信息
        // 设置位置服务信息
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
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    locationListener);
            location = locationManager
                    .getLastKnownLocation(provider);
            while (location == null) {
                locationManager.requestLocationUpdates(provider, 1000, 0,
                        locationListener);
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
        //lng+lat
        String locationStr = location.getLongitude() + "+" + location.getLatitude();
        return locationStr;
    }
}
