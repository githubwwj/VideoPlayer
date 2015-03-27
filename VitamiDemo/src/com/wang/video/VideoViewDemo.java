/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wang.video;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.VideoView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.wang.activity.R;
import com.wang.activity.R.anim;
import com.wang.activity.R.drawable;
import com.wang.activity.R.id;
import com.wang.activity.R.layout;
import com.wang.activity.R.string;
import com.wang.domain.VideoInfo;
import com.wang.util.LogUtil;
import com.wang.util.PubParamInfo;
import com.wang.util.Utils;

public class VideoViewDemo extends Activity {

	private final static String TAG = "VideoPlayer";

	private int position;
	private String radia = null;


	private int mCurrentPosition = 0;

	private VideoView mVideoView = null;

	private SeekBar mPlayerSeekBar = null;

	private SeekBar mSeekBarvolume = null;

	private TextView mEndTime = null;
	private TextView mCurrentTime = null;

	
	private TextView mVideoName = null;

	private ImageView mBatteryState = null;

	private TextView mLastModify = null;

	private Button mBtnSetplay = null;

	private GestureDetector mGestureDetector = null;
	private AudioManager mAudioManager = null;

	private int currentVolume = 0;
	private Button mDiaplayMode = null;
	private Button mPrevButton = null;
	private Button mPlayOrPause = null;
	private Button mNextButton = null;
	private Button mPlayerVolume = null;

	private static int screenWidth = 0;
	private static int screenHeight = 0;
//	private static int controlViewHeight = 0;
	private final static int TIME = 6868;
	private boolean isControllerShow = true;
	private boolean isPaused = false;
	private boolean isFullScreen = false;
	private boolean isSilent = false;


	private boolean isOnCompletion = false;

	private final static int SCREEN_FULL = 0;
	private final static int SCREEN_DEFAULT = 1;

	private final static int HIDE_CONTROLER = 1;

	private final static int PAUSE = 3;

	private final static int EXIT_TEXT = 5;
	private final static int PROGRESS_CHANGED = 0;

	private final static int BUFFER = 6;

	private final static int BUFFERING_TAG = 7;

	private final static int EXIT = 8;

	private final static int SET_PAUSE_BUTTON = 9;

	private final static int IS_PAUSE_BUTTON = 10;

	private final static int SEEK_BACKWARD = 11;

	private final static int SEEK_FORWARD = 12;

	private Intent mIntent;

	private Uri uri;
	private Button mPlayerButtonBack = null;

	private StringBuilder mFormatBuilder;
	private Formatter mFormatter;

	private LinearLayout frame = null;
	private FrameLayout mFrameLayout = null;

	private LinearLayout mPlayerLoading;

	private LinearLayout mVideoBuffer;

	private boolean isLocal = false;

	private boolean isLoading = true;

	// private SharedPreferences sp = null;

	public class PausePlayerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if ("com.wang.ui".equals(intent.getAction())) {
				mHandler.sendEmptyMessageDelayed(PAUSE, 950);
			}
		}
	}

	private int level = 0;
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			level = intent.getIntExtra("level", 0);
			// level加%就是当前电量了
		}
	};
	

	private void setBattery(int level) {
		if (level <= 0) {
			mBatteryState.setBackgroundResource(R.drawable.ic_battery_0);
		} else if (0 < level && level <= 10) {
			mBatteryState.setBackgroundResource(R.drawable.ic_battery_10);
		} else if (10 < level && level <= 20) {
			mBatteryState.setBackgroundResource(R.drawable.ic_battery_20);
		} else if (20 < level && level <= 40) {
			mBatteryState.setBackgroundResource(R.drawable.ic_battery_40);
		} else if (40 < level && level <= 60) {
			mBatteryState.setBackgroundResource(R.drawable.ic_battery_60);
		} else if (60 < level && level <= 80) {
			mBatteryState.setBackgroundResource(R.drawable.ic_battery_80);
		} else if (80 < level && level <= 100) {
			mBatteryState.setBackgroundResource(R.drawable.ic_battery_100);
		}
	}

	private SharedPreferences preference = null;
