package com.k4b.Bib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class ReaderActivity extends Activity{

	//---GENERAL KEYS---
	private static final int NUMBER_OF_ALL_BOOKS = 73;
	private static final String TAG = "BIB_READ";
	//---READER EXTRAS KEYS---
	private static final String EXTRA_TRANSLATION = "TRANSLATION";
	private static final String EXTRA_BOOK = "BOOK";
	private static final String EXTRA_CHAPTER = "CHAPTER";
	//---COMPARATOR EXTRAS KEYS---
	private static final String EXTRA_TRANSLATION_1 = "TRANSLATION_1";
	private static final String EXTRA_TRANSLATION_2 = "TRANSLATION_2";
	
	private DBAdapter db;
	private ArrayList<String> chapter;
	private Hashtable<Integer, Integer> book_chaptersNo_table;
	private HashMap<Integer, Integer> itemID_translationID_map;
	private CharSequence[] compareItems;
	private TextView textTV, titleTV;
	private BibleBook openedBook, newBook;
	private BibleAddress newAddress;
	private int translationIDToCompare = 0;
	private Button okButton;
	private String newText, newtitle;
	public BibleAddress openedAddress, searchedAddress;
	public List<String> chapters;
	public Spinner chapterSpinner;
	public int bookChosen, chapterChosen;
	public SearchDialog dialog;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reader);
		
		initialize();
		
		Bundle extras = getIntent().getExtras();
		if(extras!=null)
		{
			openedAddress = new BibleAddress(extras.getInt(EXTRA_TRANSLATION), extras.getInt(EXTRA_BOOK), extras.getInt(EXTRA_CHAPTER), 0);
		}
		setCompareItems();
		setUI();	
	}
	
	private void initialize()
	{
		db = new DBAdapter(this);
		db.open();
		
		book_chaptersNo_table = createBookChapterHashtable();
	}
	
	private void setCompareItems()
	{
		ArrayList<BibleTranslation> downloadedTranslations = db.getTranslations(1);
		itemID_translationID_map = new HashMap<Integer, Integer>();
		compareItems = new CharSequence[downloadedTranslations.size()];
		Log.d(TAG, "" + downloadedTranslations.size());
		for(int i=0; i<downloadedTranslations.size();i++)
    	{
//			if(downloadedTranslations.get(i).getIsPresent()==0)
				compareItems[i] = downloadedTranslations.get(i).getFullName();
    		itemID_translationID_map.put(i, downloadedTranslations.get(i).getId());
    	}
	}
	
	private void setUI()
	{
		String text = prepareText(openedAddress);
		openedBook = db.getBook(openedAddress.getBook());
		String title = "" + openedBook.getFullNamePL() + " : " + openedAddress.getChapter();
		
		titleTV = (TextView) findViewById(R.id.title);
		titleTV.setText(title);
		
		textTV = (TextView) findViewById(R.id.text);
		textTV.setText(text);
	}
	
	private String prepareText(BibleAddress address)
	{
		String text = "";
		chapter = db.getChapter(address.getTranslation(), address.getBook(), address.getChapter());
		int iterator;
		for(iterator=0; iterator < chapter.size(); iterator++)
		{
			text += "(" + (iterator+1) + ")" + chapter.get(iterator);
		}
		return text;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		newAddress = new BibleAddress();
	    switch (item.getItemId()) {
	        case R.id.menuItemBack:
	        	Log.d(TAG, "Menu Back item chosen");
	        	if(openedAddress.getChapter()!=1)
	        	{
		        	newAddress = new BibleAddress(openedAddress.getTranslation(), openedAddress.getBook(),openedAddress.getChapter()-1, 0);
	        	} else if(openedAddress.getChapter()==1 && openedAddress.getBook()!=1)
	        	{
	        		newAddress = new BibleAddress(openedAddress.getTranslation(), openedAddress.getBook()-1,openedBook.getChaptersNumber(), 0);
	        	} else
	        		newAddress = openedAddress;
	    	    newBook = db.getBook(newAddress.getBook());
	    	    newText = prepareText(newAddress);
	    	    newtitle = "" + newBook.getFullNamePL() + " : " + newAddress.getChapter();
	    	    titleTV.setText(newtitle);
	    	    textTV.setText(newText);
	    	    openedAddress = newAddress;
	            break;
	        case R.id.menuItemSearch:
	        	Log.d(TAG, "Menu Search item chosen");
	        	showDialog(0);
	            break;
	        case R.id.menuItemCompare:
	        	Log.d(TAG, "Menu Compare item chosen");
	        	showDialog(1);
	        	break;
	        case R.id.menuItemNext:	
	        	Log.d(TAG, "Menu Next item chosen");
	        	if((openedAddress.getChapter()+1)<=openedBook.getChaptersNumber())
	        	{
		        	newAddress = new BibleAddress(openedAddress.getTranslation(), openedAddress.getBook(),openedAddress.getChapter()+1, 0);
	        	} else if(openedAddress.getChapter()==openedBook.getChaptersNumber() && openedAddress.getBook()!=NUMBER_OF_ALL_BOOKS)
	        	{
	        		newAddress = new BibleAddress(openedAddress.getTranslation(), openedAddress.getBook()+1, 1, 0);
	        	} else
	        		newAddress = openedAddress;
	    	    newBook = db.getBook(newAddress.getBook());
	    	    newText = prepareText(newAddress);
	    	    newtitle = "" + newBook.getFullNamePL() + " : " + newAddress.getChapter();
	    	    titleTV.setText(newtitle);
	    	    textTV.setText(newText);
	    	    openedAddress = newAddress;
	        	break;
	    }
	    return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		
		switch(id)
    	{
    	case 0:
    		dialog = new SearchDialog(this, getBookNames(), book_chaptersNo_table);
    		
    		okButton = (Button) dialog.findViewById(R.id.OKButton);
    		Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
    		okButton.setOnClickListener(new OnClickListener() {
    			
    			@Override
    			public void onClick(View arg0) {
    				if(dialog.isBookChosen)
    				{
    					searchedAddress = new BibleAddress(openedAddress.getTranslation(), dialog.bookChosen, dialog.chapterChosen, 0);	
        				dialog.dismiss();
        				
        				newAddress = searchedAddress;
        			    newBook = db.getBook(newAddress.getBook());
        			    newText = prepareText(newAddress);
        			    newtitle = "" + newBook.getFullNamePL() + " : " + newAddress.getChapter();
        			    titleTV.setText(newtitle);
        			    textTV.setText(newText);
        			    openedAddress = newAddress;
    				}
    			}
    		});
    		cancelButton.setOnClickListener(new OnClickListener() {
    			
    			@Override
    			public void onClick(View arg0) {
    				dialog.cancel();
    			}
    		});
    		return dialog;
    	case 1:
    		Dialog alertDialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle("Wybierz t³umaczenie:")
				.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				})
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent i = new Intent("com.k4b.COMPARATOR");
						Bundle extras = new Bundle();
						extras.putInt(EXTRA_TRANSLATION_1, openedAddress.getTranslation());
						extras.putInt(EXTRA_TRANSLATION_2, translationIDToCompare);
						extras.putInt(EXTRA_BOOK, openedAddress.getBook());
						extras.putInt(EXTRA_CHAPTER, openedAddress.getChapter());
						i.putExtras(extras);
						startActivity(i);
						dialog.dismiss();
					}
				})					
				.setSingleChoiceItems(compareItems, translationIDToCompare, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int itemChecked) {
						translationIDToCompare = itemID_translationID_map.get(itemChecked);
						Log.d(TAG, "itemChecked=" +itemChecked);
						Log.d(TAG, "translationIDToCompare=" +translationIDToCompare);
					}
				}).create();
    		return alertDialog;
    	default: 
    		return null;
    	}
	}
	
	private Hashtable<Integer, Integer> createBookChapterHashtable()
	{
		ArrayList<BibleBook> booksArray = db.getBooks();
		Hashtable<Integer, Integer> table = new Hashtable<Integer, Integer>();
		for(BibleBook book : booksArray)
		{
			table.put(book.getId(), book.getChaptersNumber());
		}
		return table;
	}
	
	private List<String> getBookNames()
	{
		ArrayList<BibleBook> books = db.getBooks();
		List<String> bookNames = new ArrayList<String>();
		for(BibleBook book : books)
		{
			bookNames.add(book.getFullNamePL());
		}
		return bookNames;
	}
	
	@Override
	protected void onDestroy()
	{
		db.close();
		super.onDestroy();
	}
}
