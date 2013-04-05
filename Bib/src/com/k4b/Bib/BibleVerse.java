package com.k4b.Bib;

public class BibleVerse {

	private BibleAddress address;
	private String verseText;
	
	public BibleVerse(){};
	
	public BibleVerse(BibleAddress addr)
	{
		this.address = addr;
	}
	
	public BibleVerse(BibleAddress addr, String text)
	{
		this(addr);
		this.verseText = text;
	}
	
	@Override
	public String toString()
	{
		return address.getVerse() + verseText;
	}

	public BibleAddress getAddress() {
		return address;
	}

	public void setAddress(BibleAddress address) {
		this.address = address;
	}

	public String getVerseText() {
		return verseText;
	}

	public void setVerseText(String verseText) {
		this.verseText = verseText;
	}
}
