package com.cw.litenote.drawer;

import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_drawer;
import com.cw.litenote.folder.FolderUi;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.operation.delete.DeleteFolders;
import com.mobeta.android.dslv.DragSortListView;

/**
 * Created by CW on 2016/8/24.
 */
public class Drawer {


    public static DrawerLayout drawerLayout;
    private FragmentActivity act;
    public ActionBarDrawerToggle drawerToggle;
    public static NavigationView mNavigationView;
    DragSortListView listView;


    public Drawer(FragmentActivity activity)
    {
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);

//        relativeLayout = activity.findViewById(R.id.nav_rel_layout);
        mNavigationView = (NavigationView) activity.findViewById(R.id.nav_view);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.ADD_NEW_FOLDER:
                        FolderUi.renewFirstAndLast_folderId();
                        FolderUi.addNewFolder(MainAct.mAct, FolderUi.mLastExist_folderTableId +1, MainAct.mFolder.getAdapter());
                        return true;

                    case R.id.ENABLE_FOLDER_DRAG_AND_DROP:
                        if(MainAct.mPref_show_note_attribute.getString("KEY_ENABLE_FOLDER_DRAGGABLE", "no")
                                .equalsIgnoreCase("yes"))
                        {
                            MainAct.mPref_show_note_attribute.edit().putString("KEY_ENABLE_FOLDER_DRAGGABLE","no")
                                    .apply();
                            DragSortListView listView = (DragSortListView) act.findViewById(R.id.drawer_listview);
                            listView.setDragEnabled(false);
                            Toast.makeText(act,act.getResources().getString(R.string.drag_folder)+
                                            ": " +
                                            act.getResources().getString(R.string.set_disable),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            MainAct.mPref_show_note_attribute.edit().putString("KEY_ENABLE_FOLDER_DRAGGABLE","yes")
                                    .apply();
                            DragSortListView listView = (DragSortListView) act.findViewById(R.id.drawer_listview);
                            listView.setDragEnabled(true);
                            Toast.makeText(act,act.getResources().getString(R.string.drag_folder) +
                                            ": " +
                                           act.getResources().getString(R.string.set_enable),
                                    Toast.LENGTH_SHORT).show();
                        }
                        MainAct.mFolder.getAdapter().notifyDataSetChanged();
                        act.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                        return true;

                    case R.id.DELETE_FOLDERS:

                        DB_drawer dB_drawer = new DB_drawer(act);
                        if(dB_drawer.getFoldersCount(true)>0)
                        {
                            closeDrawer();
                            MainAct.mMenu.setGroupVisible(R.id.group_notes, false); //hide the menu
                            DeleteFolders delFoldersFragment = new DeleteFolders();
                            MainAct.mFragmentTransaction = MainAct.fragmentManager.beginTransaction();
                            MainAct.mFragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_left, R.anim.fragment_slide_out_left, R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
                            MainAct.mFragmentTransaction.replace(R.id.content_frame, delFoldersFragment).addToBackStack("delete_folders").commit();
                        }
                        else
                        {
                            Toast.makeText(act, R.string.config_export_none_toast, Toast.LENGTH_SHORT).show();
                        }
                        return true;

                    default:
                        return true;
                }
            }
        });



        act = activity;
        listView = (DragSortListView) act.findViewById(R.id.drawer_listview);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        drawerToggle =new ActionBarDrawerToggle(act,                  /* host Activity */
                                                drawerLayout,         /* DrawerLayout object */
                                                R.drawable.ic_drawer,  /* navigation drawer image to replace 'Up' caret */
                                                R.string.drawer_open,  /* "open drawer" description for accessibility */
                                                R.string.drawer_close  /* "close drawer" description for accessibility */
                                                )
                {
                    public void onDrawerOpened(View drawerView)
                    {
                        System.out.println("Drawer / _onDrawerOpened ");

                        act.findViewById(R.id.content_frame).setVisibility(View.INVISIBLE);
                        act.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                        if(listView.getCount() >0) {
//                            act.getActionBar().setTitle(MainAct.mAppTitle);
                            // will call Folder_adapter _getView to update audio playing high light
                            listView.invalidateViews();
                        }
                    }

                    public void onDrawerClosed(View view)
                    {
                        System.out.println("Drawer / _onDrawerClosed / FolderUi.getFocus_folderPos() = " + FolderUi.getFocus_folderPos());
                        act.findViewById(R.id.content_frame).setVisibility(View.VISIBLE);

                        FragmentManager fragmentManager = act.getSupportFragmentManager();
                        if(fragmentManager.getBackStackEntryCount() ==0 )
                        {
                            act.invalidateOptionsMenu(); // creates a call to onPrepareOptionsMenu()

                            DB_drawer dB_drawer = new DB_drawer(act);
                            if (dB_drawer.getFoldersCount(true) > 0)
                            {
                                int pos = listView.getCheckedItemPosition();
                                MainAct.mFolderTitle = dB_drawer.getFolderTitle(pos,true);
//                                act.getActionBar().setTitle(MainAct.mFolderTitle);

                                //todo TBD
                                // add for deleting folder condition
//                                if (TabsHost.mTabsHost == null)
//                                    FolderUi.selectFolder(act,FolderUi.getFocus_folderPos());
                            }
                            else
                                act.findViewById(R.id.content_frame).setVisibility(View.INVISIBLE);
                        }
                    }
               };
    }

    public void initDrawer()
    {
        // set a custom shadow that overlays the main content when the drawer opens
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    public void closeDrawer()
    {
        drawerLayout.closeDrawer(mNavigationView);
    }


    public boolean isDrawerOpen()
    {
        return drawerLayout.isDrawerOpen(mNavigationView);
    }
}
