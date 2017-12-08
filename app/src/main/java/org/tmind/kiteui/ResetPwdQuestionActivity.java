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

public class ResetPwdQuestionActivity extends AppCompatActivity {

    private Context context;
    private TextView resetPwdQuestion;
    private EditText resetPwdAnswer;

    private Button confirmBtn;
    private Button cancelBtn;

    private SQLiteDatabase db;
    private String pwdQuestion = null;
    private String pwdAnswer = null;

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
                Intent intent = new Intent(context, ResetPwdActivity.class);
                startActivity(intent);
            }
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
