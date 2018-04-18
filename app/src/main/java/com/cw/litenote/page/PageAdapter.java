package com.cw.litenote.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cw.litenote.R;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.note.Note;
import com.cw.litenote.operation.audio.AudioManager;
import com.cw.litenote.operation.audio.AudioPlayer_page;
import com.cw.litenote.tabs.TabsHost;
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
import static com.cw.litenote.page.Page.mDb_page;

// Pager adapter
public class PageAdapter extends SimpleDragSortCursorAdapter // DragSortCursorAdapter //ResourceDragSortCursorAdapter//SimpleDragSortCursorAdapter
{
	FragmentActivity mAct;
	Cursor cursor;
    int count;
	String linkUri;
	int style;
	int page_pos;

	public PageAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int page_position)
	{
		super(context, layout, c, from, to, page_position);
		mAct = (FragmentActivity) context;
		cursor = c;
        page_pos = page_position;
		this.style = Util.getCurrentPageStyle(page_position);

		if(c != null)
		    count = c.getCount();
		else
		    count = 0;

        System.out.println("PageAdapter / _Page_new_adapter / count =" + count);

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

	@SuppressLint("ClickableViewAccessibility")
    @Override
	public View getView(final int position, View convertView, ViewGroup parent) {
//		System.out.println("PageAdapter / _getView / position = " +  position);
		final ViewHolder holder;

		SharedPreferences pref_show_note_attribute = mAct.getSharedPreferences("show_note_attribute", 0);

		if (convertView == null)
		{
			convertView = mAct.getLayoutInflater().inflate(R.layout.page_view_row, parent, false);

			// set rectangular background
//				view.setBackgroundColor(Util.mBG_ColorArray[style]);
			
			//set round corner and background color
            switch(style)
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
//			holder.rowDivider = convertView.findViewById(R.id.row_divider);
			holder.textBodyBlock = convertView.findViewById(R.id.row_body);
			holder.textBody = (TextView) convertView.findViewById(R.id.row_body_text_view);
			holder.textTime = (TextView) convertView.findViewById(R.id.row_time);
			convertView.setTag(holder);

		}
		else
		{
//			System.out.println("PageAdapter / _getView / convertView != null");
			holder = (ViewHolder) convertView.getTag();
		}

        // on note view
        convertView.findViewById(R.id.row_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabsHost.getCurrentPage().currPlayPosition = position;
                DB_page mDb_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
                int count = mDb_page.getNotesCount(true);
                if(position < count)
                {
                    // apply Note class
                    Intent intent;
                    intent = new Intent(mAct, Note.class);
                    intent.putExtra("POSITION", position);
                    mAct.startActivity(intent);
                }
            }
        });

        // on photo
        convertView.findViewById(R.id.row_thumb_nail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabsHost.getCurrentPage().currPlayPosition = position;
                DB_page mDb_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
                mDb_page.open();
                int count = mDb_page.getNotesCount(false);
                String linkStr = mDb_page.getNoteLinkUri(position,false);
                mDb_page.close();

                if(position < count)
                {
                    if(Util.isYouTubeLink(linkStr)) {
                        AudioManager.stopAudioPlayer();

                        // apply native YouTube
                        Util.openLink_YouTube(mAct, linkStr);
                    }
                }
            }
        });

        // on mark
        holder.imageCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("PageAdapter / _getView / _onClick");
                // toggle marking
				int markingNow = toggleNoteMarking(MainAct.mAct,position);

                // Stop if unmarked item is at playing state
                if(AudioManager.mAudioPos == position) {
                    UtilAudio.stopAudioIfNeeded();
                }

                TabsHost.reloadCurrentPage();
