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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.design.widget.TabLayout;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_drawer;
import com.cw.litenote.db.DB_folder;
import com.cw.litenote.folder.FolderUi;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.page.Page_new;
import com.cw.litenote.util.preferences.Pref;

import java.util.ArrayList;
import java.util.List;

import tabanimation.TabAnimation;

public class TabsHost_new extends AppCompatDialogFragment  implements TabLayout.OnTabSelectedListener
{
    FragmentActivity mAct;
    View rootView;
	ViewPager viewPager;
	ViewPagerAdapter viewPagerAdapter;
	public static DB_folder mDbFolder;

	//todo
    public static List<Page_new> mFragmentList;
//    public static List<TabAnimation.DummyFragment> mFragmentList;

//    static int selectedPos;
    int selectedPos;
    int reSelectedPos;
    int unSelectedPos;

    public TabsHost_new()
    {
        System.out.println("TabsHost_new / construct");


//        if(mFragmentList != null) {
//            for (int i = 0; i < mFragmentList.size(); i++) {
//                mAct.getSupportFragmentManager().beginTransaction().remove(mFragmentList.get(i)).commit();
//            }
//        }

        mFragmentList = new ArrayList<>();
        mAct = MainAct.mAct;//getActivity();

        DB_drawer dB_drawer = new DB_drawer(mAct);
        dB_drawer.open();
        int folderTableId = dB_drawer.getFolderTableId(FolderUi.getFocus_folderPos(),false);
        dB_drawer.close();
        mDbFolder = new DB_folder(mAct,folderTableId);

        if(viewPagerAdapter == null) {
            System.out.println("TabsHost_new / construct / viewPagerAdapter = null");
            viewPagerAdapter = new ViewPagerAdapter(mAct.getSupportFragmentManager());
            int pageCount = mDbFolder.getPagesCount(true);

            for(int i=0;i<pageCount;i++)
            {
                int pageTableId = mDbFolder.getPageTableId(i, true);
//                viewPagerAdapter.addFrag(Page_new.newInstance(pageTableId));
                viewPagerAdapter.addFrag(new Page_new(pageTableId));
            }
        }

//        if(viewPagerAdapter == null) {
//            System.out.println("TabAnimation / construct / viewPagerAdapter = null");
//            viewPagerAdapter = new ViewPagerAdapter(mAct.getSupportFragmentManager());
//            viewPagerAdapter.addFrag(new TabAnimation.DummyFragment(
//                    ContextCompat.getColor(mAct, R.color.blue_grey)));
//            viewPagerAdapter.addFrag(new TabAnimation.DummyFragment(
//                    ContextCompat.getColor(mAct, R.color.amber)));
//            viewPagerAdapter.addFrag(new TabAnimation.DummyFragment(
//                    ContextCompat.getColor(mAct, R.color.cyan)));
//        }
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        System.out.println("TabsHost_new / _onCreate");
	}

//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
//	{
//		System.out.println("TabsHost_new / _onCreateView");
//		if(FolderUi.getFolder_pagesCount(mAct,FolderUi.getFocus_folderPos()) == 0) {
//		rootView = inflater.inflate(R.layout.page_view_blank, container, false);
//		rootView = inflater.inflate(R.layout.psts_main, container, false);

//			System.out.println("TabsHost_new / _onCreateView / rootView is empty TextView");
//		}
//		else {
//			// set layout by orientation
//			if (Util.isLandscapeOrientation(mAct))
//				rootView = inflater.inflate(R.layout.page_view_landscape, container, false);
//			else
//        rootView = inflater.inflate(R.layout.page_view_portrait, container, false);
//        rootView = inflater.inflate(R.layout.page_view_portrait_new, container, false);
//		}

//        if(viewPagerAdapter == null) {
//            System.out.println("TabsHost_new / construct / viewPagerAdapter = null");
//            viewPagerAdapter = new ViewPagerAdapter(mAct.getSupportFragmentManager());
//            int pageCount = mDbFolder.getPagesCount(true);
//
//            for(int i=0;i<pageCount;i++)
//            {
//                int pageTableId = mDbFolder.getPageTableId(i, true);
//                viewPagerAdapter.addFrag(new Page_new(pageTableId));//todo: add async?
//            }
//        }

//		viewPager = (ViewPager) rootView.findViewById(R.id.pager);
//
//        // setup view pager
//        viewPager.setAdapter(viewPagerAdapter);
////        viewPagerAdapter.notifyDataSetChanged();
//
//        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
//		tabLayout.setupWithViewPager(viewPager);
//        tabLayout.setOnTabSelectedListener(this);

//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                System.out.println("-> TabsHost_new / _onTabSelected: " + tab.getPosition());
//                selectedPos = tab.getPosition();
//                Pref.setPref_focusView_page_tableId(mAct, mDbFolder.getPageTableId(selectedPos, true));
//
////                if(isResumed())
//				    viewPager.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                System.out.println("TabsHost_new / _onTabUnselected: " + tab.getPosition());
//                unSelectedPos = tab.getPosition();
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//                System.out.println("TabsHost_new / _onTabReselected: " + tab.getPosition());
//                reSelectedPos = tab.getPosition();
//            }
//        });

//		return rootView;
//	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAct.setContentView(R.layout.psts_main);
        System.out.println("TabsHost_new / _onActivityCreated");

