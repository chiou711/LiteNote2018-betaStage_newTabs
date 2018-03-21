package com.cw.litenote.page;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cw.litenote.R;
import com.cw.litenote.operation.audio.AudioManager;
import com.cw.litenote.util.ColorSet;
import com.cw.litenote.util.CustomWebView;
import com.cw.litenote.util.Util;
import com.cw.litenote.util.audio.UtilAudio;
import com.cw.litenote.util.image.AsyncTaskAudioBitmap;
import com.cw.litenote.util.image.UtilImage;
import com.cw.litenote.util.image.UtilImage_bitmapLoader;
import com.cw.litenote.util.uil.UilCommon;
import com.cw.litenote.util.video.UtilVideo;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import static com.cw.litenote.db.DB_page.KEY_NOTE_AUDIO_URI;
import static com.cw.litenote.db.DB_page.KEY_NOTE_BODY;
import static com.cw.litenote.db.DB_page.KEY_NOTE_CREATED;
import static com.cw.litenote.db.DB_page.KEY_NOTE_LINK_URI;
import static com.cw.litenote.db.DB_page.KEY_NOTE_MARKING;
import static com.cw.litenote.db.DB_page.KEY_NOTE_PICTURE_URI;
import static com.cw.litenote.db.DB_page.KEY_NOTE_TITLE;
import static com.cw.litenote.page.Page_new.mDb_page;

// Pager adapter
public class Page_new_adapter extends SimpleDragSortCursorAdapter // DragSortCursorAdapter //ResourceDragSortCursorAdapter//SimpleDragSortCursorAdapter
{
	FragmentActivity mAct;
	Cursor cursor;
    int count;

    Page_new_adapter(Context context, int layout, Cursor c,
                     String[] from, int[] to, int flags)
	{
		super(context, layout, c, from, to, flags);
		mAct = (FragmentActivity) context;
		cursor = c;
		if(c != null)
		    count = c.getCount();
		else
		    count = 0;

        System.out.println("Page_adapter / _Page_adapter / count =" + count);

        // add this for fixing java.lang.IllegalStateException: attempt to re-open an already-closed object
        mDb_page.open();
        mDb_page.close();

	}

    private class ViewHolder {
		ImageView imageCheck;
		TextView rowId;
		View audioBlock;
		ImageView imageAudio;
		TextView audioName;
		TextView textTitle;
		View rowDivider;
		View textBodyBlock;
		TextView textBody;
		TextView textTime;
		ImageView imageDragger;
		View thumbBlock;
		ImageView thumbPicture;
		ImageView thumbAudio;
		CustomWebView thumbWeb;
		ProgressBar progressBar;
	}
	
