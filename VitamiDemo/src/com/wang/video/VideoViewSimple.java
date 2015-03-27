/*
 * Copyright (C) 2013 yixia.com
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
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.Toast;

import com.wang.activity.R;
import com.wang.activity.R.id;
import com.wang.activity.R.layout;
import com.wang.util.PubParamInfo;

public class VideoViewSimple extends Activity {

	/**
	 * TODO: Set the path variable to a streaming video URL or a local media
	 * file path.
	 */
	private VideoView mVideoView;
	/** 当前缩放模式 */
	private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
	private GestureDetector mGestureDetector;
	private int position;
	private Intent mIntent;
	private String radia = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!LibsChecker.checkVitamioLibs(this)){
			System.out.println("==========checkVitamioLibs==============================");
			return;
		}
		setContentView(R.layout.videoviewsimple);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		
		mIntent = getIntent();
		if(mIntent!=null){
			position = mIntent.getIntExtra("CurrentPosInMediaIdList", 0);
	  		radia = mIntent.getStringExtra("radia");
		}
		setVideoNamePlayVideo();
		mGestureDetector = new GestureDetector(this, new MyGestureListener());
	}
	
	
	private void setVideoNamePlayVideo() {
//		mVideoName.setText(PubParamInfo.videoInfoCache.get(position).getDiaplayName());
		File f = new File(PubParamInfo.videoInfoCache.get(position).getUrl());
		if (f == null || !f.exists()) {
			Toast.makeText(VideoViewSimple.this, "文件不存在", Toast.LENGTH_LONG)
					.show();
			return ;
		}
		mVideoView.setVideoPath(PubParamInfo.videoInfoCache.get(position).getUrl());
		mVideoView.setMediaController(new MediaController(this));
		mVideoView.requestFocus();
		mVideoView.start();
}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;

		return super.onTouchEvent(event);
	}

	private class MyGestureListener extends SimpleOnGestureListener {

		/** 双击 */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
				mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
			else
				mLayout++;
			if (mVideoView != null)
				mVideoView.setVideoLayout(mLayout, 0);
			return true;
		}

	}
}
