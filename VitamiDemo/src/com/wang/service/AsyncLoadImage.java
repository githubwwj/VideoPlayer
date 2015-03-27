package com.wang.service;



import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.wang.activity.R;
import com.wang.domain.CacheImg;

public class AsyncLoadImage {
	
	private static final String TAG = AsyncLoadImage.class.getSimpleName();

	private Context context;
	private Handler handler;
	
	private Object lock = new Object();
	private boolean isAllow = true;
	
	private ConcurrentLinkedQueue<CacheImg> imageCache;
	
	public AsyncLoadImage(Context context,Handler handler) {
		this.context = context;
		this.handler = handler;
		imageCache = new ConcurrentLinkedQueue<CacheImg>();
	}
	
	//1.子线程加载图片
	public void loadImage(ImageView imageView) {
		String path = (String) imageView.getTag();
		
		imageView.setImageResource(R.drawable.format_picture);
		new LoadImageThread(imageView).start();
		
	}
	// 
	public void loadImage2(ImageView imageView) {
		String path = (String) imageView.getTag();
		imageView.setImageResource(R.drawable.format_picture);
		
		//1.在滑动中时将加载线程的工作暂停，让其处于等待状态，以一个默认图片显示
		
		//2.待滑动停止时，在通知子线程开始加载图片，加载完毕后再通知主线程更新UI
		new LoadImageThread2(imageView).start();
	}
	
	public void loadImage3(ImageView imageView) {
		String path = (String) imageView.getTag();
		imageView.setImageResource(R.drawable.format_media);
		
		//1.根据路径从内存中取已经加载过的图片，如果有，直接从内存中返回
		
		for(CacheImg img : imageCache){
			if (path.equals(img.getPath())) {
				Bitmap bitmap = img.getBitmap();
				imageView.setImageBitmap(bitmap);
				return;
			}
		}
		//2.如果没有，再去子线程中去取
		new LoadImageThread3(imageView).start();
		
		
	}
	
	class LoadImageThread3 extends Thread {
		private ImageView imageView;
		private String path;
		
		public LoadImageThread3(ImageView imageView) {
			this.imageView = imageView;
			this.path = (String) imageView.getTag();
		}
		
		@Override
		public void run() {
			
			//加载图片
			if (!isAllow) {
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						Log.e(TAG, "", e);
					}
				}
			}
			final Bitmap	bitmap =ThumbnailUtils.createVideoThumbnail(path, 96*96);
//			final Bitmap    bitmap=ThumbnailUtils.extractThumbnail(bitmap0, 100, 100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inSampleSize = 4;
//			final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
			CacheImg img = new CacheImg();
			if(bitmap==null){
				img.setBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.format_media));
			}else{
				img.setBitmap(bitmap);
			}
			img.setPath(path);
			
			//把得到bitmap和路径的CacheImage对象存入到内存里边，如果大于要求的缓存数量在，就把缓存当中的第一个元素删掉。
			if (imageCache.size() >= 30) {
				imageCache.poll();
			}
			imageCache.add(img);
			
			if(bitmap!=null){
				//2.完毕之后通过handler主线程更新
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						imageView.setImageBitmap(bitmap);
					}
				});
			}
		}
	}
	
	public void lock() {
		isAllow = false;
	}
	
	public void unLock() {
		isAllow = true;
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
	class LoadImageThread2 extends Thread {
		private ImageView imageView;
		private String path;
		
		public LoadImageThread2(ImageView imageView) {
			this.imageView = imageView;
			this.path = (String) imageView.getTag();
		}
		
		@Override
		public void run() {
			
			//加载图片
			if (!isAllow) {
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						Log.e(TAG, "", e);
					}
				}
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
			//2.完毕之后通过handler主线程更新
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					imageView.setImageBitmap(bitmap);
				}
			});
			
		}
	}
	
	class LoadImageThread extends Thread {

		private ImageView imageView;
		private String path;
		
		public LoadImageThread(ImageView imageView) {
			this.imageView = imageView;
			this.path = (String) imageView.getTag();
		}
		@Override
		public void run() {
			//加载图片
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
			//2.完毕之后通过handler主线程更新
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					imageView.setImageBitmap(bitmap);
				}
			});
			
		}
		
	}
	
	
	
}
