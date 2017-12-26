package org.tmind.kiteui.task;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import org.tmind.kiteui.MainActivity;
import org.tmind.kiteui.utils.LogUtil;

import java.util.List;
import java.util.TimerTask;

/**
 * Created by vali on 12/7/2017.
 */

public class KillProcessTask extends TimerTask {
    private final String pkg;
    private final Context context;

    private final static String TAG = "kill process task";

    public KillProcessTask(String pkg, Context context){
        this.pkg = pkg;
        this.context = context;
    }
    @Override
    public void run() {
        Looper.prepare();
            LogUtil.d(TAG, "Will kill pkg:"+pkg);
            killProcess(context, pkg);
        Looper.loop();
    }

    private void killProcess(Context context , String packageName){
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        // 通过调用ActivityManager的getRunningAppServicees()方法获得系统里所有正在运行的进程
        List<ActivityManager.RunningServiceInfo> runServiceList = mActivityManager
                .getRunningServices(50);
        // ServiceInfo Model类 用来保存所有进程信息
        for (ActivityManager.RunningServiceInfo runServiceInfo : runServiceList) {
            ComponentName serviceCMP = runServiceInfo.service;
            String serviceName = serviceCMP.getShortClassName(); // service 的类名
            String pkgName = serviceCMP.getPackageName(); // 包名

            if (pkgName.equals(packageName)) {
                mActivityManager.killBackgroundProcesses(packageName);
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

        }
    }
}
