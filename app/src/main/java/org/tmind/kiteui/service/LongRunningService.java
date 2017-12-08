package org.tmind.kiteui.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import org.tmind.kiteui.task.KillProcessTask;
import org.tmind.kiteui.task.ShowAlertMsgTask;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;

/**
 * Created by vali on 12/6/2017.
 */

public class LongRunningService extends Service {

    private final static String TAG = "Background Service";

    private Timer killProcessTimer;

    private Timer alertMsgTimer;

    private Context context;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String pkg = intent.getStringExtra("pkg");
                String endTimeHour = intent.getStringExtra("endTimeHour");
                String endTimeMinute = intent.getStringExtra("endTimeMinute");

                context = getApplicationContext();

                Calendar killProcessCalendar = Calendar.getInstance();
                killProcessCalendar.set(Calendar.HOUR_OF_DAY,Integer.valueOf(endTimeHour.split(":")[0]));
                killProcessCalendar.set(Calendar.MINUTE,Integer.valueOf(endTimeMinute));
                Date killProcessTime = killProcessCalendar.getTime();

                killProcessTimer = new Timer();
                killProcessTimer.schedule(new KillProcessTask(pkg,context),killProcessTime);

                Calendar alertMsgCanlendar = new GregorianCalendar();
                alertMsgCanlendar.setTime(killProcessTime);
                alertMsgCanlendar.add(Calendar.MINUTE, -5); //warning alert message, 5 mins in advance

                alertMsgTimer = new Timer();
                alertMsgTimer.schedule(new ShowAlertMsgTask("程序将在五分钟后关闭",context), alertMsgCanlendar.getTime());

            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 60 * 1000;   // 这是一小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour; Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "CAll destory function, timer caceled");
        if(killProcessTimer != null){
            killProcessTimer.cancel();
        }
        if(alertMsgTimer!=null){
            alertMsgTimer.cancel();
        }
    }
}
