package com.cw.litenote.operation.audio;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cw.litenote.R;
import com.cw.litenote.main.MainAct;
import com.cw.litenote.tabs.AudioUi_page;
import com.cw.litenote.tabs.TabsHost;
import com.cw.litenote.util.Util;
import com.mobeta.android.dslv.DragSortListView;

import java.util.Locale;

public class AudioPlayer_page
{
	private static final String TAG = "AUDIO_PLAYER"; // error logging tag
	private static final int DURATION_1S = 1000; // 1 seconds per slide
    private static AudioManager mAudioManager; // slide show being played
	private static int mPlaybackTime; // time in miniSeconds from which media should play
	private static int mAudio_tryTimes; // use to avoid useless looping in Continue mode
    private FragmentActivity act;
    private Async_audioUrlVerify mAudioUrlVerifyTask;
	private AudioUi_page audioUi_page;
    public static Handler mAudioHandler;

	public AudioPlayer_page(FragmentActivity act, AudioUi_page audioUi_page){
		this.act = act;
		this.audioUi_page = audioUi_page;

		System.out.println("AudioPlayer_page / constructor ");
		// start a new handler
        mAudioHandler = new Handler();
	}

    /**
     * prepare audio info
     */
    public static void prepareAudioInfo()
    {
        mAudioManager = new AudioManager();
        mAudioManager.updateAudioInfo();
    }

	/**
     *  Run audio state
     */
    public void runAudioState()
	{
	   	System.out.println("AudioPlayer_page / _runAudioState ");
	   	// if media player is null, set new fragment
		if(AudioManager.mMediaPlayer == null)
		{
		 	// show toast if Audio file is not found or No selection of audio file
			if( (AudioManager.getAudioFilesCount() == 0) &&
				(AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)        )
			{
				Toast.makeText(act,R.string.audio_file_not_found,Toast.LENGTH_SHORT).show();
			}
			else
			{
				mPlaybackTime = 0;
                AudioManager.setPlayerState(AudioManager.PLAYER_AT_PLAY);
				mAudio_tryTimes = 0;
				startNewAudio();
			}
		}
		else
		{
			// from play to pause
			if(AudioManager.mMediaPlayer.isPlaying())
			{
				System.out.println("AudioPlayer_page / _runAudioState / play -> pause");
				AudioManager.mMediaPlayer.pause();
				mAudioHandler.removeCallbacks(page_runnable);
                AudioManager.setPlayerState(AudioManager.PLAYER_AT_PAUSE);
			}
			else // from pause to play
			{
				System.out.println("AudioPlayer_page / _runAudioState / pause -> play");
                mAudio_tryTimes = 0;
				AudioManager.mMediaPlayer.start();

                if(AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)
					mAudioHandler.post(page_runnable);

                AudioManager.setPlayerState(AudioManager.PLAYER_AT_PLAY);
			}
		}
	}


	// set list view footer audio control
	private void showAudioPanel(FragmentActivity act,boolean enable)
	{
		System.out.println("AudioPlayer_page / _showAudioPanel / enable = " + enable);
		View audio_panel = act.findViewById(R.id.audio_panel);
        if(audio_panel != null) {
            TextView audio_panel_title_textView = (TextView) audio_panel.findViewById(R.id.audio_panel_title);
            SeekBar seekBarProgress = (SeekBar) audio_panel.findViewById(R.id.audioPanel_seek_bar);

            // show audio panel
            if (enable) {
                audio_panel.setVisibility(View.VISIBLE);
                audio_panel_title_textView.setVisibility(View.VISIBLE);

                // set footer message with audio name
                String audioStr = AudioManager.getAudioStringAt(AudioManager.mAudioPos);
                audio_panel_title_textView.setText(Util.getDisplayNameByUriString(audioStr, act));

                // show audio playing item number
                TextView audioPanel_audio_number = (TextView) audio_panel.findViewById(R.id.audioPanel_audio_number);
                String message = act.getResources().getString(R.string.menu_button_play) +
                        "#" +
                        (AudioManager.mAudioPos +1);
                audioPanel_audio_number.setText(message);

                seekBarProgress.setVisibility(View.VISIBLE);
            } else {
                audio_panel.setVisibility(View.GONE);
            }
        }
	}

