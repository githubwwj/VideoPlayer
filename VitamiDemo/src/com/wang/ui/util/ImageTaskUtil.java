package com.wang.ui.util;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.wang.domain.CacheImg;


public class ImageTaskUtil extends AsyncTask<Object,Object,Object> {

	private static Object lock = new Object();
	private static boolean isAllow = true;
//	private static final String TAG = ImageTaskUtil.class.getSimpleName();
	
	
	private TaskAction thisAction = null;
	public ImageTaskUtil(TaskAction action) {
		this.thisAction = action;
	}
	
	/**
	 * 第一个参数:SD中图片的地址
	 */
	@Override
	protected Object doInBackground(Object... params) {
		
//		if (!isAllow) {
//			synchronized (lock) {
//				try {
//					lock.wait();
//				} catch (InterruptedException e) {
//					Log.e(TAG, "", e);
//				}
//			}
//		}
		
		ImageView imageView=(ImageView)params[0];
		String path = (String) imageView.getTag();
//		for (CacheImg img : PubParamInfo.imageCache) {
//			if (path.equals(img.getPath())) {
//				Bitmap bitmap = img.getBitmap();
//				if(bitmap!=null){
//					return bitmap;
//				}else{
//					return null;
//				}
//			}
//		}
		 Bitmap	bitmap =ThumbnailUtils.createVideoThumbnail(path, 90*90);
//		 final Bitmap    bitmap=ThumbnailUtils.extractThumbnail(bitmap0, 150, 150,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		
		 	CacheImg cacheImg=new CacheImg();
		 	cacheImg.setBitmap(bitmap);
		 	cacheImg.setPath(path);
//			if (PubParamInfo.imageCache.size() >= 30) {
//				PubParamInfo.imageCache.poll();
//			}
//			PubParamInfo.imageCache.add(cacheImg);
			return bitmap;
	}
	
	@Override
	protected void onPostExecute(Object result) {
		if (this.thisAction != null)
			this.thisAction.postResult(result);
		super.onPostExecute(result);
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
	
	protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
	}
	
	public static void lock(){
		isAllow = false;
	}
	
	public static void unLock() {
		isAllow = true;
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
}
