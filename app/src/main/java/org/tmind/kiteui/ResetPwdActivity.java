package org.tmind.kiteui;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.tmind.kiteui.utils.DBHelper;

public class ResetPwdActivity extends AppCompatActivity {

    private static final String TAG = "Reset Password";
    private Context context;

    private EditText oldPwd;
    private EditText newPwd;
    private EditText emergencePhoneNo;

    private Button confirmBtn;
    private Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);
        context = this;

        oldPwd = (EditText)findViewById(R.id.resetpwd_edit_pwd_old);
        newPwd = (EditText)findViewById(R.id.resetpwd_edit_pwd_new);
        emergencePhoneNo = (EditText)findViewById(R.id.emergence_call_no);

        confirmBtn = (Button)findViewById(R.id.register_btn_sure);
        cancelBtn = (Button)findViewById(R.id.register_btn_cancel);

        confirmBtn.setOnClickListener(new ResetPwdActivity.ConfirmBtnClick());
        cancelBtn.setOnClickListener(new ResetPwdActivity.CancelBtn());
    }

    class ConfirmBtnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String oldPwdStr = oldPwd.getText().toString().trim();
            String newPwdStr = newPwd.getText().toString().trim();
            String emergencePhoneNoStr = emergencePhoneNo.getText().toString().trim();

            if(oldPwdStr==null || ("").equals(oldPwdStr)){
                Toast.makeText(context,"新密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if(newPwdStr==null || ("").equals(newPwdStr)){
                Toast.makeText(context,"请重复密码", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!newPwdStr.equals(oldPwdStr)){
                Toast.makeText(context,"两次密码输入错误", Toast.LENGTH_SHORT).show();
                return;
            }

            if(emergencePhoneNoStr!=null && emergencePhoneNoStr.length()!=11){
                Toast.makeText(context,"请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                return;
            }

            //
            SQLiteDatabase db = new DBHelper(context).getDbInstance();
            //insert
            String insertPwd = "update parent_control_password_table set parent_password = '"+newPwdStr+"' where password_type = 'pwd'";
            db.execSQL(insertPwd);

            if(emergencePhoneNoStr!=null && emergencePhoneNoStr.length() ==1) {
                String insertEmergencePhoneNo = "update emergence_phone_table set phone_no='"+emergencePhoneNoStr+"'";
                db.execSQL(insertEmergencePhoneNo);
            }

            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        }
    }

    class CancelBtn implements View.OnClickListener{

        //clean
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        }
    }
}
