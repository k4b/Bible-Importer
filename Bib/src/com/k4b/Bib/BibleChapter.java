package com.k4b.Bib;

import java.util.ArrayList;

public class BibleChapter {

	private BibleAddress address;
	private ArrayList<BibleVerse> verses;
	
	public BibleChapter(){};
	
	public BibleChapter(BibleAddress addr)
	{
		this.address = addr;
		verses = new ArrayList<BibleVerse>();
	}
	
	public BibleChapter(BibleAddress addr, ArrayList<BibleVerse> ver)
	{
		this(addr);
		verses = ver;
	}
	
	@Override
	public String toString()
	{
		String output = "Chapter: " + address.getChapter() + "\n";
		for (BibleVerse verse : verses) {
			output += verse.getAddress().getVerse() + verse.getVerseText() + "\n";
		}
		return output;
	}
	
	public void addVerse(BibleVerse verse)
	{
		verses.add(verse);
	}
	
	public BibleVerse getVerse(int num)
	{
		return verses.get(num);
	}

	public BibleAddress getAddress() {
		return address;
	}

	public void setAddress(BibleAddress address) {
		this.address = address;
	}

	public ArrayList<BibleVerse> getVerses() {
		return verses;
	}

	public void setVerses(ArrayList<BibleVerse> verses) {
		this.verses = verses;
	}
}
