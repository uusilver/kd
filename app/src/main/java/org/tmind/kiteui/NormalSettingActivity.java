package org.tmind.kiteui;

import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

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

}
