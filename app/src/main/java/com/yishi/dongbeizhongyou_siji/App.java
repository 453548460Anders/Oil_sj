package com.yishi.dongbeizhongyou_siji;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;


import com.yishi.dongbeizhongyou_siji.tools.TagAliasOperatorHelper;

import cn.jpush.android.api.JPushInterface;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        String regId = JPushInterface.getRegistrationID(this);
        Log.i("RegistrationID",regId);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }



}
