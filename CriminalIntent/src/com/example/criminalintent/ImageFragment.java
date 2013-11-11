package com.example.criminalintent;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageFragment extends DialogFragment {

	public static final String EXTRA_IMAGE_PATH = "com.example.criminalintent.image_path";
	
	private ImageView mImageView;
	
	public static ImageFragment newInstance(String imagePath)
	{
		ImageFragment imageFragment = new ImageFragment();
		Bundle args = new Bundle();
		args.putString( EXTRA_IMAGE_PATH, imagePath);
		imageFragment.setArguments(args);
		// Set the fragment style to STYLE_NO_TITLE, to achieve a minimalist look
		imageFragment.setStyle( DialogFragment.STYLE_NO_TITLE, 0);
		return imageFragment;
	}

	// Notice: since we don't need the title or buttons provided by AlertDialog, then
	// it's cleaner, quicker and more flexible to override onCreateView and use a simple
	// View rather than override onCreateDialog and use a Dialog
	// Notice : we don't have a layout resource, just an ImageView to return
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, parent, savedInstanceState);
		
		mImageView = new ImageView(getActivity());
		String path = (String) getArguments().getString(EXTRA_IMAGE_PATH);
		BitmapDrawable image = PictureUtils.getScaledDrawable(getActivity(), path);
		
		mImageView.setImageDrawable(image);
		
		return mImageView;
	}
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		PictureUtils.cleanImageView(mImageView);
	}
	
	
}
