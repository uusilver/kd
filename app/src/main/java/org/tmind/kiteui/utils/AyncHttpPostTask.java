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

public class AyncHttpPostTask extends AsyncTask<String,Void,String> {
    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection con=null;
        InputStream is=null;
        StringBuilder adb=new StringBuilder();
        try {
            URL url=new URL(strings[0]);
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
            String params=strings[1];
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
        return adb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
//        show.setText(s);
    }
}
