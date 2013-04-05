package com.k4b.Bib;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Toast;

public class DownloadTask extends AsyncTask<Integer, Integer, String[]>{
	
	private static final String SOURCE_biblia_info_pl = "http://www.biblia.net.pl/cgi-bin/biblia.cgi?";
	private static final String TAG = "BIB_DS";
	private DBAdapter db;
	private BibleChapter chapter;
	private BibleTranslation translation;
	private int translationID;
	private ArrayList<BibleBook> books;
	private Date date1, date2;
	private MainActivity uiActivity;
	private int progress;
	
	public DownloadTask(MainActivity ma) 
	{
		uiActivity = ma;
		db = new DBAdapter(uiActivity.getApplicationContext());
		db.open();
		books = new ArrayList<BibleBook>();
		books = db.getBooks();
	}
	
	@Override
	protected String[] doInBackground(Integer... arg0) 
	{
		translationID = arg0[0];
		Log.d(TAG, getTime());
		date1 = new Date();
		String translationName = downloadTranslation(translationID);
		testProgress();
		Log.d(TAG, getTime());
		date2 = new Date();
		long time = date2.getTime()-date1.getTime();
		Log.d(TAG, "Time: " + time);
//		String[] result = {translationName, "" +translationID};
		String[] result = {db.getTranslations().get(translationID-1).getFullName(), "" +translationID};
		return result;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		if(progress[0]<books.size())
		{
	        uiActivity.notification.contentView.setProgressBar(R.id.status_progress, 100, progress[0], false);
	        // inform the progress bar of updates in progress
	        uiActivity.notificationManager.notify(42, uiActivity.notification);
		}
		else
		{
			//---remove progress bar---
			uiActivity.notificationManager.cancel(42);
		}
    }
	
