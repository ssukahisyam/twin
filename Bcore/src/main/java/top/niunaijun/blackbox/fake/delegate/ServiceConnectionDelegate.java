package top.niunaijun.blackbox.fake.delegate;

import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import black.android.app.BRIServiceConnectionO;
import top.niunaijun.blackbox.utils.compat.BuildCompat;


public class ServiceConnectionDelegate extends IServiceConnection.Stub {
    private static final Map<IBinder, ServiceConnectionDelegate> sServiceConnectDelegate = new HashMap<>();
    private final IServiceConnection mConn;
    private final ComponentName mComponentName;
    private final boolean mWrapGmsBroker;

    private ServiceConnectionDelegate(IServiceConnection mConn, ComponentName targetComponent, boolean wrapGmsBroker) {
        this.mConn = mConn;
        this.mComponentName = targetComponent;
        this.mWrapGmsBroker = wrapGmsBroker;
    }

    public static ServiceConnectionDelegate getDelegate(IBinder iBinder) {
        return sServiceConnectDelegate.get(iBinder);
    }

    public static IServiceConnection createProxy(IServiceConnection base, Intent intent) {
        return createProxyInternal(base, intent != null ? intent.getComponent() : null, false);
    }

    public static IServiceConnection createHostGmsProxy(IServiceConnection base, ComponentName component) {
        return createProxyInternal(base, component, true);
    }

    private static IServiceConnection createProxyInternal(IServiceConnection base, ComponentName component, boolean wrapGmsBroker) {
        final IBinder iBinder = base.asBinder();
        ServiceConnectionDelegate delegate = sServiceConnectDelegate.get(iBinder);
        if (delegate == null) {
            try {
                iBinder.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        sServiceConnectDelegate.remove(iBinder);
                        iBinder.unlinkToDeath(this, 0);
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            delegate = new ServiceConnectionDelegate(base, component, wrapGmsBroker);
            sServiceConnectDelegate.put(iBinder, delegate);
        }
        return delegate;
    }

    private IBinder maybeWrapService(ComponentName name, IBinder service) {
        if (service == null) {
            return null;
        }
        if (mWrapGmsBroker || (name != null && "com.google.android.gms".equals(name.getPackageName()))) {
            return GmsBrokerBinderProxy.wrapIfNeeded(service);
        }
        return service;
    }

    @Override
    public void connected(ComponentName name, IBinder service) throws RemoteException {
        connected(name, service, false);
    }

    public void connected(ComponentName name, IBinder service, boolean dead) throws RemoteException {
        IBinder wrapped = maybeWrapService(name, service);
        ComponentName target = mComponentName != null ? mComponentName : name;
        if (BuildCompat.isOreo()) {
            BRIServiceConnectionO.get(mConn).connected(target, wrapped, dead);
        } else {
            mConn.connected(target, wrapped);
        }
    }

    // Android 16+ signature (added to framework after this project was originally compiled).
    // Intentionally NOT annotated with @Override to keep compatibility with older compile SDKs.
    public void connected(ComponentName name, IBinder service, android.app.IBinderSession session, boolean dead)
            throws RemoteException {
        IBinder wrapped = maybeWrapService(name, service);
        try {
            Method method = mConn.getClass().getMethod(
                    "connected",
                    ComponentName.class,
                    IBinder.class,
                    android.app.IBinderSession.class,
                    boolean.class
            );
            method.invoke(mConn, mComponentName != null ? mComponentName : name, wrapped, session, dead);
            return;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RemoteException) {
                throw (RemoteException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
        } catch (Throwable ignored) {
            // fall through
        }

        connected(name, service, dead);
    }
}
