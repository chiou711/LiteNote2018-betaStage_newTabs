package com.cw.litenote.tabs;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.cw.litenote.db.DB_folder;
import com.cw.litenote.page.Page;
import com.cw.litenote.util.preferences.Pref;

import java.util.ArrayList;

/**
 * Created by cw on 2018/3/20.
 *
 *  View Pager Adapter Class
 *
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {
    public ArrayList<Page> mFragmentList = new ArrayList<>();
    DB_folder mDbFolder;

    TabsPagerAdapter(FragmentActivity act, FragmentManager fm)
    {
        super(fm);
        int folderTableId = Pref.getPref_focusView_folder_tableId(act);
        mDbFolder = new DB_folder(act, folderTableId);
    }

    @Override
    public Page getItem(int position)
    {
//        System.out.println("TabsPagerAdapter / _getItem / position = " + position);
        return mFragmentList.get(position);
    }

    // add fragment
    public void addFragment(Page fragment) {
        mFragmentList.add(fragment);
    }

    @Override
    public int getCount(){
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
//        System.out.println("TabsPagerAdapter / _getPageTitle / position = " + position);
        return mDbFolder.getPageTitle(position,true);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
//        System.out.println("TabsPagerAdapter / _setPrimaryItem / position = " + position);
    }

}

