package com.example.geoquiz;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends Activity {

	private static final String TAG = "QuizActivity";
	private static final String KEY_INDEX = "index";
	private static final String KEY_CHEATED_INDEXES = "cheated_indexes";
	public static final String EXTRA_ANSWER_IS_TRUE = "com.example.geoquiz.answer_is_true";
	public static final String EXTRA_ANSWER_IS_CHEATED = "com.example.geoquiz.answer_is_cheated";
	private static final int ID_CHEAT = 0;
	private Button mTrueButton;
	private Button mFalseButton;
	private Button mCheatButton;
	private Button mNextButton;
	private Button mPreviousButton;
	private TextView mQuestionTextView;
	
	private TrueFalse[] mQuestionBank = new TrueFalse[]
			{ 
				new TrueFalse(R.string.question_oceans, true),
				new TrueFalse(R.string.question_mideast, false),
				new TrueFalse(R.string.question_africa, false),
				new TrueFalse(R.string.question_americas, true),
				new TrueFalse(R.string.question_asia, false),
			};
	
	private int mCurrentIndex = 0;
	//private boolean mIsCheater = false;
	
	@Override
	protected void onPause()
	{
		super.onPause();
		Log.i(TAG, "onPause");
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		//When  onSaveInstanceState(…)  is called, the data is saved to the  Bundle  object. That  Bundle  object is 
		//then stuffed into your activity’s  "activity record" by the OS 
		super.onSaveInstanceState(savedInstanceState);
		Log.i(TAG, "onSaveInstanceState: save index " + mCurrentIndex);
		savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
		
		boolean[] cheatedIndexes = new boolean[mQuestionBank.length];
		for (int i = 0; i < mQuestionBank.length; i++)
		{
			cheatedIndexes[i] = mQuestionBank[i].isCheater();
		}
		
		savedInstanceState.putBooleanArray(KEY_CHEATED_INDEXES, cheatedIndexes);
	}
	
	@TargetApi(11)
	private void buildActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			Log.i(TAG, "BuildActionBar");
			ActionBar actionBar = getActionBar();
			actionBar.setSubtitle(R.string.subtitle);
		}
		else
		{
			Log.i(TAG, "Cannot BuildActionBar");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quiz);
		
		buildActionBar();
		
		if (savedInstanceState != null)
		{
			mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
			boolean[] cheatedIndexes = savedInstanceState.getBooleanArray(KEY_CHEATED_INDEXES);
			for (int i = 0; i < cheatedIndexes.length; i++)
			{
				 mQuestionBank[i].setCheater(cheatedIndexes[i]); 
			}
		}
		
		mTrueButton = (Button) findViewById( R.id.true_button );
		
		mTrueButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkAnswer(true);
			}
		});
		
		mFalseButton = (Button) findViewById( R.id.false_button );

		mFalseButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkAnswer(false);			
			}
		});
		
		mQuestionTextView = (TextView) findViewById( R.id.question_text_view );
		mQuestionTextView.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
				updateQuestion();
			}
		});
		
		updateQuestion();
		
		mNextButton = (Button) findViewById( R.id.next_button );
		mNextButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
				Log.d(TAG, "NextButton new index = " + mCurrentIndex);
				updateQuestion();
			}
		});

		mPreviousButton = (Button) findViewById( R.id.previous_button );
		mPreviousButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCurrentIndex = (((mCurrentIndex - 1) < 0) ? (mQuestionBank.length - 1) : (mCurrentIndex - 1));
				updateQuestion();
			}
		});
		
		mCheatButton = (Button) findViewById( R.id.cheat_button );
		mCheatButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent i = new Intent(QuizActivity.this, CheatActivity.class);
				i.putExtra(EXTRA_ANSWER_IS_TRUE, mQuestionBank[mCurrentIndex].isTrueQuestion());
				i.putExtra(EXTRA_ANSWER_IS_CHEATED, mQuestionBank[mCurrentIndex].isCheater());
				startActivityForResult(i, ID_CHEAT);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data != null && requestCode == ID_CHEAT && resultCode == Activity.RESULT_OK)
		{
			if (!mQuestionBank[mCurrentIndex].isCheater())
			{
				boolean isCheater = data.getBooleanExtra(CheatActivity.EXTRA_ANSWER_SHOWN, false);
				mQuestionBank[mCurrentIndex].setCheater(isCheater);
			}
		}
	}
	
	private void updateQuestion()
	{
		int question = mQuestionBank[mCurrentIndex].getQuestion();
		mQuestionTextView.setText(question);
	}
	
	private boolean isCheater()
	{
		return mQuestionBank[mCurrentIndex].isCheater();
	}
	
	private void checkAnswer(boolean userPressedTrue)
	{
		boolean answerIsTrue = mQuestionBank[mCurrentIndex].isTrueQuestion();
		int messageResId = 0;
		
		if (isCheater())
		{
			messageResId = R.string.judgement_toast;
		}
		else if (userPressedTrue == answerIsTrue)
		{
			messageResId = R.string.correct_toast;
		}
		else
		{
			messageResId = R.string.incorrect_toast;
		}
		// Context is needed by Toast class to resolve the string's resource ID
		Toast.makeText( this , messageResId, Toast.LENGTH_SHORT).show();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quiz, menu);
		return true;
	}

}
