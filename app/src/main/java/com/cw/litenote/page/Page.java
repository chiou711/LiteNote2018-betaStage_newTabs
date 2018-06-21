package com.cw.litenote.page;

import java.util.ArrayList;
import java.util.List;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.folder.FolderUi;
import com.cw.litenote.operation.audio.AudioManager;
import com.cw.litenote.operation.audio.AudioPlayer_page;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.tabs.TabsHost;
import com.cw.litenote.util.preferences.Pref;
import com.cw.litenote.util.uil.UilCommon;
import com.cw.litenote.util.uil.UilListViewBaseFragment;
import com.cw.litenote.util.Util;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;


public class Page extends UilListViewBaseFragment
//						  implements LoaderManager.LoaderCallbacks<List<String>>
{
	Cursor mCursor_note;
	public static DB_page mDb_page;
	public SharedPreferences pref_show_note_attribute;

	// This is the Adapter being used to display the list's data.
	public DragSortListView drag_listView;
	DragSortController drag_controller;
    public static int mStyle = 0;
	public AppCompatActivity mAct;
	String mClassName;
    public static int mHighlightPosition;
	public SeekBar seekBarProgress;
	ProgressBar mSpinner;
    public static int currPlayPosition;
	public int page_tableId;
	int page_pos;

    public Page(){
    }

	@SuppressLint("ValidFragment")
	public Page(int pos,int id){
		page_pos = pos;
    	page_tableId = id;
	}

	View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		System.out.println("Page / _onCreateView / page_tableId = " + page_tableId);

        rootView = inflater.inflate(R.layout.page_view, container, false);

		mAct = MainAct.mAct;
		mClassName = getClass().getSimpleName();
        listView = (DragSortListView)rootView.findViewById(android.R.id.list);
		drag_listView = listView;

		if(Build.VERSION.SDK_INT >= 21)
			drag_listView.setSelector(R.drawable.ripple);

//		mSpinner = (ProgressBar) rootView.findViewById(R.id.list1_progress);
//		new SpinnerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//		new ProgressBarTask().execute();

		//refer to
		// http://stackoverflow.com/questions/9119627/android-sdk-asynctask-doinbackground-not-running-subclass
		//Behavior of AsyncTask().execute(); has changed through Android versions.
		// -Before Donut (Android:1.6 API:4) tasks were executed serially,
		// -from Donut to Gingerbread (Android:2.3 API:9) tasks executed paralleled;
		// -since Honeycomb (Android:3.0 API:11) execution was switched back to sequential;
		// a new method AsyncTask().executeOnExecutor(Executor) however, was added for parallel execution.

		// show scroll thumb: always
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//			drag_listView.setFastScrollAlwaysVisible(true);

		// show scroll thumb: when touched
		drag_listView.setScrollbarFadingEnabled(true);
		drag_listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
		Util.setScrollThumb(getActivity(),drag_listView);

		mStyle = Util.getCurrentPageStyle(page_pos);
//    	System.out.println("Page / _onCreateView / mStyle = " + mStyle);

		UilCommon.init();

		drag_controller = buildController(drag_listView);
		drag_listView.setFloatViewManager(drag_controller);
		drag_listView.setOnTouchListener(drag_controller);
		//called on it but does not override performClick
		drag_listView.setDragEnabled(true);

		// We have a menu item to show in action bar.
//		setHasOptionsMenu(true);

		// Create an empty mTabsPagerAdapter we will use to display the loaded data.
//		mAdapter = new NoteListAdapter(getActivity());

//		setListAdapter(mAdapter);

		// Start out with a progress indicator.
//		setListShown(true); //set progress indicator

		// Prepare the loader. Either re-connect with an existing one or start a new one.
//        getLoaderManager().initLoader(0, null, this);
//        getLoaderManager().initLoader(page_tableId, null, Page.this);

        fillData(mAct, drag_listView);
		mItemAdapter.notifyDataSetChanged();

		return rootView;
	}

	int mFirstVisibleIndex;
	int mFirstVisibleIndexTop;

	/**
	 * fill data
	 */
	public PageAdapter mItemAdapter;
	public void fillData(AppCompatActivity mAct, DragSortListView listView)
	{
//		System.out.println("Page / _fillData / page_tableId = " + page_tableId);

        mDb_page = new DB_page(getActivity(), page_tableId);
		mDb_page.open();
		mCursor_note = mDb_page.mCursor_note;

		// set adapter
		String[] from = new String[] { DB_page.KEY_NOTE_TITLE};
		int[] to = new int[] { R.id.row_whole};

		mItemAdapter = new PageAdapter(
				mAct,
//				R.layout.page_view_row,
				R.layout.page_view_card,
				mCursor_note,
				from,
				to,
				page_pos
		);
		mDb_page.close();// set close here, if cursor is used in mTabsPagerAdapter

		listView.setAdapter(mItemAdapter);

        listView.setDropListener(onDrop);
        listView.setDragListener(onDrag);
//        listView.setAudioListener(onAudio);
        listView.setOnScrollListener(onScroll);

        //init
        TabsHost.showFooter(mAct);
	}

	private class SpinnerTask extends AsyncTask <Void,Void,Void>{
	    @Override
	    protected void onPreExecute(){
			drag_listView.setVisibility(View.GONE);
			TabsHost.mFooterMessage.setVisibility(View.GONE);
			mSpinner.setVisibility(View.VISIBLE);
	    }

	    @Override
	    protected Void doInBackground(Void... arg0) {
			return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	    	mSpinner.setVisibility(View.GONE);
			drag_listView.setVisibility(View.VISIBLE);
			TabsHost.mFooterMessage.setVisibility(View.VISIBLE);
			if(!this.isCancelled())
			{
				this.cancel(true);
			}
	    }
	}

    // list view listener: on drag
    public DragSortListView.DragListener onDrag = new DragSortListView.DragListener()
    {
                @Override
                public void drag(int startPosition, int endPosition) {
                	//add highlight boarder
//                    View v = drag_listView.mFloatView;
//                    v.setBackgroundColor(Color.rgb(255,128,0));
//                	v.setBackgroundResource(R.drawable.listview_item_shape_dragging);
//                    v.setPadding(0, 4, 0,4);
                }
    };

    // list view listener: on drop
    public DragSortListView.DropListener onDrop = new DragSortListView.DropListener()
    {
        @Override
        public void drop(int startPosition, int endPosition) {

        	int oriStartPos = startPosition;
        	int oriEndPos = endPosition;

            mDb_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
			if(startPosition >= mDb_page.getNotesCount(true)) // avoid footer error
				return;

			//reorder data base storage
			int loop = Math.abs(startPosition-endPosition);
			for(int i=0;i< loop;i++)
			{
				swapRows(mDb_page, startPosition,endPosition);
				if((startPosition-endPosition) >0)
					endPosition++;
				else
					endPosition--;
			}

			if( PageUi.isAudioPlayingPage() &&
	     		(AudioManager.mMediaPlayer != null)				   )
			{
				if( (mHighlightPosition == oriEndPos)  && (oriStartPos > oriEndPos))
				{
					mHighlightPosition = oriEndPos+1;
				}
				else if( (mHighlightPosition == oriEndPos) && (oriStartPos < oriEndPos))
				{
					mHighlightPosition = oriEndPos-1;
				}
				else if( (mHighlightPosition == oriStartPos)  && (oriStartPos > oriEndPos))
				{
					mHighlightPosition = oriEndPos;
				}
				else if( (mHighlightPosition == oriStartPos) && (oriStartPos < oriEndPos))
				{
					mHighlightPosition = oriEndPos;
				}
				else if(  (mHighlightPosition < oriEndPos) &&
						  (mHighlightPosition > oriStartPos)   )
				{
					mHighlightPosition--;
				}
				else if( (mHighlightPosition > oriEndPos) &&
						 (mHighlightPosition < oriStartPos)  )
				{
					mHighlightPosition++;
				}

				AudioManager.mAudioPos = mHighlightPosition;
				AudioPlayer_page.prepareAudioInfo();
			}

            TabsHost.reloadCurrentPage();

            // update footer
			TabsHost.showFooter(mAct);
        }
    };

	// list view listener: on audio
	public DragSortListView.AudioListener onAudio = new DragSortListView.AudioListener()
	{   @Override
		public void audio(int position)
		{
	//			System.out.println("Page / _onAudio");
		}
	};


    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv)
    {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);

        //drag
	  	pref_show_note_attribute = getActivity().getSharedPreferences("show_note_attribute", 0);
	  	if(pref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "no").equalsIgnoreCase("yes"))
	  		controller.setDragInitMode(DragSortController.ON_DOWN); // click
	  	else
	        controller.setDragInitMode(DragSortController.MISS);

	  	controller.setDragHandleId(R.id.img_dragger);// handler
