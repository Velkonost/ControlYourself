package com.velkonost.controlyourself;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Velkonost
 */

public class AlarmSetter extends BroadcastReceiver {

    public AlarmSetter() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentNotification = new Intent(context, BackgroundAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 3000, pendingIntent);
    }
}

