package com.cw.litenote.mFragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Oclemmy on 5/9/2016 for ProgrammingWizards Channel and http://www.Camposha.com.
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    ArrayList<Fragment> fragments=new ArrayList<>();

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        System.out.println("MyPagerAdapter / _getItem / position = " + position);
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    //ADD PAGE
    public void addFragment(Fragment f)
    {
        System.out.println("MyPagerAdapter / _addFragment / " + f.toString());
        fragments.add(f);
    }

    //set title

    @Override
    public CharSequence getPageTitle(int position) {
        String title=fragments.get(position).toString();
        System.out.println("MyPagerAdapter / _getPageTitle / title = " + title);
        return title.toString();
    }
}
