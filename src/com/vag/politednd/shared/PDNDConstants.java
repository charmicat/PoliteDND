package com.vag.politednd.shared;

import java.util.Calendar;
import java.util.HashMap;

import com.vag.politednd.activity.R;

import android.content.res.Resources;
import android.util.SparseArray;

public interface PDNDConstants {

	static Resources r = App.getContext().getResources();

	public static enum WeekdaysEnum {
		NONE(0, r.getString(R.string.noRepeat)), SUNDAY(Calendar.SUNDAY, r
				.getString(R.string.su)), MONDAY(Calendar.MONDAY, r
				.getString(R.string.mo)), TUESDAY(Calendar.TUESDAY, r
				.getString(R.string.tu)), WEDNESDAY(Calendar.WEDNESDAY, r
				.getString(R.string.we)), THURSDAY(Calendar.THURSDAY, r
				.getString(R.string.th)), FRIDAY(Calendar.FRIDAY, r
				.getString(R.string.fr)), SATURDAY(Calendar.SATURDAY, r
				.getString(R.string.sa));

		private static SparseArray<WeekdaysEnum> codeToWeekdaysMapping;
		private static HashMap<String, WeekdaysEnum> labelToWeekdaysMapping;
		private int code;
		private String label;

		private WeekdaysEnum(int code, String label) {
			this.code = code;
			this.label = label;
		}

		public int getCode() {
			return code;
		}

		public String toString() {
			return label;
		}

		public static WeekdaysEnum getWeekday(int c) {
			if (codeToWeekdaysMapping == null) {
				initMapping();
			}
			WeekdaysEnum result = null;
			for (WeekdaysEnum s : values()) {
				result = codeToWeekdaysMapping.get(c);
			}
			return result;
		}

		public static WeekdaysEnum getWeekday(String l) {
			if (labelToWeekdaysMapping == null) {
				initMapping();
			}
			WeekdaysEnum result = null;
			for (WeekdaysEnum s : values()) {
				result = labelToWeekdaysMapping.get(l);
			}
			return result;
		}

		private static void initMapping() {
			codeToWeekdaysMapping = new SparseArray<WeekdaysEnum>(7);
			labelToWeekdaysMapping = new HashMap<String, WeekdaysEnum>(7);
			for (WeekdaysEnum s : values()) {
				codeToWeekdaysMapping.put(s.code, s);
				labelToWeekdaysMapping.put(s.label, s);
			}
		}
	}
}
