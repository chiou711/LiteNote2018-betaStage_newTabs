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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_drawer;
import com.cw.litenote.db.DB_folder;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.folder.FolderUi;
import com.cw.litenote.mFragments.CrimeFragment;
import com.cw.litenote.mFragments.DocumentaryFragment;
import com.cw.litenote.mFragments.DramaFrgament;
import com.cw.litenote.mFragments.MyPagerAdapter;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.page.Page_new;
import com.cw.litenote.page.Page_simple;
import com.cw.litenote.util.Util;
import com.cw.litenote.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

public class TabsHostAct extends AppCompatActivity implements TabLayout.OnTabSelectedListener
{
    FragmentActivity mAct;
	ViewPager viewPager;
	public static DB_folder mDbFolder;

    int selectedPos;
    int reSelectedPos;
    int unSelectedPos;

    public TabsHostAct()
    {
        System.out.println("TabsHosAct / construct");
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        System.out.println("TabsHosAct / _onCreate");

        setContentView(R.layout.psts_main);

        // tool bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);

        // DB
        DB_drawer dB_drawer = new DB_drawer(this);
        dB_drawer.open();
        int folderTableId = dB_drawer.getFolderTableId(FolderUi.getFocus_folderPos(),false);
        dB_drawer.close();
        mDbFolder = new DB_folder(this,folderTableId);

        // view pager
        viewPager = (ViewPager) findViewById(R.id.pager);

        // adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this.getSupportFragmentManager());

        // add pages to adapter
        addPages(adapter);

        // set adapter of view pager
        viewPager.setAdapter(adapter);

        // tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
	}

    private void addPages(ViewPagerAdapter adapter)
    {
        //todo
        // case 1
//        int pageCount = mDbFolder.getPagesCount(true);
//        for(int i=0;i<pageCount;i++)
//        {
//            int pageTableId = mDbFolder.getPageTableId(i, true);
//            System.out.println("TabsHosAct / _addPages / pageTableId = " + pageTableId);
//            Page_new.mDb_page = new DB_page(mAct, pageTableId);
//            adapter.addFragment(new Page_new(pageTableId));
//        }

        // todo
        // case 2
//        pagerAdapter.addFragment(new CrimeFragment());
//        pagerAdapter.addFragment(new DramaFrgament());
//        pagerAdapter.addFragment(new DocumentaryFragment());

        // todo
        // case 3
        adapter.addFragment(new Page_simple(1));
        adapter.addFragment(new Page_simple(2));
//        adapter.addFragment(new Page_simple(3));
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        System.out.println("-> TabsHosAct / _onTabSelected: " + tab.getPosition());

        selectedPos = tab.getPosition();

        int pageTableId = mDbFolder.getPageTableId(selectedPos, true);

        Pref.setPref_focusView_page_tableId(this, pageTableId);

        Page_new.pageTableId = pageTableId;

        viewPager.setCurrentItem(selectedPos);
    }
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        System.out.println("TabsHosAct / _onTabUnselected: " + tab.getPosition());
        unSelectedPos = tab.getPosition();
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        System.out.println("TabsHosAct / _onTabReselected: " + tab.getPosition());
        reSelectedPos = tab.getPosition();
    }

    @Override
	public void onResume() {
		super.onResume();

		// Get current position
        int pageCount = mDbFolder.getPagesCount(true);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = mDbFolder.getPageTableId(i, true);

            if(pageTableId == Pref.getPref_focusView_page_tableId(this))
            {
                selectedPos = i;
                Page_new.pageTableId = pageTableId;
            }
        }

        viewPager.setCurrentItem(selectedPos);
//        viewPagerAdapter.notifyDataSetChanged();
        System.out.println("TabsHostAct / _onResume / selectedPos = " + selectedPos);
	}


    /**
     *  View Pager Adapter Class
     */
	public class ViewPagerAdapter extends FragmentPagerAdapter {
	    // todo
//        ArrayList<Page_new> mFragmentList = new ArrayList<>();
        ArrayList<Page_simple> mFragmentList = new ArrayList<>();

		public ViewPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position)
        {
            System.out.println("TabsHosAct / ViewPagerAdapter / _getItem / position = " + position);
            return mFragmentList.get(position);
        }

        // todo
        // add fragment
//        public void addFragment(Page_new fragment) {
            public void addFragment(Page_simple fragment) {
			mFragmentList.add(fragment);
		}

        @Override
        public int getCount(){
            return mDbFolder.getPagesCount(true);
        }

		@Override
		public CharSequence getPageTitle(int position){
			return mDbFolder.getPageTitle(position,true);
		}

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
			System.out.println("TabsHosAct / ViewPagerAdapter / _setPrimaryItem / position = " + position);
        }

    }

}