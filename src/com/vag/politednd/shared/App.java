package com.vag.politednd.shared;

import com.tapfortap.AppWall;
import com.tapfortap.TapForTap;
import com.vag.politednd.activity.R;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class App extends Application {

	private static Context mContext;
	private static String TAG = "App";

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		Log.d(TAG, "App onCreate");
		TapForTap.initialize(this, "37f703393d1bc1ab8521e37281255174");
		AppWall.prepare(this);
	}

	public static Context getContext() {
		return mContext;
	}

	public static boolean toggleAirplaneMode(boolean enable, boolean notify) {
		boolean status = true;
		int id = 666;

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Settings.System.RADIO_BLUETOOTH);
		stringBuilder.append(",");
		stringBuilder.append(Settings.System.RADIO_CELL);
		stringBuilder.append(",");
		stringBuilder.append("nfc"); // Settings.System.RADIO_NFC
		stringBuilder.append(",");
		stringBuilder.append(Settings.System.RADIO_WIFI);
		String defaultRadios = stringBuilder.toString();

		Log.d(TAG, "toggleAirplaneMode " + enable + " " + notify);

		boolean isAplModeOn = Settings.System.getInt(
				mContext.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;

		if (isAplModeOn)
			Settings.System.putString(mContext.getContentResolver(),
					Settings.System.AIRPLANE_MODE_RADIOS, "cell");
		else
			Settings.System.putString(mContext.getContentResolver(),
					Settings.System.AIRPLANE_MODE_RADIOS, defaultRadios);

		status = Settings.System.putInt(mContext.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, enable ? 1 : 0);

		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", isAplModeOn);
		mContext.sendBroadcast(intent);

		status = Settings.System.getInt(mContext.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) == 1;

		if (enable && !status) {
			// should be enabled but wasnt, could be lack of support
			Toast toast = Toast.makeText(mContext, mContext.getResources()
					.getString(R.string.airplaneError), Toast.LENGTH_LONG);
			toast.show();
			isAplModeOn = false;
		} else if (notify) {
			Log.d("PDNDNotification", "PDNDService wants notification: "
					+ enable + " " + id);
			PDNDNotification.notify(id, enable ? mContext.getResources()
					.getString(R.string.started) : mContext.getResources()
					.getString(R.string.stopped), mContext);
		} else {
			PDNDNotification.clear(id);
		}

		return status;
	}
}