//        drag_controller.setDragInitMode(DragSortController.ON_LONG_PRESS); //long click to drag
	  	controller.setBackgroundColor(Color.argb(128,128,64,0));// background color when dragging

        // audio
        controller.setAudioEnabled(true);
//        drag_controller.setClickAudioId(R.id.img_audio);
        controller.setClickAudioId(R.id.audio_block);
        controller.setAudioMode(DragSortController.ON_DOWN);

        return controller;
    }

    @Override
    public void onResume() {
		System.out.println("Page / _onResume / page_tableId = " + page_tableId);

        super.onResume();

        if(Pref.getPref_focusView_page_tableId(MainAct.mAct) == page_tableId) {
            System.out.println("Page / _onResume / resume_listView_vScroll");
            TabsHost.resume_listView_vScroll(drag_listView);
        }

    }

    @Override
    public void onPause() {
    	super.onPause();
		System.out.println("Page / _onPause / page_tableId = " + page_tableId);
	 }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
//		System.out.println(mClassName + " / onSaveInstanceState");
    }

//	@Override
//	public Loader<List<String>> onCreateLoader(int id, Bundle args)
//	{
//		// This is called when a new Loader needs to be created.
//		return new NoteListLoader(getActivity());
//	}

//	@Override
//	public void onLoadFinished(Loader<List<String>> loader,
//							   List<String> data)
//	{
//		if(en_dbg_msg)
//			System.out.println("Page / _onLoadFinished / page_tableId = " + page_tableId);
//
//        // Set the new data in the mTabsPagerAdapter.
//		mAdapter.setData(data);
//
//		// The list should now be shown.
//		if (isResumed()) {
////            setListShown(true); //Can't be used with a custom content view???
//        }
////		else
////			setListShownNoAnimation(true);
//
//		fillData();
////        getLoaderManager().destroyLoader(0); // add for fixing callback twice
//        getLoaderManager().destroyLoader(page_tableId); // add for fixing callback twice
//
//	}

