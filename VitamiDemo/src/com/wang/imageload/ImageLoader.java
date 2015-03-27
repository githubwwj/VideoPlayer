package com.wang.imageload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class ImageLoader {

	public static MemoryCache memoryCache = new MemoryCache();
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	// 线程池
	private ExecutorService executorService;
	AnimationSet animationSet = new AnimationSet(true);

	public ImageLoader(Context context,ContentResolver contentResolver) {
		executorService = Executors.newFixedThreadPool(5);
		ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		animationSet.addAnimation(scaleAnimation);
		animationSet.setDuration(1000);
		
	}

	// 最主要的方法
	public void DisplayImage(String url, ImageView imageView, boolean isLoadOnlyFromCache) {
		imageViews.put(imageView, url);
		// 先从内存缓存中查找

		Bitmap bitmap = memoryCache.get(url);
		
		if (bitmap != null){
			imageView.setImageBitmap(bitmap);
//			imageView.startAnimation(animationSet);
		}			
		else if (!isLoadOnlyFromCache){
			// 若没有的话则开启新线程加载图片
			queuePhoto(url, imageView);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}
	
	private Bitmap SDCARDBitmap(String url){
		Bitmap bm=null;
		String pic_url=url;
		String picName=pic_url.substring(pic_url.lastIndexOf("/")+1);
		File file=new File(Environment.getExternalStorageDirectory()+"/ZD_DB_PIC");
		if(file.exists()){
			String picUrl=file+"/"+picName;
//			 bm=Utils.getSmallBitmap(picUrl, 240, 240);
		}
//		if (PubParamInfo.imageCache.size() >=40) {
//			PubParamInfo.imageCache.poll();
//		}
//		CacheImg img = new CacheImg();
//		img.setPath(dishe.getDishe_picUrl());
//		img.setBitmap(bm);
//		PubParamInfo.imageCache.add(img);
		return bm;
	}



	// decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 100;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp =SDCARDBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			// 更新的操作放在UI线程中
			Activity a = (Activity) photoToLoad.imageView.getContext();
//			photoToLoad.imageView.startAnimation(animationSet);
			a.runOnUiThread(bd);
		}
	}

	/**
	 * 防止图片错位
	 * 
	 * @param photoToLoad
	 * @return
	 */
	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// 用于在UI线程中更新界面
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
	
		}
	}

	public void clearCache() {
		memoryCache.clear();
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			Log.e("", "CopyStream catch Exception...");
		}
	}

	public void DisplayImage(int id,ImageView imageView, boolean b) {
		imageViews.put(imageView, id+"");
		// 先从内存缓存中查找

		Bitmap bitmap = memoryCache.get(id+"");
		
		if (bitmap != null){
			imageView.setImageBitmap(bitmap);
		}			
		else if (!b){
			// 若没有的话则开启新线程加载图片
			queuePhoto(id+"", imageView);
		}
	}


}