package com.k4b.Bib;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class SearchDialog extends Dialog{
	
	private Context context;
	private Hashtable<Integer, Integer> book_chaptersNo_table;
	private List<String> bookNames;
	public Spinner bookSpinner;
	public Spinner chapterSpinner;
	public int bookChosen, chapterChosen;
	public boolean isBookChosen;
	private List<String> chapters;

	public SearchDialog(Context context, List<String> bookNames, Hashtable<Integer, Integer> books_chapters_table) {
		super(context);
		this.context = context;
		this.bookNames = bookNames;
		this.book_chaptersNo_table = books_chapters_table;
		initialize();
	}
	
	public void initialize()
	{
		setContentView(R.layout.search);
		setTitle("Wybierz ksiêgê i rozdzia³");
		
		isBookChosen = false;
		bookSpinner = (Spinner) findViewById(R.id.bookSpinner);
		chapterSpinner = (Spinner) findViewById(R.id.chapterSpinner);
		bookNames.add(0, " ");
		
		ArrayAdapter<String> bookDataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, bookNames);
		bookDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		bookSpinner.setAdapter(bookDataAdapter);
		
		bookSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(position!=0)
				{
					bookChosen = position;
					isBookChosen = true;
					chapters = new ArrayList<String>();
					for(int i=1; i<=book_chaptersNo_table.get(bookChosen); i++)
					{
						chapters.add(""+i);
					}
					chapters.add(0, " ");
					ArrayAdapter<String> chapterDataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, chapters);
					chapterDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
					chapterSpinner.setAdapter(chapterDataAdapter);
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				if(isBookChosen)
					chapterChosen = 1;
			}
		});
		
		chapterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(position!=0)
				{
					if(isBookChosen)
						chapterChosen = position;
					else
						Toast.makeText(context, "Wybierz ksiêgê", Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(context, "Wybierz rozdzia³", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				if(isBookChosen)
					chapterChosen = 1;
			}
		});

	}

}
