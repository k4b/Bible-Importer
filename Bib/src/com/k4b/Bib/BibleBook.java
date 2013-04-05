package com.k4b.Bib;

import java.util.ArrayList;

public class BibleBook {

	private BibleAddress address;
	private String fullNamePL;
	private String shortNamePL;
	private String fullNameENG;
	private String shortNameENG;
	private int chaptersNumber;
	private int id;
	
	public BibleBook(){};
	
	public BibleBook(int num)
	{
		this.address.setBook(num);
	}
	
	public BibleBook(BibleAddress addr)
	{
		this.address = addr;
	}
	
	public BibleBook(BibleAddress addr, String fullnPL, String shortnPL, String fullnENG, String shortnENG)
	{
		this(addr);
		this.fullNamePL = fullnPL;
		this.shortNamePL = shortnPL;
		this.fullNameENG = fullnENG;
		this.shortNameENG = shortnENG;
	}
	
	public BibleBook(int id, String fullnPL, String fullnENG, int chaptNum, String shortnPL, String shortnENG)
	{
		this.id = id;
		this.fullNamePL = fullnPL;
		this.fullNameENG = fullnENG;
		this.chaptersNumber = chaptNum;
		this.shortNamePL = shortnPL;
		this.shortNameENG = shortnENG;
	}	
	
	public BibleBook(BibleAddress addr, String fullnPL, String shortnPL, String fullnENG, String shortnENG, ArrayList<BibleChapter> chapt)
	{
		this(addr);
		this.fullNamePL = fullnPL;
		this.shortNamePL = shortnPL;
		this.fullNameENG = fullnENG;
		this.shortNameENG = shortnENG;
	}

	public BibleAddress getAddress() {
		return address;
	}

	public void setAddress(BibleAddress address) {
		this.address = address;
	}

	public String getFullNamePL() {
		return fullNamePL;
	}

	public void setFullNamePL(String fullNamePL) {
		this.fullNamePL = fullNamePL;
	}

	public String getShortNamePL() {
		return shortNamePL;
	}

	public void setShortNamePL(String shortNamePL) {
		this.shortNamePL = shortNamePL;
	}

	public String getFullNameENG() {
		return fullNameENG;
	}

	public void setFullNameENG(String fullNameENG) {
		this.fullNameENG = fullNameENG;
	}

	public String getShortNameENG() {
		return shortNameENG;
	}

	public void setShortNameENG(String shortNameENG) {
		this.shortNameENG = shortNameENG;
	}

	public int getChaptersNumber() {
		return chaptersNumber;
	}

	public void setChaptersNumber(int chaptersNumber) {
		this.chaptersNumber = chaptersNumber;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
