package com.jpvocab.vocabsite.model;

public class DailyStudyRow {
	private java.sql.Date study_date;
	private int words;
	public java.sql.Date getStudy_date() {
		return study_date;
	}
	public void setStudy_date(java.sql.Date study_date) {
		this.study_date = study_date;
	}
	public int getWords() {
		return words;
	}
	public void setWords(int words) {
		this.words = words;
	}
	
}