	@Override
	public int getCount() {
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		System.out.println("Page_adapter / _getView / position = " +  position);
		final ViewHolder holder;

		SharedPreferences pref_show_note_attribute = mAct.getSharedPreferences("show_note_attribute", 0);

		if (convertView == null)
		{
			convertView = mAct.getLayoutInflater().inflate(R.layout.page_view_row, parent, false);

			// set rectangular background
//				view.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
			
			//set round corner and background color
    		switch(Page.mStyle)
    		{
    			case 0:
                    convertView.setBackgroundResource(R.drawable.bg_0);
    				break;
    			case 1:
					convertView.setBackgroundResource(R.drawable.bg_1);
    				break;
    			case 2:
					convertView.setBackgroundResource(R.drawable.bg_2);
    				break;
    			case 3:
					convertView.setBackgroundResource(R.drawable.bg_3);
    				break;
    			case 4:
					convertView.setBackgroundResource(R.drawable.bg_4);
    				break;
    			case 5:
					convertView.setBackgroundResource(R.drawable.bg_5);
    				break;
    			case 6:
					convertView.setBackgroundResource(R.drawable.bg_6);
    				break;
    			case 7:
					convertView.setBackgroundResource(R.drawable.bg_7);
    				break;
    			case 8:
					convertView.setBackgroundResource(R.drawable.bg_8);
    				break;
    			case 9:
					convertView.setBackgroundResource(R.drawable.bg_9);
    				break;
    			default:
    				break;
    		}
    		
			holder = new ViewHolder();
			holder.rowId= (TextView) convertView.findViewById(R.id.row_id);
			holder.audioBlock = convertView.findViewById(R.id.audio_block);
			holder.imageAudio = (ImageView) convertView.findViewById(R.id.img_audio);
			holder.audioName = (TextView) convertView.findViewById(R.id.row_audio_name);
			holder.imageCheck= (ImageView) convertView.findViewById(R.id.img_check);
			holder.thumbBlock = convertView.findViewById(R.id.row_thumb_nail);
			holder.thumbPicture = (ImageView) convertView.findViewById(R.id.thumb_picture);
			holder.thumbAudio = (ImageView) convertView.findViewById(R.id.thumb_audio);
			holder.thumbWeb = (CustomWebView) convertView.findViewById(R.id.thumb_web);
			holder.imageDragger = (ImageView) convertView.findViewById(R.id.img_dragger);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.thumb_progress);
			holder.textTitle = (TextView) convertView.findViewById(R.id.row_title);
			holder.rowDivider = convertView.findViewById(R.id.row_divider);
			holder.textBodyBlock = convertView.findViewById(R.id.row_body);
			holder.textBody = (TextView) convertView.findViewById(R.id.row_body_text_view);
			holder.textTime = (TextView) convertView.findViewById(R.id.row_time);
			convertView.setTag(holder);

		}
		else
		{
//			System.out.println("Page_adapter / _getView / convertView != null");
			holder = (ViewHolder) convertView.getTag();
		}

		// show row Id
		holder.rowId.setText(String.valueOf(position+1));
		holder.rowId.setTextColor(ColorSet.mText_ColorArray[Page.mStyle]);
		
		// show check box, title , picture
		String strTitle = null;
        String pictureUri = null;
        String audioUri = null;
        String linkUri = null;
		if(cursor.moveToPosition(position)) {
            strTitle = cursor.getString(cursor.getColumnIndex(KEY_NOTE_TITLE));
            pictureUri = cursor.getString(cursor.getColumnIndex(KEY_NOTE_PICTURE_URI));
            audioUri = cursor.getString(cursor.getColumnIndex(KEY_NOTE_AUDIO_URI));
            linkUri = cursor.getString(cursor.getColumnIndex(KEY_NOTE_LINK_URI));
        }

		// set title
		if( Util.isEmptyString(strTitle) )
		{

			if(Util.isYouTubeLink(linkUri)) {
				strTitle = Util.getYouTubeTitle(linkUri);
				holder.textTitle.setVisibility(View.VISIBLE);
				holder.textTitle.setText(strTitle);
				holder.textTitle.setTextColor(Color.GRAY);
			}
			else if(linkUri.startsWith("http"))
			{
				holder.textTitle.setVisibility(View.VISIBLE);
				Util.setHttpTitle(linkUri, mAct,holder.textTitle);
			}
			else
			{
				// make sure empty title is empty after scrolling
				holder.textTitle.setVisibility(View.VISIBLE);
				holder.textTitle.setText("");
			}
		}
		else
		{
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setText(strTitle);
			holder.textTitle.setTextColor(ColorSet.mText_ColorArray[Page.mStyle]);
		}


		// set audio name
		String audio_name = null;
		if(!Util.isEmptyString(audioUri))
			audio_name = Util.getDisplayNameByUriString(audioUri, mAct);

		if(Util.isUriExisted(audioUri, mAct))
			holder.audioName.setText(audio_name);
		else
			holder.audioName.setText(R.string.file_not_found);

//			holder.audioName.setTextSize(12.0f);

		if(!Util.isEmptyString(audioUri))
			holder.audioName.setTextColor(ColorSet.mText_ColorArray[Page.mStyle]);

