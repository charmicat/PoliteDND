package com.vag.politednd.receiver;

import com.vag.politednd.service.PDNDService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, PDNDService.class);
		context.startService(i);
	}
}