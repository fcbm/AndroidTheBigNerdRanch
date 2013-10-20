package com.example.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CheatActivity extends Activity {
	
	public static final String EXTRA_ANSWER_SHOWN = "com.example.geoquiz.extra_answer_shown";
	private static final String KEY_ANSWER_VAL = "answer_val";
	private static final String KEY_ANSWER_SHOWN = "answer_shown";
	private boolean mAnswerIsTrue = false;
	private boolean mIsAnswerShown = false;
	
	@Override
	public void onSaveInstanceState(Bundle onSavedInstanceState)
	{
		onSavedInstanceState.putBoolean(KEY_ANSWER_SHOWN, mIsAnswerShown);
		onSavedInstanceState.putBoolean(KEY_ANSWER_VAL, mAnswerIsTrue);
	}
	
	@Override
	protected void onCreate(Bundle onSavedInstanceState)
	{
		super.onCreate(onSavedInstanceState);
		setContentView(R.layout.activity_cheat);
		
		if (onSavedInstanceState != null)
		{
			mIsAnswerShown = onSavedInstanceState.getBoolean( KEY_ANSWER_SHOWN );
			mAnswerIsTrue = onSavedInstanceState.getBoolean( KEY_ANSWER_VAL );
		}
		else
		{
			Intent i = getIntent();
			mAnswerIsTrue = i.getBooleanExtra(QuizActivity.EXTRA_ANSWER_IS_TRUE, true);
			mIsAnswerShown = i.getBooleanExtra(QuizActivity.EXTRA_ANSWER_IS_CHEATED, false);
		}
		
		if (mIsAnswerShown)
		{
			showAnswer();
		}
		
		setAnswerShownResult();
		
		Button mShowAnswerButton = (Button) findViewById( R.id.show_answer_button );
		mShowAnswerButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showAnswer();
				mIsAnswerShown = true;
				setAnswerShownResult();
			}
		});
	}
	
	private void showAnswer()
	{
		TextView answerTextView = (TextView)findViewById( R.id.answer_text );
		if (mAnswerIsTrue)
		{
			answerTextView.setText( R.string.true_button );
		}
		else
		{
			answerTextView.setText( R.string.false_button );
		}
	}
	
	private void setAnswerShownResult()
	{
		Intent i = new Intent();
		i.putExtra(EXTRA_ANSWER_SHOWN, mIsAnswerShown);
		setResult(RESULT_OK, i);
	}

}
