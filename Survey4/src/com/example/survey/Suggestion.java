package com.example.survey;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Suggestion extends Activity {
	String id;
	String url;
	public static final String URL = "com.example.survey.URL";
	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestion);
		
		Intent intent = getIntent();
		id = intent.getStringExtra(SuggestionList.ID);
		String title = intent.getStringExtra(SuggestionList.TITLE);
		String description = intent.getStringExtra(SuggestionList.DESCRIPTION);
		url = intent.getStringExtra(SuggestionList.URL);
		
		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText(title);
		TextView descriptionTextView = (TextView) findViewById(R.id.description);
		descriptionTextView.setText(description);
		
		ImageButton likeButton = (ImageButton) findViewById(R.id.likeButton);
		if(SuggestionList.disabled.contains(id)) {
			likeButton.setEnabled(false);
			likeButton.setColorFilter(Color.argb(0, 255, 255, 0));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.suggestion, menu);
		return true;
	}
	
	public void loadWebPage(View view) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", "website_click_"+id));
		params.add(new BasicNameValuePair("value", "1"));
		new SuggestionList().new postData().execute(params);
		
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}
	
	public void onBackButtonClick(View view) {
		onBackPressed();
	}
	
	public void onLikeButtonClick(View view) {
		SuggestionList.disabled.add(id);
		view.setEnabled(false);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", "suggestion_select_"+id));
		params.add(new BasicNameValuePair("value", "1"));
		new SuggestionList().new postData().execute(params);
		
		SuggestionList.numBookmarked++;
		if(SuggestionList.numBookmarked < SuggestionList.MIN_BOOKMARKED) {
			Toast.makeText(mContext, R.string.bookmark, Toast.LENGTH_SHORT).show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage(R.string.bookmark);
			if(DisplayContext.contextIndex < DisplayContext.numContexts) {
				builder.setPositiveButton("Select new city", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SuggestionList.numBookmarked = 0;
						Intent intent = new Intent(mContext,DisplayContext.class);
						intent.putExtra(DisplayContext.EXTRA_CONTEXT, DisplayContext.contextIndex+1);
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
			if(SuggestionList.numBookmarked < SuggestionList.count) {
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
	
	public void onBackPressed() {
		super .onBackPressed();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", "suggestion_click_"+id));
		params.add(new BasicNameValuePair("duration", Long.toString((System.nanoTime() - SuggestionList.time)/1000000L)));
		new SuggestionList().new postData().execute(params);
	}

}
