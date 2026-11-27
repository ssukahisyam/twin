package top.niunaijun.blackbox.fake.delegate;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.FileDescriptor;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.Slog;

/**
 * Wraps a host IGmsServiceBroker IBinder so GetServiceRequest.callingPackage (SafeParcelable
 * field 4) matches the host UID while preserving all Binder tokens via Parcel.appendFrom.
 */
public final class GmsBrokerBinderProxy implements IBinder {
    private static final String TAG = "GmsBrokerBinderProxy";
    private static final String GMS_BROKER_DESCRIPTOR =
            "com.google.android.gms.common.internal.IGmsServiceBroker";
    private static final int TRANSACTION_GET_SERVICE = 46;
    private static final int SAFE_PARCEL_MAGIC = 0x4F45;
    private static final int FIELD_CALLING_PACKAGE = 4;

    private final IBinder mBase;

    private GmsBrokerBinderProxy(IBinder base) {
        this.mBase = base;
    }

    public static IBinder wrapIfNeeded(IBinder binder) {
        if (binder == null || binder instanceof GmsBrokerBinderProxy) {
            return binder;
        }
        try {
            String desc = binder.getInterfaceDescriptor();
            if (desc != null && !GMS_BROKER_DESCRIPTOR.equals(desc)) {
                return binder;
            }
        } catch (Throwable ignored) {
        }
        return new GmsBrokerBinderProxy(binder);
    }

    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return mBase.getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        return mBase.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return mBase.isBinderAlive();
    }

    @Override
    public IInterface queryLocalInterface(String descriptor) {
        return null;
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        mBase.dump(fd, args);
    }

    @Override
    public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
        mBase.dumpAsync(fd, args);
    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == TRANSACTION_GET_SERVICE && data != null) {
            Parcel patched = tryRewriteGetServiceRequest(data);
            if (patched != null) {
                try {
                    return mBase.transact(code, patched, reply, flags);
                } finally {
                    patched.recycle();
                }
            }
        }
        return mBase.transact(code, data, reply, flags);
    }

    private Parcel tryRewriteGetServiceRequest(Parcel data) {
        int origPos = data.dataPosition();
        Parcel newObj = null;
        try {
            String hostPkg = BlackBoxCore.getHostPkg();
            if (hostPkg == null || hostPkg.isEmpty()) {
                return null;
            }

            data.setDataPosition(0);
            data.enforceInterface(GMS_BROKER_DESCRIPTOR);
            data.readStrongBinder(); // IGmsCallbacks
            int hasRequest = data.readInt();
            if (hasRequest != 1) {
                return null;
            }

            int reqStartPos = data.dataPosition();
            int objHeader = data.readInt();
            if ((objHeader & 0xFFFF) != SAFE_PARCEL_MAGIC) {
                return null;
            }
            int objByteSize = objHeader >>> 16;
            if (objByteSize == 0xFFFF) {
                objByteSize = data.readInt();
            }
            int objContentStart = data.dataPosition();
            int objEndPos = objContentStart + objByteSize;
            if (objByteSize <= 0 || (objByteSize & 3) != 0 || objEndPos > data.dataSize()) {
                return null;
            }

            while (data.dataPosition() < objEndPos) {
                int fieldStart = data.dataPosition();
                int fieldHeader = data.readInt();
                int fieldId = fieldHeader & 0xFFFF;
                int fieldSize = fieldHeader >>> 16;
                if (fieldSize == 0xFFFF) {
                    fieldSize = data.readInt();
                }
                int valStart = data.dataPosition();
                int fieldEnd = valStart + fieldSize;
                if (fieldSize < 0 || (fieldSize & 3) != 0 || fieldEnd > objEndPos) {
                    return null;
                }

                if (fieldId == FIELD_CALLING_PACKAGE) {
                    String callingPkg = data.readString();
                    if (callingPkg == null
                            || callingPkg.equals(hostPkg)
                            || "com.google.android.gms".equals(callingPkg)) {
                        return null;
                    }

                    newObj = Parcel.obtain();
                    newObj.writeInt(SAFE_PARCEL_MAGIC | (0xFFFF << 16));
                    newObj.writeInt(0);
                    int newObjContentStart = newObj.dataPosition();

                    if (fieldStart > objContentStart) {
                        newObj.appendFrom(data, objContentStart, fieldStart - objContentStart);
                    }

                    newObj.writeInt(FIELD_CALLING_PACKAGE | (0xFFFF << 16));
                    int fSizePos = newObj.dataPosition();
                    newObj.writeInt(0);
                    int fValStart = newObj.dataPosition();
                    newObj.writeString(hostPkg);
                    int fValEnd = newObj.dataPosition();
                    newObj.setDataPosition(fSizePos);
                    newObj.writeInt(fValEnd - fValStart);
                    newObj.setDataPosition(fValEnd);

                    if (objEndPos > fieldEnd) {
                        newObj.appendFrom(data, fieldEnd, objEndPos - fieldEnd);
                    }

                    int newObjEnd = newObj.dataPosition();
                    newObj.setDataPosition(4);
                    newObj.writeInt(newObjEnd - newObjContentStart);

                    Parcel patched = Parcel.obtain();
                    patched.appendFrom(data, 0, reqStartPos);
                    patched.appendFrom(newObj, 0, newObjEnd);
                    if (data.dataSize() > objEndPos) {
                        patched.appendFrom(data, objEndPos, data.dataSize() - objEndPos);
                    }
                    Slog.d(TAG, "Rewrote GMS GetServiceRequest callingPackage: "
                            + callingPkg + " -> " + hostPkg);
                    return patched;
                }

                data.setDataPosition(fieldEnd);
            }
        } catch (Throwable t) {
            Slog.w(TAG, "Skipping GMS GetServiceRequest rewrite: " + t.getMessage());
        } finally {
            if (newObj != null) {
                newObj.recycle();
            }
            try {
                data.setDataPosition(origPos);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
        mBase.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return mBase.unlinkToDeath(recipient, flags);
    }
}
