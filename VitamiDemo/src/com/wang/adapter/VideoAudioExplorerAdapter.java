package com.wang.adapter;


import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wang.activity.R;
import com.wang.domain.VideoInfo;
import com.wang.imageload.ImageLoader;

public class VideoAudioExplorerAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	public static Parcelable state;
	ArrayList<VideoInfo> al;
	ContentResolver contentResolver;
	private ImageLoader mImageLoader;

	public VideoAudioExplorerAdapter(Context context, ArrayList<VideoInfo> al) {
		this.al = al;
		inflater = LayoutInflater.from(context);
		contentResolver=context.getContentResolver();
	}

	@Override
	public int getCount() {
		return al == null ? 0 : this.al.size();
	}

	@Override
	public Object getItem(int position) {
		return al.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	public View getView( int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.file_item, null);
			holder.fileIcon = (ImageView) convertView
					.findViewById(R.id.file_icon);
			holder.fileName = (TextView) convertView
					.findViewById(R.id.file_name);
			holder.fileTimeLength = (TextView) convertView
					.findViewById(R.id.file_time_length);
			holder.duration = (TextView) convertView.findViewById(R.id.duration);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String fileName = al.get(position).getDiaplayName().toLowerCase();
		if(al.get(position).getFlags()==2){
			holder.fileIcon.setImageResource(R.drawable.format_music);
		}else{
			if(al.get(position).getBitmap()!=null)
				holder.fileIcon.setImageBitmap(al.get(position).getBitmap());
			else
			holder.fileIcon.setImageResource(R.drawable.format_media);
		}
            
			holder.fileName.setText(fileName);
			double l = (double)al.get(position).getFileSize() /1024 /1024;
			if((l+"").length()>4){
				holder.fileTimeLength.setText((l+"").subSequence(0, 4)+ "M");
			}else
				holder.fileTimeLength.setText((l+"")+ "M");
//			String duration=""+Integer.parseInt(al.get(position).getDuration())/60000f;
			al.get(position).getDuration();
			int secondnd = (Integer.parseInt(al.get(position).getDuration()) / 1000) / 60;

			int million = (Integer.parseInt(al.get(position).getDuration()) / 1000) % 60;

			String f = String.valueOf(secondnd);

			String m = million >= 10 ? String.valueOf(million) : "0"

			+ String.valueOf(million);
			
			holder.duration.setText( f + ":" + m);
		return convertView;
	}

	static  class ViewHolder {
		 ImageView fileIcon;
		 TextView fileName;
		 TextView fileTimeLength;
		 TextView duration;
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}
	
}
