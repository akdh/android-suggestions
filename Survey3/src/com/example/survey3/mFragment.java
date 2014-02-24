package com.example.survey3;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class mFragment extends Fragment {
	
	public static final String TITLE = "com.example.survey.TITLE";
	public static final String DESCRIPTION = "com.example.survey.DESCRIPTION";
	public static final String URL = "com.example.survey.URL";
	public static final String ID = "com.example.survey.ID";
	
	public final static String EXTRA_CONTEXT = "com.example.survey3.CONTEXT";
	
	// minimum number of attractions to bookmark before option for new city
	public static final int MIN_BOOKMARKED = 2;
	public static int numBookmarked = 0;
	private List<String> disabled = new ArrayList<String>();
	
	static Context mContext;
	
	public static Fragment newInstance(String title, String description,
			String url, String suggestionId, Context context) {
		mFragment fragment = new mFragment();
		Bundle bundle = new Bundle(1);
		bundle.putString(TITLE, title);
		bundle.putString(DESCRIPTION, description);
		bundle.putString(URL, url);
		bundle.putString(ID, suggestionId);
		fragment.setArguments(bundle);
		
		mContext = context;
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		String title = getArguments().getString(TITLE);
		String description = getArguments().getString(DESCRIPTION);
		final String url = getArguments().getString(URL);
		final String id = getArguments().getString(ID);
		
		View view =  inflater.inflate(R.layout.fragment_layout, 
				container, false);
		TextView titleText = (TextView) view.findViewById(R.id.title);
		titleText.setText(title);
		TextView descText = (TextView) view.findViewById(R.id.description);
		descText.setText(description);
		
		Button webButton = (Button) view.findViewById(R.id.web_button);
		webButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("name", "website_click_"+id));
				params.add(new BasicNameValuePair("value", "1"));
				new Suggestion().new postData().execute(params);
				
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
						Uri.parse(url));
				startActivity(browserIntent);
				
			}
			
		});
		
		Button likeButton = (Button) view.findViewById(R.id.likeButton);
		likeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disabled.add(id);
				v.setEnabled(false);
				
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("name", "suggestion_select_"+id));
				params.add(new BasicNameValuePair("value", "1"));
				new Suggestion().new postData().execute(params);
				
				numBookmarked++;
				if(numBookmarked < MIN_BOOKMARKED) {
					Toast.makeText(mContext, R.string.bookmark, Toast.LENGTH_SHORT).show();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setMessage(R.string.bookmark);
					if(DisplayContext.contextIndex < DisplayContext.numContexts) {
						builder.setPositiveButton("Select new city", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								numBookmarked = 0;
								Intent intent = new Intent(mContext,DisplayContext.class);
								intent.putExtra(EXTRA_CONTEXT, DisplayContext.contextIndex+1);
								mContext.startActivity(intent);
								
							}
						});
					} else {
						builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(mContext,Finish.class);
								mContext.startActivity(intent);
								
							}
						});
					}
					// If all the suggestions have been selected, only display the 
					// 'Select next city' button.
					if(numBookmarked < Suggestion.count) {
						builder.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
					}
					
					builder.create().show();
				}
			}
			
		});
		
		return view;
		
	}

}
