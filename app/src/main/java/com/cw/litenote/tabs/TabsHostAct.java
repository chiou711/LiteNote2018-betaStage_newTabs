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
import com.cw.litenote.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

public class TabsHostAct extends AppCompatActivity implements TabLayout.OnTabSelectedListener
{
    FragmentActivity mAct;
    View rootView;
	ViewPager viewPager;
	ViewPagerAdapter viewPagerAdapter;
	public static DB_folder mDbFolder;

	//todo
    public static List<Page_new> mFragmentList;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.pager);

        mFragmentList = new ArrayList<>();

        DB_drawer dB_drawer = new DB_drawer(this);
        dB_drawer.open();
        int folderTableId = dB_drawer.getFolderTableId(FolderUi.getFocus_folderPos(),false);
        dB_drawer.close();
        mDbFolder = new DB_folder(this,folderTableId);

        viewPagerAdapter = new ViewPagerAdapter(this.getSupportFragmentManager());

        // add pages
        this.addPages(viewPagerAdapter);

        // setup view pager
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
	}

    private void addPages(ViewPagerAdapter pagerAdapter)
    {
//        pagerAdapter.addFragment(new CrimeFragment());
//        pagerAdapter.addFragment(new DramaFrgament());
//        pagerAdapter.addFragment(new DocumentaryFragment());


        ///
        int pageCount = mDbFolder.getPagesCount(true);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = mDbFolder.getPageTableId(i, true);
//            pageTableId = 1;
            Page_new.mDb_page = new DB_page(mAct,pageTableId);
//            DB_page.setFocusPage_tableId(pageTableId);
            System.out.println("TabsHosAct / _addPages / pageTableId = " + pageTableId);
            pagerAdapter.addFragment(new Page_new(pageTableId));//add runnable?
//            pagerAdapter.addFragment(new Page1(1));
//            pagerAdapter.addFragment(new Page2(1));
//            pagerAdapter.addFragment(new Page3(1));
        }
        ///

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
            if(pageTableId == Pref.getPref_focusView_page_tableId(this)) {
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
        ArrayList<Page_new> mFragmentList = new ArrayList<>();

		public ViewPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position)
        {
            System.out.println("TabsHosAct / ViewPagerAdapter / _getItem / position = " + position);
            return mFragmentList.get(position);
        }

        // add fragment
        public void addFragment(Page_new fragment) {
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