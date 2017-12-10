package org.tmind.kiteui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tmind.kiteui.utils.DBHelper;
import org.tmind.kiteui.utils.TimeUtils;

import java.util.Date;

public class ResetPwdQuestionActivity extends AppCompatActivity {

    private Context context;
    private TextView resetPwdQuestion;
    private EditText resetPwdAnswer;

    private Button confirmBtn;
    private Button cancelBtn;

    private SQLiteDatabase db;
    private String pwdQuestion = null;
    private String pwdAnswer = null;

    private final static String passwordControlTable = "password_control_table";
    private final static int MAX_WRONG_PASSWORD_TIMES = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd_question);

        context = this;

        db =  new DBHelper(context).getDbInstance();


        Cursor cursor = db.rawQuery("select question, answer from reset_password_table",null);
        if(cursor.moveToNext()){
            pwdQuestion = cursor.getString(0);
            pwdAnswer = cursor.getString(1);
        }

        resetPwdQuestion = (TextView) findViewById(R.id.resetpwd_question);
        resetPwdQuestion.setText(pwdQuestion);

        resetPwdAnswer = (EditText)findViewById(R.id.resetpwd_answer);

        confirmBtn = (Button)findViewById(R.id.register_btn_sure);
        cancelBtn = (Button)findViewById(R.id.register_btn_cancel);

        confirmBtn.setOnClickListener(new ResetPwdQuestionActivity.ConfirmBtnClick());
        cancelBtn.setOnClickListener(new ResetPwdQuestionActivity.CancelBtn());
    }

    class ConfirmBtnClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String resetPwdAnswerStr = resetPwdAnswer.getText().toString().trim();


            if(resetPwdAnswerStr==null || ("").equals(resetPwdAnswerStr)){
                Toast.makeText(context,"密码重置问题的答案不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            //
            if(resetPwdAnswerStr.equals(pwdAnswer)){
                cleanPasswordWrongTimesTable();
                Intent intent = new Intent(context, ResetPwdActivity.class);
                startActivity(intent);
            }else{
                String[] result = getPasswordWrongTimes();
                int wrongTimes = Integer.valueOf(result[0]);
                if(result[1]!=null){
                    long wrongDateStr = Long.valueOf(result[1]);
                    if((TimeUtils.isYeaterday(new Date(wrongDateStr), new Date())!=0) && wrongTimes>=3){
                        Toast.makeText(getApplicationContext(), R.string.password_rst_wrong_over_time, Toast.LENGTH_LONG).show();
                    }
                    else{
                        int currentWrongTimes = wrongTimes+1;
                        int leftWrongTimes = MAX_WRONG_PASSWORD_TIMES-currentWrongTimes;
                        //更新数据库密码输入错误次数
                        updatePasswordWrongTimes(String.valueOf(currentWrongTimes));
                        String msg = getResources().getString(R.string.password_rst_wrong);
                        String showMsg = String.format(msg, currentWrongTimes, leftWrongTimes);
                        Toast.makeText(getApplicationContext(), showMsg,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String[] getPasswordWrongTimes(){
        String wrongTimeInDb = null;
        String wrongDate = null;
        Cursor cursor = db.rawQuery("select wrong_times, password_date_time from "+passwordControlTable+" where password_type='rst'",null);
        if(cursor.moveToFirst()){
            wrongTimeInDb = cursor.getString(0);
            wrongDate = cursor.getString(1);
        }

        return new String[]{wrongTimeInDb, wrongDate};
    }

    private void updatePasswordWrongTimes(String times){
        db.execSQL("update "+passwordControlTable+" set wrong_times='"+times+"', password_date_time='"+new Date().getTime()+"' where password_type='rst'");
    }

    private void cleanPasswordWrongTimesTable(){
        db.execSQL("update "+passwordControlTable+" set wrong_times='0', password_date_time='"+new Date().getTime()+"' where password_type='rst'");
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