//	@Override
//	public void onLoaderReset(Loader<List<String>> loader) {
//		// Clear the data in the adapter.
//		mAdapter.setData(null);
//	}


    public OnScrollListener onScroll = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
//            mFirstVisibleIndex = drag_listView.getFirstVisiblePosition();
//            View v = drag_listView.getChildAt(0);
//            mFirstVisibleIndexTop = (v == null) ? 0 : v.getTop();
//
//            if( (TabsHost.getFocus_tabPos() == MainAct.mPlaying_pagePos)&&
//                    (MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos()) &&
//                    (AudioManager.getPlayerState() == AudioManager.PLAYER_AT_PLAY) &&
//                    (drag_listView.getChildAt(0) != null)                    )
//            {
//                // do nothing when playing audio
//                System.out.println("_onScrollStateChanged / do nothing");
//            }
//            else
//            {
//                // keep index and top position
//                Pref.setPref_focusView_list_view_first_visible_index(getActivity(), mFirstVisibleIndex);
//                Pref.setPref_focusView_list_view_first_visible_index_top(getActivity(), mFirstVisibleIndexTop);
//            }
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
//			System.out.println("_onScroll / firstVisibleItem " + firstVisibleItem);
//			System.out.println("_onScroll / visibleItemCount " + visibleItemCount);
//			System.out.println("_onScroll / totalItemCount " + totalItemCount);
		}
	};


    // swap rows
	protected static void swapRows(DB_page dB_page, int startPosition, int endPosition)
	{
		Long mNoteNumber1;
		String mNoteTitle1;
		String mNotePictureUri1;
		String mNoteAudioUri1;
		String mNoteLinkUri1;
		String mNoteBodyString1;
		int mMarkingIndex1;
		Long mCreateTime1;
		Long mNoteNumber2 ;
		String mNotePictureUri2;
		String mNoteAudioUri2;
		String mNoteLinkUri2;
		String mNoteTitle2;
		String mNoteBodyString2;
		int mMarkingIndex2;
		Long mCreateTime2;

		dB_page.open();
		mNoteNumber1 = dB_page.getNoteId(startPosition,false);
        mNoteTitle1 = dB_page.getNoteTitle(startPosition,false);
        mNotePictureUri1 = dB_page.getNotePictureUri(startPosition,false);
        mNoteAudioUri1 = dB_page.getNoteAudioUri(startPosition,false);
        mNoteLinkUri1 = dB_page.getNoteLinkUri(startPosition,false);
        mNoteBodyString1 = dB_page.getNoteBody(startPosition,false);
        mMarkingIndex1 = dB_page.getNoteMarking(startPosition,false);
    	mCreateTime1 = dB_page.getNoteCreatedTime(startPosition,false);

		mNoteNumber2 = dB_page.getNoteId(endPosition,false);
        mNoteTitle2 = dB_page.getNoteTitle(endPosition,false);
        mNotePictureUri2 = dB_page.getNotePictureUri(endPosition,false);
        mNoteAudioUri2 = dB_page.getNoteAudioUri(endPosition,false);
        mNoteLinkUri2 = dB_page.getNoteLinkUri(endPosition,false);
        mNoteBodyString2 = dB_page.getNoteBody(endPosition,false);
        mMarkingIndex2 = dB_page.getNoteMarking(endPosition,false);
    	mCreateTime2 = dB_page.getNoteCreatedTime(endPosition,false);

        dB_page.updateNote(mNoteNumber2,
				 mNoteTitle1,
				 mNotePictureUri1,
				 mNoteAudioUri1,
				 "",
				 mNoteLinkUri1,
				 mNoteBodyString1,
				 mMarkingIndex1,
				 mCreateTime1,false);

		dB_page.updateNote(mNoteNumber1,
		 		 mNoteTitle2,
		 		 mNotePictureUri2,
		 		 mNoteAudioUri2,
				 "",
				 mNoteLinkUri2,
		 		 mNoteBodyString2,
		 		 mMarkingIndex2,
		 		 mCreateTime2,false);

		dB_page.close();
	}

	static public void swap(DB_page dB_page)
	{
        int startCursor = dB_page.getNotesCount(true)-1;
        int endCursor = 0;

		//reorder data base storage for ADD_NEW_TO_TOP option
		int loop = Math.abs(startCursor-endCursor);
		for(int i=0;i< loop;i++)
		{
			swapRows(dB_page, startCursor,endCursor);
			if((startCursor-endCursor) >0)
				endCursor++;
			else
				endCursor--;
		}
	}

    public int getNotesCountInPage(AppCompatActivity mAct)
    {
        DB_page mDb_page = new DB_page(mAct,page_tableId );
        mDb_page.open();
        int count = mDb_page.getNotesCount(false);
        mDb_page.close();
        return count;
    }


	/*
	 * inner class for note list loader
	 */
	public static class NoteListLoader extends AsyncTaskLoader<List<String>>
	{
		List<String> mApps;

		NoteListLoader(Context context) {
			super(context);

		}

		@Override
		public List<String> loadInBackground() {
			return new ArrayList<>();
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}
	}

	/*
	 * 	inner class for note list adapter
	 */
//	public static class NoteListAdapter extends ArrayAdapter<String>
//	{
//		NoteListAdapter(Context context) {
//			super(context, android.R.layout.simple_list_item_1);
//		}
//		public void setData(List<String> data) {
//			clear();
//			if (data != null) {
//					addAll(data);
//			}
//		}
//	}

}