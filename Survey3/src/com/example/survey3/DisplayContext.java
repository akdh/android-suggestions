package com.example.survey3;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class DisplayContext extends Activity {
	
	private static final String URL_CONTEXT = "http://plg.uwaterloo.ca/~adeanhal/android/android3/contexts3.php";
	
	public final static String EXTRA_CONTEXT = "com.example.survey3.CONTEXT";
	
//	String userid;
	
	static JSONObject contexts; // {contextIndex:{context:, context_city:}}
	static int contextIndex;
	public static int numContexts; // number of contexts in the database
	static int context;
	String contextCity; // City,State of the context

	private AlertDialog connectionErrorDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_context);
		
		contextCity = (String) getLastNonConfigurationInstance();
		if(contextCity == (String) null) {
			Intent intent = getIntent();
			contextIndex = intent.getIntExtra(EXTRA_CONTEXT, 1);
			
			if(contextIndex == 1) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				new GetContexts().execute(params);
			} else {
				loadContext(Integer.toString(contextIndex));
			}
			
		} else {
			TextView textView = (TextView) findViewById(R.id.context_textview);
			textView.setText("You are in "+contextCity+".\n" +
					"Select an activity that you would like to do.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_context, menu);
		return true;
	}
	
	@Override
	public String onRetainNonConfigurationInstance() {
		return contextCity;
	}
	
	private void onConnectionError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_connection_error);
		builder.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				new GetContexts().execute(params);
			}
		});
		connectionErrorDialog = builder.create();
		connectionErrorDialog.show();
	}
	
	public class GetContexts extends AsyncTask<List<NameValuePair>, Void, String> {
		ProgressDialog pDialog;
		
		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(DisplayContext.this);
			pDialog.setMessage("Loading...");
	        pDialog.setIndeterminate(true);
	        pDialog.setCancelable(false);
	        pDialog.show();
		}
		
		@Override
		protected String doInBackground(List<NameValuePair>... params) {
			return HttpRequest.makeHttpRequest(URL_CONTEXT, "GET", params[0]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result == "ConnectionError") {
				pDialog.dismiss();
				onConnectionError();
			} else {
				try {
					JSONObject jsonData = new JSONObject(result);
					if(jsonData.getInt("success") == 1) {
						DisplayContext.contexts = jsonData.getJSONObject("contexts");
						DisplayContext.numContexts = DisplayContext.contexts.length();
					} else {
						pDialog.dismiss();
						onConnectionError();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					pDialog.dismiss();
					onConnectionError();
				}
				loadContext(Integer.toString(contextIndex));
				pDialog.dismiss();
			}
		}
	}
	
	public void loadContext(String contextNum) {
		try {
			contextCity = DisplayContext.contexts.getJSONObject(contextNum).getString("context_city");
			DisplayContext.context = Integer.valueOf(DisplayContext.contexts.getJSONObject(contextNum).getString("context"));
			TextView textView = (TextView) findViewById(R.id.context_textview);
			textView.setText("You are in "+contextCity+".\n" +
					"Select an activity that you would like to do.");
		} catch (JSONException e) {
			e.printStackTrace();
			onConnectionError();
		}
	}
	
	public void onContinueClick(View view) {
		Intent intent = new Intent(this, Suggestion.class);
//		intent.putExtra(EXTRA_USERID, userid);
		intent.putExtra(EXTRA_CONTEXT, DisplayContext.context);
		startActivity(intent);
	}
}