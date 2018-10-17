package com.dyb.h5test;

import android.app.Application;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;

/**
 * 自定义application
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initX5Envir();
    }

    private void initX5Envir() {
        QbSdk.initX5Environment(getApplicationContext(), new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {

            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.d("MyApplication", "onViewInitFinished is " + String.valueOf(b));
            }
        });
    }
}
