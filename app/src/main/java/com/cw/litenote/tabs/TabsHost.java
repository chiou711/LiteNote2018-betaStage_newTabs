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
import com.cw.litenote.operation.audio.AudioManager;
import com.cw.litenote.operation.audio.AudioPlayer_page;
import com.cw.litenote.page.Page;
import com.cw.litenote.util.ColorSet;
import com.cw.litenote.util.audio.UtilAudio;
import com.cw.litenote.util.preferences.Pref;
import com.mobeta.android.dslv.DragSortListView;


public class TabsHost extends AppCompatDialogFragment implements TabLayout.OnTabSelectedListener
{
    public static int mStyle;
    TabLayout tabLayout;
    ViewPager viewPager;
    public static TabsPagerAdapter adapter;
    public static int currPageTableId;

    public static int selectedPos;
    int reSelectedPos;
    int unSelectedPos;
    public static int lastPageTableId;
    public static int audioPlayingPos;

    public static Page_audio page_audio;
    public static AudioPlayer_page audioPlayer_page;

    public TabsHost()
    {
        System.out.println("TabsHost / construct");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("TabsHost / _onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tabs_host, container, false);
        // tool bar
//        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.tabanim_toolbar);
//        rootView.setSupportActionBar(toolbar);

        // view pager
        viewPager = (ViewPager) rootView.findViewById(R.id.tabs_pager);

        // adapter
        adapter = new TabsPagerAdapter(getActivity(),getActivity().getSupportFragmentManager());

        // add pages to adapter
        addPages(adapter);

        // set adapter of view pager
        viewPager.setAdapter(adapter);

        // set tab layout
        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(this);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setBackgroundColor(ColorSet.getBarColor(getActivity()));

        // tab indicator
        tabLayout.setSelectedTabIndicatorHeight(15);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFF7F00"));
//        tabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));

//        tabLayout.setTabTextColors(
//                ContextCompat.getColor(getActivity(),R.color.bg_light),
//                ContextCompat.getColor(getActivity(),R.color.highlight_color)
//        );

        return rootView;
    }

    /**
     * Add pages
     */
    private void addPages(TabsPagerAdapter adapter)
    {
        lastPageTableId = 0;
        int pageCount = adapter.mDbFolder.getPagesCount(true);
        System.out.println("TabsHost / _addPages / pageCount = " + pageCount);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = adapter.mDbFolder.getPageTableId(i, true);

            if(pageTableId > lastPageTableId)
                lastPageTableId = pageTableId;

            System.out.println("TabsHost / _addPages / pageTableId = " + pageTableId);
            adapter.addFragment(new Page(pageTableId));
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
        System.out.println("TabsHost / _onTabSelected: " + tab.getPosition());

        selectedPos = tab.getPosition();

        // keep focus view page table Id
        int pageTableId = adapter.mDbFolder.getPageTableId(selectedPos, true);
        Pref.setPref_focusView_page_tableId(getActivity(), pageTableId);

        // current page table Id
        currPageTableId = pageTableId;

        // refresh list view of selected page
        Page page = adapter.mFragmentList.get(selectedPos);
        if( (tab.getPosition() == audioPlayingPos) && (page != null) && (page.mItemAdapter != null) )
        {
            DragSortListView listView = page.mDndListView;
            if( (listView != null) &&
                (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP)  ) {
                audioPlayer_page.scrollHighlightAudioItemToVisible(listView);
            }
        }

        if(page.mItemAdapter != null)
            page.mItemAdapter.notifyDataSetChanged();

        // set pager item
        viewPager.setCurrentItem(selectedPos);

        // set tab audio icon when audio playing
        if ( (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
                (tab.getPosition() == audioPlayingPos) )
            tab.setIcon(R.drawable.ic_audio);
        else
            tab.setIcon(null);

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        System.out.println("TabsHost / _onTabUnselected: " + tab.getPosition());
        unSelectedPos = tab.getPosition();

        if ( (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
             (tab.getPosition() == audioPlayingPos) )
            tab.setIcon(R.drawable.ic_audio);
        else
            tab.setIcon(null);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        System.out.println("TabsHost / _onTabReselected: " + tab.getPosition());
        reSelectedPos = tab.getPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        // default
        selectedPos = 0;

        // restore focus view page
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

        System.out.println("TabsHost / _onResume / selectedPos = " + selectedPos);

        // set audio icon after Key Protect
        if ( (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP)  )
            tabLayout.getTabAt(audioPlayingPos).setIcon(R.drawable.ic_audio);
        else
            tabLayout.getTabAt(audioPlayingPos).setIcon(null);

        // for incoming phone call case or after Key Protect
        if( (page_audio != null) &&
            (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
            (AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)   )
        {
            page_audio.initAudioBlock(getActivity());

            audioPlayer_page.mRunContinueMode.run();

            UtilAudio.updateAudioPanel(page_audio.audioPanel_play_button,
                                       page_audio.audio_panel_title_textView);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("TabsHost / _onPause");

        store_listView_vScroll(adapter.mFragmentList.get(selectedPos).mDndListView);

        //  Remove fragments
        if( adapter.mFragmentList != null) {
            for (int i = 0; i < adapter.mFragmentList.size(); i++) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(adapter.mFragmentList.get(i)).commit();
            }
        }
    }

    // store scroll of list view
    public static void store_listView_vScroll(DragSortListView listView)
    {
//        DragSortListView listView = adapter.mFragmentList.get(selectedPos).mDndListView;
        int mFirstVisibleIndex = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        int mFirstVisibleIndexTop = (v == null) ? 0 : v.getTop();

        System.out.println("TabsHost / _store_listView_vScroll / mFirstVisibleIndex = " + mFirstVisibleIndex +
                " , mFirstVisibleIndexTop = " + mFirstVisibleIndexTop);

        // keep index and top position
        Pref.setPref_focusView_list_view_first_visible_index(MainAct.mAct, mFirstVisibleIndex);
        Pref.setPref_focusView_list_view_first_visible_index_top(MainAct.mAct, mFirstVisibleIndexTop);
    }

    // resume scroll of list view
    public static void resume_listView_vScroll(DragSortListView listView)
    {
        // recover scroll Y
        int mFirstVisibleIndex = Pref.getPref_focusView_list_view_first_visible_index(MainAct.mAct);
        int mFirstVisibleIndexTop = Pref.getPref_focusView_list_view_first_visible_index_top(MainAct.mAct);

        System.out.println("TabsHost / _resume_scroll_listView / mFirstVisibleIndex = " + mFirstVisibleIndex +
                " , mFirstVisibleIndexTop = " + mFirstVisibleIndexTop);

        // restore index and top position
        listView.setSelectionFromTop(mFirstVisibleIndex, mFirstVisibleIndexTop);
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