package com.dyb.h5test;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class LikeYsActivity extends AppCompatActivity {

    private X5WebView mWebView;

    private static final String TAG = "LikeYsActivity";

    private long exitTime;

    private AudioManager audio;

    private static String[] PERMISSION = {Manifest.permission.READ_PHONE_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_ys);
        if (islacksOfPermission(PERMISSION[0])) {
            ActivityCompat.requestPermissions(this, PERMISSION, 0x12);
        } else {
            initWebView();
            audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0x12) {
            initWebView();
            audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        } else {
            finish();
        }
    }

    private boolean islacksOfPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_DENIED;
        }
        return false;
    }

    private void initWebView() {
        mWebView = (X5WebView) findViewById(R.id.web_view);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setDatabasePath(getDir("databases", MODE_PRIVATE).getPath());
        webSettings.setGeolocationDatabasePath(getDir("geolocation", MODE_PRIVATE).getPath());
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAppCacheMaxSize(Long.MAX_VALUE);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        CookieSyncManager.createInstance(getApplicationContext());
        CookieSyncManager.getInstance().sync();

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                Log.d(TAG, "onHideCustomView");
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // :( Parsing error. Please contact me.
                Log.d(TAG, url);
                if (url.contains("weixin://wap/pay?")) {
                    Log.e(TAG, "wxPay and aliPay");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                if (url.contains("platformapi/startapp")) {
                    Log.e(TAG, "just aliPay");
                    startAlipayActivity(url);
                    return true;
                }
                if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                        && (url.contains("platformapi") && url.contains("startapp"))) {
                    Log.e(TAG, "just aliPay");
                    startAlipayActivity(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        mWebView.loadUrl(getString(R.string.app_url));

    }

    // 调起支付宝并跳转到指定页面
    private void startAlipayActivity(String url) {
        Intent intent;
        try {
            intent = Intent.parseUri(url,
                    Intent.URI_INTENT_SCHEME);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.loadUrl("javascript:window.pauseAudio();");
            Log.d(TAG, "onpause...");
            mWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mWebView != null) {
            mWebView.onResume();
            mWebView.loadUrl("javascript:window.resumeAudio();");
            Log.d(TAG, "onresume...");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
        }
    }

    /**
     * 按键响应，在WebView中查看网页时，按返回键的时候按浏览历史退回,如果不做此项处理则整个WebView返回退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown=======");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - exitTime > 1000) {
                Toast.makeText(this, R.string.click_exit, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
                return true;
            }
            finish();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