		// show audio highlight if audio is not at Stop
		if( PageUi.isSamePageTable() &&
			(position == AudioManager.mAudioPos)  &&
			(AudioManager.mMediaPlayer != null) &&
			(AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
			(AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE))
		{
			Page.mHighlightPosition = position;
			holder.audioBlock.setBackgroundResource(R.drawable.bg_highlight_border);
			holder.audioBlock.setVisibility(View.VISIBLE);

			// set type face
			holder.audioName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

			// set icon
			holder.imageAudio.setVisibility(View.VISIBLE);
			holder.imageAudio.setImageResource(R.drawable.ic_audio);

			// set animation
			Animation animation = AnimationUtils.loadAnimation(mContext , R.anim.right_in);
			holder.audioBlock.startAnimation(animation);
		}
		else
		{

			holder.audioBlock.setBackgroundResource(R.drawable.bg_gray_border);
			holder.audioBlock.setVisibility(View.VISIBLE);

			// set type face
			holder.audioName.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);

			// set icon
			holder.imageAudio.setVisibility(View.VISIBLE);
			if(Page.mStyle % 2 == 0)
				holder.imageAudio.setImageResource(R.drawable.ic_audio_off_white);
			else
				holder.imageAudio.setImageResource(R.drawable.ic_audio_off_black);
		}

		// audio icon and block
		if(Util.isEmptyString(audioUri))
		{
			holder.imageAudio.setVisibility(View.INVISIBLE);
			holder.audioBlock.setVisibility(View.INVISIBLE);
		}


		// Show image thumb nail if picture Uri is none and YouTube link exists
		if(Util.isEmptyString(pictureUri) &&
		   Util.isYouTubeLink(linkUri)      )
		{
			pictureUri = "http://img.youtube.com/vi/"+Util.getYoutubeId(linkUri)+"/0.jpg";
		}
//		System.out.println("Page_adapter / _getView / pictureUri = " + pictureUri);

		// show thumb nail if picture Uri exists
		if(UtilImage.hasImageExtension(pictureUri, mAct ) ||
		   UtilVideo.hasVideoExtension(pictureUri, mAct )   )
		{
			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbPicture.setVisibility(View.VISIBLE);
			holder.thumbAudio.setVisibility(View.GONE);
			holder.thumbWeb.setVisibility(View.GONE);
			// load bitmap to image view
			try
			{
				new UtilImage_bitmapLoader(holder.thumbPicture,
										   pictureUri,
										   holder.progressBar,
										   (Page.mStyle % 2 == 1 ?
											UilCommon.optionsForRounded_light:
											UilCommon.optionsForRounded_dark),
										   mAct);
			}
			catch(Exception e)
			{
				Log.e("Page_adapter", "UtilImage_bitmapLoader error");
				holder.thumbBlock.setVisibility(View.GONE);
				holder.thumbPicture.setVisibility(View.GONE);
				holder.thumbAudio.setVisibility(View.GONE);
				holder.thumbWeb.setVisibility(View.GONE);
			}
		}
		// show audio thumb nail if picture Uri is none and audio Uri exists
		else if((Util.isEmptyString(pictureUri) && UtilAudio.hasAudioExtension(audioUri) ) )
		{
			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.VISIBLE);
			holder.thumbWeb.setVisibility(View.GONE);
			try
			{
			    AsyncTaskAudioBitmap audioAsyncTask;
			    audioAsyncTask = new AsyncTaskAudioBitmap(mAct,
						    							  audioUri,
						    							  holder.thumbAudio,
						    							  holder.progressBar,
                                                          true);
				audioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Searching media ...");
			}
			catch(Exception e)
			{
				Log.e("Page_adapter", "AsyncTaskAudioBitmap error");
				holder.thumbBlock.setVisibility(View.GONE);
				holder.thumbPicture.setVisibility(View.GONE);
				holder.thumbAudio.setVisibility(View.GONE);
				holder.thumbWeb.setVisibility(View.GONE);
			}
		}
		// set web title and web view thumb nail of link if no title content
		else if(!Util.isEmptyString(linkUri) &&
                linkUri.startsWith("http")   &&
				!Util.isYouTubeLink(linkUri)   )
		{
			// reset web view
			CustomWebView.pauseWebView(holder.thumbWeb);
			CustomWebView.blankWebView(holder.thumbWeb);

			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbWeb.setInitialScale(50);
			holder.thumbWeb.getSettings().setJavaScriptEnabled(true);//Using setJavaScriptEnabled can introduce XSS vulnerabilities
			holder.thumbWeb.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT );
