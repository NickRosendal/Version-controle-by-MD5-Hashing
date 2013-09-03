package versionsStyring;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper
{

	public static final String TABLE_FILES = "files";
	public static final String COLUMN_FILE_NAME = "filename";
	public static final String COLUMN_HASHVALUE = "hashvalue";
	private static final String DATABASE_NAME = "filesOnPhone.db";
	
	private static final int DATABASE_VERSION = 1;

	public static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_FILES + "(" + COLUMN_FILE_NAME + " text primary key , " + COLUMN_HASHVALUE + " text);";

	public MySQLiteOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO cross that bridge when you get there

	}

}
