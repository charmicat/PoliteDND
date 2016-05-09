package com.vag.politednd.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PDNDNotification {
	static NotificationCompat.Builder mBuilder;
	static NotificationManager nm;

	public static void notify(int id, String action, Context c) {
		mBuilder = new NotificationCompat.Builder(c);
		nm = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(id);

		Log.d("PDNDNotification", "New PDNDNotification: " + id + " - "
				+ action);

		// PendingIntent to launch activity if the user selects
		// the notification
		Intent i = new Intent(c, MainActivity.class);
		i.putExtra("NotifID", id);

		PendingIntent detailsIntent = PendingIntent.getActivity(c, 0, i, 0);

		Notification notif = mBuilder.setContentTitle("PDND Schedule")
				.setContentText(action).setSmallIcon(R.drawable.ic_launcher_bw)
				.setOnlyAlertOnce(true).setContentIntent(detailsIntent).build();

		// 100ms delay, vibrate for 250ms, pause for 100 ms and
		// then vibrate for 500ms
		notif.vibrate = new long[] { 100, 250, 100, 500 };
		notif.defaults |= Notification.DEFAULT_SOUND;
		nm.notify(id, notif);
		
	}
}
