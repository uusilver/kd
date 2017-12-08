package org.tmind.kiteui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.tmind.kiteui.model.MertoItemView;
import org.tmind.kiteui.utils.DBHelper;
import org.tmind.kiteui.utils.TimeUtils;

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

    //Location variables
    private LocationManager locationManager;
    private String locationProvider;

    private SQLiteDatabase db;

    private final static String parentControlPasswordTable = "parent_control_password_table";
    private final static String applicationControlTable = "application_control_table";
    private final static String resetPasswordTable = "reset_password_table";
    private final static String emergencePhoneTable = "emergence_phone_table";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //检查数据库 & 表
        db = new DBHelper(this).getDbInstance();
        //检查表是否存在，不存在则创建，存在则返回
        createTableIfNotExist();
        //进行初始化设置
        if(ifNeedGotoInitActivity()) {
            route2Activity(InitialSettingActivity.class);
        }
        //初始化整个UI
        initView();
        //获取地理位置管理器
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    //初始化UI桌面图标，名称和功能
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
        emergenceHelpItem.setText("求助");
        emergenceHelpItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO send emergence information to remote server
                //TODO information format telno+imei+time+location
                String locationStr = getLocationInfo();
                String timeStr = TimeUtils.getCurrentTime();
                String telnoPlusIMEI = getTelNoPlusIMEI();
                String stringKeepedInRemoteServer = telnoPlusIMEI+"+"+timeStr+"+"+locationStr;
                //TODO send 2 server
                Log.d(TAG,stringKeepedInRemoteServer);
                //TODO get setted help number from SQLite
                call(getEmergencePhoneNo(), true);
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
        parenetControllItem.setText("家长设置");
        parenetControllItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPasswordBeforeOpenParentControll();
            }
        });
    }

    private String getEmergencePhoneNo(){
        Cursor cursor = db.rawQuery("select phone_no from "+emergencePhoneTable+"",null);
        if(cursor.moveToFirst()){
            return cursor.getString(0);
        }else {
            return null;
        }
    }
    /**
     * 拨打电话
     * @param phone
     */
    private void call(String phone, boolean emergenceFlag) {
        Intent intent=new Intent();
        if(emergenceFlag){
            intent.setAction(Intent.ACTION_CALL);
        }else{
            intent.setAction(Intent.ACTION_DIAL);
        }
        intent.setData(Uri.parse("tel:"+phone));
        //开始这个企图
        startActivity(intent);
    }

    /**
     * 发送短信
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
    private void photoAndVideo(int code){
        Intent intent = null;
        if(code == 1) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }else if(code == 2){
            intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }
        startActivity(intent);
    }

    /**
     * 跳转到Activity
     */
    private void route2Activity(Class<?> activityClass){
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }

    private String getLocationInfo(){
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
        try{
            locationManager.requestLocationUpdates(provider, 1000, 0,
                locationListener);
            location = locationManager
                .getLastKnownLocation(provider);
        while (location == null) {
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    locationListener);
        }
        }catch (SecurityException e){
            Log.w(TAG, e.getMessage());
            return null;
        }
        String locationStr = "维度：" + location.getLatitude() +"+"
                + "经度：" + location.getLongitude();
        return locationStr;
    }

    private String getTelNoPlusIMEI(){
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String tel = tm.getLine1Number();//手机号
        String imei = tm.getSimSerialNumber();
        return tel+"+"+imei;
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
                        if(savedParentPassword!=null && inputPassword.equals(savedParentPassword)){
                            route2Activity(ParentControlActivity.class);
                        }else if(("@123@").equals(inputPassword)){
                            route2Activity(ResetPwdQuestionActivity.class); //reset password page
                        }
                        else{
                            Toast.makeText(getApplicationContext(), R.string.password_wrong,Toast.LENGTH_SHORT).show();
                        }
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

    private boolean createTableIfNotExist(){

//        dropDb(db);
        if(!checkIfTableExist(parentControlPasswordTable)){
            db.execSQL("create table "+parentControlPasswordTable+"(_id integer primary key autoincrement, parent_password varchar(50), password_type varchar(20))");
        }
        if(!checkIfTableExist(applicationControlTable))
        {
            db.execSQL("create table "+applicationControlTable+"(_id integer primary key autoincrement, " +
                    "application_name varchar(200), " +
                    "use_flag varchar(20), " +
                    "start_time_hour varchar(50), " +
                    "start_time_minute varchar(50), " +
                    "end_time_hour varchar(50), " +
                    "end_time_minute varchar(50))");
        }
        if(!checkIfTableExist(resetPasswordTable)){
            db.execSQL("create table "+resetPasswordTable+"(_id integer primary key autoincrement, question varchar(50), answer varchar(50))");
        }

        if(!checkIfTableExist(emergencePhoneTable)){
            db.execSQL("create table "+emergencePhoneTable+"(_id integer primary key autoincrement, phone_no varchar(50))");
        }
        return true;
    }

    private boolean checkIfTableExist(String tableName){
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private boolean ifNeedGotoInitActivity(){
        Cursor cursor = db.rawQuery("select parent_password from "+parentControlPasswordTable+"",null);
        if(cursor.moveToFirst()){
            return false;
        }else {
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

    private String getParentPassword(){
        Cursor cursor = db.rawQuery("select parent_password from "+parentControlPasswordTable+"",null);
        if(cursor.moveToFirst()){
            return cursor.getString(0);
        }else {
            return null;
        }
    }


}