	@Override
	protected void onPostExecute(String[] s)
	{
		String name = s[0];
		int id = new Integer(s[1]);
		Log.d(TAG, "Downloaded " + name + " id=" + id);
		uiActivity.makeTile(name, id);
		//---Notify user about completion of download---
		Notification n = new Notification(R.drawable.icon, "Translation downloaded.", java.lang.System.currentTimeMillis());
		String ns = Activity.NOTIFICATION_SERVICE;
		NotificationManager nm = (NotificationManager) uiActivity.getSystemService(ns);
		// configure the intent
        Intent intent = new Intent(uiActivity, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(uiActivity.getApplicationContext(), 0, intent, 0);
        n.setLatestEventInfo(uiActivity, "Pobrano biblię", s[0], pendingIntent);
        nm.notify(43, n);
        
		db.close();
	}
	
	private void testProgress()
	{
		int count = 100;
		for(int i=1; i<=count; i++)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG, ""+i);
			progress = (int)((i/(float)count)*100);
			publishProgress(progress);
		}
		uiActivity.notificationManager.cancel(42);
	}
	
	private String downloadTranslation(int translationID)
	{
		Log.d(TAG, "Downloading translation ID=" + translationID);
		translation = db.getTranslation(translationID);
		boolean lostConnection = false;
		
		if(isOnline())
		{
			createVersesTable(translation.getShortName());

			int bookCounter =1;
			for(BibleBook book : books)
			{
				if(isCancelled())
				{
					Toast.makeText(uiActivity, "Download Cancelled", Toast.LENGTH_SHORT).show();
					break;
				}
				if(!isOnline())
				{
					lostConnection = true;
					Toast.makeText(uiActivity, "Connection lost", Toast.LENGTH_SHORT).show();
					break;
				}
				for(int i = 1; i <= book.getChaptersNumber(); i++)
				{
					if(isCancelled())
					{
						Toast.makeText(uiActivity, "Download Cancelled", Toast.LENGTH_SHORT).show();
						break;
					}
					if(!isOnline())
					{
						lostConnection = true;
						Toast.makeText(uiActivity, "Connection lost", Toast.LENGTH_SHORT).show();
						break;
					}
					Log.d(TAG, "Downloading book " + book.getFullNamePL() + " chapter " + i);
					String url = createURLtoChapter(translation.getShortName(), book.getShortNamePL(), i);
					String webPageSource = httpGet(url);
					BibleAddress adr = new BibleAddress(translationID, book.getId(), i, 0);
					chapter = new BibleChapter();
					chapter = prepareBibleChapter(webPageSource, adr);
					saveChapterToDB(translation.getShortName(), chapter);
				}
				progress = (int)((bookCounter/(float)books.size())*100);
				publishProgress(progress);
				bookCounter++;
			}
		} else
			Toast.makeText(uiActivity, "No connection", Toast.LENGTH_SHORT).show();
		if(!isCancelled() && lostConnection==false)
			updateTranslation(translation.getId(), 1);
		return translation.getFullName();
	}

	private void updateTranslation(int id, int downloaded)
	{
		BibleTranslation t = db.getTranslation(id);
		db.updateTranslation(t.getFullName(), t.getShortName(), downloaded);
	}
	
	private void saveChapterToDB(String translationAlias, BibleChapter chap)
	{
		for (BibleVerse verse : chap.getVerses()) {
			db.insertVerse(translationAlias, verse.getAddress().getBook(), verse.getAddress().getChapter(), verse.getAddress().getVerse(), verse.getVerseText());
		}
	}
	
	private void createVersesTable(String translationAlias)
	{
		Log.d(TAG, "Creating table: Verses_" + translationAlias);
		db.createVersesTable(translationAlias);
	}
	
	private BibleChapter prepareBibleChapter(String pageSource, BibleAddress adr)
	{
		BibleAddress chapterAddress = adr;
		BibleVerse verse = new BibleVerse();
		String[] substrings;
		ArrayList<BibleVerse> verses = new ArrayList<BibleVerse>();
		substrings = pageSource.split("<SPAN class=\"nrWersetu\">");
		//first element of substrings[] is not important 
		for(int i=1; i<=substrings.length-1;i++)
		{
			int verseNumber = 0;
			try{
				verseNumber = Integer.valueOf(substrings[i].substring(1, substrings[i].indexOf(")")));
			} catch (Exception e) {
				verseNumber = i;
			}
			
			BibleAddress a = new BibleAddress(adr.getTranslation(), adr.getBook(), adr.getChapter(), 
					verseNumber);
			if(i==(substrings.length-1))
			{
				verse = new BibleVerse(a, substrings[i].substring(substrings[i].indexOf("</SPAN>")+7, substrings[i].indexOf("<BR>")));
			}
			else
				verse = new BibleVerse(a, substrings[i].substring(substrings[i].indexOf(">")+1));
			verses.add(verse);

		}
		chapter = new BibleChapter(chapterAddress, verses);
		Log.d(TAG, "Downloaded verses: " + chapter.getVerses().size());
//		Log.d(LOG, chapter.toString());
		return chapter;
	}
	
	/**
	 * Method for downloading a Web page to a String by HTTP GET request
	 * @param urlString
	 * @return
	 */
	public String httpGet(String urlString)
	{
		String result = "error";
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        try {
			request.setURI(new URI(urlString));
	        HttpResponse response = client.execute(request);
	        result = EntityUtils.toString(response.getEntity(), "iso-8859-2");
        } catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return result;
	}

	public String createURLtoChapter(String translation, String book, int chapter)
	{
		String source = SOURCE_biblia_info_pl;
		source += book + chapter + ".1-999/t" + translation + "/o/li";
		Log.d(TAG, "Created URL: " + source);
		return source;
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) uiActivity.getApplicationContext().getSystemService(
		        Context.CONNECTIVITY_SERVICE);

		    NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		    if (wifiNetwork != null && wifiNetwork.isConnected()) {
		      return true;
		    }

		    NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		    if (mobileNetwork != null && mobileNetwork.isConnected()) {
		      return true;
		    }

		    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		    if (activeNetwork != null && activeNetwork.isConnected()) {
		      return true;
		    }

		    return false;
	}
	
	private String getTime()
	{
		Calendar calendar = new GregorianCalendar();
		  String am_pm;
		  int hour = calendar.get(Calendar.HOUR);
		  int minute = calendar.get(Calendar.MINUTE);
		  int second = calendar.get(Calendar.SECOND);
		  if(calendar.get(Calendar.AM_PM) == 0)
			  am_pm = "AM";
		  else
			  am_pm = "PM";
		  String out = "Current Time : " + hour + ":" + minute + ":" + second + " " + am_pm;
		  return out;
	}
	
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}	
}