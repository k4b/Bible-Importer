package com.k4b.Bib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

public class MainActivity extends Activity {
	
	private static final String TAG = "BIB_ACT";
	private static final String EXTRA_TRANSLATION = "TRANSLATION";
	private static final String EXTRA_BOOK = "BOOK";
	private static final String EXTRA_CHAPTER = "CHAPTER";
	private static final String DB_FILENAME = "Translations.jpg";
	private static final String APP_FOLDER = "/Bib/Database/";
	
	private CharSequence[] downloadItems; 
	private int checkedTranslation =0;
	private ArrayList<BibleTranslation> downloadedTranslations;
	private ArrayList<BibleTranslation> missingTranslations;
	private DownloadTask task;
//	private ArrayList<BibleTranslation> bibleTranslations;
	private HashMap<Integer, String> translationID_Name_map;
	private HashMap<Integer, Integer> itemID_translationID_map;
	public DBAdapter db;
	public int progress;
	public Notification notification;
	public NotificationManager notificationManager;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //---DB Initialization---
        initializeDB();
        
        //---Arrays Initialization---
        loadBibleTranslations();
        setDownloadItems();
        
        //---displaying UI---
        displayAvailableTranslations();
        setButtons();
        
        //---configure the custom notification---
        configureCustomNotification();
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
    	switch(id)
    	{
    	case 0:
    		return new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle("Pobierz t³umaczenie:")
    				.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.cancel();
						}
					})
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
							notificationManager.notify(42, notification);
							downloadTranslation(checkedTranslation);
						}
					})					
					.setSingleChoiceItems(downloadItems, checkedTranslation, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int itemChecked) {
							//+1 because items numbers start from 0
							checkedTranslation = itemChecked;
							Log.d(TAG, "checkedTranslation=" +checkedTranslation);
						}
					}).create();
    	}
		return null;
    }
    
    private void downloadTranslation(int checkedItem)
    {
    	int translationID = itemID_translationID_map.get(checkedItem);
    	Log.d(TAG, "Item " + checkedItem + " chosen : " + translationID_Name_map.get(translationID));
    	task = new DownloadTask(this);
    	task.execute(translationID);
    }
    
    private void initializeDB()
    {
    	db = new DBAdapter(this);
        boolean isDBPresent = db.checkDataBase();
        if(!isDBPresent)
        	copyDBToExternalStorage();
        else
        	Log.d(TAG, "DB present");
        db.open();
    }
    
    private void loadBibleTranslations()
    {    	
    	translationID_Name_map = new HashMap<Integer, String>();
    	translationID_Name_map = db.getTranslationIDs_Names_map();
    	
    	downloadedTranslations = db.getTranslations(1);
    	missingTranslations = db.getTranslations(0);
    }
    
    private void setDownloadItems()
    {
    	downloadItems = new CharSequence[missingTranslations.size()];
    	itemID_translationID_map = new HashMap<Integer, Integer>();
    	for(int i=0; i<missingTranslations.size();i++)
    	{
    		downloadItems[i] = missingTranslations.get(i).getFullName();
    		itemID_translationID_map.put(i, missingTranslations.get(i).getId());
    	}
    }
    
    //---displaying downloaded translations as a buttons on homescreen---
    private void displayAvailableTranslations()
    {
        if(downloadedTranslations.size()!=0)
        {
	        for(int i=0;i<downloadedTranslations.size();i++)
	        {
	        	makeTile(downloadedTranslations.get(i).getFullName(), downloadedTranslations.get(i).getId());
	        }
        }
    }
    
    private void setButtons()
    {
    	//---button1---
        Button cancelBtn = (Button)findViewById(R.id.button1);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				task.cancel(true);
				notificationManager.cancel(42);
			}
		});
        
        //---button2---
        Button downloadBtn = (Button)findViewById(R.id.button2);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDialog(0);
				setDownloadItems();
			}
		});
    }
    
    private void configureCustomNotification()
    {
    	Intent intent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        //---configure the notification---
        notification = new Notification(R.drawable.icon, "Downloading new translation", System.currentTimeMillis());
        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_progress);
        contentView.setImageViewResource(R.id.status_icon, R.drawable.bib_icon48);
        contentView.setTextViewText(R.id.status_text, "Downloading translation...");
        contentView.setProgressBar(R.id.status_progress, 100, progress, false);
        notification.contentView = contentView;
        notification.contentIntent = pendingIntent;
        String ns = Context.NOTIFICATION_SERVICE;
		notificationManager = (NotificationManager)getSystemService(ns);
    }

    /*
     * Method for creating an ImageButton of new Bible translation on the HomeScreen
     */
    public void makeTile(String text, final int t_id)
    {    	
    	LinearLayout entriesLayout = (LinearLayout)findViewById(R.id.linearLayoutEntries);
    	Button tileBtn = new Button(this);
    	tileBtn.setText(text);
    	tileBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bib_icon48, 0, 0, 0);
    	entriesLayout.addView(tileBtn);
    	
    	tileBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//---preparing intent to open translation---
				Intent i = new Intent("com.k4b.READER");
				Bundle extras = new Bundle();
				extras.putInt(EXTRA_TRANSLATION, t_id);
				extras.putInt(EXTRA_BOOK, 1);
				extras.putInt(EXTRA_CHAPTER, 1);
				i.putExtras(extras);
				startActivity(i);
			}
		});
    }
    
