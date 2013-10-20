package com.example.geoquiz;

public class TrueFalse {

	private int mQuestion;
	private boolean mTrueQuestion;
	private boolean mCheater;
	
	public TrueFalse(int question, boolean trueQuestion)
	{
		mQuestion = question;
		mTrueQuestion = trueQuestion;
		mCheater = false;
	}

	public boolean isCheater() {
		return mCheater;
	}

	public void setCheater(boolean isCheater) {
		mCheater = isCheater;
	}

	public int getQuestion() {
		return mQuestion;
	}

	public void setQuestion(int question) {
		mQuestion = question;
	}

	public boolean isTrueQuestion() {
		return mTrueQuestion;
	}

	public void setTrueQuestion(boolean trueQuestion) {
		mTrueQuestion = trueQuestion;
	}
	
}
