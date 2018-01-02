package org.tmind.kiteui.utils;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by vali on 1/1/2018.
 */

public class AyncHttpPostTask extends Thread {
    private static final String TAG = "AyncHttpPostTask";
    private String accessAdr;
    private String params;

    public AyncHttpPostTask(String accessAdr, String params){
        this.accessAdr = accessAdr;
        this.params = params;
    }

    @Override
    public void run(){
        HttpURLConnection con=null;
        InputStream is=null;
        StringBuilder adb=new StringBuilder();
        try {
            URL url=new URL(accessAdr);
            con= (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(5 * 1000);
            con.setRequestMethod("POST");
            //请求方法，与GET的区别
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            //是否存储数据
            con.setRequestProperty("Charset", "UTF-8");
            //Charset 字符集
            con.setRequestProperty("Content-type",
                    "application/x-www-form-urlencoded");
            //params应该是这样的样式=》option=getUserName&uName=jerehedu.没有问号？？？
            OutputStream os=con.getOutputStream();
            os.write(params.getBytes());
            os.flush();
            os.close();
            if (con.getResponseCode()==200){
                is=con.getInputStream();
                int next=0;
                byte[] b=new byte[1024];
                while ((next=is.read(b))>0){
                    adb.append(new String(b,0,next));
                }
                LogUtil.d(TAG, adb.toString());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (con!=null){
                con.disconnect();
            }
        }
    }

}