//				TabsHost.getPage_rowItemView(position); //??? use this will not update item view, what else?
				TabsHost.getCurrentPage().showFooter(MainAct.mAct);

                // update audio info
                if(PageUi.isSamePageTable())
                    AudioPlayer_page.prepareAudioInfo();
            }
        });

		// show row Id
		holder.rowId.setText(String.valueOf(position+1));
		holder.rowId.setTextColor(ColorSet.mText_ColorArray[style]);
		
		// show check box, title , picture
		String strTitle = null;
		String strBody = null;
        String pictureUri = null;
        String audioUri = null;
        Long timeCreated = null;
        linkUri = null;
        int marking = 0;

        if(cursor.moveToPosition(position)) {
            strTitle = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_TITLE));
            strBody = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_BODY));
            pictureUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_PICTURE_URI));
            audioUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_AUDIO_URI));
            linkUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_LINK_URI));
            marking = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTE_MARKING));
			timeCreated = cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED));
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
			else if( (linkUri != null) && (linkUri.startsWith("http")))
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
			holder.textTitle.setTextColor(ColorSet.mText_ColorArray[style]);
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
			holder.audioName.setTextColor(ColorSet.mText_ColorArray[style]);

		// show audio highlight if audio is not at Stop
		if( PageUi.isSamePageTable() &&
			(position == AudioManager.mAudioPos)  &&
//			(AudioManager.mMediaPlayer != null) &&
			(AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
			(AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE) 	)
		{
//            System.out.println("PageAdapter / _getView / show highlight / position = " + position);
			TabsHost.getCurrentPage().mHighlightPosition = position;
			holder.audioBlock.setBackgroundResource(R.drawable.bg_highlight_border);
			holder.audioBlock.setVisibility(View.VISIBLE);

			// set type face
			holder.audioName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

			// set icon
			holder.imageAudio.setVisibility(View.VISIBLE);
			holder.imageAudio.setImageResource(R.drawable.ic_audio);

			// set animation
//			Animation animation = AnimationUtils.loadAnimation(mContext , R.anim.right_in);
//			holder.audioBlock.startAnimation(animation);
		}
		else
		{

//			System.out.println("PageAdapter / _getView / not show highlight ");
			holder.audioBlock.setBackgroundResource(R.drawable.bg_gray_border);
			holder.audioBlock.setVisibility(View.VISIBLE);

			// set type face
			holder.audioName.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);

			// set icon
			holder.imageAudio.setVisibility(View.VISIBLE);
			if(style % 2 == 0)
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
//		System.out.println("PageAdapter / _getView / pictureUri = " + pictureUri);

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
										   (style % 2 == 1 ?
											UilCommon.optionsForRounded_light:
											UilCommon.optionsForRounded_dark),
										   mAct);
			}
			catch(Exception e)
			{
				Log.e("PageAdapter", "UtilImage_bitmapLoader error");
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

            try {
                AsyncTaskAudioBitmap audioAsyncTask;
                audioAsyncTask = new AsyncTaskAudioBitmap(mAct,
                        audioUri,
                        holder.thumbAudio,
                        holder.progressBar,
                        true);
                audioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Searching media ...");
            } catch (Exception e) {
                Log.e("PageAdapter", "AsyncTaskAudioBitmap error");
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
                        //todo TBD
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUri));
                        mAct.startActivity(intent);
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
							holder.rowId.setTextColor(ColorSet.mText_ColorArray[style]);

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
//            String strBody = cursor.getString(cursor.getColumnIndex(KEY_NOTE_BODY));
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

//			holder.rowDivider.setVisibility(View.VISIBLE);
			holder.textBody.setTextColor(ColorSet.mText_ColorArray[style]);
			// time stamp
            holder.textTime.setText(Util.getTimeString(timeCreated));
			holder.textTime.setTextColor(ColorSet.mText_ColorArray[style]);
	  	}
	  	else
	  	{
//			holder.rowDivider.setVisibility(View.INVISIBLE);
	  		holder.textBodyBlock.setVisibility(View.INVISIBLE);
	  	}


	  	// dragger
	  	if(pref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "no").equalsIgnoreCase("yes"))
	  		holder.imageDragger.setVisibility(View.VISIBLE);
	  	else
	  		holder.imageDragger.setVisibility(View.GONE);

	  	// marking
        if(marking == 1)
        {
			holder.imageCheck.setBackgroundResource(style % 2 == 1 ?
					R.drawable.btn_check_on_holo_light :
					R.drawable.btn_check_on_holo_dark);
		}
		else
		{
			holder.imageCheck.setBackgroundResource(style % 2 == 1 ?
					R.drawable.btn_check_off_holo_light :
					R.drawable.btn_check_off_holo_dark);
		}

		return convertView;
	}

    // toggle mark of note
    public static int toggleNoteMarking(FragmentActivity mAct,int position)
    {
        int marking = 0;
		DB_page mDb_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
        mDb_page.open();
        int count = mDb_page.getNotesCount(false);
        if(position >= count) //end of list
        {
            mDb_page.close();
            return marking;
        }

        String strNote = mDb_page.getNoteTitle(position,false);
        String strPictureUri = mDb_page.getNotePictureUri(position,false);
        String strAudioUri = mDb_page.getNoteAudioUri(position,false);
        String strLinkUri = mDb_page.getNoteLinkUri(position,false);
        String strNoteBody = mDb_page.getNoteBody(position,false);
        Long idNote =  mDb_page.getNoteId(position,false);

        // toggle the marking
        if(mDb_page.getNoteMarking(position,false) == 0)
        {
            mDb_page.updateNote(idNote, strNote, strPictureUri, strAudioUri, "", strLinkUri, strNoteBody, 1, 0, false);
            marking = 1;
        }
        else
        {
            mDb_page.updateNote(idNote, strNote, strPictureUri, strAudioUri, "", strLinkUri, strNoteBody, 0, 0, false);
            marking = 0;
        }
        mDb_page.close();

        System.out.println("Page / _toggleNoteMarking / position = " + position + ", marking = " + mDb_page.getNoteMarking(position,true));
        return  marking;
    }

}