package com.wang.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wang.util.Utils;
import com.wang.video.VideoViewDemo;
/**
 * @author wwj
 */
public class WebViewMovie extends Activity {

	private WebView mWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置为屏幕常亮
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.my_webview);
		// 执行初始化函数
		Utils.showProgress(this,null);
		init();
		loadurl(mWebView,"http://movie.youku.com/",false);
	}
	
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Utils.dismissProgress();
		}
	};

	public void init() {
		mWebView = (WebView) findViewById(R.id.webview);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);// 可用JS
		webSettings.setAllowFileAccess(true);
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(final WebView view,
					final String url) {
				
				if (Utils.isMediaFile(url)) {
					loadurl(view, url, true);// 载入网页,播放多媒体文件
				} else {
					loadurl(view, url, false);
				}

				return true;
			}// 重写点击动作,用webview载入

		});
		mWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发
				
				if (progress == 100) {
					mHandler.sendEmptyMessage(1);
					// 如果全部载入,隐藏进度对话框
				}
				super.onProgressChanged(view, progress);
			}
		});

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {// 捕捉返回键
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			WebViewMovie.this.finish();// 关闭activity
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 加载网络页面
	 * @param view
	 * @param url
	 * @param isVideoUrl
	 */
	public void loadurl(final WebView view, final String url,
			final boolean isVideoUrl) {
		new Thread() {
			public void run() {
				String uris = url;
				if (isVideoUrl) {
					//判断是否是播放地址，并启动播放的
					if (Utils.isMediaFile(uris)) {
						Uri uri = Uri.parse(uris);
						Log.i("info", "uris="+uris);
						Intent intent = new Intent(WebViewMovie.this,VideoViewDemo.class);
						intent.setDataAndType(uri, "video/*");
						Log.i("info", "=========VideoViewDemo======");
						WebViewMovie.this.startActivity(intent);
					}
				} else {
					view.loadUrl(url);// 载入网页
				}

			}
		}.start();
	}

}