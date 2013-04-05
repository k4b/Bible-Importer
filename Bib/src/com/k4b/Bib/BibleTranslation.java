package com.k4b.Bib;

public class BibleTranslation {

	private int id;
	private String fullName;
	private String shortName;
	private int isPresent;	//1=TRUE , 0=FALSE;
	
	public BibleTranslation(){};
	
	public BibleTranslation(int i)
	{
		this.id = i;
	}
	
	public BibleTranslation(int i, String fulln, String shortn, int isPresent)
	{
		this(i);
		this.fullName = fulln;
		this.shortName = shortn;
		this.isPresent = isPresent;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public int getIsPresent() {
		return isPresent;
	}

	public void setIsPresent(int isPresent) {
		this.isPresent = isPresent;
	}
}
