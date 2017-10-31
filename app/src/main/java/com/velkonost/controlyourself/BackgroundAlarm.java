package com.velkonost.controlyourself;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * @author Velkonost
 */

public class BackgroundAlarm extends BroadcastReceiver {

    private Context mContext;
    private DBHelper mDBHelper;

    private ArrayList<String> packages;
    private ArrayList<Long> wasteTime;
    private ArrayList<Long> maxTime;

    public BackgroundAlarm() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        packages = new ArrayList<>();
        wasteTime = new ArrayList<>();
        maxTime = new ArrayList<>();

        mDBHelper = new DBHelper(context);
        Cursor cursor = mDBHelper.getAllApplications();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                packages.add(cursor.getString(cursor.getColumnIndex("application")));
                wasteTime.add(cursor.getLong(cursor.getColumnIndex("waste_time")));
                maxTime.add(cursor.getLong(cursor.getColumnIndex("max_time")));

                cursor.moveToNext();
            }
        }

        test();
    }

    private void test() {
        Log.i(TAG, "method called");
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
        int processId = 0;
        for (int i = 0; i < pids.size(); i++) {
            ActivityManager.RunningAppProcessInfo info = pids.get(i);
            if (packages.contains(info.processName) && (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)) {
                int applicationIndex = packages.indexOf(info.processName);
                long applicationMaxTime = maxTime.get(applicationIndex);
                long applicationWasteTime = wasteTime.get(applicationIndex);

                if (applicationWasteTime > applicationMaxTime) {
                    processId = info.pid;
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(startMain);

                    am.killBackgroundProcesses(info.processName);
                    android.os.Process.sendSignal(processId, android.os.Process.SIGNAL_KILL);
                } else {
                    mDBHelper.updateApplicationByPackageName(info.processName);
                }
            }
        }
    }
}