	private boolean isAudioPanelOn()
    {
        View audio_panel = act.findViewById(R.id.audio_panel);
        boolean isOn = false;
        if(audio_panel != null)
            isOn = (audio_panel.getVisibility() == View.VISIBLE);
        return isOn;
    }

    /**
     * Continue mode runnable
     */
	private String audioUrl_page;
	public Runnable page_runnable = new Runnable()
	{   @Override
		public void run()
		{
            if(!AudioManager.isRunnableOn_page)
            {
//                System.out.println("AudioPlayer_page / _mRunContinueMode / AudioManager.isRunnableOn_page = " + AudioManager.isRunnableOn_page);
                stopHandler();
                stopAsyncTask();

                if((audioUi_page != null) &&
                   (AudioManager.getPlayerState() == AudioManager.PLAYER_AT_STOP))
                    showAudioPanel(act,false);
                return;
            }

	   		if( AudioManager.getCheckedAudio(AudioManager.mAudioPos) == 1 )
	   		{
                // for incoming call case
                if(!isAudioPanelOn())
                    showAudioPanel(act,true);

	   			if(AudioManager.mMediaPlayer == null)
	   			{
//					System.out.println("AudioPlayer_page / page_runnable / AudioManager.mMediaPlayer = null");
		    		// check if audio file exists or not
   					audioUrl_page = AudioManager.getAudioStringAt(AudioManager.mAudioPos);

					if(!Async_audioUrlVerify.mIsOkUrl)
					{
						mAudio_tryTimes++;
						nextAudio_player();
					}
					else
   					{
                        System.out.println("AudioPlayer_page / page_runnable / AudioManager.isRunnableOn = " + AudioManager.isRunnableOn_page);

   						//create a MediaPlayer
   						AudioManager.mMediaPlayer = new MediaPlayer();
	   					AudioManager.mMediaPlayer.reset();
	   					AudioUi_page.mProgress = 0;


						// for network stream buffer change
	   					AudioManager.mMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener()
	   					{
	   						@Override
	   						public void onBufferingUpdate(MediaPlayer mp, int percent) {
								if(TabsHost.getCurrentPage().seekBarProgress != null)
	   								TabsHost.getCurrentPage().seekBarProgress.setSecondaryProgress(percent);
	   						}
	   					});
   						
	   					// set listeners
                        setMediaPlayerListeners();
   						
   						try
   						{
   							// set data source
//							AudioManager.mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
							AudioManager.mMediaPlayer.setDataSource(act, Uri.parse(audioUrl_page));
   							
   							// prepare the MediaPlayer to play, could delay system response
   							AudioManager.mMediaPlayer.prepare();
   						}
   						catch(Exception e)
   						{
   							System.out.println("AudioPlayer_page on Exception");
   							Log.e(TAG, e.toString());
							mAudio_tryTimes++;
   							nextAudio_player();
   						}
   					}
	   			}
	   			else//AudioManager.mMediaPlayer != null
	   			{
//                    System.out.println("AudioPlayer_page / page_runnable / AudioManager.mMediaPlayer != null");
	   				// keep looping, do not set post() here, it will affect slide show timing
	   				if(mAudio_tryTimes < AudioManager.getAudioFilesCount())
	   				{
						// update page audio seek bar
						if(audioUi_page != null)
	   						update_audioPanel_progress(audioUi_page);

						if(mAudio_tryTimes == 0)
							mAudioHandler.postDelayed(page_runnable,DURATION_1S);
						else
							mAudioHandler.postDelayed(page_runnable,DURATION_1S/10);
	   				}
	   			}
	   		}
	   		else if( (AudioManager.getCheckedAudio(AudioManager.mAudioPos) == 0 ) )// for non-audio item
	   		{
//	   			System.out.println("AudioPlayer_page / page_runnable / for non-audio item");
				nextAudio_player();

				TabsHost.audioPlayer_page.scrollHighlightAudioItemToVisible(TabsHost.getCurrentPage().drag_listView);
				TabsHost.getCurrentPage().mItemAdapter.notifyDataSetChanged();

			}
		}
	};	

	private void stopHandler()
    {
        if(mAudioHandler != null) {
            mAudioHandler.removeCallbacks(page_runnable);
            mAudioHandler = null;
        }
    }

    private void stopAsyncTask()
    {
        // stop async task
        // make sure progress dialog will disappear
        if( (mAudioUrlVerifyTask!= null) &&
            (!mAudioUrlVerifyTask.isCancelled()) )
        {
            mAudioUrlVerifyTask.cancel(true);

            if( (mAudioUrlVerifyTask.mUrlVerifyDialog != null) &&
                    mAudioUrlVerifyTask.mUrlVerifyDialog.isShowing()	)
            {
                mAudioUrlVerifyTask.mUrlVerifyDialog.dismiss();
            }

            if( (mAudioUrlVerifyTask.mAsyncTaskAudioPrepare != null) &&
                    (mAudioUrlVerifyTask.mAsyncTaskAudioPrepare.mPrepareDialog != null) &&
                    mAudioUrlVerifyTask.mAsyncTaskAudioPrepare.mPrepareDialog.isShowing()	)
            {
                mAudioUrlVerifyTask.mAsyncTaskAudioPrepare.mPrepareDialog.dismiss();
            }
        }

    }


    public static int media_file_length;
    /**
     * Set audio player listeners
     */
	private void setMediaPlayerListeners()
	{
			// On Completion listener
			AudioManager.mMediaPlayer.setOnCompletionListener(new OnCompletionListener()
			{	@Override
				public void onCompletion(MediaPlayer mp) 
				{
					System.out.println("AudioPlayer_page / _setAudioPlayerListeners / _onCompletion");
					
					if(AudioManager.mMediaPlayer != null)
						AudioManager.mMediaPlayer.release();
	
					AudioManager.mMediaPlayer = null;
					mPlaybackTime = 0;

					// get next index
					if(AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)
					{
                        nextAudio_player();
                        // todo TBD
						TabsHost.audioPlayer_page.scrollHighlightAudioItemToVisible(TabsHost.getCurrentPage().drag_listView);
						TabsHost.getCurrentPage().mItemAdapter.notifyDataSetChanged();
					}
				}
			});
			
			// - on prepared listener
			AudioManager.mMediaPlayer.setOnPreparedListener(new OnPreparedListener()
			{	@Override
				public void onPrepared(MediaPlayer mp)
				{
					System.out.println("AudioPlayer_page / _setAudioPlayerListeners / _onPrepared");

					if (AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)
					{
                        showAudioPanel(act,true);

						// media file length
						media_file_length = AudioManager.mMediaPlayer.getDuration(); // gets the song length in milliseconds from URL
                        System.out.println("AudioPlayer_page / _setAudioPlayerListeners / media_file_length = " + media_file_length);

						// set footer message: media name
						if (!Util.isEmptyString(audioUrl_page))
//                                &&
//                            listView.isShown()                ) //todo How to handle list view? If not, side effect?
						{
                            // set seek bar progress
                            if(audioUi_page != null)
                                update_audioPanel_progress(audioUi_page);

							TextView audioPanel_file_length = (TextView) act.findViewById(R.id.audioPanel_file_length);
							// show audio file length of playing
							int fileHour = Math.round((float)(media_file_length / 1000 / 60 / 60));
							int fileMin = Math.round((float)((media_file_length - fileHour * 60 * 60 * 1000) / 1000 / 60));
							int fileSec = Math.round((float)((media_file_length - fileHour * 60 * 60 * 1000 - fileMin * 1000 * 60 )/ 1000));
                            if(audioPanel_file_length != null) {
                                audioPanel_file_length.setText(String.format(Locale.US, "%2d", fileHour) + ":" +
                                        String.format(Locale.US, "%02d", fileMin) + ":" +
                                        String.format(Locale.US, "%02d", fileSec));
                            }

                            scrollHighlightAudioItemToVisible(TabsHost.getCurrentPage().drag_listView);
							TabsHost.getCurrentPage().mItemAdapter.notifyDataSetChanged();
                        }

						if (AudioManager.mMediaPlayer != null)
						{
							AudioManager.mIsPrepared = true;
							AudioManager.mMediaPlayer.start();
                            AudioManager.mMediaPlayer.seekTo(mPlaybackTime);

							// add for calling runnable
							if (AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)
								mAudioHandler.postDelayed(page_runnable, Util.oneSecond / 4);
						}
					}
				}
			});
			
			// - on error listener
			AudioManager.mMediaPlayer.setOnErrorListener(new OnErrorListener()
			{	@Override
				public boolean onError(MediaPlayer mp,int what,int extra) 
				{
					// more than one error when playing an index 
					System.out.println("AudioPlayer_page / _setAudioPlayerListeners / _onError / what = " + what + " , extra = " + extra);
					return false;
				}
			});
	}

    private static final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
	/**
	* Scroll highlight audio item to visible position
	*
	* At the following conditions
	* 	1) click audio item of list view (this highlight is not good for user expectation, so cancel this condition now)
	* 	2) click previous/next item in audio controller
	* 	3) change tab to playing tab
	* 	4) back from key protect off
	* 	5) if seeker bar reaches the end
	* In order to view audio highlight item, playing(highlighted) audio item can be auto scrolled to top,
	* unless it is at the end page of list view, there is no need to scroll.
	*/
	//todo How scroll and show highlight?
	public void scrollHighlightAudioItemToVisible(DragSortListView listView)
	{
		System.out.println("AudioPlayer_page / _scrollHighlightAudioItemToVisible");

		// version limitation: _scrollListBy
		// NoteFragment.drag_listView.scrollListBy(firstVisibleIndex_top);
		if(Build.VERSION.SDK_INT < 19)
			return;

		// check playing drawer and playing tab
//		if(
//		    (PageUi.getFocus_pagePos() == MainAct.mPlaying_pagePos) &&
//			(MainAct.mPlaying_folderPos == FolderUi.getFocus_folderPos()) &&
//			(listView.getChildAt(0) != null)
//                )
		{
            int pos;
            int itemHeight = 50;//init
            int dividerHeight;
            int firstVisible_note_pos;
            View v;

			pos = listView.getFirstVisiblePosition();
//			System.out.println("---------------- pos = " + pos);

            View childView;
			if(listView.getAdapter() != null) {
                childView = listView.getAdapter().getView(pos, null, listView);
                childView.measure(UNBOUNDED, UNBOUNDED);
                itemHeight = childView.getMeasuredHeight();
//                System.out.println("---------------- itemHeight = " + itemHeight);
            }

			dividerHeight = listView.getDividerHeight();
//			System.out.println("---------------- dividerHeight = " + dividerHeight);

			firstVisible_note_pos = listView.getFirstVisiblePosition();
			System.out.println("---------------- firstVisible_note_pos = " + firstVisible_note_pos);

			v = listView.getChildAt(0);

			int firstVisibleNote_top = (v == null) ? 0 : v.getTop();
//			System.out.println("---------------- firstVisibleNote_top = " + firstVisibleNote_top);

			System.out.println("---------------- AudioManager.mAudioPos = " + AudioManager.mAudioPos);

			if(firstVisibleNote_top < 0)
			{
				listView.scrollListBy(firstVisibleNote_top);
//				System.out.println("----- scroll backwards by firstVisibleNote_top " + firstVisibleNote_top);
			}

			boolean noScroll = false;
			// base on AudioManager.mAudioPos to scroll
			if(firstVisible_note_pos != AudioManager.mAudioPos)
			{
				while ((firstVisible_note_pos != AudioManager.mAudioPos) && (!noScroll))
				{
					int offset = itemHeight + dividerHeight;
					// scroll forwards
					if (firstVisible_note_pos > AudioManager.mAudioPos)
					{
						listView.scrollListBy(-offset);
//						System.out.println("-----scroll forwards (to top)" + (-offset));
					}
					// scroll backwards
					else if (firstVisible_note_pos < AudioManager.mAudioPos)
					{
						listView.scrollListBy(offset);
//						System.out.println("-----scroll backwards (to bottom)" + offset);
					}

//					System.out.println("---------------- firstVisible_note_pos = " + firstVisible_note_pos);
//					System.out.println("---------------- Page.drag_listView.getFirstVisiblePosition() = " + listView.getFirstVisiblePosition());
					if(firstVisible_note_pos == listView.getFirstVisiblePosition())
						noScroll = true;
					else {
						// update first visible index
						firstVisible_note_pos = listView.getFirstVisiblePosition();
					}
				}

				// do v scroll
                TabsHost.store_listView_vScroll(listView);
                TabsHost.resume_listView_vScroll(listView);
			}
		}
	}


    /**
     * Start new audio
     */
	private void startNewAudio()
	{
        System.out.println("AudioPlayer_page / _startNewAudio / AudioManager.mAudioPos = " + AudioManager.mAudioPos);

		// remove call backs to make sure next toast will appear soon
		if(mAudioHandler != null)
			mAudioHandler.removeCallbacks(page_runnable);
        mAudioHandler = null;
        mAudioHandler = new Handler();

        AudioManager.isRunnableOn_page = true;
        AudioManager.isRunnableOn_note = false;
		AudioManager.mMediaPlayer = null;

		// verify audio URL
		Async_audioUrlVerify.mIsOkUrl = false;

		if( (AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE) &&
            (AudioManager.getCheckedAudio(AudioManager.mAudioPos) == 0)          )
		{
			mAudioHandler.postDelayed(page_runnable,Util.oneSecond/4);
		}
		else
		{
			mAudioUrlVerifyTask = new Async_audioUrlVerify(act, mAudioManager.getAudioStringAt(AudioManager.mAudioPos));
			mAudioUrlVerifyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Searching media ...");

			while(!Async_audioUrlVerify.mIsOkUrl)
			{
				//wait for Url verification
				try {
					Thread.sleep(Util.oneSecond/20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// prepare audio
			if(Async_audioUrlVerify.mIsOkUrl)
			{
				// launch handler
				if( (AudioManager.getPlayerState() != AudioManager.PLAYER_AT_STOP) &&
						(AudioManager.getAudioPlayMode() == AudioManager.PAGE_PLAY_MODE)   )
				{
					mAudioHandler.postDelayed(page_runnable, Util.oneSecond / 4);
				}

				// during audio Preparing
				Async_audioPrepare mAsyncTaskAudioPrepare = new Async_audioPrepare(act);
				mAsyncTaskAudioPrepare.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Preparing to play ...");
			}
		}

    }
	

    /**
     * Play next audio at AudioPlayer_page
     */
    private void nextAudio_player()
    {
//		Toast.makeText(act,"Can not open file, try next one.",Toast.LENGTH_SHORT).show();
        System.out.println("AudioPlayer_page / _playNextAudio");
        if(AudioManager.mMediaPlayer != null)
        {
            AudioManager.mMediaPlayer.release();
            AudioManager.mMediaPlayer = null;
        }
        mPlaybackTime = 0;

        // new audio index
        AudioManager.mAudioPos++;

		if(AudioManager.mAudioPos >= AudioManager.getPlayingPage_notesCount())
            AudioManager.mAudioPos = 0; //back to first index

        // check try times,had tried or not tried yet, anyway the audio file is found
        System.out.println("AudioPlayer_page / check mTryTimes = " + mAudio_tryTimes);
        if(mAudio_tryTimes < AudioManager.getAudioFilesCount() )
        {
            startNewAudio();
        }
        else // try enough times: still no audio file is found
        {
            Toast.makeText(act,R.string.audio_message_no_media_file_is_found,Toast.LENGTH_SHORT).show();

            // do not show highlight
            if(MainAct.mSubMenuItemAudio != null)
                MainAct.mSubMenuItemAudio.setIcon(R.drawable.ic_menu_slideshow);

            // stop media player
            AudioManager.stopAudioPlayer();
        }
        System.out.println("AudioPlayer_page / _playNextAudio / AudioManager.mAudioPos = " + AudioManager.mAudioPos);
    }

    private void update_audioPanel_progress(AudioUi_page audioUi_page)
    {
//        if(!listView.isShown())
//            return;

//		System.out.println("AudioPlayer_page / _update_audioPanel_progress");

        // get current playing position
        int currentPos = 0;
        if(AudioManager.mMediaPlayer != null)
            currentPos = AudioManager.mMediaPlayer.getCurrentPosition();

        int curHour = Math.round((float)(currentPos / 1000 / 60 / 60));
        int curMin = Math.round((float)((currentPos - curHour * 60 * 60 * 1000) / 1000 / 60));
        int curSec = Math.round((float)((currentPos - curHour * 60 * 60 * 1000 - curMin * 60 * 1000)/ 1000));

        // set current playing time
        audioUi_page.audioPanel_curr_pos.setText(String.format(Locale.US,"%2d", curHour)+":" +
                String.format(Locale.US,"%02d", curMin)+":" +
                String.format(Locale.US,"%02d", curSec) );//??? why affect audio title?

        // set current progress
        AudioUi_page.mProgress = (int)(((float)currentPos/ media_file_length)*100);
        audioUi_page.seekBarProgress.setProgress(AudioUi_page.mProgress); // This math construction give a percentage of "was playing"/"song length"
    }
}