package com.vag.politednd.activity;

import com.vag.politednd.service.PDNDService;

import android.R.drawable;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity_sharedprefs extends FragmentActivity {
	SharedPreferences settings;
	Intent service;
	int howMany;
	TableLayout mainTable;

	// TODO: tocar algum audio quando habilitar/desabilitar airplane mode

	public static final String PREFS_NAME = "PDND_PREFS";

	public PDNDService s;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			s = ((PDNDService.MyBinder) binder).getService();

			Toast.makeText(MainActivity_sharedprefs.this, "Connected", Toast.LENGTH_SHORT)
					.show();
		}

		public void onServiceDisconnected(ComponentName className) {
			Toast.makeText(MainActivity_sharedprefs.this, "Disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupApp();
	}

	public void setupApp() {
		settings = getSharedPreferences(PREFS_NAME, 0);

		// TODO: remover quando estiver pronto!
		// SharedPreferences.Editor ed = settings.edit();
		// ed.clear();
		// ed.commit();

		howMany = settings.getInt("howMany", 0);

		mainTable = (TableLayout) findViewById(R.id.schedule);

		for (int i = 1; i <= howMany; i++) {
			String scheduleKey = "schedule" + i;

			String scheduleVal = settings.getString(scheduleKey, null);

			if (scheduleVal != null) {
				TableRow t = createScheduleRow(scheduleVal);
				mainTable.addView(t, new TableLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
		}

		doBindService();
	}

	public TableRow createScheduleRow(final String scheduleVal) {
		// scheduleVal = 1|00:00|-1-1:-1-1|Mo,Tu,We,Th,Fr,Sa,Su|1
		TableRow t = new TableRow(this);

		final String toks[] = scheduleVal.split("\\|");

		Log.d("PDNDActivity", "parseScheduleStr: (" + toks.length + ")"
				+ toks[0] + "|" + toks[1] + "|" + toks[2] + "|" + toks[3] + "|"
				+ toks[4]);

		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.tablerow, t);

		t.setId(Integer.parseInt(toks[0]));
		t.setBackgroundResource(drawable.list_selector_background);
		t.setClickable(true);

		final Intent intent = new Intent(this, ConfigurationActivity.class);
		t.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("PDNDActivity",
						"Row clicked: " + Integer.parseInt(toks[0]));
				intent.putExtra("scheduleVal", scheduleVal);
				startActivity(intent);
				doUnbindService();
			}
		});

		String timeRangeStr = toks[1] + " until ";
		timeRangeStr += toks[2].charAt(0) == '-' ? "disabled" : toks[2];

		TextView timeRangeView = (TextView) t.findViewById(R.id.hourRange);
		timeRangeView.setText(timeRangeStr);

		String weekDaysRangeStr = "";

		weekDaysRangeStr = toks[3].length() == 20 ? "Daily" : toks[3];

		TextView weekDaysRangeView = (TextView) t
				.findViewById(R.id.weekDayRange);
		weekDaysRangeView.setText(weekDaysRangeStr);

		ToggleButton enabled = (ToggleButton) t.findViewById(R.id.enabled);
		enabled.setChecked(Integer.parseInt(toks[4]) == 1 ? true : false);

		ImageButton delete = (ImageButton) t.findViewById(R.id.delete);
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = ((TableRow) v.getParent().getParent()).getId();

				SharedPreferences.Editor ed = settings.edit();
				ed.putInt("howMany", --howMany);
				ed.remove("schedule" + id);
				ed.commit();

				doUnbindService();

				Log.d("PDNDActivity", "Delete button clicked " + id);
			}
		});

		return t;
	}

	public void onAddButtonClicked(View view) {
		Intent intent = new Intent(this, ConfigurationActivity.class);
		startActivity(intent);
		doUnbindService();
	}

	void doBindService() {
		service = new Intent(this, PDNDService.class);
		this.startService(service);
		bindService(new Intent(this, PDNDService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		Log.i("PDNDActivity", "Activity binded to service");
	}

	void doUnbindService() {
		unbindService(mConnection);
		stopService(service);
		Log.i("PDNDActivity", "Service disabled");
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
