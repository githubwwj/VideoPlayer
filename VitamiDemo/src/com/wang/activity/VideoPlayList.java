package com.wang.activity;


import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.adapter.VideoAudioExplorerAdapter;
import com.wang.dialog.DialogVideoChooseItem;
import com.wang.domain.VideoInfo;
import com.wang.util.PubParamInfo;
import com.wang.util.Utils;
import com.wang.video.VideoViewDemo;

public class VideoPlayList extends Activity implements OnItemClickListener,
		OnItemLongClickListener {

	private ListView listView;
	private VideoAudioExplorerAdapter adapter;
	ContentResolver contentResolver =null;
	ArrayList<VideoInfo> musicInfo=new ArrayList<VideoInfo>();
	private QueryVideoFileThread mQueryVideoFileThread;
	private QueryAudioFileThred mQueryAudioFileThred;
	private TextView videoNumber;
	private TextView audioNumber;
	private TextView all_number;
	private int position;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.media_audio_file);
		contentResolver = VideoPlayList.this.getContentResolver();
		// 检测SD卡是否存在
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "没有SD卡", Toast.LENGTH_LONG).show();
			finish();
		}
		Utils.showProgress(this,null);
		initUI();
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if(PubParamInfo.videoInfoCache.size()>=1){
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
			}else{
				mQueryVideoFileThread=new QueryVideoFileThread();
				mQueryVideoFileThread.start();
				mQueryAudioFileThred=new QueryAudioFileThred();
				mQueryAudioFileThred.start();
			}
		} else {
			Utils.dismissProgress();
			Toast.makeText(this, getString(R.string.sdcard_error_message),Toast.LENGTH_LONG).show();
		}

		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	}
	
	
	private class QueryVideoFileThread extends Thread {
		@Override
		public void run() {
			 String[] projection = new String[]{
					 	MediaStore.Video.Media.TITLE,  
		               	MediaStore.Video.Media.DURATION,								                
		               	MediaStore.Video.Media.ARTIST,
		                MediaStore.Video.Media._ID,
		                MediaStore.Video.Media.SIZE,
		                MediaStore.Video.Media.DISPLAY_NAME,
		                MediaStore.Video.Media.DATA,
		                MediaStore.Video.Media.MIME_TYPE,
		                MediaStore.Video.Media.ALBUM,
		                };
		        
		        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, 
		                null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
		        cursor.moveToFirst();
		        int fileNum = cursor.getCount();
		       
		        for(int counter = 0; counter < fileNum; counter++){
		        	VideoInfo videoInfo=new VideoInfo();
		        	videoInfo.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
		        	videoInfo.setDuration(""+cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
		        	videoInfo.setFileSize(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)));
		        	videoInfo.setUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
		        	videoInfo.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)));
		        	videoInfo.setDiaplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
		        	videoInfo.setMimeType(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)));
		        	videoInfo.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM)));
		        	
		        	videoInfo.setFlags(1);
		        	int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
		        	
		        	BitmapFactory.Options options = new BitmapFactory.Options();
		            options.inDither = false;
		            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		            Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, id,
		                     Images.Thumbnails.MINI_KIND, options);
		        	videoInfo.setId(id);
		        	videoInfo.setBitmap(bitmap);
		            cursor.moveToNext();
		            PubParamInfo.videoInfoCache.add(videoInfo);
		        }
		        cursor.close();
		        PubParamInfo.videoNumber=PubParamInfo.videoInfoCache.size();
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
		}
	}
	
	
	private class QueryAudioFileThred extends Thread {
		@Override
		public void run() {
				
					String[] musicprojection = new String[]{
								MediaStore.Audio.Media.TITLE,  
				               	MediaStore.Audio.Media.DURATION,								                
				               	MediaStore.Audio.Media.ARTIST,
				                MediaStore.Audio.Media._ID,
				                MediaStore.Audio.Media.SIZE,
				                MediaStore.Audio.Media.DISPLAY_NAME,
				                MediaStore.Audio.Media.DATA,
				                MediaStore.Audio.Media.ALBUM,
				                MediaStore.Audio.Media.YEAR,
				                MediaStore.Audio.Media.MIME_TYPE
				                };
					Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicprojection, 
			                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			        cursor.moveToFirst();
			        int  fileNum = cursor.getCount();
			        for(int counter = 0; counter < fileNum; counter++){
			        	VideoInfo videoInfo=new VideoInfo();
			        	videoInfo.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
			        	videoInfo.setDuration(""+cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
			        	videoInfo.setFileSize(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
			        	videoInfo.setUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
			        	videoInfo.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)));
			        	videoInfo.setDiaplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)));
			        	int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
			        	videoInfo.setId(id);
			        	videoInfo.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM)));
			        	videoInfo.setYear(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)));
			        	videoInfo.setMimeType(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)));
			            videoInfo.setFlags(2);
			            cursor.moveToNext();
			            musicInfo.add(videoInfo);
			        }
			        cursor.close();
			        PubParamInfo.audioNumber=musicInfo.size();
			        Message msg = new Message();
					msg.what = 2;
					handler.sendMessage(msg);
				}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void setAdpater() {
		if(adapter==null){
			adapter = new VideoAudioExplorerAdapter(this, PubParamInfo.videoInfoCache);
			listView.setAdapter(adapter);
			Utils.dismissProgress();
		}else{
			Utils.dismissProgress();
			adapter.notifyDataSetChanged();
		}
			
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				sendData();
				break;
			case 2:
				sendData();
				break;
			default:
				break;
			}
		}
		
		private void sendData() {
			if(mQueryVideoFileThread!=null&&mQueryAudioFileThred!=null&&!mQueryVideoFileThread.isAlive()&&!mQueryAudioFileThred.isAlive()){
				showData();
			}else{
				if(mQueryVideoFileThread==null&&mQueryAudioFileThred==null)
					showData1();
			}
		}

		
	};
	
	private void showData() {
		audioNumber.setText(PubParamInfo.audioNumber+"个");
		videoNumber.setText(PubParamInfo.videoNumber+"个");
		for(int i=0;i<musicInfo.size();i++){
			PubParamInfo.videoInfoCache.add(musicInfo.get(i));
			musicInfo.remove(i);
			i=0;
		}
		all_number.setText(PubParamInfo.audioNumber+PubParamInfo.videoNumber+"个");
		setAdpater();
	}
	
	private void showData1() {
		audioNumber.setText(PubParamInfo.audioNumber+"个");
		videoNumber.setText(PubParamInfo.videoNumber+"个");
		all_number.setText(PubParamInfo.videoInfoCache.size()+"个");
		setAdpater();
	}

	int file = 0;
	public void  updateShowData() {
		if(PubParamInfo.videoInfoCache.get(position).getFlags()==2){
			if(PubParamInfo.audioNumber>0){
				PubParamInfo.audioNumber=PubParamInfo.audioNumber-1;
				audioNumber.setText(PubParamInfo.audioNumber+"个");
			}else
				audioNumber.setText(0+"个");
				
		}else {
			if(PubParamInfo.videoNumber>0){
				PubParamInfo.videoNumber=PubParamInfo.videoNumber-1;
				videoNumber.setText(PubParamInfo.videoNumber+"个");
			}else
				videoNumber.setText(0+"个");
		}
		all_number.setText(PubParamInfo.audioNumber+PubParamInfo.videoNumber+"个");
		PubParamInfo.videoInfoCache.remove(position);
		setAdpater();
	}
	

	private void initUI() {
		listView = (ListView) findViewById(R.id.listview);
		videoNumber=(TextView)findViewById(R.id.video_number);
		audioNumber=(TextView)findViewById(R.id.audio_number);
		all_number=(TextView)findViewById(R.id.allaa_number);
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		 Intent intent = new Intent(VideoPlayList.this,VideoViewDemo.class);
		 intent.putExtra("aa", "al");
		 intent.putExtra("CurrentPosInMediaIdList", position);
		 startActivity(intent);
		 overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Utils.dismissProgress();
			if(mQueryVideoFileThread!=null&&mQueryAudioFileThred!=null)
				if(mQueryVideoFileThread.isAlive()||mQueryAudioFileThred.isAlive())
					PubParamInfo.videoInfoCache.clear();
			finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		}
		return true;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		this.position=position;
		DialogVideoChooseItem dialog=new DialogVideoChooseItem(VideoPlayList.this,position,VideoPlayList.this);
		dialog.show();
		
		return false;
	}

}
