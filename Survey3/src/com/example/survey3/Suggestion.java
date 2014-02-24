package com.example.survey3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.survey3.HttpRequest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class Suggestion extends FragmentActivity {

	private PageAdapter pageAdapter;
	private ProgressDialog pDialog;
	private AlertDialog connectionErrorDialog;
	private JSONObject jsonData;
	private PageChangeListener listener;
	private final Context mContext = this;
	public JSONObject attractions;
	public static int context;
	public static int count;
	
	
	
	private static final String url_list = "http://plg.uwaterloo.ca/~adeanhal/android/android3/list3.php";
	private static final String url_post = "http://plg.uwaterloo.ca/~adeanhal/android/android3/post3.php";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestion);
		
		Intent intent = getIntent();
		context = intent.getIntExtra(DisplayContext.EXTRA_CONTEXT, 0);
		
		attractions = (JSONObject) getLastCustomNonConfigurationInstance();
		if (attractions == null) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("context", Integer.toString(context)));
			new LoadSuggestionList().execute(params);
		} else {
			loadList();
		}
	}
	
	@Override
	public JSONObject onRetainCustomNonConfigurationInstance() {
		return attractions;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.suggestion, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
		super .onPause();
		if(connectionErrorDialog != null && 
				connectionErrorDialog.isShowing() == true) {
			connectionErrorDialog.dismiss();
		}
	}

	private void onConnectionError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(R.string.dialog_connection_error);
		builder.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				new LoadSuggestionList().execute(params);
			}
		});
		connectionErrorDialog = builder.create();
		connectionErrorDialog.show();
	}

	public class LoadSuggestionList extends AsyncTask<List<NameValuePair>, String, String> {

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(Suggestion.this);
			pDialog.setMessage("Loading...");
	        pDialog.setIndeterminate(true);
	        pDialog.setCancelable(false);
	        pDialog.show();
		}
		@Override
		protected String doInBackground(List<NameValuePair>... params) {
			return HttpRequest.makeHttpRequest(url_list, "GET", params[0]);
		}
		@Override
		protected void onPostExecute(String result) {
			if (result == "ConnectionError") {
				pDialog.dismiss();
				onConnectionError();
			}
			else {
				try {
					jsonData = new JSONObject(result);
					if(jsonData.getInt("success") == 1) {
						attractions = jsonData.getJSONObject("attractions");
					} else {
						pDialog.dismiss();
						onConnectionError();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					pDialog.dismiss();
					onConnectionError();
				}
				loadList();
				pDialog.dismiss();
			}
		}
	}
	
	private void loadList() {
		TreeSet<Integer> treeSet = new TreeSet<Integer>();
		List<Fragment> fragmentList = new ArrayList<Fragment>();
		Iterator<String> jsonKeys = attractions.keys();
		while(jsonKeys.hasNext()) {
			int key = Integer.valueOf(jsonKeys.next());
			treeSet.add(key);
		}
		Iterator<Integer> keys = treeSet.iterator();
		while(keys.hasNext()) {
			String key = keys.next().toString();
			JSONObject suggestion;
			try {
				suggestion = attractions.getJSONObject(key);
				String title = suggestion.getString("title");
				String description = suggestion.getString("description");
				String url = suggestion.getString("url");
				String suggestionId = suggestion.getString("runid_num") + "_" + 
						suggestion.getString("profile") + "_" + 
						suggestion.getString("context") + "_" + 
						suggestion.getString("rank");
				fragmentList.add(mFragment.newInstance(title,description,url,suggestionId,mContext));
			} catch (JSONException e) {
				e.printStackTrace();
				onConnectionError();
			}
		}
		pageAdapter = new PageAdapter(getSupportFragmentManager(), fragmentList);
		count = pageAdapter.getCount();
		listener = new PageChangeListener(getSupportFragmentManager(), fragmentList);
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(pageAdapter);
		viewPager.setOnPageChangeListener(listener);
		listener.onPageSelected(viewPager.getCurrentItem());
	}
	
	public class postData extends AsyncTask<List<NameValuePair>, Void, String> {
		@Override
		protected String doInBackground(List<NameValuePair>... params) {
			String data = HttpRequest.makeHttpRequest(url_post, "POST", params[0]);
			return data;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(result == "ConnectionError") {
				onConnectionError();
			}
		}
	}
}
