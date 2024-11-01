package cn.ijiami.minibot.efpaas;

import static cn.ijiami.minibot.efpaas.Entry.TAG;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.lang.reflect.Method;

public class Installer extends BroadcastReceiver {

    private boolean installed = false;

    private Opener mOpener;
    private AlertsGuard mAlertsGuard;
    private ProxySupport mProxySupport;
    private AppxInjector mAppxInjector;

    public static Context getContext() {
        try {
            Class ActivityThread = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = ActivityThread.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            Application currentApplication = (Application) currentApplicationMethod.invoke(null);
            return currentApplication.getApplicationContext();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public void install(ClassLoader loader) {
        if (isInstalled()) return;
        installed = installOpener(loader)
                && installProxySupport(loader)
                && installAppxInjector(loader)
                && installAlertGuard()
                && installCommandReceiver();
    }

    private boolean installOpener(ClassLoader loader) {
        if (mOpener != null) return true;

        Class<?> MiniProgram = Helper.loadClassSafe(loader, "com.nantian.iBank.MiniProgram");
        if (MiniProgram == null) {
            Log.e(TAG, "MiniProgram == null");
            return false;
        }

        mOpener = new Opener(MiniProgram);
        Log.e(TAG, "installed efPaaS Opener");
        return true;
    }

    private boolean installCommandReceiver() {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "getContext == null");
            return false;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.OPEN);
        filter.addAction(Actions.ALERTS);
        filter.addAction(Actions.PROXY);
        context.registerReceiver(this, filter);
        Log.e(TAG, "registered command receiver.");
        return true;
    }

    private boolean installAlertGuard() {
        if (mAlertsGuard != null) return true;
        mAlertsGuard = new AlertsGuard();
        Log.e(TAG, "installed efPaaS AlertsGuard");
        return true;
    }

    private boolean installProxySupport(ClassLoader loader) {
        if (mProxySupport != null) return true;
        mProxySupport = new ProxySupport(loader);
        Log.e(TAG, "installed efPaaS ProxySupport");
        return true;
    }

    private boolean installAppxInjector(ClassLoader loader) {
        if (mAppxInjector != null) return true;
        mAppxInjector = new AppxInjector(loader);
        Log.e(TAG, "installed efPaaS AppxInjector");
        return true;
    }

    public boolean isInstalled() {
        synchronized (this) {
            return installed;
        }
    }

    void handleAlerts(Intent intent) {
        if ((intent.getIntExtra("enable", 1) == 1)) {
            mAlertsGuard.enable();
        } else {
            mAlertsGuard.disable();
        }
    }

    void handleProxy(Intent intent) {
        String proxyHost = intent.getStringExtra("host");
        int proxyPort = intent.getIntExtra("port", 0);
        Log.e(TAG, "handleProxy: " + proxyHost + ":" + proxyPort);
        if (proxyHost == null || proxyHost.isEmpty() || proxyPort <= 0) {
            mProxySupport.disable();
        } else {
            mProxySupport.setProxy(proxyHost, proxyPort);
        }
    }

    void handleOpen(Intent intent) {
        mOpener.openUrl(intent.getStringExtra("url"));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case Actions.ALERTS:
                handleAlerts(intent);
                break;
            case Actions.PROXY:
                handleProxy(intent);
                break;
            case Actions.OPEN:
                handleOpen(intent);
                break;
        }
    }
}
