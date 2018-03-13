/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cw.litenote.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.page.Page;

public class CardFragment extends Fragment {

	private static final String ARG_PAGE_TABLE_ID = "pageTableId";

	private int pageTableId;

	public static CardFragment newInstance(int pageTableId) {
		System.out.println("CardFragment / _newInstance");
		CardFragment f = new CardFragment();
		Bundle b = new Bundle();
		b.putInt(ARG_PAGE_TABLE_ID, pageTableId);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("CardFragment / _onCreate");

		pageTableId = getArguments().getInt(ARG_PAGE_TABLE_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		System.out.println("CardFragment / _onCreateView");
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		FrameLayout fl = new FrameLayout(getActivity());
		fl.setLayoutParams(params);

		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
				.getDisplayMetrics());

		TextView v = new TextView(getActivity());
		params.setMargins(margin, margin, margin, margin);
		v.setLayoutParams(params);
		v.setLayoutParams(params);
		v.setGravity(Gravity.CENTER);
		v.setBackgroundResource(R.drawable.psts_background_card);
		v.setText("Page Table " + pageTableId);
		fl.addView(v);
//		Page page = new Page();//??? null?

//		fl.addView(page.mDndListView);
		return fl;
	}

}