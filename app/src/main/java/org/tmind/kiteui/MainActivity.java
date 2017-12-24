package org.tmind.kiteui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.tmind.kiteui.model.MertoItemView;
import org.tmind.kiteui.service.LockAppService;
import org.tmind.kiteui.utils.DBHelper;
import org.tmind.kiteui.utils.AyncHttpTask;
import org.tmind.kiteui.utils.PhoneUtil;
import org.tmind.kiteui.utils.TimeUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * @ClassName: MertoActivity
 * @Description: Kid UI 主页面
 * @author: li junying
 * @date: 2017年11月26日 上午10:44:21
 */
public class MainActivity extends Activity {

    private static final String TAG = "Kid-UI-Main_Page";
    //	private Button addBt;
    private FrameLayout mertoContent;
    private LinearLayout itemLayout1, itemLayout2;
    private MertoItemView telephoneItem, smsItem, photoItem, micItem, emergenceHelpItem, pictureItem, appBox,
            parenetControllItem;

    private SharedPreferences sharedPreferences;

    //Location variables
    private LocationManager locationManager;
    private String locationProvider;

    private SQLiteDatabase db;

    private final static String parentControlPasswordTable = "parent_control_password_table";
    private final static String applicationControlTable = "application_control_table";
    private final static String resetPasswordTable = "reset_password_table";
    private final static String emergencePhoneTable = "emergence_phone_table";
    private final static String passwordControlTable = "password_control_table";

    private final static int MAX_WRONG_PASSWORD_TIMES = 3;

    private Context context;

