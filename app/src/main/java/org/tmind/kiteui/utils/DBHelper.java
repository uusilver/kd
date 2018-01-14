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

    private volatile static SQLiteDatabase instance = null;

    //必须要有构造函数
    private DBHelper() {
    }

    public static SQLiteDatabase getDbInstance(Context context){
        if(instance != null){//懒汉式
        }else{
            //创建实例之前可能会有一些准备性的耗时工作
            synchronized (DBHelper.class) {
                if(instance == null){//二次检查
                    instance = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir().getAbsoluteFile()+"/kid-ui.db3", null);
                }
            }
        }
        return instance;
    }


}