//	private ArrayList<VideoInfo> mCurrentPlayList=new ArrayList<VideoInfo>();
	private VideoInfo videoInfo = null;
	boolean onTouch=false;
	private ProgressDialog mPD;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this)){
			return;
		}		
		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		
		setContentView(R.layout.video_player);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		preference = PreferenceManager.getDefaultSharedPreferences(this);

		LogUtil.v(TAG, getIntent().toString());

		registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		initView();

		mIntent = getIntent();
		if (mIntent != null) {
			
			uri = mIntent.getData();
			if (uri != null) {
				String name = Utils.getFileName(uri.toString());
				mVideoName.setText(name);
				isLocal = false;
			} else {
				isLocal = true;
			}
				if(isLocal){
					String al=mIntent.getStringExtra("aa");
					if(null!=al){
						if(mIntent.getSerializableExtra("MediaIdListFiles")!=null){
							PubParamInfo.videoInfoCache=(ArrayList<VideoInfo>)mIntent.getSerializableExtra("MediaIdListFiles");
						}
					}
				}
		  		position = mIntent.getIntExtra("CurrentPosInMediaIdList", 0);
		  		radia = mIntent.getStringExtra("radia");
		  	    
		  		String strLocaluri = null;
			    if(videoInfo != null){
			    	strLocaluri = videoInfo.getUrl();
			    }
				if (strLocaluri == null&&PubParamInfo.videoInfoCache!=null) {
					if(PubParamInfo.videoInfoCache.size()>0){
						videoInfo = PubParamInfo.videoInfoCache.get(position);
						strLocaluri = PubParamInfo.videoInfoCache.get(position).getUrl();
					}
				}

				if (strLocaluri != null) {
					String name = Utils.getFileName(strLocaluri);
					uri = Uri.parse(strLocaluri);
					
					if(isCheckButton&&PubParamInfo.videoInfoCache!=null&&PubParamInfo.videoInfoCache.size()>1){
						mVideoName.setText(PubParamInfo.videoInfoCache.get(position).getDiaplayName());
					}else{
						 if(videoInfo != null){
							 	mVideoName.setText(videoInfo.getDiaplayName());
						 }
					}
				}


			if (uri != null) {
				isHttp = Utils.checkUri(VideoViewDemo.this, uri);
			}
			if (isHttp) {
				isCheckButton = Utils.isCheckUriByM3u8(VideoViewDemo.this, uri);
				if(isCheckButton&&PubParamInfo.videoInfoCache!=null){
					mVideoName.setText(PubParamInfo.videoInfoCache.get(position).getDiaplayName());
					mPlayerSeekBar.setEnabled(false);
				}
				
			}
			
						if(PubParamInfo.videoInfoCache!=null){
							if(PubParamInfo.videoInfoCache.size()==1){
								setNextEnabled(false);
								setPrevEnabled(false);
							}else if(PubParamInfo.videoInfoCache.size()>1){
								if(position==0){
									setPrevEnabled(false);
									setNextEnabled(true);
								}else if(position>0&&position<(PubParamInfo.videoInfoCache.size()-1)){
										setPrevEnabled(true);
										setNextEnabled(true);
								}else{
									setPrevEnabled(true);
									setNextEnabled(false);
								}
							}
						}
			  	}
			
			// uri=Uri.parse("rtsp://live.android.maxlab.cn/maxtv-ln.sdp");
			// uri=Uri.parse("http://cms.doplive.com.cn/video1/index_multi.m3u8?date=20120302220001&uid=0&rnd=2012030222000112069&deviceid=123&key=5311e457e8b47402676dae4cd2368118&count=1330675490");

			// mMedia
			// =(Media)mIntentContent.getSerializableExtra(Utils.MEDIA_KEY);
			// mPlayHistory =
			// (PlayHistoryInfo)mIntentContent.getSerializableExtra(Utils.PLAY_HISTORY_KEY);
			// sp =
			// getApplicationContext().getSharedPreferences("play_loading",0);
			// if("play_history_loading".equals(mHistoryLoading)){
			// mLoadingText.setText("正在加载中...");
			// }

			isOnCompletion = false;


		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
		
		getScreenSize();

		mVideoView.setOnErrorListener(new OnErrorListener() {

			public boolean onError(MediaPlayer mp, int what, int extra) {
					LogUtil.i(TAG, " ---出错了Error: " + what + "," + extra);
					
					LogUtil.i(TAG, " ---要报错了: " + what + "," + extra);
					isError = true;
					if (isError) {
						if (mVideoBuffer != null) {
							mVideoBuffer.setVisibility(View.GONE);
						}
					}
					errorType = what;
					LogUtil.i(TAG, "Error: " + what + "," + extra);
						ConfirmExit();
					mHandler.sendEmptyMessage(SET_PAUSE_BUTTON);
				
				return true;

			}

		});
		
		mVideoView.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

					public void onBufferingUpdate(MediaPlayer arg0,
							int bufferingProgress) {

					}
				});

		mVideoView.setOnInfoListener(new OnInfoListener() {

			public boolean onInfo(MediaPlayer mp, int what, int extra) {

				return false;
			}
		});


		mPlayerSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekbar,int progress, boolean fromUser) {
						if (fromUser) {
							mVideoView.seekTo(progress);
							cancelDelayHide();
						}
					}

					public void onStartTrackingTouch(SeekBar arg0) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						if (uri == null && !isPaused) {
							isBuffering = false;
							if (mVideoBuffer != null) {
								mVideoBuffer.setVisibility(View.VISIBLE);
							}
							mHandler.sendEmptyMessageDelayed(BUFFERING_TAG,
									1000);
						}
						mHandler.sendEmptyMessage(SET_PAUSE_BUTTON);
						hideControllerDelay();
					}
				});

		mSeekBarvolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekbar,int progress, boolean fromUser) {
						currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

						LogUtil.i(TAG, "progress" + progress + "---fromUser="
								+ fromUser + "------currentVolume="
								+ currentVolume);
						if (fromUser) {
							if (progress >= 15) {
								isSilent = false;
								updateVolume(15);
							} else if (progress <= 0) {
								isSilent = true;
								updateVolume(0);
							} else {
								isSilent = false;
								updateVolume(progress);
							}

						}
						cancelDelayHide();
					}

					public void onStartTrackingTouch(SeekBar arg0) {

					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						hideControllerDelay();
					}
				});

		mGestureDetector = new GestureDetector(new SimpleOnGestureListener() {

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				
				return super.onFling(e1, e2, velocityX, velocityY);
			}
			
//			@Override
//			public boolean onDoubleTap(MotionEvent e) {
//				// long time = System.currentTimeMillis();
//				// if (time - lastTimeonDoubleTap < CLICK_INTERVAL) {
//				// return true;
//				// }
//				// lastTimeonDoubleTap = time;
//				//
//				// if (isFullScreen) {
//				// setVideoScale(SCREEN_DEFAULT);
//				// } else {
//				// setVideoScale(SCREEN_FULL);
//				// }
//				// isFullScreen = !isFullScreen;
//				//
//				// if (isControllerShow) {
//				// showController();
//				// }
//
//				return true;
//			}


		});

		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			public void onPrepared(MediaPlayer arg0) {

				mPlayerLoading.setVisibility(View.VISIBLE);
				isControllerShow = false;

				isBuffering = true;

				setVideoScale(SCREEN_DEFAULT);

				if (!isLoading) {
					hideController();
				}

				int i = (int) mVideoView.getDuration();
				Log.d("onCompletion", "" + i);
				mPlayerSeekBar.setMax(i);
				mEndTime.setText(stringForTime(i));

				mVideoView.start();
				LogUtil.i(TAG, " ---播放成功了: " );
				isLoading = false;
				Utils.isErrorNum = false;
				mPlayerLoading.setVisibility(View.GONE);
				isOnCompletion = false;
				isError = false;
				mHandler.sendEmptyMessage(SET_PAUSE_BUTTON);
				if(uri.toString().contains("mp3")||uri.toString().contains("ogg")){
					showController();
					cancelDelayHide();
					hideControllerDelay();
				}else{
					cancelDelayHide();
					hideControllerDelay();
				}
				mHandler.removeMessages(PROGRESS_CHANGED);
				mHandler.sendEmptyMessage(PROGRESS_CHANGED);

				mHandler.removeMessages(BUFFER);
				mHandler.sendEmptyMessage(BUFFER);

			}
		});
		mVideoView.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer arg0) {
				int n=0;
				if(PubParamInfo.videoInfoCache==null)
					return;
				isOnCompletion=true;
				n= PubParamInfo.videoInfoCache.size();
				if (n >1&&position<n) {
					if (++position < n)  {
						videoPlayer();
						setVideoNamePlayVideo();
					} 
				}else{
						position=0;
						setVideoNamePlayVideo();
				}
			}
			
		});
		
		startPlay();

	}
	
	private void setVideoNamePlayVideo() {
			mVideoName.setText(PubParamInfo.videoInfoCache.get(position).getDiaplayName());
			mVideoView.setVideoPath(PubParamInfo.videoInfoCache.get(position).getUrl());
			isOnCompletion=false;
	}

	private int errorType = 0;

	public void setPauseButtonImage() {
		if (mVideoView != null) {
			try {
				if (mVideoView.isPlaying()) {
					mPlayOrPause.setBackgroundResource(R.drawable.btn_pause);
				} else {
					mPlayOrPause.setBackgroundResource(R.drawable.btn_play);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	int quikProgress=1*10;
	private void initView() {

		frame = (LinearLayout) findViewById(R.id.frame);
		mFrameLayout = (FrameLayout) findViewById(R.id.mFrameLayout);

		mPlayerLoading = (LinearLayout) findViewById(R.id.player_loading);

		mVideoBuffer = (LinearLayout) findViewById(R.id.video_buffer);

		mVideoView = (VideoView) findViewById(R.id.video_view);

		mVideoName = (TextView) findViewById(R.id.video_name);

		mBatteryState = (ImageView) findViewById(R.id.battery_state);

		mLastModify = (TextView) findViewById(R.id.last_modify);

		mBtnSetplay = (Button) findViewById(R.id.btn_setplay);

		mPlayerButtonBack = (Button) findViewById(R.id.btn_exit);

		mPlayerSeekBar = (SeekBar) findViewById(R.id.PlaybackProgressBar);

		mSeekBarvolume = (SeekBar) findViewById(R.id.VioceProgressBar);

		mCurrentTime = (TextView) findViewById(R.id.current_time);

		mEndTime = (TextView) findViewById(R.id.total_time);

		mDiaplayMode = (Button) findViewById(R.id.diaplay_mode);

		mPrevButton = (Button) findViewById(R.id.btn_back);
		
		mPlayOrPause = (Button) findViewById(R.id.btn_play_pause);

		mNextButton = (Button) findViewById(R.id.btn_forward);

		mPlayerVolume = (Button) findViewById(R.id.btn_voice);


		if (currentVolume <= 0) {
			mPlayerVolume.setBackgroundDrawable(VideoViewDemo.this.getResources()
					.getDrawable(R.drawable.btn_voice));
		} else {
			mPlayerVolume.setBackgroundDrawable(VideoViewDemo.this.getResources()
					.getDrawable(R.drawable.btn_voice));
		}
		
		mPrevButton.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				onTouch=true;
				return false;
			}
		});
		
		mNextButton.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				onTouch=true;
				return false;
			}
		});
		
