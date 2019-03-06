package com.wdkl.callingbed.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wdkl.callingbed.ui.InitActivity;


public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {        // install
            String packageName = intent.getDataString();
            //LogUtil.i("homer", "installed :" + packageName);
        }

        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {    // uninstall
            String packageName = intent.getDataString();
            //LogUtil.i("homer", "remove :" + packageName);
        }

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {        // boot
            Intent intentStartup = new Intent(context, InitActivity.class);
            intentStartup.setAction("android.intent.action.MAIN");
            intentStartup.addCategory("android.intent.category.LAUNCHER");
            intentStartup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentStartup);
        }
    }
}