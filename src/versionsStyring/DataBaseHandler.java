package versionsStyring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import designPatterns.ObserverPattern_Observer;

public class DataBaseHandler
{

	private SQLiteDatabase database;
	private MySQLiteOpenHelper dbHelper;
	Context context;
	public DataBaseHandler(Context context)
	{
		this.context = context;
	}

	public void open() throws SQLException
	{
		dbHelper = new MySQLiteOpenHelper(context);
		database = dbHelper.getWritableDatabase();
	}

	public void close()
	{
		database.close();
		dbHelper.close();
	}

	public void clear()
	{
		database.execSQL("DROP TABLE " + dbHelper.TABLE_FILES);
		database.execSQL(dbHelper.DATABASE_CREATE);
	}

	public void deleteEntry(String entry)
	{
		database.execSQL("DELETE FROM " + dbHelper.TABLE_FILES + " WHERE " + dbHelper.COLUMN_FILE_NAME + "='" + entry + "'");
	}

	public void addEntry(String entry, String md5)
	{
		Log.i("version", entry + " : " + md5);
		database.execSQL("INSERT OR REPLACE INTO " + dbHelper.TABLE_FILES + "(" + dbHelper.COLUMN_FILE_NAME + ", " + dbHelper.COLUMN_HASHVALUE + ") VALUES ('"
				+ entry + "', '" + md5 + "');");
	}

	public Map<String, String> getDBentriesAsMap()
	{
		Map<String, String> returnMap = new HashMap<String, String>();

		Cursor cursor = database.query(dbHelper.TABLE_FILES, null, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			returnMap.put(cursor.getString(0), cursor.getString(1));
			cursor.moveToNext();
		}
		return returnMap;
	}
}
