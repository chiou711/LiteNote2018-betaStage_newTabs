package com.cw.litenote.tabs;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
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
    public ArrayList<Page> fragmentList = new ArrayList<>();
    DB_folder dbFolder;

    TabsPagerAdapter(AppCompatActivity act, FragmentManager fm)
    {
        super(fm);
        int folderTableId = Pref.getPref_focusView_folder_tableId(act);
        dbFolder = new DB_folder(act, folderTableId);
    }

    @Override
    public Page getItem(int position)
    {
//        System.out.println("TabsPagerAdapter / _getItem / position = " + position);
        return fragmentList.get(position);
    }

    // add fragment
    public void addFragment(Page fragment) {
        fragmentList.add(fragment);
    }

    @Override
    public int getCount(){
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
//        System.out.println("TabsPagerAdapter / _getPageTitle / position = " + position);
        return dbFolder.getPageTitle(position,true);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
//        System.out.println("TabsPagerAdapter / _setPrimaryItem / position = " + position);
    }

}