    private Intent lockAppService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        context = this;
        //start app lock service
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //TODO should record?
        }
        lockAppService = new Intent(this, LockAppService.class);
        startService(lockAppService);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //检查数据库 & 表
        db = new DBHelper(this).getDbInstance();
        //检查表是否存在，不存在则创建，存在则返回
        createTableIfNotExist();
        //进行初始化设置
        if (ifNeedGotoInitActivity()) {
            route2Activity(InitialSettingActivity.class);
        }
        //初始化整个UI
        initView();
        //获取地理位置管理器
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDestroy(){
        stopService(lockAppService);
        super.onDestroy();
    }

    //初始化UI桌面图标，名称和功能
    /**
     * @see res/arrays.xml
     */
    private void initView() {
        mertoContent = (FrameLayout) findViewById(R.id.merto_content);
        itemLayout1 = (LinearLayout) findViewById(R.id.item_layout1);
        itemLayout2 = (LinearLayout) findViewById(R.id.item_layout2);
        telephoneItem = (MertoItemView) findViewById(R.id.merto_0);
        smsItem = (MertoItemView) findViewById(R.id.merto_1);
        photoItem = (MertoItemView) findViewById(R.id.merto_2);
        micItem = (MertoItemView) findViewById(R.id.merto_3);
        emergenceHelpItem = (MertoItemView) findViewById(R.id.merto_4);
        pictureItem = (MertoItemView) findViewById(R.id.merto_5);
        appBox = (MertoItemView) findViewById(R.id.merto_6);
        parenetControllItem = (MertoItemView) findViewById(R.id.merto_7);
        initColor();
        telephoneItem.setTag(0);
        smsItem.setTag(1);
        photoItem.setTag(2);
        micItem.setTag(3);
        emergenceHelpItem.setTag(4);
        pictureItem.setTag(5);
        appBox.setTag(6);
        parenetControllItem.setTag(7);

        //电话
        telephoneItem.setIcon(R.drawable.kid_ui_telephone);
        telephoneItem.setText("电话");
        telephoneItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                call("", false);//拨打电话
            }
        });
        //短信
        smsItem.setIcon(R.drawable.kid_ui_sms);
        smsItem.setText("短信");
        smsItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS(""); //发短信
            }
        });
        //照像
        photoItem.setIcon(R.drawable.kid_ui_photo);
        photoItem.setText("照相机");
        photoItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                photoAndVideo(1);
            }
        });
        //录音机
        micItem.setIcon(R.drawable.kid_ui_mic);
        micItem.setText("录音机");
        micItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                route2Activity(MicRecordActivity.class);
            }
        });
        //求助
        emergenceHelpItem.setIcon(R.drawable.kid_ui_help);
        emergenceHelpItem.setText("一键呼救");
        emergenceHelpItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), R.string.press_long_click, Toast.LENGTH_LONG).show();
            }
        });
        emergenceHelpItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO send emergence information to remote server
                //TODO information format telno+imei+time+location
                String locationStr = PhoneUtil.getLocationInfo(locationManager, locationListener);
                String timeStr = String.valueOf(new Date().getTime());
                ;
                String telnoPlusIMEI = PhoneUtil.getPhoneNo(context) + "+" + PhoneUtil.getImei(context);
                String emergenceCallNo = null;
                emergenceCallNo = getEmergencePhoneNo();
                String stringKeepedInRemoteServer = telnoPlusIMEI + "+" + locationStr + "+" + emergenceCallNo + "+" + timeStr;
                String remoteServerAddr = getResources().getString(R.string.remote_server_address);
                String url = remoteServerAddr + "/rest/insertHelpInfo/" + stringKeepedInRemoteServer;
                new AyncHttpTask().execute(url);
                //TODO send 2 server
                Log.d(TAG, stringKeepedInRemoteServer);
                //TODO get setted help number from SQLite
                call(emergenceCallNo, true);
                return true;

            }
        });
        //图库
        pictureItem.setIcon(R.drawable.kid_ui_picture);
        pictureItem.setText("照片库");
        pictureItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivity(intent);
            }
        });

        //应用盒子
        appBox.setIcon(R.drawable.kid_ui_box);
        appBox.setText("应用大全");
        appBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                route2Activity(AppBoxActivity.class);
            }
        });
        //设置
        parenetControllItem.setIcon(R.drawable.kid_ui_setting);
        parenetControllItem.setText("设置");
        parenetControllItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               route2Activity(NormalSettingActivity.class);
            }
        });
        parenetControllItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                inputPasswordBeforeOpenParentControll();
                return false;
            }
        });
    }

    private void initColor(){
        //获得颜色代码, 0 -> pink, 1 -> blue, 2 -> red
        int currentThemeValue = sharedPreferences.getInt("current_theme", 1);
        if(currentThemeValue == 0){
            mertoContent.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_bkg));
            telephoneItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_telephoneItem));
            smsItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_smsItem));
            photoItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_photoItem));
            micItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_micItem));
            emergenceHelpItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_emergenceHelpItem));
            pictureItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_pictureItem));
            appBox.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_appBox));
            parenetControllItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_pink_parenetControllItem));
        }
        if(currentThemeValue == 1){
            mertoContent.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_bkg));
            telephoneItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_telephoneItem));
            smsItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_smsItem));
            photoItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_photoItem));
            micItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_micItem));
            emergenceHelpItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_emergenceHelpItem));
            pictureItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_pictureItem));
            appBox.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_appBox));
            parenetControllItem.setBackgroundColor(ContextCompat.getColor(context, R.color.theme_blue_parenetControllItem));
        }

    }

    private String getEmergencePhoneNo() {
        Cursor cursor = db.rawQuery("select phone_no from " + emergencePhoneTable + "", null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return null;
        }
    }

    /**
     * 拨打电话
     *
     * @param phone
     */
    private void call(String phone, boolean emergenceFlag) {
        Intent intent = new Intent();
        if (emergenceFlag) {
            intent.setAction(Intent.ACTION_CALL);
        } else {
            intent.setAction(Intent.ACTION_DIAL);
        }
        intent.setData(Uri.parse("tel:" + phone));
        //开始这个企图
        startActivity(intent);
    }

    /**
     * 发送短信
     *
     * @param smsBody
     */
    private void sendSMS(String smsBody) {
        //"smsto:xxx" xxx是可以指定联系人的
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        //"sms_body"必须一样，smsbody是发送短信内容content
        intent.putExtra("sms_body", smsBody);
        startActivity(intent);
    }

    /**
     * 拍照/摄像
     */
    private void photoAndVideo(int code) {
        Intent intent = null;
        if (code == 1) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        } else if (code == 2) {
            intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }
        startActivity(intent);
    }

    /**
     * 跳转到Activity
     */
    private void route2Activity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }

    private String getLocationInfo() {
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
        } catch (SecurityException e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
        //lng+lat
        String locationStr = location.getLongitude() + "+" + location.getLatitude();
        return locationStr;
    }

    private String getTelNoPlusIMEI() {
        String tel = null;
        String imei = null;
        try {
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            tel = tm.getLine1Number();//手机号
            imei = tm.getSimSerialNumber();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return tel + "+" + imei;
    }

    private void inputPasswordBeforeOpenParentControll() {

        final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.input_password_title))
                .setIcon(R.drawable.kid_ui_password)
                .setView(inputServer).setNegativeButton(
                getString(R.string.cancel_button), null);
        builder.setPositiveButton(getString(R.string.confirm_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String inputPassword = inputServer.getText().toString();
                        //TODO password corrent to route
                        //获取密码
                        String savedParentPassword = getParentPassword();
                        if (savedParentPassword != null && inputPassword.equals(savedParentPassword)) {
                            cleanPasswordWrongTimesTable();
                            route2Activity(ParentControlActivity.class);
                        } else {
                            String[] result = getPasswordWrongTimes();
                            int wrongTimes = Integer.valueOf(result[0]);
                            if (result[1] != null) {
                                long wrongDateStr = Long.valueOf(result[1]);
                                if ((TimeUtils.isYeaterday(new Date(wrongDateStr), new Date()) != 0) && wrongTimes >= 3) {
                                    Toast.makeText(getApplicationContext(), R.string.password_wrong_over_time, Toast.LENGTH_LONG).show();
                                } else {
                                    int currentWrongTimes = wrongTimes + 1;
                                    int leftWrongTimes = MAX_WRONG_PASSWORD_TIMES - currentWrongTimes;
                                    //更新数据库密码输入错误次数
                                    updatePasswordWrongTimes(String.valueOf(currentWrongTimes));
                                    String msg = getResources().getString(R.string.password_wrong);
                                    String showMsg = String.format(msg, currentWrongTimes, leftWrongTimes);
                                    Toast.makeText(getApplicationContext(), showMsg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
        //重置密码
        builder.setNeutralButton(R.string.forget_password_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                route2Activity(ResetPwdQuestionActivity.class);
            }
        });
        builder.show();
    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + ".." + Thread.currentThread().getName());
            //如果位置发生变化,重新显示
            //TODO if location changed, do we need to save?
//            showLocation(location);
        }
    };

    private boolean createTableIfNotExist() {

//        dropDb(db);

        //家长密码表
        if (!checkIfTableExist(parentControlPasswordTable)) {
            db.execSQL("create table " + parentControlPasswordTable + "(_id integer primary key autoincrement, parent_password varchar(50), password_type varchar(20))");
        }
        //
        if (!checkIfTableExist(passwordControlTable)) {
            db.execSQL("create table " + passwordControlTable + "(_id integer primary key autoincrement, wrong_times varchar(50), password_type varchar(20), password_date_time varchar(100))");
            //初始化表
            db.execSQL("insert into " + passwordControlTable + " (wrong_times, password_type, password_date_time) values ('0','pwd','" + new Date().getTime() + "')"); //输入密码错误的次数
            db.execSQL("insert into " + passwordControlTable + " (wrong_times, password_type, password_date_time) values ('0','rst','" + new Date().getTime() + "')"); //输入忘记密码的提示问题的错误次数
        }
        //家长控制表
        if (!checkIfTableExist(applicationControlTable)) {
            db.execSQL("create table " + applicationControlTable + "(_id integer primary key autoincrement, " +
                    "application_name varchar(200), " +
                    "use_flag varchar(20), " +
                    "start_time_hour varchar(50), " +
                    "start_time_minute varchar(50), " +
                    "end_time_hour varchar(50), " +
                    "end_time_minute varchar(50))");
        }
        //重置密码
        if (!checkIfTableExist(resetPasswordTable)) {
            db.execSQL("create table " + resetPasswordTable + "(_id integer primary key autoincrement, question varchar(50), answer varchar(50))");
        }
        //紧急联系人电话
        if (!checkIfTableExist(emergencePhoneTable)) {
            db.execSQL("create table " + emergencePhoneTable + "(_id integer primary key autoincrement, phone_no varchar(50))");
        }
        return true;
    }

    private boolean checkIfTableExist(String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private boolean ifNeedGotoInitActivity() {
        Cursor cursor = db.rawQuery("select parent_password from " + parentControlPasswordTable + "", null);
        if (cursor.moveToFirst()) {
            return false;
        } else {
            return true;
        }
    }


    private void dropDb(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type ='table' AND name != 'sqlite_sequence'", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                db.execSQL("DROP TABLE " + cursor.getString(0));
            }
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

    private String getParentPassword() {
        Cursor cursor = db.rawQuery("select parent_password from " + parentControlPasswordTable + "", null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return null;
        }
    }

    private String[] getPasswordWrongTimes() {
        String wrongTimeInDb = null;
        String wrongDate = null;
        Cursor cursor = db.rawQuery("select wrong_times, password_date_time from " + passwordControlTable + " where password_type='pwd'", null);
        if (cursor.moveToFirst()) {
            wrongTimeInDb = cursor.getString(0);
            wrongDate = cursor.getString(1);
        }

        return new String[]{wrongTimeInDb, wrongDate};
    }

    private void updatePasswordWrongTimes(String times) {
        db.execSQL("update " + passwordControlTable + " set wrong_times='" + times + "', password_date_time='" + new Date().getTime() + "' where password_type='pwd'");
    }

    private void cleanPasswordWrongTimesTable() {
        db.execSQL("update " + passwordControlTable + " set wrong_times='0', password_date_time='" + new Date().getTime() + "' where password_type='pwd'");
    }

}
