package com.k4b.Bib;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ComparatorActivity extends Activity{
	
	private static final String EXTRA_TRANSLATION_1 = "TRANSLATION_1";
	private static final String EXTRA_TRANSLATION_2 = "TRANSLATION_2";
	private static final String EXTRA_BOOK = "BOOK";
	private static final String EXTRA_CHAPTER = "CHAPTER";
	public String tex1, text2;
	public TextView tv1, tv2, title1, title2;
	private BibleAddress originalAddress, comparedAddress;
	private ArrayList<String> chapter;
	private DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comparator);
		db = new DBAdapter(this);
		db.open();
		
		title1 = (TextView) findViewById(R.id.title1);
		tv1 = (TextView) findViewById(R.id.text1);
		title2 = (TextView) findViewById(R.id.title2);
		tv2 = (TextView) findViewById(R.id.text2);
		
		Bundle extras = getIntent().getExtras();
		if(extras!=null)
		{
			originalAddress = new BibleAddress(extras.getInt(EXTRA_TRANSLATION_1), extras.getInt(EXTRA_BOOK), extras.getInt(EXTRA_CHAPTER), 0);
			comparedAddress = new BibleAddress(extras.getInt(EXTRA_TRANSLATION_2), extras.getInt(EXTRA_BOOK), extras.getInt(EXTRA_CHAPTER), 0);
		}
		
		BibleBook book1 = db.getBook(originalAddress.getBook());
		BibleTranslation translation1 = db.getTranslation(originalAddress.getTranslation());
		String t1 = "" + translation1.getFullName() + " : " + book1.getFullNamePL() + " : " + originalAddress.getChapter();
		title1.setText(t1);
		tv1.setText(prepareText(originalAddress));
		
		BibleBook book2 = db.getBook(comparedAddress.getBook());
		BibleTranslation translation2 = db.getTranslation(comparedAddress.getTranslation());
		String t2 = "" + translation2.getFullName() + " : " + book2.getFullNamePL() + " : " + comparedAddress.getChapter();
		title2.setText(t2);
		tv2.setText(prepareText(comparedAddress));
		
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
	protected void onDestroy()
	{
		db.close();
		super.onDestroy();
	}
}
