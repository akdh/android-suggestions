package com.example.survey2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

public class SuggestionList extends ExpandableListActivity {
	
	// PHP scripts
	private static final String URL_POST = "http://plg.uwaterloo.ca/~adeanhal/android/android2/post2.php";
	private static final String URL_LIST = "http://plg.uwaterloo.ca/~adeanhal/android/android2/list2.php";
	
	// minimum number of attractions to bookmark before option for new city
	public static final int MIN_BOOKMARKED = 2;
	public static int numBookmarked = 0;
	private JSONObject jsonData = null;
	ProgressDialog pDialog;
	public ExpandableListView expListView;
	ExpandableListAdapter exAdapter;
	Map<String,Object> lastExpanded = new HashMap<String,Object>();
	public static int context;
	public static JSONObject attractions;
	public static int groupPosition;
	private AlertDialog connectionErrorDialog;
//	public static String userid;
	
	public static final String TITLE = "com.example.survey.TITLE";
	public static final String DESCRIPTION = "com.example.survey.DESCRIPTION";
	public static final String URL = "com.example.survey.URL";
	public static final String ID = "com.example.survey.ID";
	public static final String LAST_EXPANDED = "com.example.survey.LAST_EXPANDED";
	public static final String JSONDATA = "com.example.survey.JSONDATA";
//	public final static String EXTRA_USERID = "com.example.survey2.USERID";
	public final static String EXTRA_CONTEXT = "com.example.survey2.CONTEXT";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestion_list);
		
		lastExpanded.put("pos", -1);
		lastExpanded.put("id", "");
		lastExpanded.put("time",0L);
		
		Intent intent = getIntent();
		context = intent.getIntExtra(EXTRA_CONTEXT, 0);
