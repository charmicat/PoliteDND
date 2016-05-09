package com.vag.politednd.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.vag.politednd.shared.PDNDConstants;
import com.vag.politednd.shared.PDNDConstants.WeekdaysEnum;

import android.util.Log;

public class Schedule {

	private boolean enabled, isNoRepeat, isNonStop;
	private String iniString, endString, weekdaysString;

	private int id;

	private HashMap<WeekdaysEnum, Calendar> ini, end;

	String TAG = "Schedule";

	public Schedule(int id, String iniStr, String endStr, String weekdays) {
		ini = new HashMap<WeekdaysEnum, Calendar>();
		end = new HashMap<WeekdaysEnum, Calendar>();

		this.id = id;
		this.iniString = iniStr;
		this.endString = endStr;
		this.weekdaysString = weekdays;

		Log.d(TAG, id + " New schedule: ini=" + iniStr + " end=" + endStr
				+ " weekdays=" + weekdays);

		if (weekdays.charAt(0) == '-') { // No repeat
			isNoRepeat = true;
		} else {
			isNoRepeat = false;
		}

		if (endStr.charAt(0) == '-') { // Never
			isNonStop = true;
		} else {
			isNonStop = false;
		}

		String wd[] = weekdays.split(",");

		for (int i = 0; i < wd.length; i++) {
			String tks_ini[] = iniStr.split(":");
			Calendar c = new GregorianCalendar();
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tks_ini[0]));
			c.set(Calendar.MINUTE, Integer.parseInt(tks_ini[1]));

			String tks_end[] = endStr.split(":");
			Calendar c1 = new GregorianCalendar();
			if (!isNonStop) {
				c1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tks_end[0]));
				c1.set(Calendar.MINUTE, Integer.parseInt(tks_end[1]));
			} else {
				c1.set(Calendar.HOUR_OF_DAY, 0);
				c1.set(Calendar.MINUTE, 0);
			}

			if (isNoRepeat) {
				ini.put(WeekdaysEnum.NONE, c);
				end.put(WeekdaysEnum.NONE, c1);
			} else {
				ini.put(PDNDConstants.WeekdaysEnum.getWeekday(Integer
						.parseInt(wd[i])), c);

				end.put(PDNDConstants.WeekdaysEnum.getWeekday(Integer
						.parseInt(wd[i])), c1);
			}
		}
	}

	public int getId() {
		return this.id;
	}

	public boolean isNoRepeat() {
		return isNoRepeat;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public HashMap<WeekdaysEnum, Calendar> getIni() {
		return ini;
	}

	public HashMap<WeekdaysEnum, Calendar> getEnd() {
		return end;
	}

	public String getIniString() {
		return iniString;
	}

	public void setIniString(String iniString) {
		this.iniString = iniString;
	}

	public String getEndString() {
		return endString;
	}

	public void setEndString(String endString) {
		this.endString = endString;
	}

	public String getWeekdaysString() {
		return weekdaysString;
	}

	public void setWeekdaysString(String weekdaysString) {
		this.weekdaysString = weekdaysString;
	}

	public boolean isNonStop() {
		return isNonStop;
	}
}
