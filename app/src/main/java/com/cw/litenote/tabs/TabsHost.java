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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_folder;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.folder.FolderUi;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.operation.audio.AudioManager;
import com.cw.litenote.operation.audio.AudioPlayer_page;
import com.cw.litenote.page.Page;
import com.cw.litenote.util.ColorSet;
import com.cw.litenote.util.Util;
import com.cw.litenote.util.audio.UtilAudio;
import com.cw.litenote.util.preferences.Pref;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;


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

    public static AudioUi_page audioUi_page;
    public static AudioPlayer_page audioPlayer_page;
    public static boolean isDoingMarking;

    public TabsHost()
    {
//        System.out.println("TabsHost / construct");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        System.out.println("TabsHost / _onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("TabsHost / _onCreateView");

        View rootView;

        // set layout by orientation
        if (Util.isLandscapeOrientation(MainAct.mAct))
            rootView = inflater.inflate(R.layout.tabs_host_landscape, container, false);
        else
            rootView = inflater.inflate(R.layout.tabs_host_portrait, container, false);

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

//        mTabLayout.setBackgroundColor(ColorSet.getBarColor(getActivity()));
        mTabLayout.setBackgroundColor(ColorSet.getButtonColor(getActivity()));
//        mTabLayout.setBackgroundColor(Color.parseColor("#FF303030"));

        // tab indicator
        mTabLayout.setSelectedTabIndicatorHeight(15);
        mTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFF7F00"));
//        mTabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));

        mTabLayout.setTabTextColors(
                ContextCompat.getColor(getActivity(),R.color.colorGray), //normal
                ContextCompat.getColor(getActivity(),R.color.colorWhite) //selected
        );

        mFooterMessage = (TextView) rootView.findViewById(R.id.footerText);
        mFooterMessage.setBackgroundColor(Color.BLUE);
        mFooterMessage.setVisibility(View.VISIBLE);

        return rootView;
    }

    /**
     * Add pages
     */
    private void addPages(TabsPagerAdapter adapter)
    {
        lastPageTableId = 0;
        int pageCount = adapter.dbFolder.getPagesCount(true);
        System.out.println("TabsHost / _addPages / pagesCount = " + pageCount);

        if(pageCount > 0) {
            for (int i = 0; i < pageCount; i++) {
                int pageTableId = adapter.dbFolder.getPageTableId(i, true);

                if (i == 0)
                    setFirstPos_pageId(adapter.dbFolder.getPageId(i, true));

                if (pageTableId > lastPageTableId)
                    lastPageTableId = pageTableId;

//                System.out.println("TabsHost / _addPages / page_tableId = " + pageTableId);
                adapter.addFragment(new Page(i, pageTableId));
            }
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

        setFocus_tabPos(tab.getPosition());

        // keep focus view page table Id
        int pageTableId = mTabsPagerAdapter.dbFolder.getPageTableId(getFocus_tabPos(), true);
        Pref.setPref_focusView_page_tableId(MainAct.mAct, pageTableId);

        // current page table Id
        mFocusPageTableId = pageTableId;

        // refresh list view of selected page
        Page page = mTabsPagerAdapter.fragmentList.get(getFocus_tabPos());
        if( (tab.getPosition() == audioPlayTabPos) && (page != null) && (page.mItemAdapter != null) )
        {
            DragSortListView listView = page.drag_listView;
            if( !isDoingMarking &&
                (listView != null) &&
                (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP)  )
            {
                audioPlayer_page.scrollHighlightAudioItemToVisible(listView);//todo Affected by toggle marking
            }
        }

        // add for update page item view
        if((page != null) && (page.mItemAdapter != null))
            page.mItemAdapter.notifyDataSetChanged();

        // set tab audio icon when audio playing
        if ( (MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos()) &&
             (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
             (tab.getPosition() == audioPlayTabPos)                              )
        {
            if(tab.getCustomView() == null) {
                LinearLayout tabLinearLayout = (LinearLayout) MainAct.mAct.getLayoutInflater().inflate(R.layout.tab_custom, null);
                TextView title = (TextView) tabLinearLayout.findViewById(R.id.tabTitle);
                title.setText(mTabsPagerAdapter.dbFolder.getPageTitle(tab.getPosition(), true));
                title.setTextColor(MainAct.mAct.getResources().getColor(R.color.colorWhite));
                title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audio, 0, 0, 0);
                tab.setCustomView(title);
            }
        }
        else
            tab.setCustomView(null);

        // call onCreateOptionsMenu
        MainAct.mAct.invalidateOptionsMenu();

        // set text color
        mTabLayout.setTabTextColors(
                ContextCompat.getColor(getActivity(),R.color.colorGray), //normal
                ContextCompat.getColor(getActivity(),R.color.colorWhite) //selected
        );

        // set long click listener
        setLongClickListener();

        TabsHost.showFooter(MainAct.mAct);

        isDoingMarking = false;
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onResume() {
        super.onResume();
        // default
        setFocus_tabPos(0);

        // restore focus view page
        int pageCount = mTabsPagerAdapter.dbFolder.getPagesCount(true);
        for(int i=0;i<pageCount;i++)
        {
            int pageTableId = mTabsPagerAdapter.dbFolder.getPageTableId(i, true);

            if(pageTableId == Pref.getPref_focusView_page_tableId(getActivity())) {
                setFocus_tabPos(i);
                mFocusPageTableId = pageTableId;
            }
        }
        mViewPager.setCurrentItem(getFocus_tabPos());

        System.out.println("TabsHost / _onResume / _getFocus_tabPos = " + getFocus_tabPos());

        // auto scroll to show focus tab
        new Handler().postDelayed(
                new Runnable() {
                    @Override public void run() {
                        mTabLayout.getTabAt(getFocus_tabPos()).select();
                    }
                }, 100);

        // set audio icon after Key Protect
        TabLayout.Tab tab =  mTabLayout.getTabAt(audioPlayTabPos);
        if(tab != null) {
            if( (MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos()) &&
                (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP)   )
            {
                if(tab.getCustomView() == null)
                {
                    LinearLayout tabLinearLayout = (LinearLayout) MainAct.mAct.getLayoutInflater().inflate(R.layout.tab_custom, null);
                    TextView title = (TextView) tabLinearLayout.findViewById(R.id.tabTitle);
                    title.setText(mTabsPagerAdapter.dbFolder.getPageTitle(tab.getPosition(), true));
                    title.setTextColor(MainAct.mAct.getResources().getColor(R.color.colorWhite));
                    title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audio, 0, 0, 0);
                    tab.setCustomView(title);
                }
            }
            else
                tab.setCustomView(null);
        }

        // for incoming phone call case or after Key Protect
        if( (audioUi_page != null) &&
            (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
            (AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)   )
        {
            audioUi_page.initAudioBlock(getActivity());

            audioPlayer_page.page_runnable.run();

            UtilAudio.updateAudioPanel(audioUi_page.audioPanel_play_button,
                                       audioUi_page.audio_panel_title_textView);
        }

        // set long click listener
        setLongClickListener();

    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("TabsHost / _onPause");

        //  Remove fragments
        ArrayList<Page> fragmentList = mTabsPagerAdapter.fragmentList;
        if( (fragmentList != null) &&
            (fragmentList.size() >0) )
        {
            DragSortListView listView = fragmentList.get(getFocus_tabPos()).drag_listView;
            if(listView != null)
                store_listView_vScroll(listView);

            for (int i = 0; i < fragmentList.size(); i++) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(fragmentList.get(i)).commit();
            }
        }
    }

    // store scroll of list view
    public static void store_listView_vScroll(DragSortListView listView)
    {
        int firstVisibleIndex = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        int firstVisibleIndexTop = (v == null) ? 0 : v.getTop();

        System.out.println("TabsHost / _store_listView_vScroll / firstVisibleIndex = " + firstVisibleIndex +
                                                          " , firstVisibleIndexTop = " + firstVisibleIndexTop);

        // keep index and top position
        Pref.setPref_focusView_list_view_first_visible_index(MainAct.mAct, firstVisibleIndex);
        Pref.setPref_focusView_list_view_first_visible_index_top(MainAct.mAct, firstVisibleIndexTop);
    }

    // resume scroll of list view
    public static void resume_listView_vScroll(DragSortListView listView)
    {
        // recover scroll Y
        int firstVisibleIndex = Pref.getPref_focusView_list_view_first_visible_index(MainAct.mAct);
        int firstVisibleIndexTop = Pref.getPref_focusView_list_view_first_visible_index_top(MainAct.mAct);

        System.out.println("TabsHost / _resume_listView_vScroll / firstVisibleIndex = " + firstVisibleIndex +
                                                           " , firstVisibleIndexTop = " + firstVisibleIndexTop);

        // restore index and top position
        listView.setSelectionFromTop(firstVisibleIndex, firstVisibleIndexTop);
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
        int pagePos = getFocus_tabPos();
        mViewPager.setAdapter(mTabsPagerAdapter);
        mViewPager.setCurrentItem(pagePos);
    }

    public static Page getCurrentPage()
    {
        return mTabsPagerAdapter.fragmentList.get(getFocus_tabPos());
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
        mTabsPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Set long click listeners for tabs editing
     */
    void setLongClickListener()
    {
//        System.out.println("TabsHost / _setLongClickListener");

        //https://stackoverflow.com/questions/33367245/add-onlongclicklistener-on-android-support-tablayout-tablayout-tab
        // on long click listener
        LinearLayout tabStrip = (LinearLayout) mTabLayout.getChildAt(0);
        final int tabsCount =  tabStrip.getChildCount();
        for (int i = 0; i < tabsCount; i++)
        {
            final int tabPos = i;
            tabStrip.getChildAt(tabPos).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    editPageTitle(tabPos,MainAct.mAct);
                    return false;
                }
            });
        }
    }

    /**
     * edit page title
     *
     */
    static void editPageTitle(final int tabPos, final FragmentActivity act)
    {
        final DB_folder mDbFolder = mTabsPagerAdapter.dbFolder;

        // get tab name
        String title = mDbFolder.getPageTitle(tabPos, true);

        final EditText editText1 = new EditText(act.getBaseContext());
        editText1.setText(title);
        editText1.setSelection(title.length()); // set edit text start position
        editText1.setTextColor(Color.BLACK);

        //update tab info
        AlertDialog.Builder builder = new AlertDialog.Builder(mTabLayout.getContext());
        builder.setTitle(R.string.edit_page_tab_title)
                .setMessage(R.string.edit_page_tab_message)
                .setView(editText1)
                .setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener()
                                    {   @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {/*cancel*/}
                                    })
                .setNeutralButton(R.string.edit_page_button_delete, new DialogInterface.OnClickListener()
                    {   @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // delete
                            Util util = new Util(act);
                            util.vibrate();

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(mTabLayout.getContext());
                            builder1.setTitle(R.string.confirm_dialog_title)
                                    .setMessage(R.string.confirm_dialog_message_page)
                                    .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog1, int which1){
                                            /*nothing to do*/}})
                                    .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog1, int which1){
                                            deletePage(tabPos, act);
                                        }})
                                    .show();
                        }
                    })
                .setPositiveButton(R.string.edit_page_button_update, new DialogInterface.OnClickListener()
                    {   @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            // save
                            final int pageId =  mDbFolder.getPageId(tabPos, true);
                            final int pageTableId =  mDbFolder.getPageTableId(tabPos, true);

                            int tabStyle = mDbFolder.getPageStyle(tabPos, true);
                            mDbFolder.updatePage(pageId,
                                                 editText1.getText().toString(),
                                                 pageTableId,
                                                 tabStyle,
                                                 true);

                            FolderUi.startTabsHostRun();
                        }
                    })
                .setIcon(android.R.drawable.ic_menu_edit);

        AlertDialog d1 = builder.create();
        d1.show();
        // android.R.id.button1 for positive: save
        ((Button)d1.findViewById(android.R.id.button1))
                .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);

        // android.R.id.button2 for negative: color
        ((Button)d1.findViewById(android.R.id.button2))
                .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);

        // android.R.id.button3 for neutral: delete
        ((Button)d1.findViewById(android.R.id.button3))
                .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
    }

    /**
     * delete page
     *
     */
    public static  void deletePage(int tabPos, final FragmentActivity activity)
    {

        final DB_folder mDbFolder = mTabsPagerAdapter.dbFolder;
        int pageId =  mDbFolder.getPageId(tabPos, true);
        mDbFolder.open();
        // check if only one page left
        int pagesCount = mDbFolder.getPagesCount(false);
        int mFirstPos_PageId = 0;
        Cursor mPageCursor = mDbFolder.getPageCursor();
        if(mPageCursor.isFirst())
            mFirstPos_PageId = pageId;

        if(pagesCount > 0)
        {
            //if current page is the first page and will be delete,
            //try to get next existence of note page
            System.out.println("TabsHost / deletePage / tabPos = " + tabPos);
            System.out.println("TabsHost / deletePage / mFirstPos_PageId = " + mFirstPos_PageId);
            if(pageId == mFirstPos_PageId)
            {
                int cGetNextExistIndex = getFocus_tabPos() +1;
                boolean bGotNext = false;
                while(!bGotNext){
                    try{
                        mFirstPos_PageId =  mDbFolder.getPageId(cGetNextExistIndex, false);
                        bGotNext = true;
                    }catch(Exception e){
                        bGotNext = false;
                        cGetNextExistIndex++;}}
            }

            //change to first existing page
            int newFirstPageTblId = 0;
            for(int i=0 ; i<pagesCount; i++)
            {
                if(	mDbFolder.getPageId(i, false)== mFirstPos_PageId)
                {
                    newFirstPageTblId =  mDbFolder.getPageTableId(i, false);
                    System.out.println("TabsHost / deletePage / newFirstPageTblId = " + newFirstPageTblId);
                }
            }
            System.out.println("TabsHost / deletePage / --- after delete / newFirstPageTblId = " + newFirstPageTblId);
            Pref.setPref_focusView_page_tableId(activity, newFirstPageTblId);
        }
//		else
//		{
//             Toast.makeText(activity, R.string.toast_keep_one_page , Toast.LENGTH_SHORT).show();
//             return;
//		}
        mDbFolder.close();

        //todo TBD
        // set scroll X
//        int scrollX = 0; //over the last scroll X
//        SharedPreferences mPref_FinalPageViewed = activity.getSharedPreferences("focus_view", 0);
//        Pref.setPref_focusView_scrollX_byFolderTableId(activity, scrollX );


        // get page table Id for dropping
        int pageTableId = mDbFolder.getPageTableId(tabPos, true);
        System.out.println("TabsHost / _deletePage / pageTableId =  " + pageTableId);

        // delete tab name
        mDbFolder.dropPageTable(pageTableId,true);
        mDbFolder.deletePage(DB_folder.getFocusFolder_tableName(),pageId,true);
//        mPagesCount--;

        // After Delete page, update highlight tab
        if(getFocus_tabPos() < MainAct.mPlaying_pagePos)
        {
            MainAct.mPlaying_pagePos--;
        }
        else if((getFocus_tabPos() == MainAct.mPlaying_pagePos) &&
                (MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos()))
        {
            if(AudioManager.mMediaPlayer != null)
            {
                AudioManager.stopAudioPlayer();
                AudioManager.mAudioPos = 0;
                AudioManager.setPlayerState(AudioManager.PLAYER_AT_STOP);
            }
        }

        // update change after deleting tab
        FolderUi.startTabsHostRun();

        //todo TBD
        // Note: _onTabChanged will reset scroll X to another value,
        // so we need to add the following to set scroll X again
//        mHorScrollView.post(new Runnable()
//        {
//            @Override
//            public void run() {
//                mHorScrollView.scrollTo(0, 0);
//                Pref.setPref_focusView_scrollX_byFolderTableId(activity, 0 );
//            }
//        });
    }

    public static TextView mFooterMessage;

    // set footer
    public static void showFooter(AppCompatActivity mAct)
    {
//		System.out.println("TabsHost / _showFooter ");

        // show footer
        mFooterMessage.setTextColor(ColorSet.color_white);
        if(mFooterMessage != null) //add this for avoiding null exception when after e-Mail action
        {
            mFooterMessage.setText(getFooterMessage(mAct));
            mFooterMessage.setBackgroundColor(ColorSet.getBarColor(mAct));
        }
    }

    // get footer message of list view
    static String getFooterMessage(AppCompatActivity mAct)
    {
        DB_page mDb_page = new DB_page(mAct, mTabsPagerAdapter.getItem(getFocus_tabPos()).page_tableId);
        return mAct.getResources().getText(R.string.footer_checked).toString() +
                "/" +
                mAct.getResources().getText(R.string.footer_total).toString() +
                ": " +
                mDb_page.getCheckedNotesCount() +
                "/" +
                mDb_page.getNotesCount(true);
    }

    /**
     * Get focus tab position
    */
    public static int getFocus_tabPos()
    {
        return mFocusTabPos;
    }

    /**
     * Set focus tab position
     * @param pos
     */
    public static void setFocus_tabPos(int pos)
    {
        mFocusTabPos = pos;
    }

}