package cn.ijiami.minibot.efpaas;

import static cn.ijiami.minibot.efpaas.Entry.TAG;
import static cn.ijiami.minibot.efpaas.Helper.AOP_NOTHING;

import android.util.Log;

import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebViewFeature;

import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.ijiami.zjh.aop.api.AopHelpers;
import cn.ijiami.zjh.aop.api.AopMethod;

public class ProxySupport {
    private String proxyHost;
    private int proxyPort;


    public ProxySupport(ClassLoader loader) {
        unOkHTTPv3(loader);
        unTrustManager();
        unTrustManagerImpl(loader);

        Class<?> NetProperties = Helper.loadClassSafe(loader, "sun.net.NetProperties");
        if (NetProperties == null) Log.e(TAG, "ProxySupport: NetProperties == null");
        else {
            AopHelpers.findAndAopMethod(NetProperties, "get", String.class, new AopMethod() {
                @Override
                public void before(MethodAopParam param) {
                    if(param.args[0].equals("proxyHost") && proxyHost != null) {
                        param.setResult(proxyHost);
                        param.returnEarly = true;
                    }
                }
            });
            AopHelpers.findAndAopMethod(NetProperties, "getInteger", String.class, int.class, new AopMethod() {
                @Override
                public void before(MethodAopParam param) {
                    if(param.args[0].equals("proxyPort") && proxyPort != 0) {
                        param.setResult(proxyPort);
                        param.returnEarly = true;
                    }
                }
            });
        }

    }

    public void setProxy(String host, int port) {
        this.proxyHost = host;
        this.proxyPort = port;
    }

    public void disable() {
        this.proxyHost = null;
        this.proxyPort = 0;
    }


    private void unOkHTTPv3(ClassLoader loader) {
        Class<?> CertificatePinner = Helper.loadClassSafe(loader, "okhttp3.CertificatePinner");
        if (CertificatePinner == null) {
            Log.e(TAG, "unOkHTTPv3: CertificatePinner == null");
            return;
        }
        Helper.findAndAopMethodSafe(CertificatePinner, "check", String.class, List.class, AOP_NOTHING);
        Helper.findAndAopMethodSafe(CertificatePinner, "check", String.class, Certificate.class, AOP_NOTHING);
        Helper.findAndAopMethodSafe(CertificatePinner, "check", String.class, Certificate[].class, AOP_NOTHING);
    }

    private void unTrustManager() {
        Helper.findAndAopMethodSafe(SSLContext.class, "init", KeyManager[].class, TrustManager[].class, SecureRandom.class, new AopMethod() {
            @Override
            public void before(MethodAopParam param) {
                param.args[1] = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};
            }
        });
    }

    private void unTrustManagerImpl(ClassLoader loader) {
        Class<?> TrustManagerImpl = Helper.loadClassSafe(loader, "com.android.org.conscrypt.TrustManagerImpl");

        if (TrustManagerImpl == null) {
            Log.e(TAG, "unTrustManagerImpl: TrustManagerImpl == null");
            return;
        }

        Helper.findAndAopMethodSafe(TrustManagerImpl, "checkTrustedRecursive", X509Certificate[].class, byte[].class, byte[].class, String.class, boolean.class, ArrayList.class, ArrayList.class, Set.class, new AopMethod() {
            @Override
            public void before(MethodAopParam param) {
                param.setResult(new ArrayList<>());
                param.returnEarly = true;
            }
        });
    }

}
