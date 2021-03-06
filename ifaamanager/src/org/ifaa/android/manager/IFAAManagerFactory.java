package org.ifaa.android.manager;

import android.app.KeyguardManager;
import android.annotation.UnsupportedAppUsage;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.IHwBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import java.util.List;
import java.util.ArrayList;
import vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IFingerprintPay;

public class IFAAManagerFactory {
    public static final String TAG = "IFAAManagerFactory";
    private static IFAAManager sFAAManager;

    private static class IFAAManagerOppo extends IFAAManagerV4 {
        public static final int BIOMETRIC_NOUSE_NOSET_KEYGUARD = 1003;
        public static final int BIOMETRIC_NOUSE_NOT_ENROLLED = 1002;
        public static final int BIOMETRIC_NOUSE_SYSTEMLOCKED = 1001;
        public static final int BIOMETRIC_USE_READY = 1000;
        private Context mContext;
        private FingerprintManager mFingerprintManager = null;
        private KeyguardManager mKeyguardManager = null;
        private IFingerprintPay mFingerprintPay = null;


        static {
            try {
                System.loadLibrary("teeclientjni");
            } catch (UnsatisfiedLinkError e) {
                Log.e(IFAAManagerFactory.TAG, e.toString());
            }
        }

        IFAAManagerOppo(Context context) {
            this.mContext = context;
            ensureNeedServiceAvailable();
        }

        void ensureNeedServiceAvailable() {
            FingerprintManager fingerprintManager = this.mFingerprintManager;
            String str = IFAAManagerFactory.TAG;
            if (fingerprintManager == null) {
                this.mFingerprintManager = (FingerprintManager) this.mContext.getSystemService("fingerprint");
                if (this.mFingerprintManager == null) {
                    Log.e(str, "getIFAAManager: mFingerprintManager = null!");
                }
            }
            if (this.mKeyguardManager == null) {
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
            }
        }

        public int getSupportBIOTypes(Context context) {
           int type;
           if (Build.MODEL.equals("realme X") || Build.DEVICE.equals("RMX1901") || Build.DEVICE.equals("RMX1903") || Build.DEVICE.equals("RMX1921") || Build.DEVICE.equals("RMX1922")) {
               type = IFAAManager.AUTH_TYPE_OPTICAL_FINGERPRINT;
           } else if (Build.MODEL.equals("realme Q") || Build.DEVICE.equals("RMX1971") || Build.DEVICE.equals("RMX1972") || Build.DEVICE.equals("RMX1973") || Build.DEVICE.equals("RMX1855")) {
               type = IFAAManager.AUTH_TYPE_FINGERPRINT;
           } else {
               type = IFAAManager.AUTH_TYPE_FINGERPRINT;
          }
            return type;
        }

        public int startBIOManager(Context context, int authType) {
            if (authType != 1) {
                return -1;
            }
            try {
                Log.e(TAG, "startBIOManager" + context);
                context.startActivityAsUser(new Intent("android.settings.FINGERPRINT_SETTINGS"), UserHandle.OWNER);
                return 0;
            } catch (ActivityNotFoundException e) {
                return -1;
            }
        }

        public String getDeviceModel() {
            String model = "OPPO-Default";
            if (Build.MODEL.equals("realme X") || Build.DEVICE.equals("RMX1901") || Build.DEVICE.equals("RMX1903") || Build.DEVICE.equals("RMX1921") || Build.DEVICE.equals("RMX1922")) {
                model = "realme-R9601";
            } else if (Build.MODEL.equals("realme Q") || Build.DEVICE.equals("RMX1971") || Build.DEVICE.equals("RMX1972") || Build.DEVICE.equals("RMX1973") || Build.DEVICE.equals("RMX1855")) {
                model = "realme-R8637";
            }
            return model;
        }

        public int getVersion() {
           int version = 3;
           if (Build.MODEL.equals("realme X") || Build.DEVICE.equals("RMX1901") || Build.DEVICE.equals("RMX1903") || Build.DEVICE.equals("RMX1921") || Build.DEVICE.equals("RMX1922")) {
               version = 3;
           } else if (Build.MODEL.equals("realme Q") || Build.DEVICE.equals("RMX1971") || Build.DEVICE.equals("RMX1972") || Build.DEVICE.equals("RMX1973") || Build.DEVICE.equals("RMX1855")) {
               version = 3;
           }
            return version;
        }

        public byte[] processCmdV2(Context context, byte[] param) {
           if (mFingerprintPay == null) {
               mFingerprintPay = getAliPayService();
           }
           if (mFingerprintPay == null) {
               Log.w(IFAAManagerFactory.TAG, "alipayInvokeCommand: no FingerprintPayService!");
               return null;
           }
           byte[] receiveBuffer = null;
           try {
               ArrayList<Byte> paramByteArray = new ArrayList();
               int i = 0;
               for (byte b : param) {
                   paramByteArray.add(new Byte(b));
               }
               ArrayList<Byte> receiveBufferByteArray = mFingerprintPay.alipay_invoke_command(paramByteArray);
               receiveBuffer = new byte[receiveBufferByteArray.size()];
               while (i < receiveBufferByteArray.size()) {
                   receiveBuffer[i] = ((Byte) receiveBufferByteArray.get(i)).byteValue();
                   i++;
               }
           } catch (RemoteException e) {
               Log.e(IFAAManagerFactory.TAG, "processCmdV2 failed", e);
           }
           return receiveBuffer;
        }

