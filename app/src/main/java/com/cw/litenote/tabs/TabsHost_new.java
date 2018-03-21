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

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cw.litenote.R;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.page.Page_new;
import com.cw.litenote.util.ColorSet;
import com.cw.litenote.util.preferences.Pref;


public class TabsHost_new extends AppCompatDialogFragment implements TabLayout.OnTabSelectedListener
{
    static TabLayout tabLayout;
    ViewPager viewPager;
    TabsPagerAdapter adapter;
    public static int currPageTableId;

    public static int selectedPos;
    int reSelectedPos;
    int unSelectedPos;
    public static int lastPageTableId;


    public TabsHost_new()
    {
        System.out.println("TabsHost_new / construct");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("TabsHost_new / _onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tabs_host_new, container, false);
        // tool bar
//        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.tabanim_toolbar);
//        rootView.setSupportActionBar(toolbar);

        // view pager
        viewPager = (ViewPager) rootView.findViewById(R.id.pager);

        // adapter
        adapter = new TabsPagerAdapter(getActivity(),getActivity().getSupportFragmentManager());

        // add pages to adapter
        addPages(adapter);

        // set adapter of view pager
        viewPager.setAdapter(adapter);

        // set tab layout
        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setSelectedTabIndicatorHeight(10);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setBackgroundColor(ColorSet.getBarColor(getActivity()));

        return rootView;
    }

    /**
     * Add pages
     */
    private void addPages(TabsPagerAdapter adapter)
    {
        lastPageTableId = 0;
        int pageCount = adapter.mDbFolder.getPagesCount(true);
        System.out.println("TabsHost_new / _addPages / pageCount = " + pageCount);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = adapter.mDbFolder.getPageTableId(i, true);

            if(pageTableId > lastPageTableId)
                lastPageTableId = pageTableId;

            System.out.println("TabsHost_new / _addPages / pageTableId = " + pageTableId);
            adapter.addFragment(new Page_new(pageTableId));
        }
    }

    /**
     * Get last page table Id
     */
    public static int getLastPageTableId()
    {
        return lastPageTableId;
    }

    /**
     * Set last page table Id
     */
    public static void setLastPageTableId(int id)
    {
        lastPageTableId = id;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        System.out.println("-> TabsHost_new / _onTabSelected: " + tab.getPosition());

        selectedPos = tab.getPosition();
        int pageTableId = adapter.mDbFolder.getPageTableId(selectedPos, true);
        Pref.setPref_focusView_page_tableId(getActivity(), pageTableId);

        ///
        currPageTableId = pageTableId;
        ///


        viewPager.setCurrentItem(selectedPos);
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

        // default
        selectedPos = 0;

        // Get current position
        int pageCount = adapter.mDbFolder.getPagesCount(true);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = adapter.mDbFolder.getPageTableId(i, true);

            if(pageTableId == Pref.getPref_focusView_page_tableId(getActivity())) {
                selectedPos = i;
                currPageTableId = pageTableId;
            }
        }

        viewPager.setCurrentItem(selectedPos);
        System.out.println("TabsHost_new / _onResume / selectedPos = " + selectedPos);
    }

    @Override
    public void onPause() {
        super.onPause();
        //  Remove fragments
        if( adapter.mFragmentList != null) {
            for (int i = 0; i < adapter.mFragmentList.size(); i++) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(adapter.mFragmentList.get(i)).commit();
            }
        }
    }

    //todo TBD
    public void setAudioPlayingTab_WithHighlight(boolean highlightIsOn)
    {
        // get first tab id and last tab id
//        int tabCount = mTabsHost.getTabWidget().getTabCount();
        int tabCount = tabLayout.getTabCount();
        for (int i = 0; i < tabCount; i++)
        {
//            TextView textView= (TextView) mTabsHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            TextView textView= (TextView) tabLayout.getChildAt(i).findViewById(android.R.id.title);
            if(highlightIsOn && (MainAct.mPlaying_pagePos == i))
                textView.setTextColor(ColorSet.getHighlightColor(MainAct.mAct));
            else
            {
                int style = adapter.mDbFolder.getPageStyle(i, true);
                if((style%2) == 1)
                {
                    textView.setTextColor(Color.argb(255,0,0,0));
                }
                else
                {
                    textView.setTextColor(Color.argb(255,255,255,255));
                }
            }
        }
    }

}