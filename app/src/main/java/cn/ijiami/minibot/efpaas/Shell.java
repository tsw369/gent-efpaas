package cn.ijiami.minibot.efpaas;

import android.content.pm.PackageInfo;
import android.os.IBinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import cn.ijiami.minibot.agent.efpaas.BuildConfig;
import cn.ijiami.zjh.aop.api.callbacks.LoadPackage;
import dalvik.system.PathClassLoader;

public class Shell extends Entry {
    public static PackageInfo getPackageInfoWithoutContext(String packageName, int flags) throws Exception {
        Class ServiceManagerCls = Class.forName("android.os.ServiceManager");
        Method getServiceMethod = ServiceManagerCls.getDeclaredMethod("getService", String.class);
        IBinder binder = (IBinder) getServiceMethod.invoke(ServiceManagerCls, "package");

        Class StubCls = Class.forName("android.content.pm.IPackageManager$Stub");
        Method asInterfaceMethod = StubCls.getDeclaredMethod("asInterface", IBinder.class);
        Object pm = asInterfaceMethod.invoke(StubCls, binder);

        Class IPackageManagerCls = Class.forName("android.content.pm.IPackageManager");
        Method getPackageInfoMethod = IPackageManagerCls.getDeclaredMethod("getPackageInfo", String.class, int.class, int.class);
        return (PackageInfo) getPackageInfoMethod.invoke(pm, packageName, flags, 0);

    }

    @Override
    public void handleLoadPackage(LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            tryLoadNewPackage(loadPackageParam);
        } catch (Exception e) {
            e.printStackTrace();
            super.handleLoadPackage(loadPackageParam);
        }
    }

    private void tryLoadNewPackage(LoadPackage.LoadPackageParam loadPackageParam) throws Exception {
        PackageInfo packageInfo = getPackageInfoWithoutContext(BuildConfig.APPLICATION_ID, 0);
        ClassLoader newLoader = new PathClassLoader(packageInfo.applicationInfo.sourceDir, Shell.class.getClassLoader().getParent());
        Class clazz = newLoader.loadClass(Entry.class.getName());
        Constructor ctor = clazz.getConstructor();
        ctor.setAccessible(true);
        Object entry = ctor.newInstance();
        Method handleLoadPackageMethod = clazz.getDeclaredMethod("handleLoadPackage", LoadPackage.LoadPackageParam.class);
        handleLoadPackageMethod.invoke(entry, loadPackageParam);
    }
}