        public String getExtInfo(int authType, String keyExtInfo) {
            String res = "";
            if (Build.MODEL.equals("realme X") || Build.DEVICE.equals("RMX1901") || Build.DEVICE.equals("RMX1903") || Build.DEVICE.equals("RMX1921") || Build.DEVICE.equals("RMX1922")) {
                res = "{'type': 0, 'fullView': {'startX': 442, 'startY': 1986, 'width': 196, 'height': 196, 'navConflict': false}}";
                return res;
            }
            StringBuilder stringBuilder;
            WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
            DisplayMetrics dm = new DisplayMetrics();
            if (wm != null) {
                wm.getDefaultDisplay().getMetrics(dm);
            }
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            String str = IFAAManagerFactory.TAG;
            Log.i(str, "width = " + width + " height = " + height);
            int iconDiameter = Integer.parseInt(SystemProperties.get("persist.vendor.fingerprint.optical.iconsize", "190"));
            int iconLocation = Integer.parseInt(SystemProperties.get("persist.vendor.fingerprint.optical.iconlocation", "278"));
            int coordinate_x = (width - iconDiameter) / 2;
            int coordinate_y = (height - iconLocation) - (iconDiameter / 2);
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append("iconDiameter = ");
            stringBuilder4.append(iconDiameter);
            stringBuilder4.append(" iconLocation = ");
            stringBuilder4.append(iconLocation);
            stringBuilder4.append(" coordinate_x = ");
            stringBuilder4.append(coordinate_x);
            stringBuilder4.append(" coordinate_y = ");
            stringBuilder4.append(coordinate_y);
            Log.i(str, stringBuilder4.toString());
            if (IFAAManagerV3.KEY_GET_SENSOR_LOCATION.equals(keyExtInfo)) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("{\"type\": 0,\"fullView\": {    \"startX\" : ");
                stringBuilder.append(coordinate_x);
                stringBuilder.append(",    \"startY\" : ");
                stringBuilder.append(coordinate_y);
                stringBuilder.append(",    \"width\" : ");
                stringBuilder.append(iconDiameter);
                stringBuilder.append(",    \"height\": ");
                stringBuilder.append(iconDiameter);
                stringBuilder.append(",    \"navConflict\" :false}}");
                res = stringBuilder.toString();
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append("res = ");
            stringBuilder.append(res);
            Log.i(str, stringBuilder.toString());
            return res;
        }

        public void setExtInfo(int authType, String keyExtInfo, String valExtInfo) {
        }

        public int getEnabled(int bioType) {
            ensureNeedServiceAvailable();
            boolean isKeyguardSecure = this.mKeyguardManager.isKeyguardSecure();
            String str = IFAAManagerFactory.TAG;
            if (!isKeyguardSecure) {
                Log.e(str, "Security keyguard no set");
                return BIOMETRIC_NOUSE_NOSET_KEYGUARD;
            } else if (bioType == 1) {
                return BIOMETRIC_USE_READY;
            } else if (bioType == 4) {
                Log.w(str, "Face Hardware not available!");
                return BIOMETRIC_NOUSE_SYSTEMLOCKED;
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("bioType err:");
                stringBuilder.append(bioType);
                Log.w(str, stringBuilder.toString());
                return 0;
            }
        }

        public int[] getIDList(int bioType) {
            ensureNeedServiceAvailable();
            String str = IFAAManagerFactory.TAG;
            int[] myIDList;
            int i;
            if (bioType == 4) {
                Log.w(str, "getIDList: no mFaceManager!");
                return null;
            } else if (bioType == 1) {
                if (this.mFingerprintManager == null) {
                    Log.w(str, "getIDList: no FingerprintManager!");
                    return null;
                }
                Log.w(str, "getIDList:fingerprint list!");
                List<Fingerprint> myFingerprintList = this.mFingerprintManager.getEnrolledFingerprints();
                if (!(myFingerprintList == null || myFingerprintList.size() == 0)) {
                    myIDList = new int[myFingerprintList.size()];
                    for (i = 0; i < myFingerprintList.size() - 1; i++) {
                        if (myFingerprintList.get(i) != null) {
                            myIDList[i] = ((Fingerprint) myFingerprintList.get(i)).getBiometricId();
                        }
                    }
                    return myIDList;
                }
            }
            return null;
        }

        public String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder("");
            if (src == null || src.length <= 0) {
                return null;
            }
            for (int v : src) {
                String hv = Integer.toHexString(v & 255);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            return stringBuilder.toString();
        }

        private DeathRecipient mDeathRecipient = new DeathRecipient() {
           public void serviceDied(long cookie) {
                Log.d(IFAAManagerFactory.TAG, "IFAAManagerFactory died");
                mFingerprintPay = null;
           }
        };

        public IFingerprintPay getAliPayService() {
           IFingerprintPay fingerprintPay = null;
           try {
                fingerprintPay = IFingerprintPay.getService();
                fingerprintPay.asBinder().linkToDeath(mDeathRecipient, 0);
           } catch (RemoteException e) {
                Log.e(TAG, "Failed to open fingerprintAlipayService HAL", e);
           }
           if (fingerprintPay == null) {
                Log.e(TAG, "fingerprintPay = null, Failed to open fingerprintAlipayService HAL");
           }
           return fingerprintPay;
        }
    }

    @UnsupportedAppUsage
    public static IFAAManager getIFAAManager(Context context, int authType) {
        if (authType != 1) {
            return null;
        }
        if (sFAAManager == null) {
            sFAAManager = new IFAAManagerOppo(context);
        }
        return sFAAManager;
    }
}
