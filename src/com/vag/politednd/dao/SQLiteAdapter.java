package com.vag.politednd.dao;

import java.util.Observable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class SQLiteAdapter extends Observable {

	// http://android-er.blogspot.com.br/2011/06/simple-example-using-androids-sqlite.html
	// http://android-er.blogspot.com.br/2011/06/simple-example-using-androids-sqlite_02.html
	// http://android-er.blogspot.com.br/2011/06/add-data-to-sqlite-database-with.html

	String TAG = "SQLiteAdapter";

	public static final String MYDATABASE_NAME = "pdnd_db";
	public static final String MYDATABASE_TABLE = "schedule";
	public static final int MYDATABASE_VERSION = 1;
	public static final String ID_COL = "id";
	public static final String INI_COL = "ini";
	public static final String END_COL = "end";
	public static final String WEEKDAYS_COL = "weekdays";
	public static final String ENABLED_COL = "enabled";

	public static final String[] columns = { ID_COL, INI_COL, END_COL,
			WEEKDAYS_COL, ENABLED_COL };

	// create table MY_DATABASE (ID integer primary key, Content text not null);
	private static final String SCRIPT_CREATE_DATABASE = "create table "
			+ MYDATABASE_TABLE + " (" + ID_COL
			+ " integer primary key autoincrement, " + INI_COL
			+ " text not null, " + END_COL + " text not null, " + WEEKDAYS_COL
			+ " text not null, " + ENABLED_COL + " integer not null);";

	private SQLiteHelper sqLiteHelper;
	private SQLiteDatabase sqLiteDatabase;

	private Context context;

	public SQLiteAdapter(Context c) {
		context = c;
	}

	public SQLiteAdapter openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null,
				MYDATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getReadableDatabase();
		return this;
	}

	public SQLiteAdapter openToWrite() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null,
				MYDATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		sqLiteHelper.close();
	}

	public long insert(ContentValues content) throws Exception {
		if (content.size() != 4) {
			throw new Exception("Invalid value count: " + content.size());
		}

		long status = sqLiteDatabase.insert(MYDATABASE_TABLE, null, content);
		notifyChange();
		return status;
	}

	public int update(int id, ContentValues content) {
		int status = sqLiteDatabase.update(MYDATABASE_TABLE, content, "id="
				+ id, null);
		notifyChange();

		return status;
	}

	public int deleteAll() {
		int status = sqLiteDatabase.delete(MYDATABASE_TABLE, null, null);
		notifyChange();
		return status;
	}

	public Cursor queueAll() {
		Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE, columns, null,
				null, null, null, null);

		return cursor;
	}

	public Cursor query(String cols[], String where) {
		if (cols == null) {
			cols = columns;
		}

		Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE, cols, where,
				null, null, null, null);

		return cursor;
	}

	public int delete(String where) {
		int status = sqLiteDatabase.delete(MYDATABASE_TABLE, where, null);
		notifyChange();
		return status;
	}

	private void notifyChange() {
		setChanged(); // mark that this object has changed
		notifyObservers(this); // notify all observers
	}

	public class SQLiteHelper extends SQLiteOpenHelper {

		public SQLiteHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("SQLiteAdapter", "Creating table " + MYDATABASE_TABLE
					+ " sql: " + SCRIPT_CREATE_DATABASE);
			db.execSQL(SCRIPT_CREATE_DATABASE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}
	}
}