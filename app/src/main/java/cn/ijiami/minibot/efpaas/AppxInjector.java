package cn.ijiami.minibot.efpaas;

import static cn.ijiami.minibot.efpaas.Entry.TAG;

import android.util.Log;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import cn.ijiami.zjh.aop.api.AopMethod;

public class AppxInjector {

    public AppxInjector(ClassLoader loader) {
//        hookWebViewClient(loader);
        hookV8Worker(loader);
    }

    // 逻辑层 JS
    private void hookV8Worker(ClassLoader loader) {
        Class<?> V8Worker = Helper.loadClassSafe(loader, "com.alibaba.ariver.v8worker.V8Worker");
        if (V8Worker == null) {
            Log.e(TAG, "hookV8Worker: V8Worker == null");
            return;
        }
        Helper.findAndAopMethodSafe(V8Worker, "getAppxWorkerJS", new AopMethod() {
            @Override
            public void after(MethodAopParam param) {

                if (param.getResult() == null) return;
                String script = (String) param.getResult();
                script = "var tmps = \"\"\n" +
                        "window.addEventListener('message', (event) => tmps += ', ' + JSON.stringify(event)); \n" + script;

//                script = script + "\n" + "var tmps = \"\"";
//                script = script + "\n" + "for(var key in window) {tmps += \",\" + key}";
//                script = script + "\n" + "setTimeout(() => window.tmps = tmps, 1000);";
                script = script + "\n" + "console.log(exports)";
//                script = script + "\n" + "console.log(global)";
                param.setResult(script);
            }
        });
    }


    // 渲染层 JS
    private void hookWebViewClient(ClassLoader loader) {
        Class<?> UCWebViewClientWrapper = Helper.loadClassSafe(loader, "com.efpaas.mriver.uc.webview.UCWebViewClientWrapper");
        if (UCWebViewClientWrapper == null) {
            Log.e(TAG, "hookWebViewClient: UCWebViewClientWrapper == null");
            return;
        }

        Class<?> UCWebResourceResponse = Helper.loadClassSafe(loader, "com.uc.webview.export.WebResourceResponse");
        if (UCWebResourceResponse == null) {
            Log.e(TAG, "hookWebViewClient: UCWebResourceResponse == null");
            return;
        }

        Helper.findAndAopMethodSafe(UCWebViewClientWrapper, "handleShouldInterceptRequest", WebResourceResponse.class, String.class, new AopMethod() {
            @Override
            public void after(MethodAopParam param) {
                if (param.args.length < 2 || param.args[0] == null || param.args[1] == null) return;

                Object response = param.getResult();
                if (response == null) return;

                String url = (String) param.args[1];
                if (!url.startsWith("https://appx/af-appx.min.js")) return;

                InputStream inputStream = WebResourceResponse_getData(UCWebResourceResponse, response);

                String script = InputStreamReadString(inputStream);
                if (script == null) return;

                script = script + "\n" + "global.helloappx=1;";
                WebResourceResponse_setData(UCWebResourceResponse, response, new ByteArrayInputStream(script.getBytes()));
            }
        });
    }

    private String InputStreamReadString(InputStream inputStream) {
        if (inputStream == null) return null;

        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            return new String(bytes);
        } catch (IOException e) {
            Log.e(TAG, "InputStreamReadString: excepted at " + e);
        }
        return null;
    }

    private InputStream WebResourceResponse_getData(Class<?> UCWebResourceResponse, Object response) {
        try {
            Method getUrl_Method = UCWebResourceResponse.getDeclaredMethod("getData");
            return (InputStream) getUrl_Method.invoke(response);
        } catch (Throwable e) {
            Log.e(TAG, "WebResourceRequest_getData: excepted at " + e);
            return null;
        }
    }

    private void WebResourceResponse_setData(Class<?> UCWebResourceResponse, Object response, InputStream data) {
        try {
            Method getData_Method = UCWebResourceResponse.getDeclaredMethod("setData", InputStream.class);
            getData_Method.invoke(response, data);
        } catch (Throwable e) {
            Log.e(TAG, "WebResourceRequest_setData: excepted at " + e);
        }
    }
}
