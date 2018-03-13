package tabanimation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cw.litenote.R;
import com.cw.litenote.main.MainAct;

import java.util.ArrayList;
import java.util.List;

//public class TabAnimation extends AppCompatActivity {
    public class TabAnimation extends AppCompatDialogFragment {

    static FragmentActivity mAct;
    View rootView;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    public static List<DummyFragment> mFragmentList ;//= new ArrayList<>();
    private static List<String> mFragmentTitleList ;//= new ArrayList<>();
    static int selectedPos;
    static int reSelectedPos;
    static int unSelectedPos;

    public TabAnimation()
    {
        System.out.println("TabAnimation / construct");
        mFragmentList =  new ArrayList<>();
        mFragmentTitleList = new ArrayList<>();
        mAct = MainAct.mAct;

        if(viewPagerAdapter == null) {
            System.out.println("TabAnimation / construct / viewPagerAdapter = null");
            viewPagerAdapter = new ViewPagerAdapter(mAct.getSupportFragmentManager());
            viewPagerAdapter.addFrag(new DummyFragment(
                    ContextCompat.getColor(mAct, R.color.blue_grey)), "CAT");
            viewPagerAdapter.addFrag(new DummyFragment(
                    ContextCompat.getColor(mAct, R.color.amber)), "DOG");
            viewPagerAdapter.addFrag(new DummyFragment(
                    ContextCompat.getColor(mAct, R.color.cyan)), "MOUSE");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("TabAnimation / _onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("TabAnimation / _onCreateView");
//        setContentView(R.layout.activity_tab_animation);
        rootView = inflater.inflate(R.layout.activity_tab_animation, container, false);

        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.tabanim_toolbar);
//        setSupportActionBar(toolbar);

//        if (mAct.getSupportActionBar() != null)
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        final ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.tabanim_viewpager);
        viewPager = (ViewPager) rootView.findViewById(R.id.tabanim_viewpager);

        // setup view pager
        viewPager.setAdapter(viewPagerAdapter);
        viewPagerAdapter.notifyDataSetChanged();

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                System.out.println("TabAnimation / _onTabSelected: " + tab.getPosition());
                selectedPos = tab.getPosition();
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                System.out.println("TabAnimation / _onTabUnselected: " + tab.getPosition());
                unSelectedPos = tab.getPosition();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                System.out.println("TabAnimation / _onTabReselected: " + tab.getPosition());
                reSelectedPos = tab.getPosition();
            }
        });

        return rootView;
        //        return super.onCreateView(inflater, container, savedInstanceState);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        getMenuInflater().inflate(R.menu.menu_tab_switch, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mAct.finish();
                return true;
//            case R.id.action_switch:
//                Intent intent = new Intent(TabAnimation.this, TabsHeaderActivity.class);
//                startActivity(intent);
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.setCurrentItem(selectedPos);
        System.out.println("TabAnimation / _onResume / selectedPos = " + selectedPos);
    }

    /**
     *  View Pager Adapter Class
     */
    private static class ViewPagerAdapter extends FragmentPagerAdapter
    {
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            System.out.println("TabAnimation / ViewPagerAdapter / getItem / position = " + position);
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(DummyFragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    /**
     *  Dummy Fragment Class
     */
    public static class DummyFragment extends Fragment
    {
        int color;
        SimpleRecyclerAdapter adapter;

        public DummyFragment() {
        }

        @SuppressLint("ValidFragment")
        public DummyFragment(int color) {
            System.out.println("TabAnimation / DummyFragment / constructor / color = " + color);
            this.color = color;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            System.out.println("TabAnimation / DummyFragment / _onCreate");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.dummy_fragment, container, false);

            final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.dummyfrag_bg);
            frameLayout.setBackgroundColor(color);

            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dummyfrag_scrollableview);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setHasFixedSize(true);

            List<String> list = new ArrayList<String>();
            for (int i = 0; i < VersionModel.data.length; i++) {
                list.add(color + " " + VersionModel.data[i]);
            }

            adapter = new SimpleRecyclerAdapter(list);
            recyclerView.setAdapter(adapter);
            System.out.println("TabAnimation / DummyFragment / _onCreateView");
            return view;
        }

        @Override
        public void onResume() {
            System.out.println("TabAnimation / DummyFragment / _onResume");
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            System.out.println("TabAnimation / DummyFragment / _onPause");
//            for (int i = 0; i < mFragmentList.size(); i++) {
//                if((i!= unSelectedPos) && (i!= selectedPos))
//                    mAct.getSupportFragmentManager().beginTransaction().remove(mFragmentList.get(i)).commit();
//            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            System.out.println("TabAnimation / DummyFragment / _onDestroy");
//            for (int i = 0; i < mFragmentList.size(); i++) {
//                mAct.getSupportFragmentManager().beginTransaction().remove(mFragmentList.get(i)).commit();
//            }
        }

    }

}
