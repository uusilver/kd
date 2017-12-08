package org.tmind.kiteui.task;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.util.TimerTask;

/**
 * Created by vali on 12/7/2017.
 */

public class ShowAlertMsgTask extends TimerTask {
    private final Context context;

    private final String msg;

    public ShowAlertMsgTask(String msg, Context context) {
        this.msg = msg;
        this.context = context;
    }

    @Override
    public void run() {
        Looper.prepare();
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();

        Looper.loop();
    }
}
