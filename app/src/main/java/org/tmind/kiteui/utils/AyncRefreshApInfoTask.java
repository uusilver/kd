package org.tmind.kiteui.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by vali on 1/7/2018.
 */

public class AyncRefreshApInfoTask extends Thread {

    private static final String TAG = "AyncRefreshApInfoTask";
    private String accessAdr;
    private SQLiteDatabase db;

    public AyncRefreshApInfoTask(String accessAdr,  SQLiteDatabase db){
        this.accessAdr = accessAdr;
        this.db = db;
    }

    @Override
    public synchronized void run() {
            try {
                // 根据地址创建URL对象(网络访问的url)
                URL url = new URL(accessAdr);
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
                    String result = new String(os.toByteArray());
                    if (result.length() > 15) {
                        //成功返回请求
                        String targetPhoneNo = result.split("\\|")[0];
                        String[] appInfoList = result.split("\\|")[1].split("@");
                        for(String str : appInfoList){
                            String[] appInfoStr = str.split("\\$");
                            String pkg = appInfoStr[1];
                            String userFlag = (appInfoStr[2]);
                            String startTimeHour = (appInfoStr[3]);
                            String startTimeMinute = (appInfoStr[4]);
                            String endTimeHour = (appInfoStr[5]);
                            String endTimeMinute = (appInfoStr[6]);
                            db.execSQL("update application_control_table set use_flag=?, start_time_hour=?, start_time_minute=?, end_time_hour=?, end_time_minute=? where pkg=?", new String[]{userFlag, startTimeHour, startTimeMinute, endTimeHour, endTimeMinute, pkg});
                            LogUtil.d(TAG, "*************** refresh app info successfully ******************");
                        }
                        CacheUtil.getInstance().cleanCache();
                    }else {
                        LogUtil.d(TAG, "*************** no need to refresh ******************");
                    }

                } else {
                    LogUtil.d(TAG, "------------------链接失败-----------------");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

}
