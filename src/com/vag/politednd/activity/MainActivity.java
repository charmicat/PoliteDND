package com.vag.politednd.activity;

import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import com.tapfortap.AppWall;
import com.tapfortap.TapForTap;
import com.vag.politednd.dao.SQLiteAdapter;
import com.vag.politednd.service.PDNDService;
import com.vag.politednd.shared.App;
import com.vag.politednd.shared.PDNDConstants.WeekdaysEnum;
import com.vag.politednd.view.ConfigurationFragment;
import com.vag.politednd.view.TextPopupFragment;

import android.R.drawable;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends FragmentActivity implements Observer {
	Intent service, mainIntent;
	TableLayout mainTable;
	boolean isBind, isServiceEnabled;

	private SQLiteAdapter sqlAdapter;
	SharedPreferences settings;

	public PDNDService s;

	String TAG = "MainActivity";
	public static final String PREFS_NAME = "PDNDPrefs";

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "onServiceConnected");
			s = ((PDNDService.MyBinder) binder).getService();
			s.setSqlAdapter(sqlAdapter);
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected");
		}
	};

	private ConfigurationFragment.OnCfgSetListener cfgSetListener = new ConfigurationFragment.OnCfgSetListener() {
		@Override
		public void onCfgSet(int id) {
			refreshView();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.setTitleColor(getResources().getColor(R.color.white));

		Log.i(TAG, "Activity started");
		setupApp();
	}

	public void setupApp() {
		sqlAdapter = new SQLiteAdapter(this);
		sqlAdapter.addObserver(this);

		ToggleButton toggleService = (ToggleButton) findViewById(R.id.toggleService);

		isBind = false;

		service = new Intent(this, PDNDService.class);

		settings = getSharedPreferences(PREFS_NAME, 0);
		isServiceEnabled = settings.getBoolean("enabled", false);

		fillTableView();
		if (isServiceEnabled)
			doBindService();

		toggleService.setChecked(isServiceEnabled);
		toggleService.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			SharedPreferences.Editor editor = settings.edit();

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				editor.putBoolean("enabled", isChecked);
				editor.commit();

				if (isChecked) {
					doBindService();
				} else {
					doUnbindService();
					doStopService();
				}
			}
		});
	}

	public void fillTableView() {
		// "id", "ini", "end", "weekdays" ([0-6,-]),"enabled"
		sqlAdapter.openToRead();

		mainTable = (TableLayout) findViewById(R.id.schedule);

		Cursor c = sqlAdapter.queueAll();
		for (c.moveToFirst(); !(c.isAfterLast()); c.moveToNext()) {
			TableRow t = new TableRow(this);

			int id = c.getInt(c.getColumnIndex("id"));

			LayoutInflater layoutInflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layoutInflater.inflate(R.layout.tablerow, t);

			t.setId(id);
			t.setBackgroundResource(drawable.list_selector_background);
			t.setClickable(true);

			t.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showConfigDialog(v.getId());
				}
			});

			String timeRangeStr = c.getString(c.getColumnIndex("ini")) + " "
					+ getResources().getString(R.string.until) + " ";

			timeRangeStr += c.getString(c.getColumnIndex("end")).charAt(0) == '-' ? getResources()
					.getString(R.string.disabled) : c.getString(c
					.getColumnIndex("end"));

			TextView timeRangeView = (TextView) t.findViewById(R.id.hourRange);
			timeRangeView.setText(timeRangeStr);

			String weekDaysRangeStr = fromInttoStr(c.getString(c
					.getColumnIndex("weekdays")));

			TextView weekDaysRangeView = (TextView) t
					.findViewById(R.id.weekDayRange);
			weekDaysRangeView.setText(weekDaysRangeStr);

			ToggleButton toggleSchedule = (ToggleButton) t
					.findViewById(R.id.toggleSchedule);
			toggleSchedule
					.setChecked(c.getInt(c.getColumnIndex("enabled")) == 1 ? true
							: false);

			toggleSchedule
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {

							int id = ((TableRow) buttonView.getParent()
									.getParent().getParent().getParent())
									.getId();

							sqlAdapter.openToWrite();
							ContentValues cv = new ContentValues();
							cv.put(SQLiteAdapter.ENABLED_COL, isChecked ? 1 : 0);
							sqlAdapter.update(id, cv);

							sqlAdapter.close();
							s.toggleSchedule(id, isChecked, true);

							Log.d(TAG, "Schedule toggled " + id + " "
									+ isChecked);
							sqlAdapter.close();
						}
					});

			ImageButton delete = (ImageButton) t.findViewById(R.id.delete);
			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					int id = ((TableRow) v.getParent().getParent().getParent()
							.getParent()).getId();

					sqlAdapter.openToWrite();
					sqlAdapter.delete("id=" + id);
					s.removeSchedule(id);

					refreshView();

					Log.d(TAG, "Delete button clicked " + id);
					sqlAdapter.close();
				}
			});

			mainTable.addView(t, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}

		c.close();
		sqlAdapter.close();
	}

	public String fromInttoStr(String numList) {
		String toks[] = numList.split(",");
		String weekdaysStr = "";

		if (numList.length() == 13) {
			weekdaysStr = getResources().getString(R.string.daily);
		} else if (numList.charAt(0) == '-') { // "No repeat"
			weekdaysStr = getResources().getString(R.string.noRepeat);
		} else if (numList.length() == 3
				&& ((WeekdaysEnum.getWeekday(Integer.parseInt(toks[0])) == WeekdaysEnum.SATURDAY || WeekdaysEnum
						.getWeekday(Integer.parseInt(toks[0])) == WeekdaysEnum.SUNDAY) && (WeekdaysEnum
						.getWeekday(Integer.parseInt(toks[1])) == WeekdaysEnum.SUNDAY || WeekdaysEnum
						.getWeekday(Integer.parseInt(toks[1])) == WeekdaysEnum.SATURDAY))) {
			weekdaysStr = getResources().getString(R.string.weekends);
		} else {
			for (String s : toks) {
				weekdaysStr += WeekdaysEnum.getWeekday(Integer.parseInt(s))
						+ ", ";
			}
		}

		weekdaysStr = weekdaysStr.replaceFirst(", $", "");

		return weekdaysStr;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		Log.i(TAG, "SQLiteAdapter changed");
		refreshView();
	}

	public void showConfigDialog(int id) {
		FragmentManager fm = getSupportFragmentManager();
		ConfigurationFragment cfgFrag = ConfigurationFragment.newInstance(id,
				this, sqlAdapter);
		cfgFrag.setOnCfgSetListener(cfgSetListener);
		cfgFrag.show(fm, "fragment_cfg_editor");
	}

	public void onAddButtonClicked(View view) {
		showConfigDialog(-1);
	}

	void doBindService() {
		Log.d(TAG, "doBindService");
		if (!isBind) {
			startService(service);
			bindService(service, mConnection, 0);
			isBind = true;
			Log.i(TAG, "PDNDService started. Activity bound to service");
		}
	}

	void doUnbindService() {
		Log.d(TAG, "doUnbindService");
		if (isBind) {
			if (s != null) {
				App.toggleAirplaneMode(false, false);
			}
			unbindService(mConnection);
			isBind = false;
			Log.i(TAG, "Activity unbound to service");
		}
	}

	void doStopService() {
		Log.d(TAG, "doStopService");
		if (stopService(service))
			Log.i(TAG, "PDNDService stopped");
	}

	void finishActivity() {
		Log.d(TAG, "finishActivity");
		sqlAdapter.close();
		finish();
	}

	public void refreshView() {
		Log.d(TAG, "refreshView");
		mainIntent = new Intent(this, MainActivity.class);
		doUnbindService();
		finishActivity();
		startActivity(mainIntent);
		overridePendingTransition(0, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentManager fm = getSupportFragmentManager();
		InputStream is = null;
		TextPopupFragment textFrag = null;
		String content = "";
		Scanner s = null;

		switch (item.getItemId()) {
		case R.id.about:
			is = getResources().openRawResource(R.raw.about);
			s = new Scanner(is).useDelimiter("\\Z");

			while (s.hasNext()) {
				content += s.next();
			}
			textFrag = TextPopupFragment.newInstance("About", content);
			textFrag.show(fm, "fragment_text_popup");
			return true;
		case R.id.help:
			is = getResources().openRawResource(R.raw.help);
			s = new Scanner(is).useDelimiter("\\Z");

			while (s.hasNext()) {
				content += s.next();
			}
			textFrag = TextPopupFragment.newInstance("Help", content);
			textFrag.show(fm, "fragment_text_popup");
			return true;
		case R.id.more_apps:
			AppWall.show(this);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		if (isBind)
			unbindService(mConnection);
		sqlAdapter.deleteObserver(this);
	}

}
