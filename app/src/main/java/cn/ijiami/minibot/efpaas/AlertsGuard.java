package cn.ijiami.minibot.efpaas;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;

import java.util.concurrent.atomic.AtomicBoolean;

import cn.ijiami.zjh.aop.api.AopHelpers;
import cn.ijiami.zjh.aop.api.AopMethod;

public class AlertsGuard extends AopMethod {
    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public AlertsGuard() {
        Class<?> ContextImpl = AopHelpers.findClass("android.app.ContextImpl", Context.class.getClassLoader());
        if (ContextImpl == null) throw new RuntimeException("ContextImpl == null");

        Helper.findAndAopMethodSafe(ContextImpl, "startActivity", Intent.class, Bundle.class, this);
        Helper.findAndAopMethodSafe(ContextImpl, "startActivityAsUser", Intent.class, Bundle.class, UserHandle.class, this);
        Helper.findAndAopMethodSafe(Activity.class, "startActivityForResultAsUser", Intent.class, String.class, int.class, Bundle.class, UserHandle.class, this);
        Helper.findAndAopMethodSafe(Activity.class, "startActivityForResult", String.class, Intent.class, int.class, Bundle.class, this);
        Helper.findAndAopMethodSafe(Instrumentation.class, "execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class, this);
    }

    public void enable() {
        enabled.set(true);
    }

    public void disable() {
        enabled.set(false);
    }

    public void before(MethodAopParam param) {
        if (!enabled.get()) return;

        Intent intent = null;
        for (Object arg : param.args) {
            if (!(arg instanceof Intent)) continue;
            intent = (Intent) arg;
            break;
        }
        if (intent == null || isAllowWhite(intent)) return;

        param.returnEarly = true;
    }

    private boolean isAllowWhite(Intent intent) {
        // only allow com.nantian.iBank.ui.activity.*
        return intent.toString().contains("com.nantian.iBank.ui.activity");
    }

}
