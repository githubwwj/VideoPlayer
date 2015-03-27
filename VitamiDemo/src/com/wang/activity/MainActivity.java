package com.wang.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements View.OnClickListener{


	
	private Button localPlayerVA;
	private Button direct_broadcast;
	private Button love_movie;
	private Button about_author;
	private Button game_over;
	private int windowWidth;
	private int windowHeight;
	private LinearLayout in_layout;
	private RelativeLayout rlgone;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        windowWidth=getWindowManager().getDefaultDisplay().getWidth();
        windowHeight=getWindowManager().getDefaultDisplay().getHeight();
        localPlayerVA=(Button)this.findViewById(R.id.local_player);
        direct_broadcast=(Button)this.findViewById(R.id.direct_broadcast);
        love_movie=(Button)this.findViewById(R.id.love_movie);
        about_author=(Button)this.findViewById(R.id.about_author);
        game_over=(Button)this.findViewById(R.id.game_over);
        in_layout=(LinearLayout)this.findViewById(R.id.in_layout);
        rlgone =(RelativeLayout)findViewById(R.id.rlgone);
      
        
        localPlayerVA.setOnClickListener(this);
        direct_broadcast.setOnClickListener(this);
        love_movie.setOnClickListener(this);
        about_author.setOnClickListener(this);
        game_over.setOnClickListener(this);
    		
    }
    
    Handler mExitHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what==1) {
				finish();
			}
		}
	}; 
    
    @Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		Intent mIntent = new Intent();
		switch (v.getId()) {
		case R.id.local_player:
			mIntent.setClass(MainActivity.this, VideoPlayList.class);
			startActivity(mIntent);
			overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
			break;
		case R.id.direct_broadcast:
			mIntent.setClass(MainActivity.this, TVActivity.class);
			startActivity(mIntent);
			overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
			break;
		case R.id.love_movie:
			mIntent.setClass(MainActivity.this, WebViewMovie.class);
			startActivity(mIntent);
			overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
			break;
		case R.id.game_over:
			game_over();
			overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
			break;
		case R.id.about_author:
			mIntent.setClass(MainActivity.this, AuthorActivity.class);
			startActivity(mIntent);
			overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
			break;
		default:
			break;
		}
	}
	
	/*
	 * 关闭程序
	 */
	private void game_over() {
		Animation animation4=new TranslateAnimation(windowWidth,0 ,windowHeight, 0);
		animation4.setDuration(500);
		rlgone.setVisibility(View.GONE);
		in_layout.startAnimation(animation4);
		
		mExitHandler.sendEmptyMessageDelayed(1, 500);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			game_over();
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


   
}
