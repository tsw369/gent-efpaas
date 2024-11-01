package cn.ijiami.minibot.efpaas;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;

import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebViewFeature;

import java.io.File;

import cn.ijiami.zjh.aop.api.AopHelpers;
import cn.ijiami.zjh.aop.api.AopLoadPackage;
import cn.ijiami.zjh.aop.api.AopMethod;
import cn.ijiami.zjh.aop.api.callbacks.LoadPackage;

public class Entry implements AopLoadPackage {

    public static final String TAG = "minibot-efpaas";

    private static final Installer INSTALLER = new Installer();

    @Override
    public void handleLoadPackage(LoadPackage.LoadPackageParam loadPackageParam) {
        Log.e(TAG, "handleLoadPackage: " + loadPackageParam.processName);
        if (!isEnabled(loadPackageParam.packageName)) return;


        new Handler(Looper.getMainLooper()).postDelayed(() -> WebView.setWebContentsDebuggingEnabled(true), 1000);

        AopHelpers.findAndAopMethod(WebView.class, "setWebContentsDebuggingEnabled", boolean.class, new AopMethod() {
            @Override
            public void before(MethodAopParam param) throws Throwable {
                param.args[0] = true;
            }
        });

        AopHelpers.findAndAopMethod(Activity.class, "onCreate", Bundle.class, new AopMethod() {
            @Override
            public void before(MethodAopParam param) {
                if (INSTALLER.isInstalled()) return;
                INSTALLER.install(param.thisObject.getClass().getClassLoader());
                if (INSTALLER.isInstalled()) {
                    Log.e(TAG, "Install on " + param.thisObject.getClass().getClassLoader());
                }
            }
        });

    }


    private boolean isEnabled(String packageName) {
        return new File("/sdcard/ijm_sandbox/minibot_efpaas/" + packageName).exists();
    }
}
