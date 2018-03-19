/*
 * Copyright (C) 2018 CW Chiu <chiou711@gmail.com>
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_drawer;
import com.cw.litenote.db.DB_folder;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.folder.FolderUi;
import com.cw.litenote.page.Page_new;
import com.cw.litenote.page.Page_simple;
import com.cw.litenote.util.ColorSet;
import com.cw.litenote.util.preferences.Pref;

import java.util.ArrayList;

public class TabsHost_new extends AppCompatDialogFragment implements TabLayout.OnTabSelectedListener
{
    ViewPager viewPager;
    public static DB_folder mDbFolder;

    int selectedPos;
    int reSelectedPos;
    int unSelectedPos;


    public TabsHost_new()
    {
        System.out.println("TabsHosAct / construct");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("TabsHosAct / _onCreate");


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tabs_host_new, container, false);
        // tool bar
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.tabanim_toolbar);
//        rootView.setSupportActionBar(toolbar);

        // DB
        DB_drawer dB_drawer = new DB_drawer(getActivity());
        dB_drawer.open();
        int folderTableId = dB_drawer.getFolderTableId(FolderUi.getFocus_folderPos(),false);
        dB_drawer.close();
        mDbFolder = new DB_folder(getActivity(),folderTableId);

        // view pager
        viewPager = (ViewPager) rootView.findViewById(R.id.pager);

        // adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());

        // add pages to adapter
        addPages(adapter);

        // set adapter of view pager
        viewPager.setAdapter(adapter);

        // tab layout
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setSelectedTabIndicatorHeight(10);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setBackgroundColor(ColorSet.getBarColor(getActivity()));
        return rootView;//super.onCreateView(inflater, container, savedInstanceState);
    }

    private void addPages(ViewPagerAdapter adapter)
    {
        int pageCount = mDbFolder.getPagesCount(true);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = mDbFolder.getPageTableId(i, true);
            System.out.println("TabsHosAct / _addPages / pageTableId = " + pageTableId);
            adapter.addFragment(new Page_new(pageTableId));
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        System.out.println("-> TabsHosAct / _onTabSelected: " + tab.getPosition());

        selectedPos = tab.getPosition();
        int pageTableId = mDbFolder.getPageTableId(selectedPos, true);
        Pref.setPref_focusView_page_tableId(getActivity(), pageTableId);

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

        // default
        selectedPos = 0;

        // Get current position
        int pageCount = mDbFolder.getPagesCount(true);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = mDbFolder.getPageTableId(i, true);

            if(pageTableId == Pref.getPref_focusView_page_tableId(getActivity()))
                selectedPos = i;
        }

        viewPager.setCurrentItem(selectedPos);
        System.out.println("TabsHostAct / _onResume / selectedPos = " + selectedPos);
    }


    /**
     *  View Pager Adapter Class
     */
    public class ViewPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Page_new> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Page_new getItem(int position)
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
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mDbFolder.getPageTitle(position,true);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
//			System.out.println("TabsHosAct / ViewPagerAdapter / _setPrimaryItem / position = " + position);
        }

    }

}