package com.example.survey3;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;


public class PageChangeListener extends FragmentStatePagerAdapter 
	implements OnPageChangeListener {
	
	public static final String ID = "com.example.survey.ID";
	private List<Fragment> fragments;
	private static Long prevTime = 0L;
	private static String prevId;

	public PageChangeListener(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		
	}

	@Override
	public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {
		
	}

	@Override
	public void onPageSelected(int pos) {
		Long time = System.nanoTime();
		String id = getItem(pos).getArguments().get(ID).toString();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", "suggestion_view_"+id));
		params.add(new BasicNameValuePair("value", "1"));
		if(prevTime != 0L) {
			params.add(new BasicNameValuePair("name2", "suggestion_view_"+prevId));
			params.add(new BasicNameValuePair("duration", Long.toString((time - prevTime)/1000000L)));
		}
		new Suggestion().new postData().execute(params);
		Log.d("log","onPageSelected: "+pos+", "+id);
		prevTime = time;
		prevId = id;
	}

	@Override
	public Fragment getItem(int pos) {
		return this.fragments.get(pos);
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}

	
}
