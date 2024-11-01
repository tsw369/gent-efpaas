package cn.ijiami.minibot.efpaas;

import static cn.ijiami.minibot.efpaas.Entry.TAG;

import android.util.Log;

import cn.ijiami.zjh.aop.api.AopHelpers;
import cn.ijiami.zjh.aop.api.AopMethod;

public class Helper {

    public static final AopMethod AOP_NOTHING = new AopMethod() {
        @Override
        public void before(MethodAopParam param) {
            param.returnEarly = true;
        }
    };

    public static void findAndAopMethodSafe(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            Log.e(TAG, "findAndAopMethodSafe: " + methodName + ": " + AopHelpers.findAndAopMethod(clazz, methodName, parameterTypesAndCallback));
        } catch (Throwable ignored) {
        }
    }

    public static Class<?> loadClassSafe(ClassLoader loader, String name) {
        if (loader == null) loader = Helper.class.getClassLoader();
        try {
            return loader.loadClass(name);
        } catch (Throwable e) {
            return null;
        }
    }
}
