package com.example.criminalintent;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import android.text.format.DateFormat;

public class Crime {
	
	private static final String JSON_ID = "id";
	private static final String JSON_TITLE = "title";
	private static final String JSON_SOLVED = "solved";
	private static final String JSON_DATE = "date";
	private static final String JSON_PHOTO = "photo";
	private static final String JSON_SUSPECT = "suspect";
	
	private UUID mId;
	private String mTitle;
	private Date mDate;
	private boolean mSolved;
	private Photo mPhoto;
	private String mSuspect;
	
	public Crime()
	{
		// Generate unique identifier
		mId = UUID.randomUUID();
		// Set default values
		mTitle = new String("Default title");
		mSolved = false;
		mDate = new Date();
	}
	
	public Crime(JSONObject json) throws JSONException
	{
		mId = UUID.fromString( json.getString( JSON_ID ));
		mTitle = json.getString( JSON_TITLE );
		mSolved = json.getBoolean( JSON_SOLVED );
		mDate = new Date(json.getLong( JSON_DATE ));
		
		if (json.has(JSON_PHOTO))
			mPhoto = new Photo( json.getJSONObject( JSON_PHOTO ));
		if (json.has(JSON_SUSPECT))
			mSuspect = json.getString( JSON_SUSPECT );
	}
	
	public JSONObject toJSON() throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put(JSON_ID, mId.toString());
		json.put(JSON_TITLE, mTitle);
		json.put(JSON_SOLVED, mSolved);
		json.put(JSON_DATE, mDate.getTime());
		if (mPhoto != null)
			json.put(JSON_PHOTO, mPhoto.toJSON());
		json.put(JSON_SUSPECT, mSuspect);
		return json;
	}
	
	public Photo getPhoto()
	{
		return mPhoto;
	}
	
	public void setPhoto(Photo photo)
	{
		mPhoto = photo;
	}
	
	public String getSuspect()
	{
		return mSuspect;
	}
	
	public void setSuspect(String suspect)
	{
		mSuspect = suspect;
	}	
	
	@Override
	public String toString()
	{
		return mTitle;
	}
	
	public String getDateAsFormattedString()
	{
		return DateFormat.format( "EEEE, MMM d, yyyy kk:mm", getDate()).toString();		
	}
	
	public Date getDate() {
		return mDate;
	}
	
	public void setDate(Date date) {
		mDate = date;
	}

	public boolean isSolved() {
		return mSolved;
	}

	public void setSolved(boolean solved) {
		mSolved = solved;
	}

	public UUID getId() {
		return mId;
	}

	public void setId(UUID id) {
		mId = id;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}
}
