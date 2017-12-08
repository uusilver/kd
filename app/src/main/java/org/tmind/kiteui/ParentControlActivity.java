package org.tmind.kiteui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.tmind.kiteui.model.PackageInfoModel;
import org.tmind.kiteui.utils.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParentControlActivity extends AppCompatActivity {

    private Context context;
    private ListView listview;
    private List<PackageInfoModel> tempArray;
    private List<PackageInfoModel> realArray;
    private List<String> selectid = new ArrayList<String>();
    private ParentControlAdapter parentControlAdapter;
    private RelativeLayout layout;
    //installed package
    private PackageManager packageManager = null;

    //sqlite db object
    SQLiteDatabase db;

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
        setContentView(R.layout.activity_parent_control);
        context = this;
        //call progress dialog when time cosum opertaion run
        pd = ProgressDialog.show(context, "读取中...", "请等待", true, false);
        //inital db object
        db = new DBHelper(context).getDbInstance();
        new Thread(){
            @Override
            public void run(){
                    tempArray = new ArrayList<PackageInfoModel>();
                    realArray = new ArrayList<PackageInfoModel>();

                    initAppInfo();

                    listview = (ListView) findViewById(R.id.list);
                    layout = (RelativeLayout) findViewById(R.id.relative);

                    //handler post to update adpater
                    handler.post(setAdpaterToParent);
                    handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initAppInfo() {
        //get all saved from database;
        Cursor cursor = db.rawQuery("select application_name,use_flag,start_time_hour,start_time_minute,end_time_hour,end_time_minute from application_control_table", null);
        while (cursor.moveToNext()){
            PackageInfoModel model = new PackageInfoModel();
            model.setApplicationName(cursor.getString(0));
            model.setAllowFlag(cursor.getString(1));
            model.setStartTimeHour(cursor.getString(2));
            model.setStartTimeMinute(cursor.getString(3));
            model.setEndTimeHour(cursor.getString(4));
            model.setEndTimeMinute(cursor.getString(5));
            model.setOldAppFlag(true);
            tempArray.add(model);
        }
        //load applications
        List<ResolveInfo> mAllPackages = new ArrayList<ResolveInfo>();
        packageManager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        new ImageView(ParentControlActivity.this);

        mAllPackages = getPackageManager().queryIntentActivities(mainIntent, 0);

        for (int i = 0; i < mAllPackages.size(); i++) {
            ResolveInfo resolveInfo = mAllPackages.get(i);
            String tempPackageName = resolveInfo.loadLabel(packageManager).toString();
            Drawable tempDrawable =  resolveInfo.activityInfo.loadIcon(packageManager);
            String tempPkg = resolveInfo.activityInfo.packageName;
            String tempCls = resolveInfo.activityInfo.name;
            realArray.add(comparePackgeModelInfo(tempPackageName,tempDrawable,tempPkg,tempCls));
        }
        //insert into database if new
        for(PackageInfoModel model : realArray){
            if(!model.isOldAppFlag()){
                String insertSql = "insert into application_control_table (application_name, use_flag, start_time_hour, start_time_minute, end_time_hour, end_time_minute) VALUES (?,?,?,?,?,?)";
                db.execSQL(insertSql, new Object[]{model.getApplicationName(), model.getAllowFlag(),model.getStartTimeHour(),model.getStartTimeMinute(), model.getEndTimeHour(), model.getEndTimeMinute()});
            }
        }

        //finish
    }

    private PackageInfoModel comparePackgeModelInfo(String tempApplicationName, Drawable tempDrawable, String tempPkg, String tempCls){
            int pos = isModelExist(tempApplicationName);
            PackageInfoModel model = new PackageInfoModel();
            if(pos>=0){
                model = tempArray.get(pos);
            }else{
                model.setApplicationName(tempApplicationName);
                model.setAllowFlag("false");
                model.setStartTimeHour("8:00");
                model.setStartTimeMinute("00");
                model.setEndTimeHour("20:00");
                model.setEndTimeMinute("00");
                model.setOldAppFlag(false);
            }
            model.setPackageImage(tempDrawable);
            model.setPkg(tempPkg);
            model.setMainCls(tempCls);
        return model;
    }

    private int isModelExist(String tempApplicationName){
        int findPos = -1;
        for(int index =0; index<tempArray.size();index++){
            if(tempArray.get(index).getApplicationName().equals(tempApplicationName)) {
                findPos = index;
                break;
            }
        }
        return findPos;
    }

    // 构建Runnable对象，在runnable中更新界面
    Runnable setAdpaterToParent = new  Runnable(){
        @Override
        public void run() {
            //更新界面
            parentControlAdapter = new ParentControlAdapter(context);
            listview.setAdapter(parentControlAdapter);
        }

    };

    /**
     * @author ieasy360_1
     *         自定义Adapter
     */
    class ParentControlAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater = null;
        private HashMap<Integer, View> mView;
        public HashMap<Integer, Integer> visiblecheck;//用来记录是否显示checkBox

        private Spinner startTimeHour;
        private Spinner startTimeMinute;
        private Spinner endTimeHour;
        private Spinner endTimeMinute;

        private Button deleteAppBtn;

        private Switch appSwither;

        public ParentControlAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = new HashMap<Integer, View>();
            visiblecheck = new HashMap<Integer, Integer>();
        }

        public int getCount() {
            return realArray.size();
        }

        public Object getItem(int position) {
            return realArray.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = mView.get(position);
            if (view == null) {
                view = inflater.inflate(R.layout.parent_controll_list_item, null);
                TextView txt = (TextView) view.findViewById(R.id.txtName);
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                final TextView statusTxt = (TextView) view.findViewById(R.id.status);
                startTimeHour = (Spinner)view.findViewById(R.id.start_time_hour);
                startTimeMinute = (Spinner)view.findViewById(R.id.start_time_minute);
                endTimeHour = (Spinner)view.findViewById(R.id.end_time_hour);
                endTimeMinute = (Spinner)view.findViewById(R.id.end_time_minute);

                appSwither = (Switch)view.findViewById(R.id.app_switch);

                //init spinner value
                startTimeHour.setSelection(getSelectionPosOfHour(realArray.get(position).getStartTimeHour()));
                startTimeMinute.setSelection(getSelectionPosOfMinute(realArray.get(position).getStartTimeMinute()));
                endTimeHour.setSelection(getSelectionPosOfHour(realArray.get(position).getEndTimeHour()));
                endTimeMinute.setSelection(getSelectionPosOfMinute(realArray.get(position).getEndTimeMinute()));

                //set listener on the spinner
                startTimeHour.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int sprinnerItemPos, long id) {
                                String selectedStartTimeHour = (String)startTimeHour.getItemAtPosition(sprinnerItemPos);
                                realArray.get(position).setStartTimeHour(selectedStartTimeHour);
                                db.execSQL("update application_control_table set start_time_hour='"+selectedStartTimeHour+"' where application_name='"+realArray.get(position).getApplicationName()+"'");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                startTimeMinute.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int sprinnerItemPos, long id) {
                            String selectedStartMinute = (String)startTimeMinute.getItemAtPosition(sprinnerItemPos);
                            realArray.get(position).setStartTimeMinute(selectedStartMinute);
                            db.execSQL("update application_control_table set start_time_minute='"+selectedStartMinute+"' where application_name='"+realArray.get(position).getApplicationName()+"'");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                endTimeHour.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int sprinnerItemPos, long id) {
                            String selectEndTimeHour = (String)endTimeHour.getItemAtPosition(sprinnerItemPos);
                            realArray.get(position).setEndTimeHour(selectEndTimeHour);
                            db.execSQL("update application_control_table set end_time_hour='"+selectEndTimeHour+"' where application_name='"+realArray.get(position).getApplicationName()+"'");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                endTimeMinute.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int sprinnerItemPos, long id) {
                            String selectedEndTimeMinute = (String)endTimeMinute.getItemAtPosition(sprinnerItemPos);
                            realArray.get(position).setEndTimeMinute(selectedEndTimeMinute);
                            db.execSQL("update application_control_table set end_time_minute='"+selectedEndTimeMinute+"' where application_name='"+realArray.get(position).getApplicationName()+"'");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                appSwither.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            String useFlag = null;
                            if(isChecked){
                                statusTxt.setText(R.string.avaiable);
                                useFlag = "true";
                            }else {
                                statusTxt.setText(R.string.unavaiable);
                                useFlag = "false";
                            }
                        db.execSQL("update application_control_table set use_flag='"+useFlag+"' where application_name='"+realArray.get(position).getApplicationName()+"'");
                    }
                });

                deleteAppBtn = (Button)view.findViewById(R.id.delete_app) ;
                deleteAppBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        new AlertDialog.Builder(ParentControlActivity.this).setTitle("系统提示")//设置对话框标题
                                .setMessage("请确认要删除该应用吗！")//设置显示的内容
                                .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                        // delete app
                                        db.execSQL("delete from application_control_table where application_name='"+realArray.get(position).getApplicationName()+"'");
                                        Uri uri = Uri.parse("package:" + realArray.get(position).getPkg());
                                        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                                        context.startActivity(intent);
                                        realArray.remove(position);
                                        ParentControlAdapter updateAdpater = new ParentControlAdapter(context);
                                        listview.setAdapter(updateAdpater);
                                    }
                                }).setNegativeButton("返回",new DialogInterface.OnClickListener() {//添加返回按钮
                            @Override
                            public void onClick(DialogInterface dialog, int which) {//响应事件
                                // TODO Auto-generated method stub
                                Log.i("alertdialog"," 请保存数据！");
                            }
                        }).show();//在按键响应事件中显示此对话框

                    }
                });

                //TODO set package image
                imageView.setImageDrawable(realArray.get(position).getPackageImage());
                txt.setText(realArray.get(position).getApplicationName());
                if ("true".equals(realArray.get(position).getAllowFlag())) {
                    statusTxt.setText(R.string.avaiable);
                    appSwither.setChecked(true);
                }
                else {
                    statusTxt.setText(R.string.unavaiable);
                    appSwither.setChecked(false);
                }


                view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                            //when user click app icon, the application should run
                            String pkg = realArray.get(position).getPkg();
                            String cls = realArray.get(position).getMainCls();
                            ComponentName componet = new ComponentName(pkg, cls);
                            Intent intent = new Intent();
                            intent.setComponent(componet);
                            startActivity(intent);
                    }
                });
                mView.put(position, view);
            }
            return view;
        }

        private int getSelectionPosOfHour(String hour){
            String[] hours = getResources().getStringArray(R.array.time_hour);
            for(int index=0;index<hours.length;index++){
                if(hours[index].equals(hour))
                    return index;
            }
            return -1;
        }

        private int getSelectionPosOfMinute(String min){
            String[] mins = getResources().getStringArray(R.array.time_minute);
            for(int index=0;index<mins.length;index++){
                if(mins[index].equals(min))
                    return index;
            }
            return -1;
        }
    }

}
