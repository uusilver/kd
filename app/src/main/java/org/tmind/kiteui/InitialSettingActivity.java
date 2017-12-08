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

public class InitialSettingActivity extends AppCompatActivity {

    private final static String TAG = "InitialSettingActivity";
    private Context context;
    private EditText resetPwdQuestion;
    private EditText resetPwdAnswer;
    private EditText oldPwd;
    private EditText newPwd;
    private EditText emergencePhoneNo;

    private Button confirmBtn;
    private Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setting);
        context = this;

        resetPwdQuestion = (EditText)findViewById(R.id.resetpwd_question);
        resetPwdAnswer = (EditText)findViewById(R.id.resetpwd_answer);
        oldPwd = (EditText)findViewById(R.id.resetpwd_edit_pwd_old);
        newPwd = (EditText)findViewById(R.id.resetpwd_edit_pwd_new);
        emergencePhoneNo = (EditText)findViewById(R.id.emergence_call_no);

        confirmBtn = (Button)findViewById(R.id.register_btn_sure);
        cancelBtn = (Button)findViewById(R.id.register_btn_cancel);

        confirmBtn.setOnClickListener(new ConfirmBtnClick());
        cancelBtn.setOnClickListener(new CancelBtn());
    }

    class ConfirmBtnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String resetPwdQuestionStr = resetPwdQuestion.getText().toString().trim();
            String resetPwdAnswerStr = resetPwdAnswer.getText().toString().trim();
            String oldPwdStr = oldPwd.getText().toString().trim();
            String newPwdStr = newPwd.getText().toString().trim();
            String emergencePhoneNoStr = emergencePhoneNo.getText().toString().trim();

            if(resetPwdQuestionStr==null || ("").equals(resetPwdQuestionStr)){
                Toast.makeText(context,"密码重置问题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if(resetPwdAnswerStr==null || ("").equals(resetPwdAnswerStr)){
                Toast.makeText(context,"密码重置问题的答案不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

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

            if(emergencePhoneNoStr==null || ("").equals(emergencePhoneNoStr)){
                Toast.makeText(context,"请输入紧急联系人电话", Toast.LENGTH_SHORT).show();
                return;
            }

            if(emergencePhoneNoStr.length()!=11){
                Toast.makeText(context,"请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                return;
            }

            //
            SQLiteDatabase db = new DBHelper(context).getDbInstance();
            //insert
            String insertPwd = "INSERT INTO parent_control_password_table (parent_password, password_type) VALUES ('"+newPwdStr+"', 'pwd')";
            db.execSQL(insertPwd);

            String insertPwdQuestion = "INSERT INTO reset_password_table (question, answer)  VALUES ('"+resetPwdQuestionStr+"','"+resetPwdAnswerStr+"')";
            db.execSQL(insertPwdQuestion);

            String insertEmergencePhoneNo = "INSERT INTO emergence_phone_table (phone_no)  VALUES ('"+emergencePhoneNoStr+"')";
            db.execSQL(insertEmergencePhoneNo);

            Intent intent = new Intent(InitialSettingActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    class CancelBtn implements View.OnClickListener{

        //clean
        @Override
        public void onClick(View v) {
            resetPwdQuestion.setText("");
            resetPwdAnswer.setText("");
            oldPwd.setText("");
            newPwd.setText("");
        }
    }
}
