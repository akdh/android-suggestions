package com.example.survey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Button;

public class SuggestionList extends ListActivity {
	
	private static final String url_list = "http://plg.uwaterloo.ca/~adeanhal/android/android1/list1.php";
	private static final String url_post = "http://plg.uwaterloo.ca/~adeanhal/android/android1/post1.php";
	private JSONObject jsonData;
	public JSONObject attractions;
	public static int context;
	public static List<String> disabled = new ArrayList<String>();
	public static int numBookmarked = 0;
	// minimum number of attractions to bookmark before option for new city
	public static final int MIN_BOOKMARKED = 2;
	public static int count;
	public static Long time;
	public static final String TITLE = "com.example.survey.TITLE";
	public static final String DESCRIPTION = "com.example.survey.DESCRIPTION";
	public static final String URL = "com.example.survey.URL";
	public static final String ID= "com.example.survey.ID";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestion_list);
		
		Intent intent = getIntent();
		context = intent.getIntExtra(DisplayContext.EXTRA_CONTEXT, 0);

		
		attractions = (JSONObject) getLastNonConfigurationInstance();
		if(attractions == null) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("context", Integer.toString(context)));
			new LoadSuggestionList().execute(params);
		} else {
			loadList();
		}
		AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				Log.d(getLocalClassName(), "scroll state: "+scrollState);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("value", "1"));
				if(scrollState == 0) {
					params.add(new BasicNameValuePair("name", "end_scroll"));
				} else if(scrollState == 1) {
					params.add(new BasicNameValuePair("name", "start_scroll"));
				} else if(scrollState == 2) {
					params.add(new BasicNameValuePair("name", "start_fling"));
				}
				new postData().execute(params);
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
//				Log.d(getLocalClassName(), "onScroll called");
//				List<NameValuePair> params = new ArrayList<NameValuePair>();
//				params.add(new BasicNameValuePair("name", "scroll_list"));
//				params.add(new BasicNameValuePair("value", "1"));
//				new postData().execute(params);
			}
		};
		getListView().setOnScrollListener(scrollListener);
	}
	public void loadList() {
		TreeSet<Integer> treeSet = new TreeSet<Integer>();
		ArrayList<String> listArrayList = new ArrayList<String>();
		
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
				listArrayList.add(title);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		count = listArrayList.size();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(SuggestionList.this,R.layout.custom_list_layout,R.id.text1,listArrayList);
		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		//setListAdapter(adapter);
		
		
	}
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.suggestion_list, menu);
		return true;
	}
	
	@Override
	public JSONObject onRetainNonConfigurationInstance() {
		return attractions;
	}
	
	private void onConnectionError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_connection_error);
		builder.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				new LoadSuggestionList().execute(params);
			}
		});
		AlertDialog connectionErrorDialog = builder.create();
		connectionErrorDialog.show();
	}
	
	public class LoadSuggestionList extends AsyncTask<List<NameValuePair>, String, String> {
		ProgressDialog pDialog;
		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(SuggestionList.this);
			pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
		}
		@Override
		protected String doInBackground(List<NameValuePair>... params) {
			String data = HttpRequest.makeHttpRequest(url_list, "GET", params[0]);
			return data;
		}
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
	
	public class postData extends AsyncTask<List<NameValuePair>, Void, Void> {
		@Override
		protected Void doInBackground(List<NameValuePair>... params) {
			HttpRequest.makeHttpRequest(url_post, "POST", params[0]);
			return null;
		}
	}

	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		System.out.println("**********SUCCESFUULL**************");
		/*
		Button likeButton = (Button) v.findViewById(R.id.likeButton);
		likeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View button) {
				
			}
		});
		*/
		
		time = System.nanoTime();
		id++;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//String item = (String) getListAdapter().getItem(position);
		Intent intent = new Intent(this, Suggestion.class);
		try {
			JSONObject suggestion = attractions.getJSONObject(String.valueOf(id));
			String title = suggestion.getString("title");
			String description = suggestion.getString("description");
			String url = suggestion.getString("url");
			String suggestionId = suggestion.getString("runid_num") + "_" + 
					suggestion.getString("profile") + "_" + 
					suggestion.getString("context") + "_" + 
					suggestion.getString("rank");
			
			params.add(new BasicNameValuePair("name", "suggestion_click_"+suggestionId));
			params.add(new BasicNameValuePair("value", "1"));
			
			intent.putExtra(ID, suggestionId);
			intent.putExtra(TITLE, title);
			intent.putExtra(DESCRIPTION, description);
			intent.putExtra(URL, url);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new postData().execute(params);
		
		startActivity(intent);
		super.onListItemClick(l, v, position, id);
	}
	
	
}
