package com.cw.litenote.note;

import com.cw.litenote.note_common.Note_common;
import com.cw.litenote.operation.audio.AudioManager;
import com.cw.litenote.operation.audio.AudioPlayer_page;
import com.cw.litenote.page.Page;
import com.cw.litenote.R;
import com.cw.litenote.db.DB_page;
import com.cw.litenote.page.PageUi;
import com.cw.litenote.tabs.TabsHost;
import com.cw.litenote.util.audio.UtilAudio;
import com.cw.litenote.util.image.TouchImageView;
import com.cw.litenote.util.image.UtilImage;
import com.cw.litenote.util.ColorSet;
import com.cw.litenote.util.Util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Note_edit extends Activity 
{

    private Long noteId, createdTime;
    private String title, picUriStr, audioUri, linkUri, cameraPictureUri, body;
    Note_common note_common;
    private boolean enSaveDb = true;
    boolean bUseCameraImage;
    DB_page dB;
    TouchImageView enlargedImage;
    int position;
    final int EDIT_LINK = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // check note count first
		dB = new DB_page(this, TabsHost.getCurrentPageTableId());

        if(dB.getNotesCount(true) ==  0)
        {
        	finish(); // add for last note being deleted
        	return;
        }
        
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note_title);// set title
    	
        System.out.println("Note_edit / onCreate");
        
		enlargedImage = (TouchImageView)findViewById(R.id.expanded_image);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(ColorSet.getBarColor(this)));

    	Bundle extras = getIntent().getExtras();
    	position = extras.getInt("list_view_position");
    	noteId = extras.getLong(DB_page.KEY_NOTE_ID);
    	picUriStr = extras.getString(DB_page.KEY_NOTE_PICTURE_URI);
    	audioUri = extras.getString(DB_page.KEY_NOTE_AUDIO_URI);
    	linkUri = extras.getString(DB_page.KEY_NOTE_LINK_URI);
    	title = extras.getString(DB_page.KEY_NOTE_TITLE);
    	body = extras.getString(DB_page.KEY_NOTE_BODY);
    	createdTime = extras.getLong(DB_page.KEY_NOTE_CREATED);
        

        //initialization
        note_common = new Note_common(this, dB, noteId, title, picUriStr, audioUri, "", linkUri, body, createdTime);
        note_common.UI_init();
        cameraPictureUri = "";
        bUseCameraImage = false;

        if(savedInstanceState != null)
        {
	        System.out.println("Note_edit / onCreate / noteId =  " + noteId);
	        if(noteId != null)
	        {
	        	picUriStr = dB.getNotePictureUri_byId(noteId);
				note_common.currPictureUri = picUriStr;
	        	audioUri = dB.getNoteAudioUri_byId(noteId);
				note_common.currAudioUri = audioUri;
	        }
        }
        
    	// show view
		note_common.populateFields_all(noteId);
		
		// OK button: edit OK, save
        Button okButton = (Button) findViewById(R.id.note_edit_ok);
//        okButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
		// OK
        okButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
				if(note_common.bRemovePictureUri)
				{
					picUriStr = "";
				}
				if(note_common.bRemoveAudioUri)
				{
					audioUri = "";
				}	
				System.out.println("Note_edit / onClick (okButton) / noteId = " + noteId);
                enSaveDb = true;
                finish();
            }

        });
        
        // delete button: delete note
        Button delButton = (Button) findViewById(R.id.note_edit_delete);
//        delButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
        // delete
        delButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view)
			{
				Util util = new Util(Note_edit.this);
				util.vibrate();

				Builder builder1 = new Builder(Note_edit.this );
				builder1.setTitle(R.string.confirm_dialog_title)
					.setMessage(R.string.confirm_dialog_message)
					.setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener()
						{   @Override
							public void onClick(DialogInterface dialog1, int which1)
							{/*nothing to do*/}
						})
					.setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener()
						{   @Override
							public void onClick(DialogInterface dialog1, int which1)
							{
								note_common.deleteNote(noteId);


								if(PageUi.isAudioPlayingPage())
									AudioPlayer_page.prepareAudioInfo();

								// Stop Play/Pause if current edit item is played and is not at Stop state
								if(Page.mHighlightPosition == position)
									UtilAudio.stopAudioIfNeeded();

								// update highlight position
								if(position < Page.mHighlightPosition )
									AudioManager.mAudioPos--;

								finish();
							}
						})
					.show();//warning:end
            }
        });
        
        // cancel button: leave, do not save current modification
        Button cancelButton = (Button) findViewById(R.id.note_edit_cancel);
