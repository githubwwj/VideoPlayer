package com.wang.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.wang.service.SoundService;

public class AuthorActivity extends Activity implements  OnClickListener {
    /**
     * 返回主页按钮
     */
	private Button gomainmenu;
	 /**
    * 我的作品按钮
    */
	private Button author_give_autor;
	 /**
    * 声音服务类
    */
	private SoundService soundService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置为屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//加载布局文件
		setContentView(R.layout.author_activity);
		Log.v("ThAuthor", "onCreate()");
		
		//初始化按钮
		gomainmenu = (Button) this.findViewById(R.id.author_gomainmenu);
		author_give_autor = (Button) this.findViewById(R.id.author_give_autor);
//		author_goversion = (ImageButton) this.findViewById(R.id.author_goversion);
		
		//设置按钮点击事件
		gomainmenu.setOnClickListener(this);
		author_give_autor.setOnClickListener(this);
		//初始化声音服务类：播放按钮音效、播放作者家乡音乐
		soundService = new SoundService(AuthorActivity.this);
	}

	@Override
	public void onClick(View v) {
		
		//没点击一次按钮，播放一次点击音效
		if (soundService != null) {
			soundService.playButtonMusic(R.raw.button);
		}

		switch (v.getId()) {
		case R.id.author_gomainmenu:
			finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			break;
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==60){
			
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (soundService != null) {
			soundService.playButtonMusic(R.raw.button);
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			Toast.makeText(AuthorActivity.this, R.string.app_hint_msg, 0).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}