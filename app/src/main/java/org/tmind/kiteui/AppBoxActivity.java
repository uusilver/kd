package org.tmind.kiteui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import org.tmind.kiteui.service.LongRunningService;
import org.tmind.kiteui.utils.DBHelper;
import org.tmind.kiteui.utils.PhoneUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppBoxActivity extends AppCompatActivity {

    public static final String TAG = "AppBox";
    private Context context;

    private List<AppBoxItemModel> appBoxItems;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_box);
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = new DBHelper(context).getDbInstance();
        appBoxItems = new ArrayList<>();
        //加载app应用。
        loadApps();
        GridView gridView = (GridView) findViewById(R.id.apps_list);
        //设置默认适配器。
        mContent = getApplicationContext();
        mResources = getResources();
        gridView.setAdapter(new AppsAdapter());

        //

        gridView.setOnItemClickListener(clickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
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
            String tS = model.getStartTimeHour().split("\\:")[0]+":"+model.getStartTimeMinute();
            String tE = model.getEndTimeHour().split("\\:")[0]+":"+model.getEndTimeMinute();
            try {
                //该应用的包名
                if(PhoneUtil.isApplicationAvaiableTimeInZone(PhoneUtil.getTimeHHMM2Long(tS),PhoneUtil.getTimeHHMM2Long(tE),PhoneUtil.getCurrentTime())){
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

                }else {
                    Toast.makeText(context, R.string.app_running_not_in_time,Toast.LENGTH_LONG).show();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    };

    private static final String formatStr = "HH:mm";
    private static SimpleDateFormat sdf=new SimpleDateFormat(formatStr);




    private List<ResolveInfo> apps;
    private Resources mResources;
    private Context mContent;

    private void loadApps() {
        //load avalibables apps from database
        Cursor cursor = db.rawQuery("select application_name, start_time_hour, start_time_minute, end_time_hour, end_time_minute from application_control_table where use_flag='true'", null);
        while (cursor.moveToNext()){
            AppBoxItemModel model = new AppBoxItemModel();
            model.setApplicationName(cursor.getString(0));
            model.setStartTimeHour(cursor.getString(1));
            model.setStartTimeMinute(cursor.getString(2));
            model.setEndTimeHour(cursor.getString(3));
            model.setEndTimeMinute(cursor.getString(4));
            appBoxItems.add(model);
        }

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        new ImageView(AppBoxActivity.this);

        apps = getPackageManager().queryIntentActivities(mainIntent, 0);
        for(AppBoxItemModel m : appBoxItems){
            for(ResolveInfo info : apps){
                if(m.getApplicationName().equals(info.loadLabel(getPackageManager()))){
                    m.setPkg(info.activityInfo.packageName);
                    m.setMainCls(info.activityInfo.name);
                    m.setPackageImage(info.activityInfo.loadIcon(getPackageManager()));
                    break;
                }
            }
        }
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
