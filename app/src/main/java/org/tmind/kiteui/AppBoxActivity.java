package org.tmind.kiteui;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tmind.kiteui.model.AppBoxItemModel;
import org.tmind.kiteui.utils.DBHelper;
import org.tmind.kiteui.utils.PhoneUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AppBoxActivity extends AppCompatActivity {

    public static final String TAG = "AppBox";
    private Context context;

    private List<AppBoxItemModel> appBoxItems;
    private SQLiteDatabase db;

    //Progress dialog
    ProgressDialog pd;

    private Handler handler = new Handler() {
        @Override
        //当有消息发送出来的时候就执行Handler的这个方法
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //只要执行到这里就关闭对话框
            pd.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_box);
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = DBHelper.getDbInstance(context);
        appBoxItems = new ArrayList<>();
        //加载app应用。
        //call progress dialog when time cosum opertaion run
        pd = ProgressDialog.show(context, "读取中...", "请等待", true, false);
        new Thread() {
            @Override
            public void run() {
                loadApps();
                handler.post(setGridView);
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    // 构建Runnable对象，在runnable中更新界面
    Runnable setGridView = new Runnable() {
        @Override
        public void run() {
            //更新界面
            GridView gridView = (GridView) findViewById(R.id.apps_list);
            //设置默认适配器。
            mContent = getApplicationContext();
            mResources = getResources();
            gridView.setAdapter(new AppsAdapter());

            //

            gridView.setOnItemClickListener(clickListener);
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            AppBoxItemModel model = appBoxItems.get(i);
            //check real time
            List<String> timeList = getStartEndTime(model.getPkg());
            String tS = timeList.get(0);
            String tE = timeList.get(1);
            try {
                //该应用的包名
                if (PhoneUtil.isApplicationAvaiableTimeInZone(PhoneUtil.getTimeHHMM2Long(tS), PhoneUtil.getTimeHHMM2Long(tE), PhoneUtil.getCurrentTime())) {
                    //
                    String pkg = model.getPkg();
                    //应用的主activity类
                    String cls = model.getMainCls();
                    ComponentName componet = new ComponentName(pkg, cls);
                    //
//                    Intent serviceIntent = new Intent(context, LongRunningService.class);
//                    serviceIntent.putExtra("pkg", pkg);
//                    serviceIntent.putExtra("endTimeHour",model.getEndTimeHour());
//                    serviceIntent.putExtra("endTimeMinute",model.getEndTimeMinute());
//                    //start monitor service
//                    stopService(serviceIntent);
//                    startService(serviceIntent);
                    //route to real application
                    Intent intent = new Intent();
                    intent.setComponent(componet);
                    startActivity(intent);
                    //

                } else {
                    Toast.makeText(context, R.string.app_running_not_in_time, Toast.LENGTH_LONG).show();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    };

    private static final String formatStr = "HH:mm";
    private static SimpleDateFormat sdf = new SimpleDateFormat(formatStr);


    private List<ResolveInfo> apps;
    private Resources mResources;
    private Context mContent;

    private void loadApps() {
        //load avalibables apps from database
        Cursor cursor = db.rawQuery("select application_name, start_time_hour, start_time_minute, end_time_hour, end_time_minute from application_control_table where use_flag='true'", null);
        while (cursor.moveToNext()) {
            AppBoxItemModel model = new AppBoxItemModel();
            model.setApplicationName(cursor.getString(0));
            model.setStartTimeHour(cursor.getString(1));
            model.setStartTimeMinute(cursor.getString(2));
            model.setEndTimeHour(cursor.getString(3));
            model.setEndTimeMinute(cursor.getString(4));
            appBoxItems.add(model);
        }
        cursor.close();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        new ImageView(AppBoxActivity.this);

        apps = getPackageManager().queryIntentActivities(mainIntent, 0);
        for (AppBoxItemModel m : appBoxItems) {
            for (ResolveInfo info : apps) {
                if (m.getApplicationName().equals(info.loadLabel(getPackageManager()))) {
                    m.setPkg(info.activityInfo.packageName);
                    m.setMainCls(info.activityInfo.name);
                    m.setPackageImage(info.activityInfo.loadIcon(getPackageManager()));
                    break;
                }
            }
        }
    }

    private List<String> getStartEndTime(String pkg) {
        Cursor cursor = db.rawQuery("select application_name, start_time_hour, start_time_minute, end_time_hour, end_time_minute from application_control_table where use_flag='true' and pkg=?", new String[]{pkg});
        List<String> list = new ArrayList<>(2);
        if (cursor.moveToNext()) {
            String tS = cursor.getString(1).split("\\:")[0] + ":" + cursor.getString(2);
            String tE = cursor.getString(3).split("\\:")[0] + ":" + cursor.getString(4);
            list.add(tS);
            list.add(tE);
        }
        cursor.close();
        return list;
    }


public class AppsAdapter extends BaseAdapter {

    public AppsAdapter() {
    }

    @Override
    public int getCount() {
        return appBoxItems.size();
    }

    @Override
    public Object getItem(int i) {
        return appBoxItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        AppBoxItemModel model = appBoxItems.get(i);

        View convertView = LayoutInflater.from(mContent).inflate(R.layout.app_box_item, null);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        TextView text = (TextView) convertView.findViewById(R.id.text);
        //设置文字和图片。
        text.setText(model.getApplicationName());

        image.setImageDrawable(model.getPackageImage());

        // convertView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        //使用dp进行参数设置。进行分辨率适配。
        convertView.setLayoutParams(new GridView.LayoutParams(
                (int) mResources.getDimension(R.dimen.app_width),
                (int) mResources.getDimension(R.dimen.app_height)));
        //返回一个图文混合。
        return convertView;
    }
}
}
