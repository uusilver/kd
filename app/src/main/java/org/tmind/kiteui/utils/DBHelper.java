package org.tmind.kiteui.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vali on 12/1/2017.
 */

public class DBHelper{

    private static final String TAG = "KidUI_DB_Helper";
//    private static final String DEFAULT_DB_PATH = ;
    public static final int VERSION = 1;

    private Context context;


    //必须要有构造函数
    public DBHelper(Context context) {
        super();
        this.context = context;
    }

    public SQLiteDatabase getDbInstance(){
        return SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().getAbsoluteFile()+"/kid-ui.db3", null);
    }


}
