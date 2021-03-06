package com.cw.litenote.util.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by cw on 2017/10/11.
 */

public class Pref
{
    // set folder table id of focus view
    public static void setPref_focusView_folder_tableId(Activity act, int folderTableId )
    {
//		System.out.println("Pref / _setPref_focusView_folder_tableId / folderTableId = " + folderTableId);
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_FOCUS_VIEW_FOLDER_TABLE_ID";
        pref.edit().putInt(keyName, folderTableId).apply();
    }

    // get folder table id of focus view
    public static int getPref_focusView_folder_tableId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_FOCUS_VIEW_FOLDER_TABLE_ID";
        return pref.getInt(keyName, 1); // folder table Id: default is 1
    }

    // set page table id of focus view
    public static void setPref_focusView_page_tableId(Activity act, int pageTableId )
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOLDER_TABLE_ID_";
        int folderTableId = getPref_focusView_folder_tableId(act);
        String keyName = keyPrefix.concat(String.valueOf(folderTableId));
        pref.edit().putInt(keyName, pageTableId).apply();
    }

    // get page table id of focus view
    public static int getPref_focusView_page_tableId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOLDER_TABLE_ID_";
        int folderTableId = getPref_focusView_folder_tableId(context);
        String keyName = keyPrefix.concat(String.valueOf(folderTableId));
        // page table Id: default is 1
        return pref.getInt(keyName, 1);
    }

    // remove key of focus view
    public static void removePref_focusView_key(Activity act, int drawerFolderTableId)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOLDER_TABLE_ID_";
        String keyName = keyPrefix.concat(String.valueOf(drawerFolderTableId));
        pref.edit().remove(keyName).apply();
    }

    // set scroll X of drawer of focus view
    public static void setPref_focusView_scrollX_byFolderTableId(Activity act, int scrollX )
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOLDER_TABLE_ID_";
        int tableId = getPref_focusView_folder_tableId(act);
        String keyName = keyPrefix.concat(String.valueOf(tableId));
        keyName = keyName.concat("_SCROLL_X");
        pref.edit().putInt(keyName, scrollX).apply();
    }

    // get scroll X of drawer of focus view
    public static Integer getPref_focusView_scrollX_byFolderTableId(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyPrefix = "KEY_FOLDER_TABLE_ID_";
        int tableId = getPref_focusView_folder_tableId(act);
        String keyName = keyPrefix.concat(String.valueOf(tableId));
        keyName = keyName.concat("_SCROLL_X");
        return pref.getInt(keyName, 0); // default scroll X is 0
    }

    // Set list view first visible Index of focus view
    public static void setPref_focusView_list_view_first_visible_index(Activity act, int index )
    {
//		System.out.println("Pref / _setPref_focusView_list_view_first_visible_index / index = " + index);
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        pref.edit().putInt(keyName, index).apply();
    }

    // Get list view first visible Index of focus view
    public static Integer getPref_focusView_list_view_first_visible_index(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        return pref.getInt(keyName, 0); // default scroll X is 0
    }

    // Set list view first visible index Top of focus view
    public static void setPref_focusView_list_view_first_visible_index_top(Activity act, int top )
    {
//        System.out.println("Pref / _setPref_focusView_list_view_first_visible_index_top / top = " + top);
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX_TOP";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        pref.edit().putInt(keyName, top).apply();
    }

    // Get list view first visible index Top of focus view
    public static Integer getPref_focusView_list_view_first_visible_index_top(Activity act)
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_LIST_VIEW_FIRST_VISIBLE_INDEX_TOP";
        String location = getCurrentListViewLocation(act);
        keyName = keyName.concat(location);
        return pref.getInt(keyName, 0);
    }

    // set has default import
    public static void setPref_has_preferred_tables(Activity act, boolean has, int position )
    {
        SharedPreferences pref = act.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_HAS_PREFERRED_TABLES"+position;
        pref.edit().putBoolean(keyName, has).apply();
    }

    // get has default import
    public static boolean getPref_has_preferred_tables(Context context, int position)
    {
        SharedPreferences pref = context.getSharedPreferences("focus_view", 0);
        String keyName = "KEY_HAS_PREFERRED_TABLES"+position;
        return pref.getBoolean(keyName, false);
    }


    // location about drawer table Id and page table Id
    static String getCurrentListViewLocation(Activity act)
    {
        String strLocation = "";
        // folder
        int folderTableId = getPref_focusView_folder_tableId(act);
        String strFolderTableId = String.valueOf(folderTableId);
        // page
        int pageTableId = getPref_focusView_page_tableId(act);
        String strPageTableId = String.valueOf(pageTableId);
        strLocation = "_" + strFolderTableId + "_" + strPageTableId;
        return strLocation;
    }

    // Get YouTube auto play in note view
    public static boolean getPref_is_autoPlay_YouTubeApi(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("show_note_attribute", 0);
        String keyName = "KEY_IS_AUTO_PLAY_YOUTUBE_API";
        return pref.getBoolean(keyName, false);
    }

    // Set YouTube auto play in note view
    public static void setPref_is_autoPlay_YouTubeApi(Context context, boolean isAuto)
    {
        SharedPreferences pref = context.getSharedPreferences("show_note_attribute", 0);
        String keyName = "KEY_IS_AUTO_PLAY_YOUTUBE_API";
        pref.edit().putBoolean(keyName, isAuto).apply();
    }

}
