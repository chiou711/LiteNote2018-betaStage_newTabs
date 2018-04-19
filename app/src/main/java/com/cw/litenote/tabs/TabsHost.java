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
    public static TabLayout mTabLayout;
    public static ViewPager mViewPager;
    public static TabsPagerAdapter mTabsPagerAdapter;
    public static int mFocusPageTableId;
    public static int mFocusTabPos;

    public static int lastPageTableId;
    public static int audioPlayTabPos;

    public static int firstPos_pageId;

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
        System.out.println("TabsHost / _onCreateView");

        View rootView = inflater.inflate(R.layout.tabs_host, container, false);
        // tool bar
//        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.tabanim_toolbar);
//        rootView.setSupportActionBar(toolbar);

        // view pager
        mViewPager = (ViewPager) rootView.findViewById(R.id.tabs_pager);

        // mTabsPagerAdapter
        mTabsPagerAdapter = new TabsPagerAdapter(getActivity(),getActivity().getSupportFragmentManager());

        // add pages to mTabsPagerAdapter
        addPages(mTabsPagerAdapter);

        // set mTabsPagerAdapter of view pager
        mViewPager.setAdapter(mTabsPagerAdapter);

        // set tab layout
        mTabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setOnTabSelectedListener(this);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setBackgroundColor(ColorSet.getBarColor(getActivity()));

        // tab indicator
        mTabLayout.setSelectedTabIndicatorHeight(15);
        mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFF7F00"));
//        mTabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));

//        mTabLayout.setTabTextColors(
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
        int pageCount = adapter.dbFolder.getPagesCount(true);
        System.out.println("TabsHost / _addPages / pageCount = " + pageCount);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = adapter.dbFolder.getPageTableId(i, true);

            if(i==0)
                setFirstPos_pageId(adapter.dbFolder.getPageId(i,true));

            if(pageTableId > lastPageTableId)
                lastPageTableId = pageTableId;

            System.out.println("TabsHost / _addPages / page_tableId = " + pageTableId);
            adapter.addFragment(new Page(i,pageTableId));

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

        mFocusTabPos = tab.getPosition();

        // keep focus view page table Id
        int pageTableId = mTabsPagerAdapter.dbFolder.getPageTableId(mFocusTabPos, true);
        Pref.setPref_focusView_page_tableId(MainAct.mAct, pageTableId);

        // current page table Id
        mFocusPageTableId = pageTableId;

        // refresh list view of selected page
        Page page = mTabsPagerAdapter.fragmentList.get(mFocusTabPos);
        if( (tab.getPosition() == audioPlayTabPos) && (page != null) && (page.mItemAdapter != null) )
        {
            DragSortListView listView = page.drag_listView;
            if( (listView != null) &&
                (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP)  ) {
                audioPlayer_page.scrollHighlightAudioItemToVisible(listView);
            }
        }

        // add for update page item view
        if((page != null) && (page.mItemAdapter != null))
            page.mItemAdapter.notifyDataSetChanged();

        // set pager item
        mViewPager.setCurrentItem(mFocusTabPos);

        // set tab audio icon when audio playing
        if ( (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
                (tab.getPosition() == audioPlayTabPos) )
            tab.setIcon(R.drawable.ic_audio);
        else
            tab.setIcon(null);


    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if ( (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
             (tab.getPosition() == audioPlayTabPos) )
            tab.setIcon(R.drawable.ic_audio);
        else
            tab.setIcon(null);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onResume() {
        super.onResume();
        // default
        mFocusTabPos = 0;

        // restore focus view page
        int pageCount = mTabsPagerAdapter.dbFolder.getPagesCount(true);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = mTabsPagerAdapter.dbFolder.getPageTableId(i, true);

            if(pageTableId == Pref.getPref_focusView_page_tableId(getActivity())) {
                mFocusTabPos = i;
                mFocusPageTableId = pageTableId;
            }
        }
        mViewPager.setCurrentItem(mFocusTabPos);

        System.out.println("TabsHost / _onResume / mFocusTabPos = " + mFocusTabPos);

        // set audio icon after Key Protect
        if ( (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP)  )
            mTabLayout.getTabAt(audioPlayTabPos).setIcon(R.drawable.ic_audio);
        else
            mTabLayout.getTabAt(audioPlayTabPos).setIcon(null);

        // for incoming phone call case or after Key Protect
        if( (page_audio != null) &&
            (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
            (AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)   )
        {
            page_audio.initAudioBlock(getActivity());

            audioPlayer_page.page_runnable.run();

            UtilAudio.updateAudioPanel(page_audio.audioPanel_play_button,
                                       page_audio.audio_panel_title_textView);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("TabsHost / _onPause");

        store_listView_vScroll(mTabsPagerAdapter.fragmentList.get(mFocusTabPos).drag_listView);

        //  Remove fragments
        if( mTabsPagerAdapter.fragmentList != null) {
            for (int i = 0; i < mTabsPagerAdapter.fragmentList.size(); i++) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(mTabsPagerAdapter.fragmentList.get(i)).commit();
            }
        }
    }

    // store scroll of list view
    public static void store_listView_vScroll(DragSortListView listView)
    {
//        DragSortListView listView = mTabsPagerAdapter.fragmentList.get(mFocusTabPos).drag_listView;
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

    /**
     * Get first position page Id
     * @return page Id of 1st position
     */
    public static int getFirstPos_pageId() {
        return firstPos_pageId;
    }

    /**
     * Set first position table Id
     * @param id: page Id
     */
    public static void setFirstPos_pageId(int id) {
        firstPos_pageId = id;
    }

    public static void reloadCurrentPage()
    {
        int pagePos = mFocusTabPos;
        mViewPager.setAdapter(mTabsPagerAdapter);
        mViewPager.setCurrentItem(pagePos);
    }

    public static Page getCurrentPage()
    {
        return mTabsPagerAdapter.fragmentList.get(mFocusTabPos);
    }

    public static int getCurrentPageTableId()
    {
        return mFocusPageTableId;
    }


    public static void getPage_rowItemView(int rowPos)
    {
        DragSortListView listView = getCurrentPage().drag_listView;
        View convertView = listView.getChildAt(rowPos);
        listView.getAdapter().getView(rowPos, convertView, listView);
    }
}