//		mPrevButton.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if(mVideoView.isPlaying()){
//					quikProgress+=80;
//					mCurrentPosition= (int)mVideoView.getCurrentPosition();
//					if(quikProgress>1000)
//						if(mCurrentPosition-quikProgress>1000){
//							mVideoView.seekTo(mCurrentPosition-quikProgress);
//							quikProgress=0;
//						}
//							
//				}
//				return false;
//			}
//			
//		});
		
//		mNextButton.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if(mVideoView.isPlaying()){
//					quikProgress+=80;
//					int i= (int)mVideoView.getDuration();
//					mCurrentPosition= (int)mVideoView.getCurrentPosition();
//					if(quikProgress>1000)
//						if((mCurrentPosition+quikProgress)<i){
//							mVideoView.seekTo(mCurrentPosition+quikProgress);
//							quikProgress=0;
//						}
//				}
//				return false;
//			}
//			
//		});

		mPlayerSeekBar.setThumbOffset(13);
		mPlayerSeekBar.setMax(100);
		mPlayerSeekBar.setSecondaryProgress(0);

		mSeekBarvolume.setThumbOffset(13);
		mSeekBarvolume.setMax(13);
		mSeekBarvolume.setProgress(currentVolume);

		mPlayerButtonBack.setOnClickListener(mListener);

		mPlayOrPause.setOnClickListener(mListener);

		mPrevButton.setOnClickListener(mListener);
		mNextButton.setOnClickListener(mListener);
		mDiaplayMode.setOnClickListener(mListener);
		mPlayerVolume.setOnClickListener(mListener);

		mBtnSetplay.setOnClickListener(mListener);

		hideController();

	}
	
	private long lastTime=0;
	private long CLICK_INTERVAL=800;
	
	public boolean clickInterval(){
		long currentTime=System.currentTimeMillis();
//		Toast.makeText(this, currentTime-lastTime+"time",Toast.LENGTH_SHORT).show();
		if((currentTime-lastTime)<CLICK_INTERVAL)
			return false;
		lastTime=currentTime;
		return true; 
	}

	private View.OnClickListener mListener = new View.OnClickListener() {
		public void onClick(View v) {
			int n=0;
			SharedPreferences sp=VideoViewDemo.this.getSharedPreferences("seekTo", Context.MODE_PRIVATE);
			Editor editor=sp.edit();
			boolean internal=clickInterval();
			switch (v.getId()) {
			
			case R.id.btn_exit:
				
				if(!internal)
					return;
				exit();
				break;
			case R.id.btn_back:
				if(onTouch){
					onTouch=false;
					return ; 
				}
				if(!internal)
					return;
				editor.putInt("seekTo", 0);
				editor.commit();
				if(PubParamInfo.videoInfoCache==null)
					return ;
				n= PubParamInfo.videoInfoCache.size();
				--position;
				if (n > 1) {
					if (position < n && position >= 0) {
//						isPlayerPreviousOrNext();
						videoPlayer();
					}else{
						position=n-1;
						videoPlayer();
					}
				} 
				startSelfPlayer();
				break;

			case R.id.btn_play_pause:
				if(!internal)
					return;
				mHandler.sendEmptyMessage(IS_PAUSE_BUTTON);
				break;
			case R.id.btn_forward:
				if(onTouch){
					onTouch=false;
					return ; 
				}
				if(!internal)
					return;
				editor.putInt("seekTo", 0);
				editor.commit();
				if(PubParamInfo.videoInfoCache==null)
					return ;
					n= PubParamInfo.videoInfoCache.size();
					if (n > 1) {
						position++;
						if (position >= 0&&position<n) {
							videoPlayer();
						}else{
							position=0;
							videoPlayer();
						}
							
				}
					startSelfPlayer();
				break;
			case R.id.btn_voice:
				if(!internal)
					return;
				if (mAudioManager != null) {
					if (isSilent) {
						isSilent = false;
					} else {
						isSilent = true;
					}
					updateVolume(currentVolume);
				}

				break;

			case R.id.diaplay_mode:
				if (isFullScreen) {
					setVideoScale(SCREEN_DEFAULT);
				} else {
					setVideoScale(SCREEN_FULL);
				}
				break;
			case R.id.btn_setplay:
				if(!internal)
					return;
				break;
			}
		}
		

//		private void isPlayerPreviousOrNext() {
//						if(mCurrentPlayList!=null){
//							if(mCurrentPlayList.size()==1){
//								setNextEnabled(false);
//								setPrevEnabled(false);
//							}else if(mCurrentPlayList.size()>1){
//								if(position==0){
//									setPrevEnabled(false);
//									setNextEnabled(true);
//								}else if(position>0&&position<(mCurrentPlayList.size()-1)){
//										setPrevEnabled(true);
//										setNextEnabled(true);
//								}else{
//									setPrevEnabled(true);
//									setNextEnabled(false);
//								}
//							}
//						}
//			  	}
	};
	
	private void videoPlayer() {
		if (mVideoView != null) {
			mVideoView.stopPlayback();
			cancelDelayHide();
			hideControllerDelay();
		}
	}
	
	private void startSelfPlayer() {

		if (mVideoView != null) {
			mVideoView.stopPlayback();
		}
		 Intent intent = new Intent(VideoViewDemo.this,VideoViewDemo.class);
		 intent.putExtra("aa", "al");
		 intent.putExtra("CurrentPosInMediaIdList", position);
		 startActivity(intent);
		 finish();
		 overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
	}


	private String stringForTime(int timeMs) {
		int totalSeconds = timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
					.toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}


	public void setPlaySeekBarEnabled(boolean enabled) {
		if (mPlayerSeekBar != null) {
			mPlayerSeekBar.setEnabled(enabled && mPlayerSeekBar != null);

		}
	}

	public void setNextEnabled(boolean enabled) {
		if (mNextButton != null) {
			if(enabled)
				mNextButton.setEnabled(enabled && mListener != null);
				if (enabled) {
						mNextButton.setBackgroundDrawable(VideoViewDemo.this.getResources().getDrawable(
										R.drawable.btn_forward_one));
				}else{
					 mNextButton.setBackgroundDrawable(VideoViewDemo.this
							 .getResources().getDrawable(
							 R.drawable.btn_forward));
				}
		}
	}

	public void setPrevEnabled(boolean enabled) {

		if (mPrevButton != null) {
			if(enabled)
				mPrevButton.setEnabled(enabled);
					if (enabled) {
						mPrevButton.setBackgroundDrawable(VideoViewDemo.this.getResources().getDrawable(
										R.drawable.btn_back));
					}
					else{
						mPrevButton.setBackgroundDrawable(VideoViewDemo.this.getResources()
								.getDrawable(R.drawable.btn_back_one));
					}
		}
	
	}

	public void setPlayOrPauseEnabled(boolean enabled) {
		if (mPlayOrPause != null) {
			mPlayOrPause.setEnabled(enabled && mPlayOrPause != null);

			if (enabled) {
			} else {
				if (!isPaused) {
					mPlayOrPause.setBackgroundResource(R.drawable.video_puase_gray);

				} else {
					mPlayOrPause.setBackgroundResource(R.drawable.btn_play);
				}

			}
		}
	}
	
	private void startPlay() {
		if (uri != null && mVideoView != null) {
			if (mVideoBuffer != null) {
				mVideoBuffer.setVisibility(View.GONE);
			}
			mVideoView.stopPlayback();
			LogUtil.i(TAG, "playUri ===" + String.valueOf(uri));
			mVideoView.setVideoURI(uri);
		} 
		mHandler.removeMessages(PROGRESS_CHANGED);
		mHandler.sendEmptyMessage(PROGRESS_CHANGED);
	}

	private void setVideoScale(int flag) {

		switch (flag) {
		case SCREEN_FULL:
			mDiaplayMode.setBackgroundResource(R.drawable.btn_original_size);
			Log.d(TAG, "screenWidth: " + screenWidth + " screenHeight: "+ screenHeight);
			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ZOOM, 0);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			isFullScreen = true;
			break;

		case SCREEN_DEFAULT:

			mDiaplayMode.setBackgroundResource(R.drawable.btn_full_screen);

			int videoWidth = mVideoView.getVideoWidth();
			int videoHeight = mVideoView.getVideoHeight();
			int mWidth = screenWidth;
			int mHeight = screenHeight - 25;

			if (videoWidth > 0 && videoHeight > 0) {
				if (videoWidth * mHeight > mWidth * videoHeight) {

					mHeight = mWidth * videoHeight / videoWidth;
				} else if (videoWidth * mHeight < mWidth * videoHeight) {

					mWidth = mHeight * videoWidth / videoHeight;
				} else {

				}
			}
			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			isFullScreen = false;
			break;
		}
	}

	private void hideControllerDelay() {
		mHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}

	private void hideController() {

		if (isLoading && isBuffering) {
			frame.setVisibility(View.GONE);
			mFrameLayout.setVisibility(View.GONE);
		} else if (!isLoading && isBuffering) {
			frame.setVisibility(View.GONE);
			mFrameLayout.setVisibility(View.GONE);
		}

		isControllerShow = false;

	}

	private void cancelDelayHide() {
		mHandler.removeMessages(HIDE_CONTROLER);
	}

	private void showController() {

		if (!isLoading && isBuffering) {
			frame.setVisibility(View.VISIBLE);
			mFrameLayout.setVisibility(View.VISIBLE);
		}

		isControllerShow = true;

	}

	private boolean isBuffering = false;
	private boolean isSoftBuffering = false;
	private boolean isHttp = false;
	private boolean isCheckButton = false;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case EXIT_TEXT:
				mHandler.sendEmptyMessage(EXIT);
				break;

			case EXIT:
				exit();
				break;
			case PROGRESS_CHANGED:

				if (mVideoView == null) {
					return;
				}
				if (!Utils.isCheckNetAvailable(VideoViewDemo.this) && isHttp) {
					Utils.showDialog(" 提示", "确定", "取消", "网络不可用，请检查网络", true);
					mVideoView.stopPlayback();
					return;
				}

				int i = (int) mVideoView.getCurrentPosition();
				
				if (uri != null &&   isBuffering && !isPaused
						&& !isError&&!isCheckButton) {
					int isBuffer = i - mCurrentPosition;
				
					if (isBuffer < -500 || isBuffer < 500) {
						if (mVideoBuffer != null) {
							 mVideoBuffer.setVisibility(View.VISIBLE);
						}

					} else {
						if (mVideoBuffer != null) {
							 mVideoBuffer.setVisibility(View.GONE);
						}
					}

				} else {
					if (!isCheckButton&&isPaused) {
						if (mVideoBuffer != null) {
							 mVideoBuffer.setVisibility(View.GONE);
						}
					}

				}

				isSoftBuffering = mVideoView.isBuffering();
				if (isCheckButton  && isSoftBuffering && !isPaused
						&& !isError) {
					LogUtil.i(TAG, "--buffering....isSoftBuffering=="+isSoftBuffering+"----CurrentPosition=="+i);
					int isBuffer = i - mCurrentPosition;
				
					if (isBuffer < -500 || isBuffer < 500) {
						if (mVideoBuffer != null) {
							 mVideoBuffer.setVisibility(View.VISIBLE);
						}

					} else {
						if (mVideoBuffer != null) {
							mVideoBuffer.setVisibility(View.GONE);

						}
					}

				} else {
					if (isPaused || !isSoftBuffering) {
						if (mVideoBuffer != null) {
							mVideoBuffer.setVisibility(View.GONE);
						}
					}

				}
				mCurrentPosition = i;
				Calendar calendar = Calendar.getInstance();
				// int year = calendar.get(Calendar.YEAR);
				// int month = calendar.get(Calendar.MONTH);
				// int day = calendar.get(Calendar.DAY_OF_MONTH);
				String hourStr = null;
				String minuteStr = null;
				String timeStr = null;
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);
				int second = calendar.get(Calendar.SECOND);
				if (hour == 0) {
					hourStr = "00";
				} else if (0 < hour && hour < 10) {
					hourStr = "0" + hour;
				} else {
					hourStr = String.valueOf(hour);
				}

				if (minute == 0) {
					minuteStr = "00";
				} else if (0 < minute && minute < 10) {
					minuteStr = "0" + minute;
				} else {
					minuteStr = String.valueOf(minute);
				}

				if (second == 0) {
					timeStr = "00";
				} else if (0 < second && second < 10) {
					timeStr = "0" + second;
				} else {
					timeStr = String.valueOf(second);
				}
				String time = hourStr + ":" + minuteStr + ":" + timeStr;
				mLastModify.setText(time);

				mPlayerSeekBar.setProgress(i);
				if (isHttp) {
					int j = mVideoView.getBufferPercentage();
					int setSecondaryProgress = j * mPlayerSeekBar.getMax()
							/ 100;
					mPlayerSeekBar.setSecondaryProgress(setSecondaryProgress);

				} else {
					mPlayerSeekBar.setSecondaryProgress(0);
				}
				setBattery(level);
				mCurrentTime.setText(stringForTime(i));
				if (!isOnCompletion && !isLoading) {
					SharedPreferences.Editor editor = preference.edit();
					if (editor != null) {

						if (mCurrentPosition > 0 && uri != null) {
							editor.putInt("CurrentPosition", mCurrentPosition);
							if (uri != null) {
								editor.putString("histroyUri", uri.toString());
							}

						}
						editor.commit();
					}

				}
				if(!isError){
					mHandler.removeMessages(PROGRESS_CHANGED);
					mHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, 1000);
				}

				break;

			case HIDE_CONTROLER:
				hideController();
				break;
			case BUFFERING_TAG:
				isBuffering = true;
				break;
			case PAUSE:
				if (mVideoView != null) {
					mVideoView.pause();
				}
				break;

			case SET_PAUSE_BUTTON:
				setPauseButtonImage();
				break;

			case IS_PAUSE_BUTTON:
				if (isPaused) {
					mVideoView.start();
					mPlayOrPause.setBackgroundResource(R.drawable.btn_pause);
					isBuffering = true;
					cancelDelayHide();
					hideControllerDelay();
				} else {
					mVideoView.pause();
					mPlayOrPause.setBackgroundResource(R.drawable.btn_play);
					cancelDelayHide();
					showController();
					isBuffering = false;

				}

				isPaused = !isPaused;
				break;

			case SEEK_BACKWARD:
//				if (mVideoView != null) {
//					int pos = (int) mVideoView.getCurrentPosition();
//					Integer times = 10;
//					String key_2 = "10";
//					if (preference != null) {
//						key_2 = preference.getString(SettingActivity.key_2,
//								"10");
//						if (key_2 != null) {
//							times = Integer.valueOf(key_2);
//						}
//
//					}
//					pos -= (times * 1000);
//					// pos -= 15000;
//					mVideoView.seekTo(pos);
//				}
				cancelDelayHide();
				hideControllerDelay();
				break;

			case SEEK_FORWARD:
//				if (mVideoView != null) {
//					int pos = (int) mVideoView.getCurrentPosition();
//					Integer times = 10;
//					String key_2 = "10";
//					if (preference != null) {
//						key_2 = preference.getString(SettingActivity.key_2,
//								"10");
//						if (key_2 != null) {
//							times = Integer.valueOf(key_2);
//						}
//
//					}
//
//					pos += (times * 1000);
//					// pos += 15000;
//					mVideoView.seekTo(pos);
//				}
				cancelDelayHide();
				hideControllerDelay();
				break;

			}

			super.handleMessage(msg);
		}
	};

	private int mAudioMax;
	private int mAudioDisplayRange;
	private float mTouchY, mVol;
	private boolean mIsAudioChanged;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		mGestureDetector.onTouchEvent(event);
		if (mAudioDisplayRange == 0)
			mAudioDisplayRange = Math.min(getWindowManager()
					.getDefaultDisplay().getWidth(), getWindowManager()
					.getDefaultDisplay().getHeight());
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			mTouchY = event.getY();
			mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			mIsAudioChanged = false;
			break;

		case MotionEvent.ACTION_MOVE:
			float y = event.getY();

			int delta = (int) (((mTouchY - y) / mAudioDisplayRange) * mAudioMax);
			int vol = (int) Math.min(Math.max(mVol + delta, 0), mAudioMax);
			if (delta != 0) {
				updateVolume(vol);
				mIsAudioChanged = true;
			}
			break;

		case MotionEvent.ACTION_UP:
			if (!mIsAudioChanged) {
				if (!isControllerShow) {
					isControllerShow = false;
					showController();
					cancelDelayHide();
					hideControllerDelay();
				} else {
					isControllerShow = true;
					hideController();
					cancelDelayHide();
				}
			}
			break;
		}
		return mIsAudioChanged;

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.v(TAG, " onConfigurationChanged()");

		getScreenSize();
		if (isControllerShow) {
			hideController();
			showController();
			cancelDelayHide();
			hideControllerDelay();
		}

		super.onConfigurationChanged(newConfig);
	}

	private void updateVolume(int index) {
		LogUtil.i(TAG, "updateVolume==" + index + "----------currentVolume="
				+ currentVolume);
		if (mAudioManager != null) {
			if (isSilent) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
				mSeekBarvolume.setProgress(0);
			} else {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index,0);
				mSeekBarvolume.setProgress(index);
			}
			currentVolume = index;
		}
	}

	private void getScreenSize() {
		Display display = getWindowManager().getDefaultDisplay();
		screenHeight = display.getHeight();
		screenWidth = display.getWidth();

	}

	private AlertDialog alertDialog = null;
	AlertDialog.Builder aler = null;

	private void ConfirmExit() {// 退出确认
		aler = new AlertDialog.Builder(VideoViewDemo.this);
		aler.setTitle("提示");

		if (uri != null && isLocal) {

			if (!isOnCompletion) {
				setErrorTyp(errorType);
				aler.setNegativeButton("确定", new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						alertDialog.dismiss();
						finish();
					}
				});

			}
		}

		if (uri != null && !isLocal) {

			if (isOnCompletion) {
				aler.setMessage(getString(R.string.play_comper));
				aler.setPositiveButton("退出",
						new DialogInterface.OnClickListener() {// 退出按钮

							public void onClick(DialogInterface dialog, int i) {

								alertDialog.hide();
								finish();
								alertDialog = null;

							}
						});
				aler.setNegativeButton("进入", new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
//						Intent intent = new Intent();
//						intent.setClass(VideoPlayer.this, MainActivity.class);
//						startActivity(intent);
//						overridePendingTransition(R.anim.fade, R.anim.hold);
//						alertDialog.hide();
//						finish();
//						alertDialog = null;

					}
				});
			} else {
				aler.setPositiveButton("退出",
						new DialogInterface.OnClickListener() {// 退出按钮

							public void onClick(DialogInterface dialog, int i) {
								alertDialog.hide();
								finish();
								alertDialog = null;

							}
						});
				setErrorTyp(errorType);
				aler.setNegativeButton("进入", new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

//						Intent intent = new Intent();
//						intent.setClass(VideoPlayer.this, MainActivity.class);
//						startActivity(intent);
//						overridePendingTransition(R.anim.fade, R.anim.hold);
//						alertDialog.hide();
//						finish();
//						alertDialog = null;

					}
				});
			}

		}
		if (alertDialog == null) {
			alertDialog = aler.create();
		}
		if (alertDialog != null && !alertDialog.isShowing()) {
			alertDialog.show();
		}

	}

	private void setErrorTyp(int errorType) {
		switch (errorType) {

		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
			aler.setMessage("抱歉，该视频无法拖动！");
			break;

		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			aler.setMessage("抱歉，播放出错了!");
			
			break;
		// case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
		// aler.setMessage("抱歉，该视频文件格式错误！");
		// break;
		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			aler.setMessage("抱歉，解码时出现");
			break;
		default:
			aler.setMessage("抱歉，该视频无法播放！");
			break;
		}
	}

	private void exit() {
		try {
			if (mVideoView != null) {
				mVideoView.stopPlayback();
//				if(batteryReceiver!=null&&level!=0){
//					level=0;
//					unregisterReceiver(batteryReceiver);
//				}
			}
		} catch (Exception e) {
			if (mVideoView != null) 
				mVideoView.stopPlayback();
			finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		}
		finish();
		overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
	}

	private boolean isError = false;

	@Override
	protected void onDestroy() {
		if(batteryReceiver!=null&&level!=0){
			level=0;
			unregisterReceiver(batteryReceiver);
		}
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		Log.v(TAG, " onPause()");

		if (mVideoView != null && !isOnCompletion && !isError) {
			mCurrentPosition = (int) mVideoView.getCurrentPosition();
		}
		SharedPreferences sp=this.getSharedPreferences("seekTo", Context.MODE_PRIVATE);
		Editor editor=sp.edit();
		editor.putInt("seekTo", mCurrentPosition);
		editor.putString("songURL", PubParamInfo.videoInfoCache.get(position).getUrl());
		editor.commit();
//		if (mHandler != null&&radia==null) {
//			mHandler.sendEmptyMessage(PAUSE);
//		}
		super.onPause();
	}


	protected void onResume() {
		SharedPreferences sp=this.getSharedPreferences("seekTo", Context.MODE_PRIVATE);
		mCurrentPosition=sp.getInt("seekTo", 0);
		String songURL=sp.getString("songURL", "a");
		if(mVideoView != null){
			if(PubParamInfo.videoInfoCache!=null&&PubParamInfo.videoInfoCache.size()>0){
				if(mCurrentPosition>1&&songURL.equals(PubParamInfo.videoInfoCache.get(position).getUrl())){
					if(!songURL.toLowerCase().contains("http")||!songURL.toLowerCase().contains("rtmp"))
						mVideoView.seekTo(mCurrentPosition);
					mVideoView.start();
				}
			}else{
					mVideoView.start();
			}
		}
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return false;
	}

}