//            // speed up
//            if (Build.VERSION.SDK_INT >= 19) {
//                // chromium, enable hardware acceleration
//                holder.thumbWeb.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//            } else {
//                // older android version, disable hardware acceleration
//                holder.thumbWeb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//            }
			holder.thumbWeb.loadUrl(linkUri);
			holder.thumbWeb.setVisibility(View.VISIBLE);

			// no interactive response
			holder.thumbWeb.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
                    if( (event.getAction() == MotionEvent.ACTION_POINTER_UP) ||
                        (event.getAction() == MotionEvent.ACTION_UP)            )
                    {
                        Page.openClickedItem(position);
                    }
					return true;
				}
			});

			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.GONE);

			//Add for non-stop showing of full screen web view
			holder.thumbWeb.setWebViewClient(new WebViewClient() {
				@Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url)
			    {
			        view.loadUrl(url);
			        return true;
			    }
			});


			if (Util.isEmptyString(strTitle)) {

				holder.thumbWeb.setWebChromeClient(new WebChromeClient() {
					@Override
					public void onReceivedTitle(WebView view, String title) {
						super.onReceivedTitle(view, title);
						if (!TextUtils.isEmpty(title) &&
								!title.equalsIgnoreCase("about:blank")) {
							holder.textTitle.setVisibility(View.VISIBLE);
							holder.rowId.setText(String.valueOf(position + 1));
							holder.rowId.setTextColor(ColorSet.mText_ColorArray[Page.mStyle]);

						}
					}
				});
			}
		}
		else
		{
			holder.thumbBlock.setVisibility(View.GONE);
			holder.thumbPicture.setVisibility(View.GONE);
			holder.thumbAudio.setVisibility(View.GONE);
			holder.thumbWeb.setVisibility(View.GONE);
		}


		// Show note body or not
	  	if(pref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
	  	{
	  		// test only: enabled for showing picture path
            String strBody = cursor.getString(cursor.getColumnIndex(KEY_NOTE_BODY));
	  		if(!Util.isEmptyString(strBody)){
				//normal: do nothing
			}
	  		else if(!Util.isEmptyString(pictureUri)) {
//				strBody = pictureUri;//show picture Uri
			}
	  		else if(!Util.isEmptyString(linkUri)) {
//				strBody = linkUri; //show link Uri
			}

			holder.textBody.setText(strBody);
//			holder.textBody.setTextSize(12);

			holder.rowDivider.setVisibility(View.VISIBLE);
			holder.textBody.setTextColor(ColorSet.mText_ColorArray[Page.mStyle]);
			// time stamp
            holder.textTime.setText(Util.getTimeString(cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED))));
			holder.textTime.setTextColor(ColorSet.mText_ColorArray[Page.mStyle]);
	  	}
	  	else
	  	{
			holder.rowDivider.setVisibility(View.GONE);
	  		holder.textBodyBlock.setVisibility(View.GONE);
	  	}


	  	// dragger
	  	if(pref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "no").equalsIgnoreCase("yes"))
	  		holder.imageDragger.setVisibility(View.VISIBLE);
	  	else
	  		holder.imageDragger.setVisibility(View.GONE);

	  	// marking
        if( cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTE_MARKING)) == 1)
			holder.imageCheck.setBackgroundResource(Page.mStyle%2 == 1 ?
	    			R.drawable.btn_check_on_holo_light:
	    			R.drawable.btn_check_on_holo_dark);
		else
			holder.imageCheck.setBackgroundResource(Page.mStyle%2 == 1 ?
					R.drawable.btn_check_off_holo_light:
					R.drawable.btn_check_off_holo_dark);



		//todo TBD
		///
//		convertView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//
//				cursor.moveToPosition(position);
//				String linkUri = cursor.getString(cursor.getColumnIndex(KEY_NOTE_LINK_URI));
//				Page_new.openClickedItem(mAct,position,linkUri);
//			}
//		});
		///

		return convertView;
	}

}