//	public void copyDB() throws IOException
//	{
//		String destPath = "/data/data/" + this.getPackageName() + "/databases";
//		String fileName = "Translations.db";
//		File directory = new File(destPath);
//		boolean test = directory.mkdirs();
//		Log.d(TAG, "Directory path present: " + !test);
//		File file = new File(directory, fileName);
//		test = file.exists();
//		Log.d(TAG, "Database file exists: " + test);
//		boolean isCreated = file.createNewFile();
//		Log.d(TAG, "Database file created: " + isCreated);
//		if(isCreated)				// jeœli baza nie istnieje, to kopiuje
//		{
//			Log.d(TAG, "Copying DB to Internal Storage...");
//			InputStream inputstream = this.getBaseContext().getAssets().open(DB_FILENAME);
//			OutputStream outputstream = new FileOutputStream(file);
//			
//			byte[] buffer = new byte[1024];
//			int length;
//			while((length = inputstream.read(buffer))>0)
//			{
//				outputstream.write(buffer, 0, length);
//			}
//			inputstream.close();
//			outputstream.close();
//		}
//	}
	
	private void copyDBToExternalStorage()
    {
    	String state = Environment.getExternalStorageState();
    	
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    
    	    try {
    	    	File sdCard = Environment.getExternalStorageDirectory();
    	    	File directory = new File(sdCard.getAbsolutePath() + APP_FOLDER);
    	    	boolean test = directory.mkdirs();
    	    	Log.d(TAG, "Directory path present: " + !test);
    	    	File file = new File(directory, "Translations.db");
    	    	test = file.exists();
    	    	Log.d(TAG, "Database file exists: " + test);
    			boolean isCreated = file.createNewFile();
    			Log.d(TAG, "Database file created: " + isCreated);
    			
    			if(isCreated)
    			{
    				Log.d(TAG, "Copying DB to External Storage...");
    				
    				InputStream inputstream = this.getBaseContext().getAssets().open(DB_FILENAME);
    				OutputStream outputstream = new FileOutputStream(file);
    				
    				byte[] buffer = new byte[1024];
    				int length;
    				while((length = inputstream.read(buffer))>0)
    				{
    					outputstream.write(buffer, 0, length);
    				}
    				inputstream.close();
    				outputstream.close();
	    			
	    			//---display file saved message---
	    			Log.d(TAG, "Database saved successfully");
    			}
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	}
		
    }
	
	@Override
	protected void onDestroy()
	{
		db.close();
		super.onDestroy();
	}
	
//	private String loadFromExternalStorage(String filename)
//	{
//		String s = "";
//		try {
//			File sdCard = Environment.getExternalStorageDirectory();
//			File directory = new File(sdCard.getAbsolutePath() + appFolder);
//			System.out.println("\n\n" + directory.getAbsolutePath() + "\n\n");
//			File file = new File(directory, filename);
//			FileInputStream fIn = new FileInputStream(file);
//			InputStreamReader isr = new InputStreamReader(fIn);
//			
//			char[] inputBuffer = new char[100];
//
//			
//			int charRead;
//
//			while((charRead = isr.read(inputBuffer))>0)
//			{
//				String readString = String.copyValueOf(inputBuffer, 0, charRead);
//				s+=readString;
//				
//				inputBuffer = new char[100];
//			}
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return s;
//	}
}