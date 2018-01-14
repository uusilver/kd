package org.tmind.kiteui;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.tmind.kiteui.utils.AyncHttpTask;
import org.tmind.kiteui.utils.DBHelper;
import org.tmind.kiteui.utils.LogUtil;
import org.tmind.kiteui.utils.PhoneUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InitialSettingActivity extends AppCompatActivity {

    private final static String TAG = "InitialSettingActivity";
    private Context context;
    private EditText resetPwdQuestion;
    private EditText resetPwdAnswer;
    private EditText oldPwd;
    private EditText newPwd;
    private EditText emergencePhoneNo;
    private CheckBox showPwdCheckBox;

    private Button confirmBtn;
    private Button cancelBtn;

    String resetPwdQuestionStr = null;
    String resetPwdAnswerStr = null;
    String oldPwdStr = null;
    String newPwdStr = null;
    String emergencePhoneNoStr = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setting);
        context = this;

        resetPwdQuestion = (EditText) findViewById(R.id.resetpwd_question);
        resetPwdAnswer = (EditText) findViewById(R.id.resetpwd_answer);
        oldPwd = (EditText) findViewById(R.id.resetpwd_edit_pwd_old);
        newPwd = (EditText) findViewById(R.id.resetpwd_edit_pwd_new);
        emergencePhoneNo = (EditText) findViewById(R.id.emergence_call_no);

        showPwdCheckBox = (CheckBox) findViewById(R.id.show_pwd);

        showPwdCheckBox.setOnClickListener(new CheckOnShowPwdListener());


        confirmBtn = (Button) findViewById(R.id.register_btn_sure);
        cancelBtn = (Button) findViewById(R.id.register_btn_cancel);

        confirmBtn.setOnClickListener(new ConfirmBtnClick());
        cancelBtn.setOnClickListener(new CancelBtn());
    }

    class ConfirmBtnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            resetPwdQuestionStr = resetPwdQuestion.getText().toString().trim();
            resetPwdAnswerStr = resetPwdAnswer.getText().toString().trim();
            oldPwdStr = oldPwd.getText().toString().trim();
            newPwdStr = newPwd.getText().toString().trim();
            emergencePhoneNoStr = emergencePhoneNo.getText().toString().trim();

            if (resetPwdQuestionStr == null || ("").equals(resetPwdQuestionStr)) {
                Toast.makeText(context, "密码重置问题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (resetPwdAnswerStr == null || ("").equals(resetPwdAnswerStr)) {
                Toast.makeText(context, "密码重置问题的答案不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (oldPwdStr == null || ("").equals(oldPwdStr)) {
                Toast.makeText(context, "新密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPwdStr == null || ("").equals(newPwdStr)) {
                Toast.makeText(context, "请重复密码", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPwdStr.equals(oldPwdStr)) {
                Toast.makeText(context, "两次密码输入错误", Toast.LENGTH_SHORT).show();
                return;
            }

            if (emergencePhoneNoStr == null || ("").equals(emergencePhoneNoStr)) {
                Toast.makeText(context, "请输入紧急联系人电话", Toast.LENGTH_SHORT).show();
                return;
            }

            if (emergencePhoneNoStr.length() != 11) {
                Toast.makeText(context, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                return;
            }
            String remoteUrl = getResources().getString(R.string.remote_server_address)+"/rest/isPhoneNotExist/"+emergencePhoneNoStr;
            new CheckPhoneExistThread(remoteUrl).start();
        }
    }

    //uiHandler在主线程中创建，所以自动绑定主线程
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //imei
                    String imei = PhoneUtil.getImei(context);
                    //
                    String send2RemoteStr = imei + "+" + emergencePhoneNoStr + "+" + newPwdStr;
                    String remoteServerAddr = getResources().getString(R.string.remote_server_address);
                    String url = remoteServerAddr + "/rest/regist/" + send2RemoteStr;
                    new AyncHttpTask().execute(url);
                    SQLiteDatabase db = DBHelper.getDbInstance(context);
                    //insert
                    String insertPwd = "INSERT INTO parent_control_password_table (parent_password, password_type) VALUES ('" + newPwdStr + "', 'pwd')";
                    db.execSQL(insertPwd);

                    String insertPwdQuestion = "INSERT INTO reset_password_table (question, answer)  VALUES ('" + resetPwdQuestionStr + "','" + resetPwdAnswerStr + "')";
                    db.execSQL(insertPwdQuestion);

                    String insertEmergencePhoneNo = "INSERT INTO emergence_phone_table (phone_no)  VALUES ('" + emergencePhoneNoStr + "')";
                    db.execSQL(insertEmergencePhoneNo);
                    //进入家长设置页面
                    Intent intent = new Intent(InitialSettingActivity.this, ParentControlActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    Toast.makeText(context, "号码已存在，请重新输入", Toast.LENGTH_LONG).show();
            }
        }
    };

    class CancelBtn implements View.OnClickListener {

        //clean
        @Override
        public void onClick(View v) {
            resetPwdQuestion.setText("");
            resetPwdAnswer.setText("");
            oldPwd.setText("");
            newPwd.setText("");
        }
    }

    class CheckOnShowPwdListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (showPwdCheckBox.isChecked()) {
                newPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                oldPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                newPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                oldPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        }
    }

    private class CheckPhoneExistThread extends Thread{
        private final String remoteUrl;
        public CheckPhoneExistThread(String remoteUrl){
            this.remoteUrl = remoteUrl;
        }
        @Override
        public void run() {
            String result = null;
            Message msg = new Message();
            try{
                // 根据地址创建URL对象(网络访问的url)
                URL url = new URL(remoteUrl);
                // url.openConnection()打开网络链接
                HttpURLConnection urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.setRequestMethod("GET");// 设置请求的方式
                urlConnection.setReadTimeout(5000);// 设置超时的时间
                urlConnection.setConnectTimeout(5000);// 设置链接超时的时间
                // 设置请求的头
                urlConnection
                        .setRequestProperty("User-Agent",
                                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
                // 获取响应的状态码 404 200 505 302
                if (urlConnection.getResponseCode() == 200) {
                    // 获取响应的输入流对象
                    InputStream is = urlConnection.getInputStream();

                    // 创建字节输出流对象
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    // 定义读取的长度
                    int len = 0;
                    // 定义缓冲区
                    byte buffer[] = new byte[1024];
                    // 按照缓冲区的大小，循环读取
                    while ((len = is.read(buffer)) != -1) {
                        // 根据读取的长度写入到os对象中
                        os.write(buffer, 0, len);
                    }
                    // 释放资源
                    is.close();
                    os.close();
                    // 返回字符串
                    result = new String(os.toByteArray());
                    LogUtil.d(TAG,"***************" + result
                            + "******************");
                    if("true".equals(result)){
                        msg.what = 1;
                    }else{
                        msg.what = 2;
                    }
                } else {
                    LogUtil.d(TAG, "------------------链接失败-----------------");
                }
                uiHandler.sendMessage(msg);
            } catch (IOException e) {
                Toast.makeText(context,"网络异常，请稍后再试", Toast.LENGTH_LONG).show();
            }
        }
    }
}
