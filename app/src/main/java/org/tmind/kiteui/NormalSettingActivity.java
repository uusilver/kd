package org.tmind.kiteui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.tmind.kiteui.fragment.NormalSettingFragment;

public class NormalSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager()
                              .beginTransaction()
                               .replace(R.id.activity_normal_setting,new NormalSettingFragment())
                               .commit();

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

}
