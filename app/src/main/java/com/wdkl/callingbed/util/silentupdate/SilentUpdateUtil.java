package com.wdkl.callingbed.util.silentupdate;

import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;

import com.wdkl.callingbed.util.LogUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.wdkl.callingbed.util.DownloadUtil.FILE_APK_NAME;
import static com.wdkl.callingbed.util.DownloadUtil.FILE_APK_PATH;

/**
 * <br>
 * 静默升级<br/>
 * Created by dengzhe on 2017/12/25.
 */

public class SilentUpdateUtil {

    /**
     * 升级
     *
     * @param context
     */
    public static void updateApk(Context context) {
        if (checkIsExitAPK(context)) {
            LogUtil.d("apk", "文件存在");
        } else {
            LogUtil.d("apk", "文件不存在");
            return;
        }
        try {
            LogUtil.d("apk", "-----------1----------");
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            LogUtil.d("apk", "-----------2----------");
            Method method_getService = clazz.getMethod("getService",
                    String.class);
            LogUtil.d("apk", "-----------3----------");
            IBinder bind = (IBinder) method_getService.invoke(null, "package");
            LogUtil.d("apk", "-----------4----------");
            IPackageManager iPm = IPackageManager.Stub.asInterface(bind);
            LogUtil.d("apk", "-----------5----------");
            iPm.installPackage(Uri.fromFile(apkFile), null, 2,
                    apkFile.getName());
            LogUtil.d("apk", "静默升级成功，执行重启指令");
            if (isAppInstalled(context, context.getPackageName())) {
                doRestart(context);
            } else {
                LogUtil.d("apk", "安装失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.toString();
        }
    }

    /**
     * 检查是否存在apk
     */
    private static File apkFile;

    private static boolean checkIsExitAPK(Context context) {
//        File file = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name) + ".apk");
        File file = new File(FILE_APK_PATH + "/" + FILE_APK_NAME);
        if (file.exists()) {
            apkFile = file;
            return true;
        } else {
            apkFile = null;
            return false;
        }
    }

    private static void doRestart(final Context context) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Intent mStartActivity = new Intent(context.getApplicationContext(), InitActivity.class);
//                int mPendingIntentId = 123456;
//                PendingIntent mPendingIntent = PendingIntent.getActivity(context.getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager mgr = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 7000, mPendingIntent);
//            }
//        }).start();
        CustomToast.makeText(context, "正在升级", CustomToast.LENGTH_LONG).show();
        LogUtil.d("apk", "重启指令执行完成");
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }
}
