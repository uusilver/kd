package org.tmind.kiteui.utils;

import android.content.pm.PackageInfo;
import android.util.LruCache;

import org.tmind.kiteui.model.PackageInfoModel;

/**
 * Created by vali on 12/26/2017.
 */

public class CacheUtil {

    volatile private static CacheUtil instance = null;

    private LruCache androidLruCache = null;

    private CacheUtil(){}

    public static CacheUtil getInstance() {
        try {
            if(instance != null){//懒汉式

            }else{
                //创建实例之前可能会有一些准备性的耗时工作
                Thread.sleep(300);
                synchronized (CacheUtil.class) {
                    if(instance == null){//二次检查
                        instance = new CacheUtil();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public boolean put(String key, PackageInfoModel model){
        if(androidLruCache==null){
            initCacheObject();
        }
        androidLruCache.put(key, model);
        return true;
    }

    public boolean cleanCache(){
        androidLruCache = null;
        return true;
    }

    public PackageInfoModel get(String key){
        if(androidLruCache !=null){
            return (PackageInfoModel)androidLruCache.get(key);
        }else {
            return null;
        }
    }

    public boolean isCacheNull(){
        return androidLruCache == null ? true : false;
    }

    private void initCacheObject(){
        int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);// kB
        int cacheSize = maxMemory / 1000; // 1/1000 used
        androidLruCache = new LruCache<String,PackageInfoModel>(cacheSize);
    }


}
