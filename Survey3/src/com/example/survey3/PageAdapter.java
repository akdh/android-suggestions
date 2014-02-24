package com.example.survey3;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PageAdapter extends FragmentStatePagerAdapter{
	
	private List<Fragment> fragments;

	public PageAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
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
