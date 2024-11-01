package cn.ijiami.minibot.efpaas;

import static cn.ijiami.minibot.efpaas.Entry.TAG;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;

import cn.ijiami.zjh.aop.api.AopHelpers;
import cn.ijiami.zjh.aop.api.AopMethod;

public class Opener extends AopMethod {

    private final Class<?> MiniProgram_Class;

    public Opener(Class<?> MiniProgram) {
        if (MiniProgram == null) {
            throw new RuntimeException("MiniProgram_Class == null");
        }
        MiniProgram_Class = MiniProgram;

        AopHelpers.findAndAopMethod(MiniProgram_Class, "start", Context.class, HashMap.class, new AopMethod() {
            @Override
            public void before(MethodAopParam param) {
                Log.e(TAG, "start: " + param.args[1]);
            }
        });


    }

    public boolean openUrl(String url) {
        Uri parse = Uri.parse(url);

        HashMap<String, String> bundle = new HashMap<>();
        for (String key : parse.getQueryParameterNames()) {
            bundle.put(key, parse.getQueryParameter(key));
        }

        startMiniProgram(bundle);

        return true;
    }

    private void startMiniProgram(HashMap<String, String> params) {
        if (MiniProgram_Class == null)
            throw new RuntimeException("MiniProgram_Class Class == null");

        try {
            Method startApp_Method = MiniProgram_Class.getDeclaredMethod("start", Context.class, HashMap.class);
            startApp_Method.invoke(MiniProgram_Class, Installer.getContext(), params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private void runOnMain(Runnable runnable) {
        if (runnable != null) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                runnable.run();
            } else {
                new Handler(Looper.getMainLooper()).post(runnable);
            }
        }
    }


}