        viewPager = (ViewPager) mAct.findViewById(R.id.pager);

        // setup view pager
        viewPager.setAdapter(viewPagerAdapter);
//        viewPagerAdapter.notifyDataSetChanged();

        TabLayout tabLayout = (TabLayout) mAct.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        System.out.println("-> TabsHost_new / _onTabSelected: " + tab.getPosition());
        selectedPos = tab.getPosition();
        Pref.setPref_focusView_page_tableId(mAct, mDbFolder.getPageTableId(selectedPos, true));

//                if(isResumed())
        viewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        System.out.println("TabsHost_new / _onTabUnselected: " + tab.getPosition());
        unSelectedPos = tab.getPosition();
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        System.out.println("TabsHost_new / _onTabReselected: " + tab.getPosition());
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
            if(pageTableId == Pref.getPref_focusView_page_tableId(mAct))
                selectedPos = i;
        }

//        viewPager.setCurrentItem(selectedPos);
//        viewPagerAdapter.notifyDataSetChanged();
        System.out.println("TabsHost_new / _onResume / selectedPos = " + selectedPos);
	}


    /**
     *  View Pager Adapter Class
     */
	public class ViewPagerAdapter extends FragmentPagerAdapter {

		public ViewPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position)
        {
            System.out.println("TabsHost_new / ViewPagerAdapter / _getItem / position = " + position);
            return mFragmentList.get(position);

//            int pageTableId = mDbFolder.getPageTableId(position, true);
//                viewPagerAdapter.addFrag(Page_new.newInstance(pageTableId));//todo: add async?
//            pageTableId = 1;//todo how to set current page
//            return new Page_new(pageTableId);
        }

        //todo
        public void addFrag(Page_new fragment) {
			mFragmentList.add(fragment);
		}

//        public void addFrag(TabAnimation.DummyFragment fragment) {
//            mFragmentList.add(fragment);
//        }

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
			System.out.println("TabsHost_new / ViewPagerAdapter / _setPrimaryItem / position = " + position);
//            viewPager.setCurrentItem(selectedPos);
//            notifyDataSetChanged();
            int pageTableId = mDbFolder.getPageTableId(position, true);
//                viewPagerAdapter.addFrag(Page_new.newInstance(pageTableId));//todo: add async?
        }

//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return view.equals(object);
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            System.out.println("TabsHost_new / ViewPagerAdapter / _destroyItem / position = " + position);
////            super.destroyItem(container,position,object);
//            container.removeView((View) object);
//        }
    }

}