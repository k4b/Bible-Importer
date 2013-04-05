package com.k4b.Bib;

public class BibleAddress {

	private int verse;			// 0 - whole chapter
	private int chapter;
	private int book;
	private int translation;
	
	public BibleAddress(){};
	
	public BibleAddress(int t_id, int b_num, int c_num, int v_num)
	{
		this.translation = t_id;
		this.book = b_num;
		this.chapter = c_num;
		this.verse = v_num;
	}
	
	@Override
	public String toString()
	{
		String s = translation + ", " + book + ", " + chapter + ", " + verse + ".";
		return s;
	}

	public int getVerse() {
		return verse;
	}

	public void setVerse(int verse) {
		this.verse = verse;
	}

	public int getChapter() {
		return chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	public int getBook() {
		return book;
	}

	public void setBook(int book) {
		this.book = book;
	}

	public int getTranslation() {
		return translation;
	}

	public void setTranslation(int translation) {
		this.translation = translation;
	}
}
