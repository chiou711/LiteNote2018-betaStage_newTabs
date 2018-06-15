package com.cw.litenote.folder;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_drawer;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.operation.audio.AudioManager;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

/**
 * Created by cw on 2017/10/6.
 */

public class Folder_adapter extends SimpleDragSortCursorAdapter
{
    Folder_adapter(Context context, int layout, Cursor c,
            String[] from, int[] to, int flags)
    {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public int getCount() {
        DB_drawer db_drawer = new DB_drawer(MainAct.mAct);
        int count = db_drawer.getFoldersCount(true);
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder; // holds references to current item's GUI

        // if convertView is null, inflate GUI and create ViewHolder;
        // otherwise, get existing ViewHolder
        if (convertView == null)
        {
            convertView = MainAct.mAct.getLayoutInflater().inflate(R.layout.folder_row, parent, false);

            // set up ViewHolder for this ListView item
            viewHolder = new ViewHolder();
            viewHolder.audioPlayingIcon = (ImageView) convertView.findViewById(R.id.folder_audio);
            viewHolder.folderTitle = (TextView) convertView.findViewById(R.id.folderText);
            viewHolder.dragIcon = (ImageView) convertView.findViewById(R.id.folder_drag);
            convertView.setTag(viewHolder); // store as View's tag
        }
        else // get the ViewHolder from the convertView's tag
            viewHolder = (ViewHolder) convertView.getTag();

        // set highlight of selected drawer
        if( (AudioManager.mMediaPlayer != null) &&
            (MainAct.mPlaying_folderPos == position)        )
            viewHolder.audioPlayingIcon.setVisibility(View.VISIBLE);
        else
            viewHolder.audioPlayingIcon.setVisibility(View.GONE);

        DB_drawer db_drawer = new DB_drawer(MainAct.mAct);
        viewHolder.folderTitle.setText(db_drawer.getFolderTitle(position,true));

        // dragger
        SharedPreferences pref = MainAct.mAct.getSharedPreferences("show_note_attribute", 0);;
        if(pref.getString("KEY_ENABLE_FOLDER_DRAGGABLE", "no").equalsIgnoreCase("yes"))
            viewHolder.dragIcon.setVisibility(View.VISIBLE);
        else
            viewHolder.dragIcon.setVisibility(View.GONE);

        return convertView;
    }


    private static class ViewHolder
    {
        ImageView audioPlayingIcon;
        TextView folderTitle; // refers to ListView item's ImageView
        ImageView dragIcon;
    }
}
