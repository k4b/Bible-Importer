package com.k4b.Bib;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBAdapter {
	
//	The Android's default system path of your application database.
//    private static String DB_PATH = "/data/data/com.k4b.Bib/databases/";
    
//	VERSE row line |IDno TRANSLATIONno BOOKno CHAPTERno VERSEno TEXT|
	
	// general keys
	private static final String TAG = "Bib_DBA";
	private static final String DATABESE_NAME = "Translations.db";
	private static final String DATABASE_VERSES_TABLE = "Verses_";
	private static final String DATABASE_TRANSLATIONS_TABLE = "Translations";
	private static final String DATABASE_BOOKS_TABLE = "Books";
	private static final int DATABASE_VERSION = 1;
	private static final String APP_FOLDER = "/Bib/Database/";
	// translations table keys
	public static final String KEY_TRANSLATION_ID = "ID";
	public static final String KEY_NAME = "Name";
	public static final String KEY_ALIAS = "Alias";
	public static final String KEY_PRESENT = "Present";
	// book table keys
	public static final String KEY_BOOK_ID = "ID";
	public static final String KEY_NAME_PL = "Name_PL";
	public static final String KEY_NAME_ENG = "Name_ENG";
	public static final String KEY_ALIAS_PL = "Alias_PL";
	public static final String KEY_ALIAS_ENG = "Alias_ENG";
	public static final String KEY_CHAPTERS_NO = "ChaptersNo";
	// chapter table keys
	public static final String KEY_CHAPTER_ID = "ID";
	public static final String KEY_BOOK_CH = "Book";
	public static final String KEY_NUMBER = "Number";
	public static final String KEY_VERSES_NO = "VersesNo";
	// verse table keys
	public static final String KEY_VERSE_ID = "ID";
	public static final String KEY_BOOK_V = "Book";
	public static final String KEY_CHAPTER = "Chapter";
	public static final String KEY_VERSE = "Verse";
	public static final String KEY_TEXT = "Text";
	
	
//	private static final String DATABASE_CREATE = "create table " + DATABASE_VERSES_TABLE + " (" + KEY_VERSE_ID + " integer primary key autoincrement, " 
//			+  KEY_BOOK_V + " integer not null, " + KEY_CHAPTER + "integer not null, " + KEY_VERSE + " integer not null, " + KEY_TEXT 
//			+ " text not null);";
	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase database;
	private String dbPath="";
	
	public DBAdapter(Context ctx)
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
		dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_FOLDER + DATABESE_NAME;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{		
		public DatabaseHelper(Context context) {
			super(context, DATABESE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_VERSES_TABLE);
			onCreate(db);
		}

	}
	
	//-------------------------- DATTABASE FUNCTIONS --------------------------
	
	//--- Checks wheather the DB exists---
	public boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }
		
	//---opens the database---
	public DBAdapter open()
	{
		database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
//		database = DBHelper.getWritableDatabase();
		return this;
	}
		
	//---close database---
	public void close()
	{
		database.close();
		DBHelper.close();
	}
	
	//-------------------------- TABLE FUNCTIONS --------------------------
	
	//---create table---
	public void createVersesTable(String name)
	{
		String statement = "create table " + DATABASE_VERSES_TABLE + name + " (" + KEY_VERSE_ID + " integer primary key autoincrement, " 
				+  KEY_BOOK_V + " integer not null, " + KEY_CHAPTER + " integer not null, " + KEY_VERSE + " integer not null, " + KEY_TEXT 
				+ " text not null);";
//		System.out.println(statement);
		database.execSQL(statement);
	}
	
	//---insert a verse into the database---
	public long insertVerse(String translationAlias, int book, int chapt, int verse, String text)
	{
		ContentValues initialValues = new ContentValues();
//		initialValues.put(KEY_ID, id);
		initialValues.put(KEY_BOOK_V, book);
		initialValues.put(KEY_CHAPTER, chapt);
		initialValues.put(KEY_VERSE, verse);
		initialValues.put(KEY_TEXT, text);
		return database.insert(DATABASE_VERSES_TABLE + translationAlias, null, initialValues);
	}
	
	//-------------------------- VERSE FUNCTIONS --------------------------
	
	//---delete a particular verse---
	public boolean deleteVerse(int id, int book, int chapt, int verse)
	{
		return database.delete(DATABASE_VERSES_TABLE, KEY_VERSE_ID + "=" + id + " and " + KEY_BOOK_V + "=" + book + " and " 
				+ KEY_BOOK_V + "=" + book + " and " + KEY_CHAPTER + "=" + chapt + " and " + KEY_VERSE + "=" + verse, null) > 0;
	}
	
	//---retrieve all the verses---
	public Cursor getAllVerses()
	{
		return database.query(true, DATABASE_VERSES_TABLE, new String[] {KEY_VERSE_ID, KEY_BOOK_V, KEY_CHAPTER, KEY_VERSE, 
				KEY_TEXT}, null, null, null, null, null, null);
	}
	
	//--- get a particular verse---
	public Cursor getVerseCursor(int book, int chapt, int verse){
		Cursor mCursor = database.query(true, DATABASE_VERSES_TABLE, new String[] {KEY_VERSE_ID, KEY_BOOK_V, KEY_CHAPTER, KEY_VERSE, 
				KEY_TEXT}, KEY_BOOK_V + "=" + book + " and " + KEY_BOOK_V + "=" + book + " and " 
						+ KEY_CHAPTER + "=" + chapt + " and " + KEY_VERSE + "=" + verse, null, null, null, null, null);
		if(mCursor != null)
		{
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//---update a verse---
		/*
		public boolean updateVerse(int id, int book, int chapt, int verse, String text)
		{
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_ID, id);
			initialValues.put(KEY_BOOK, book);
			initialValues.put(KEY_CHAPTER, chapt);
			initialValues.put(KEY_VERSE, verse);
			initialValues.put(KEY_TEXT, text);
			return translationsDB.update(DATABASE_TABLE, initialValues, KEY_ID + "=" + id, null) > 0;
		}
		*/
	
	//-------------------------- CHAPTER FUNCTIONS --------------------------
	
	//---get a particular chapter---
		public ArrayList<String> getChapter(int t_id, int book, int chapt)
		{
			ArrayList<String> chapter = new ArrayList<String>();
			BibleTranslation t = getTranslation(t_id);
			Cursor mCursor = database.query(true, DATABASE_VERSES_TABLE + t.getShortName(), new String[] {KEY_VERSE_ID, KEY_BOOK_V, KEY_CHAPTER, KEY_VERSE, 
					KEY_TEXT}, KEY_BOOK_V + "=" + book + " and " + KEY_CHAPTER + "=" + chapt, null, null, null, KEY_VERSE, null);
			
			if(mCursor != null)
			{
				mCursor.moveToFirst();
				chapter.add(mCursor.getString(4));
			}
			while(mCursor.moveToNext())
			{
				chapter.add(mCursor.getString(4));
			}
			return chapter;
		}
	
	//-------------------------- BOOK FUNCTIONS --------------------------
		
	public ArrayList<BibleBook> getBooks()
	{
		ArrayList<BibleBook> books = new ArrayList<BibleBook>();
		Cursor mCursor = database.query(true, DATABASE_BOOKS_TABLE, new String[] {KEY_BOOK_ID, KEY_NAME_PL, KEY_NAME_ENG, KEY_CHAPTERS_NO, 
				KEY_ALIAS_PL, KEY_ALIAS_ENG}, null, null, null, null, KEY_BOOK_ID, null);
		if(mCursor != null)
		{
			mCursor.moveToFirst();
			do
    		{
				BibleBook book = new BibleBook(mCursor.getInt(0), mCursor.getString(1), mCursor.getString(2), mCursor.getInt(3), 
						mCursor.getString(4), mCursor.getString(5));
				books.add(book);
    		} while (mCursor.moveToNext());
		}
		return books;
	}
	
	public BibleBook getBook(int id)
	{
		Cursor mCursor = database.query(true, DATABASE_BOOKS_TABLE, new String[] {KEY_BOOK_ID, KEY_NAME_PL, KEY_NAME_ENG, KEY_CHAPTERS_NO, 
				KEY_ALIAS_PL, KEY_ALIAS_ENG}, KEY_BOOK_ID + "=" + id, null, null, null, null, null);
		if(mCursor != null)
		{
			mCursor.moveToFirst();
		}
		BibleBook b = new BibleBook(mCursor.getInt(0), mCursor.getString(1), mCursor.getString(2), mCursor.getInt(3), 
				mCursor.getString(4), mCursor.getString(5));
		return b;
	}
	
	//-------------------------- TRANSLATION FUNCTIONS --------------------------
	
	public BibleTranslation getTranslation(int ID)
	{
		Cursor mCursor = database.query(true, DATABASE_TRANSLATIONS_TABLE, new String[] {KEY_TRANSLATION_ID, KEY_NAME, 
				KEY_ALIAS, KEY_PRESENT}, KEY_TRANSLATION_ID + "=\"" + ID + "\"", null, null, null, null, null);
		if(mCursor != null)
		{
			mCursor.moveToFirst();
		}
		BibleTranslation translation = new BibleTranslation(mCursor.getInt(0), mCursor.getString(1), mCursor.getString(2), mCursor.getInt(3));
		return translation;
	}
	
	public ArrayList<BibleTranslation> getTranslations()
	{
		ArrayList<BibleTranslation> translations = new ArrayList<BibleTranslation>();
		Cursor mCursor = database.query(true, DATABASE_TRANSLATIONS_TABLE, new String[] {KEY_TRANSLATION_ID, KEY_NAME, 
				KEY_ALIAS, KEY_PRESENT}, null, null, null, null, KEY_TRANSLATION_ID, null);
		if(mCursor != null)
		{
			mCursor.moveToFirst();
			do
    		{
    			BibleTranslation bt = new BibleTranslation(mCursor.getInt(0), mCursor.getString(1), mCursor.getString(2), mCursor.getInt(3));
    			translations.add(bt);
    		} while (mCursor.moveToNext());
		}
		return translations;
	}
	
	public ArrayList<BibleTranslation> getTranslations(int isDownloaded)
	{
		ArrayList<BibleTranslation> translations = new ArrayList<BibleTranslation>();
		Cursor mCursor = database.query(true, DATABASE_TRANSLATIONS_TABLE, new String[] {KEY_TRANSLATION_ID, KEY_NAME, 
				KEY_ALIAS, KEY_PRESENT}, KEY_PRESENT + "=\"" + isDownloaded + "\"", null, null, null, KEY_TRANSLATION_ID, null);
		if(mCursor != null)
		{
			mCursor.moveToFirst();
			do
    		{
    			BibleTranslation bt = new BibleTranslation(mCursor.getInt(0), mCursor.getString(1), mCursor.getString(2), mCursor.getInt(3));
    			translations.add(bt);
    		} while (mCursor.moveToNext());
		}
		return translations;
	}
	
	public HashMap<Integer, String> getTranslationIDs_Names_map()
	{
		HashMap<Integer, String> translations = new HashMap<Integer, String>();
		Cursor mCursor = database.query(true, DATABASE_TRANSLATIONS_TABLE, new String[] {KEY_TRANSLATION_ID, KEY_NAME, 
				KEY_ALIAS, KEY_PRESENT}, null, null, null, null, KEY_TRANSLATION_ID, null);
		if(mCursor != null)
		{
			mCursor.moveToFirst();
			do
    		{
    			translations.put(mCursor.getInt(0), mCursor.getString(1));
    		} while (mCursor.moveToNext());
		}
		return translations;
	}
	
	public boolean updateTranslation(String fullName, String shortName, int present)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, fullName);
		initialValues.put(KEY_ALIAS, shortName);
		initialValues.put(KEY_PRESENT, present);
		return database.update(DATABASE_TRANSLATIONS_TABLE, initialValues, KEY_NAME + "=\"" + fullName + "\"", null) > 0;
	}
	
}