//        cancelButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                
                // check if note content is modified
               	if(note_common.isNoteModified())
            	{
               		// show confirmation dialog
            		confirmToUpdateDlg();
            	}
            	else
            	{
            		enSaveDb = false;
                    finish();
            	}
            }
        });
    }
    
    // confirm to update change or not
    void confirmToUpdateDlg()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(Note_edit.this);
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.edit_note_confirm_update)
	           // Yes, to update
			   .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						if(note_common.bRemovePictureUri)
						{
							picUriStr = "";
						}
						if(note_common.bRemoveAudioUri)
						{
							audioUri = "";
						}						
					    enSaveDb = true;
					    finish();
					}})
			   // cancel
			   .setNeutralButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{   // do nothing
					}})
			   // no, roll back to original status		
			   .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						Bundle extras = getIntent().getExtras();
						String originalPictureFileName = extras.getString(DB_page.KEY_NOTE_PICTURE_URI);

						if(originalPictureFileName.isEmpty())
						{   // no picture at first
							note_common.removePictureStringFromOriginalNote(noteId);
		                    enSaveDb = false;
						}
						else
						{	// roll back existing picture
                            note_common.bRollBackData = true;
							picUriStr = originalPictureFileName;
							enSaveDb = true;
						}	
						
						String originalAudioFileName = extras.getString(DB_page.KEY_NOTE_AUDIO_URI);

						if(originalAudioFileName.isEmpty())
						{   // no picture at first
							note_common.removeAudioStringFromOriginalNote(noteId);
		                    enSaveDb = false;
						}
						else
						{	// roll back existing picture
                            note_common.bRollBackData = true;
							audioUri = originalAudioFileName;
							enSaveDb = true;
						}	
	                    finish();
					}})
			   .show();
    }
    

    // for finish(), for Rotate screen
    @Override
    protected void onPause() {
        super.onPause();
        
        System.out.println("Note_edit / onPause / enSaveDb = " + enSaveDb);
        System.out.println("Note_edit / onPause / picUriStr = " + picUriStr);
        System.out.println("Note_edit / onPause / audioUri = " + audioUri);
        noteId = note_common.saveStateInDB(noteId, enSaveDb, picUriStr, audioUri, "");
    }

    // for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        System.out.println("Note_edit / onSaveInstanceState / enSaveDb = " + enSaveDb);
        System.out.println("Note_edit / onSaveInstanceState / bUseCameraImage = " + bUseCameraImage);
        System.out.println("Note_edit / onSaveInstanceState / cameraPictureUri = " + cameraPictureUri);
        
        if(note_common.bRemovePictureUri)
    	    outState.putBoolean("removeOriginalPictureUri",true);

        if(note_common.bRemoveAudioUri)
    	    outState.putBoolean("removeOriginalAudioUri",true);
        
        
        if(bUseCameraImage)
        {
        	outState.putBoolean("UseCameraImage",true);
        	outState.putString("showCameraImageUri", picUriStr);
        }
        else
        {
        	outState.putBoolean("UseCameraImage",false);
        	outState.putString("showCameraImageUri", "");
        }
        
        noteId = note_common.saveStateInDB(noteId, enSaveDb, picUriStr, audioUri, "");
        outState.putSerializable(DB_page.KEY_NOTE_ID, noteId);
        
    }
    
    // for After Rotate
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	if(savedInstanceState.getBoolean("UseCameraImage"))
    		bUseCameraImage = true;
    	else
    		bUseCameraImage = false;
    	
    	cameraPictureUri = savedInstanceState.getString("showCameraImageUri");
    	
    	System.out.println("Note_edit / onRestoreInstanceState / savedInstanceState.getBoolean removeOriginalPictureUri =" +
    							savedInstanceState.getBoolean("removeOriginalPictureUri"));
        if(savedInstanceState.getBoolean("removeOriginalPictureUri"))
        {
        	cameraPictureUri = "";
			note_common.oriPictureUri ="";
			note_common.currPictureUri ="";
        	note_common.removePictureStringFromOriginalNote(noteId);
			note_common.populateFields_all(noteId);
			note_common.bRemovePictureUri = true;
        }
        if(savedInstanceState.getBoolean("removeOriginalAudioUri"))
        {
			note_common.oriAudioUri ="";
			note_common.currAudioUri ="";
        	note_common.removeAudioStringFromOriginalNote(noteId);
			note_common.populateFields_all(noteId);
			note_common.bRemoveAudioUri = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public void onBackPressed() {
	    if(note_common.bShowEnlargedImage == true)
	    {
            note_common.closeEnlargedImage();
	    }
	    else
	    {
	    	if(note_common.isNoteModified())
	    	{
	    		confirmToUpdateDlg();
	    	}
	    	else
	    	{
	            enSaveDb = false;
	            finish();
	    	}
	    }
    }
    
    static final int CHANGE_LINK = R.id.ADD_LINK;
    static final int CHANGE_AUDIO = R.id.ADD_AUDIO;
    static final int CAPTURE_IMAGE = R.id.ADD_NEW_IMAGE;
    static final int CAPTURE_VIDEO = R.id.ADD_NEW_VIDEO;
	private Uri picUri;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// inflate menu
		getMenuInflater().inflate(R.menu.edit_note_menu, menu);

//	    menu.add(0, CHANGE_LINK, 0, R.string.edit_note_link )
//	    .setIcon(android.R.drawable.ic_menu_share)
//	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//
//	    menu.add(0, CHANGE_AUDIO, 1, R.string.note_audio )
//	    .setIcon(R.drawable.ic_audio_unselected)
//	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//
//	    menu.add(0, CAPTURE_IMAGE, 2, R.string.note_camera_image )
//	    .setIcon(android.R.drawable.ic_menu_camera)
//	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//
//	    menu.add(0, CAPTURE_VIDEO, 3, R.string.note_camera_video )
//	    .setIcon(android.R.drawable.presence_video_online)
//	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) 
        {
		    case android.R.id.home:
		    	if(note_common.isNoteModified())
		    	{
		    		confirmToUpdateDlg();
		    	}
		    	else
		    	{
		            enSaveDb = false;
		            finish();
		    	}
		        return true;

            case CHANGE_LINK:
//            	Intent intent_youtube_link = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com"));
//            	startActivityForResult(intent_youtube_link,EDIT_YOUTUBE_LINK);
//            	enSaveDb = false;
            	setLinkUri();
			    return true;
			    
            case CHANGE_AUDIO:
				note_common.bRemoveAudioUri = false; // reset
            	setAudioSource();
			    return true;
			    
            case CAPTURE_IMAGE:
            	Intent intentImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            	// new picture Uri with current time stamp
            	picUri = UtilImage.getPictureUri("IMG_" + Util.getCurrentTimeString() + ".jpg",
						   						   Note_edit.this); 
            	picUriStr = picUri.toString();
			    intentImage.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
			    startActivityForResult(intentImage, Util.ACTIVITY_TAKE_PICTURE); 
			    enSaveDb = true;
				note_common.bRemovePictureUri = false; // reset
			    
			    if(UtilImage.mExpandedImageView != null)
			    	UtilImage.closeExpandedImage();
		        
			    return true;
            
            case CAPTURE_VIDEO:
            	Intent intentVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            	// new picture Uri with current time stamp
            	picUri = UtilImage.getPictureUri("VID_" + Util.getCurrentTimeString() + ".mp4",
						   						   Note_edit.this); 
            	picUriStr = picUri.toString();
			    intentVideo.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
			    startActivityForResult(intentVideo, Util.ACTIVITY_TAKE_PICTURE); 
			    enSaveDb = true;
				note_common.bRemovePictureUri = false; // reset
			    
			    return true;			    
			    
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    
    void setAudioSource() 
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.edit_note_set_audio_dlg_title);
		// Cancel
		builder.setNegativeButton(R.string.btn_Cancel, new DialogInterface.OnClickListener()
		   	   {
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{// cancel
				}});
		// Set
		builder.setNeutralButton(R.string.btn_Select, new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) 
		{
		    enSaveDb = true;
	        startActivityForResult(Util.chooseMediaIntentByType(Note_edit.this,"audio/*"),
	        					   Util.CHOOSER_SET_AUDIO);
		}});

		// None
		if(!audioUri.isEmpty())
		{
			builder.setPositiveButton(R.string.btn_None, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						note_common.bRemoveAudioUri = true;
						note_common.oriAudioUri = "";
						audioUri = "";
						note_common.removeAudioStringFromCurrentEditNote(noteId);
						note_common.populateFields_all(noteId);
					}});		
		}
		
		Dialog dialog = builder.create();
		dialog.show();
    }
    
    void setLinkUri() 
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.edit_note_dlg_set_link);
		
		// select Web link
		builder.setNegativeButton(R.string.note_web_link, new DialogInterface.OnClickListener()
   	   {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
	    		Intent intent_web_link = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.google.com"));
	    		startActivityForResult(intent_web_link,EDIT_LINK);	
	    		enSaveDb = false;
			}
		});
		
		// select YouTube link
		builder.setNeutralButton(R.string.note_youtube_link, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
	        	Intent intent_youtube_link = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com"));
	        	startActivityForResult(intent_youtube_link,EDIT_LINK);
	        	enSaveDb = false;
			}
		});
		// None
		if(!linkUri.isEmpty())
		{
			builder.setPositiveButton(R.string.btn_None, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
//						Note_common.bRemoveAudioUri = true;
					note_common.oriLinkUri = "";
					linkUri = "";
					note_common.removeLinkUriFromCurrentEditNote(noteId);
					note_common.populateFields_all(noteId);
				}
			});		
		}
		
		Dialog dialog = builder.create();
		dialog.show();
    }