//		userid = intent.getStringExtra(EXTRA_USERID);
		
		expListView = getExpandableListView();
		Object[] data = (Object[]) getLastNonConfigurationInstance();
		if(data != null) {
			jsonData = (JSONObject) data[0];
			lastExpanded = (Map<String, Object>) data[1];
			loadList(expListView);
			
		} else {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("context", Integer.toString(context)));
			new LoadSuggestionList().execute(params);
		}
		
		expListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
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
					int visibleItemCount, int totalItemCount) {	}
		});
		
		expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				if(groupPosition == (Integer) lastExpanded.get("pos")) {
					// Click to close same group
					exAdapter.getChildView((Integer) lastExpanded.get("pos")).findViewById(R.id.likeButton).setEnabled(true);
					
					params.add(new BasicNameValuePair("name", "suggestion_close_"+lastExpanded.get("id")));
					params.add(new BasicNameValuePair("value", "1"));
					params.add(new BasicNameValuePair("time", Long.toString((System.nanoTime() - (Long)lastExpanded.get("time"))/1000000L)));
					new postData().execute(params);
					
					lastExpanded.put("pos", -1);
					lastExpanded.put("id", "");
					lastExpanded.put("time", 0L);
				}
				return false;
			}
		});
		
		expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
			
			@Override
			public void onGroupExpand(int groupPosition) {
				SuggestionList.groupPosition = groupPosition;
				String suggestionId = null;
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				try {
					JSONObject suggestion = SuggestionList.attractions.getJSONObject(String.valueOf(groupPosition + 1));
					suggestionId = suggestion.getString("runid_num") + "_" + 
						suggestion.getString("profile") + "_" + 
						suggestion.getString("context") + "_" + 
						suggestion.getString("rank");
				} catch (JSONException e) {
					e.printStackTrace();
					onConnectionError();
				}
				
				if((Integer)lastExpanded.get("pos") != -1 && (groupPosition != (Integer) lastExpanded.get("pos"))) {
					exAdapter.getChildView((Integer)lastExpanded.get("pos")).findViewById(R.id.likeButton).setEnabled(true);
					expListView.collapseGroup((Integer) lastExpanded.get("pos"));
					
					params.add(new BasicNameValuePair("name2", "suggestion_close_"+lastExpanded.get("id")));
					params.add(new BasicNameValuePair("value2", "1"));
					params.add(new BasicNameValuePair("time", Long.toString((System.nanoTime() - (Long)lastExpanded.get("time"))/1000000L)));

				}
				params.add(new BasicNameValuePair("name", "suggestion_open_"+suggestionId));
				params.add(new BasicNameValuePair("value", "1"));
				
				lastExpanded.put("pos", groupPosition);
				lastExpanded.put("id", suggestionId);
				lastExpanded.put("time", System.nanoTime());
				
				new postData().execute(params);
				
				
				if(exAdapter.getGroupDisabled(groupPosition)) {
					exAdapter.getChildView(groupPosition).findViewById(R.id.likeButton).setEnabled(false);
				} 
				
				
			}
		});
	}
	
	public void loadList(ExpandableListView expListView) {
		TreeSet<Integer> treeSet = new TreeSet<Integer>();
		List<Map<String,String>> titleList = new ArrayList<Map<String,String>>();
		List<List<Map<String,String>>> groupData = new ArrayList<List<Map<String,String>>>();
		
		Iterator<String> jsonKeys = SuggestionList.attractions.keys();
		while(jsonKeys.hasNext()) {
			int key = Integer.valueOf(jsonKeys.next());
			treeSet.add(key);
		}
		Iterator<Integer> keys = treeSet.iterator();
		while(keys.hasNext()) {
			try {
				Map<String,String> titleMap = new HashMap<String,String>();
				Map<String,String> descMap = new HashMap<String,String>();
				List<Map<String,String>> descList = new ArrayList<Map<String,String>>();
				
				JSONObject suggestion = SuggestionList.attractions.getJSONObject(keys.next().toString());
				titleMap.put("title", suggestion.getString("title"));
				titleList.add(titleMap);
				descMap.put("description", suggestion.getString("description"));
				descList.add(descMap);
				descMap.put("id", suggestion.getString("url") + "_" + 
						suggestion.getString("context") + "_" + 
						suggestion.getString("rank"));
				descMap.put("url", suggestion.getString("url"));
				
				groupData.add(descList);
			} catch (JSONException e) {
				e.printStackTrace();
				onConnectionError();
			}
		}
		exAdapter = new ExpandableListAdapter(this, titleList, R.layout.group_layout, new String[] {"title"}, 
						new int[] {R.id.groupText}, groupData, 	R.layout.desc_layout, new String[] {"description", "url"}, 
						new int[] {R.id.descText});
		expListView.setAdapter(exAdapter);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.suggestion_list, menu);
		return true;
	}
	
	@Override
	public Object[] onRetainNonConfigurationInstance() {
		Object[] data = new Object[2];
		data[0] = SuggestionList.attractions;
		data[1] = lastExpanded;
		return data;
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
		connectionErrorDialog = builder.create();
		connectionErrorDialog.show();
	}
	
	public class LoadSuggestionList extends AsyncTask<List<NameValuePair>, String, String> {
		
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
			return HttpRequest.makeHttpRequest(URL_LIST, "GET", params[0]);
		}
		@Override
		protected void onPostExecute(String result) {
			if (result == "ConnectionError") {
				pDialog.dismiss();
				onConnectionError();
			} else {
				try {
					jsonData = new JSONObject(result);
					if(jsonData.getInt("success") == 1) {
						SuggestionList.attractions = jsonData.getJSONObject("attractions");
					} else {
						pDialog.dismiss();
						onConnectionError();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					pDialog.dismiss();
					onConnectionError();
				}
				loadList(expListView);
				pDialog.dismiss();
			}
		}
	}
	
	public class postData extends AsyncTask<List<NameValuePair>, Void, Void> {
		@Override
		protected Void doInBackground(List<NameValuePair>... params) {
			HttpRequest.makeHttpRequest(URL_POST, "POST", params[0]);
			return null;
		}
	}
}