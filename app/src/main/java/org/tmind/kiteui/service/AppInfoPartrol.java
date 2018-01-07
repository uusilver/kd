package org.tmind.kiteui.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.SystemClock;

import org.tmind.kiteui.R;
import org.tmind.kiteui.model.PackageInfoModel;
import org.tmind.kiteui.utils.AyncHttpPostTask;
import org.tmind.kiteui.utils.AyncRefreshApInfoTask;
import org.tmind.kiteui.utils.DBHelper;
import org.tmind.kiteui.utils.LogUtil;

import java.util.Date;

public class AppInfoPartrol extends Service {
    public AppInfoPartrol() {
    }

    private static final String TAG = "UpdateAppInfoService.class";
    private final static String emergencePhoneTable = "emergence_phone_table";

    private volatile boolean sendFlag = false;
    private SQLiteDatabase db;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        db = new DBHelper(getApplicationContext()).getDbInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(sendFlag){
                    return;
                }
                sendFlag = true;
                if(sendFlag) {
                    String remoteUrl = getApplicationContext().getResources().getString(R.string.remote_server_address) + "/rest/appInfo";
                    String param = "appInfo=" + getAppInfoStr();
                    new AyncHttpPostTask(remoteUrl, param, "application/x-www-form-urlencoded").start();
                    LogUtil.d("UpdateAppInfoService", getAppInfoStr());
                    String refreshUrl = getApplicationContext().getResources().getString(R.string.remote_server_address) + "/rest/retreiveAppInfo/" + getEmergencePhoneNo();
                    //update
                    new AyncRefreshApInfoTask(refreshUrl, db).start();
                }
                sendFlag = false;
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour =   1000; // 这是10 mins的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private String getAppInfoStr() {
        Cursor cursor = db.rawQuery("select application_name,pkg, use_flag,start_time_hour,start_time_minute,end_time_hour,end_time_minute,system_flag from application_control_table where use_flag=? or system_flag=?", new String[]{"true", "true"});
        StringBuilder result = new StringBuilder();
        result.append(getEmergencePhoneNo() +"|");
        while (cursor.moveToNext()) {
            result.append(cursor.getString(0)+"$"); //setApplicationName
            result.append(cursor.getString(1)+"$"); //setPkg
            result.append(cursor.getString(2)+"$"); //setAllowFlag
            result.append(cursor.getString(3)+"$"); //setStartTimeHour
            result.append(cursor.getString(4)+"$"); //setStartTimeMinute
            result.append(cursor.getString(5)+"$"); //setEndTimeHour
            result.append(cursor.getString(6)+"$"); //setEndTimeMinute
            result.append(cursor.getString(7)+"$"); //setSystemFlag
            result.append("@");
        }
        return result.toString();
    }

    private String getEmergencePhoneNo() {
        Cursor cursor = db.rawQuery("select phone_no from " + emergencePhoneTable + "", null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return null;
        }
    }
}