//    static String selectedAudioUri;
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) 
	{
		// take picture
		if (requestCode == Util.ACTIVITY_TAKE_PICTURE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				picUri = Uri.parse(note_common.currPictureUri);
//				String str = getResources().getText(R.string.note_take_picture_OK ).toString();
//	            Toast.makeText(Note_edit.this, str + " " + imageUri.toString(), Toast.LENGTH_SHORT).show();
				note_common.populateFields_all(noteId);
	            bUseCameraImage = true;
	            cameraPictureUri = note_common.currPictureUri;
			} 
			else if (resultCode == RESULT_CANCELED)
			{
				bUseCameraImage = false;
				// to use captured picture or original picture
				if(!cameraPictureUri.isEmpty())
				{
					// update
					note_common.saveStateInDB(noteId, enSaveDb, cameraPictureUri, audioUri, "");// replace with existing picture
					note_common.populateFields_all(noteId);
		            
					// set for Rotate any times
		            bUseCameraImage = true;
		            picUriStr = note_common.currPictureUri; // for pause
		            cameraPictureUri = note_common.currPictureUri; // for save instance

				}
				else
				{
					// skip new Uri, roll back to original one
					note_common.currPictureUri = note_common.oriPictureUri;
			    	picUriStr = note_common.oriPictureUri;
					Toast.makeText(Note_edit.this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
				}
				
				enSaveDb = true;
				note_common.saveStateInDB(noteId, enSaveDb, picUriStr, audioUri, "");
				note_common.populateFields_all(noteId);
			}
		}
		
		// choose picture
        if(requestCode == Util.CHOOSER_SET_PICTURE && resultCode == Activity.RESULT_OK)
        {
			String pictureUri = Util.getPicturePathOnActivityResult(this,returnedIntent);
        	System.out.println("Note_edit / _onActivityResult / picUri = " + pictureUri);
        	
        	noteId = note_common.saveStateInDB(noteId,true,pictureUri, audioUri, "");

			note_common.populateFields_all(noteId);
			
            // set for Rotate any times
            bUseCameraImage = true;
            picUriStr = note_common.currPictureUri; // for pause
            cameraPictureUri = note_common.currPictureUri; // for save instance
        }  
        
        // choose audio
		if(requestCode == Util.CHOOSER_SET_AUDIO)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				// for audio
				Uri audioUri = returnedIntent.getData();

				// SAF support, take persistent Uri permission
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				{
					int takeFlags = returnedIntent.getFlags()
							& (Intent.FLAG_GRANT_READ_URI_PERMISSION
							| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

					// add for solving inspection error
					takeFlags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;

					//fix: no permission grant found for UID 10070 and Uri content://media/external/file/28
					String authority = audioUri.getAuthority();
					if(authority.equalsIgnoreCase("com.google.android.apps.docs.storage"))
					{
						getContentResolver().takePersistableUriPermission(audioUri, takeFlags);
					}
				}

				String scheme = audioUri.getScheme();
				String audioUriStr = audioUri.toString();

				// get real path
				if(	(scheme.equalsIgnoreCase("file") ||
					 scheme.equalsIgnoreCase("content") ) ) {

					// check if content scheme points to local file
					if (scheme.equalsIgnoreCase("content")) {
						String realPath = Util.getLocalRealPathByUri(this, audioUri);

						if (realPath != null)
							audioUriStr = "file://".concat(realPath);
					}
				}

//				System.out.println(" Note_edit / onActivityResult / Util.CHOOSER_SET_AUDIO / picUriStr = " + picUriStr);
				note_common.saveStateInDB(noteId,true, picUriStr, audioUriStr, "");

				note_common.populateFields_all(noteId);
	        	this.audioUri = audioUriStr;
	    			
	        	showSavedFileToast(audioUriStr);
			} 
			else if (resultCode == RESULT_CANCELED)
			{
				Toast.makeText(Note_edit.this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
	            setResult(RESULT_CANCELED, getIntent());
	            return; // must add this
			}
		}
		
        // choose link
		if(requestCode == EDIT_LINK)
		{
			Toast.makeText(Note_edit.this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED, getIntent());
            enSaveDb = true;
            return; // must add this
		}		
	}
	
	// show audio file name
	void showSavedFileToast(String audioUri)
	{
        String audioName = Util.getDisplayNameByUriString(audioUri, Note_edit.this);
		Toast.makeText(Note_edit.this,
						audioName,
						Toast.LENGTH_SHORT)
						.show();
	}
	
}
