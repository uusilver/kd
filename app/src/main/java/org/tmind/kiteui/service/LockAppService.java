package org.tmind.kiteui.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import org.tmind.kiteui.MainActivity;
import org.tmind.kiteui.model.PackageInfoModel;
import org.tmind.kiteui.utils.CacheUtil;
import org.tmind.kiteui.utils.DBHelper;
import org.tmind.kiteui.utils.LogUtil;
import org.tmind.kiteui.utils.PhoneUtil;

import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class LockAppService extends Service {

    private static final String TAG = "LockAppService.class";
    private HandlerThread mCheckAppLock;
    private Handler mCheckAppHandler;
    private boolean checkFlag;

    private static final int MSG_UPDATE_INFO = 0x110;

    //与UI线程管理的handler
    private Handler mHandler = new Handler();

    private SQLiteDatabase db;

    /**
     * 绑定服务时才会调用
     * 必须要实现的方法
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。
     * 如果服务已在运行，则不会调用此方法。该方法只被调用一次
     */
    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate invoke");
        checkFlag = true;
        initBackThread();
        mCheckAppHandler.sendEmptyMessage(MSG_UPDATE_INFO);
        db = new DBHelper(getApplicationContext()).getDbInstance();
        super.onCreate();
    }

    /**
     * 每次通过startService()方法启动Service时都会被回调。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand invoke");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 服务销毁时的回调
     */
    @Override
    public void onDestroy() {
        LogUtil.d(TAG,"onDestroy invoke");
        mCheckAppLock.getLooper().quit();
        super.onDestroy();
    }

    private void initBackThread() {
        mCheckAppLock = new HandlerThread("check-message-coming");
        mCheckAppLock.start();
        mCheckAppHandler = new Handler(mCheckAppLock.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                checkForUpdate();
                if (checkFlag) {
                    mCheckAppHandler.sendEmptyMessageDelayed(MSG_UPDATE_INFO, 1000); //run per min,
                    //TODO should we give a more time, like 10 mins?
                }
            }
        };

    }

    /**
     * 模拟从服务器解析数据
     */
    private void checkForUpdate() {
        //模拟耗时
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //check previliedge
                String topActivity = getTopActivty();
                LogUtil.e("TopActivity Name", new Date().toString() + ":" + topActivity);
                if (!topActivity.equals("org.tmind.kiteui") && !topActivity.startsWith("com.android")) {
                    if (isTopActivityNeed2Stop(topActivity)) {
//                            Toast.makeText(getApplicationContext(), R.string.app_running_not_in_time, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                }


            }
        });
    }

    String topPackageName = "org.tmind.kiteui";

    public String getTopActivty() {
        //android5.0以上获取方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();

            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    //
                }
            }

        }
        //android5.0以下获取方式
        else {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo taskInfo = tasks.get(0);
            topPackageName = taskInfo.topActivity.getPackageName();
        }
        return topPackageName;
    }

    private boolean isTopActivityNeed2Stop(String topActivity) {
        CacheUtil instance = CacheUtil.getInstance();
        if (instance.isCacheNull()) {
            refreshCache(instance);
        }
        PackageInfoModel model = instance.get(topActivity);
        if (model != null) {
            //系统应用不停止
            if (model.getSystemFlag() != null && "true".equals(model.getSystemFlag())) {
                return false;
            }
            String startTimeHour = model.getStartTimeHour();
            String startTimeMinute = model.getStartTimeMinute();
            String endTimeHour = model.getEndTimeHour();
            String endTimeMinute = model.getEndTimeMinute();
            String tS = startTimeHour.split("\\:")[0] + ":" + startTimeMinute;
            String tE = endTimeHour.split("\\:")[0] + ":" + endTimeMinute;
            try {

                //可运行状态时间内，不停止
                if (PhoneUtil.isApplicationAvaiableTimeInZone(PhoneUtil.getTimeHHMM2Long(tS), PhoneUtil.getTimeHHMM2Long(tE), PhoneUtil.getCurrentTime())) {
                    return false;
                }
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
            }

        }
        return true;
    }

    private void refreshCache(CacheUtil instance) {
        Cursor cursor = db.rawQuery("select application_name,pkg, use_flag,start_time_hour,start_time_minute,end_time_hour,end_time_minute,system_flag from application_control_table where use_flag=? or system_flag=?", new String[]{"true", "true"});
        while (cursor.moveToNext()) {
            PackageInfoModel model = new PackageInfoModel();
            model.setApplicationName(cursor.getString(0));
            model.setPkg(cursor.getString(1));
            model.setAllowFlag(cursor.getString(2));
            model.setStartTimeHour(cursor.getString(3));
            model.setStartTimeMinute(cursor.getString(4));
            model.setEndTimeHour(cursor.getString(5));
            model.setEndTimeMinute(cursor.getString(6));
            model.setSystemFlag(cursor.getString(7));
            model.setOldAppFlag(true);
            instance.put(model.getPkg(), model);
        }
    }
}
