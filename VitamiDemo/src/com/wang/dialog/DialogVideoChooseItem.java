package com.wang.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.activity.R;
import com.wang.activity.VideoPlayList;
import com.wang.util.PubParamInfo;


public class DialogVideoChooseItem extends Dialog implements OnItemClickListener,android.view.View.OnClickListener {


	private Activity context = null;
	private ListView listView;
	private TextView entity_list_title;
	private TextView desc;
	private LinearLayout button_gone;
	private LinearLayout ll_listview;
	private	Button reject_sure;
	private Button reject_cancle;
	private int state;
	private	int mPosition;
	private VideoPlayList videoPlayList;
	private EditText dialog_rename;

	public DialogVideoChooseItem(Activity context,Integer position,VideoPlayList videoPlayList) {
		super(context, R.style.MyDialogStyle);
		this.context = context;
		this.mPosition=position;
		this.videoPlayList=videoPlayList;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.entity_select_list_layout);
		listView = (ListView) findViewById(R.id.entity_list_listView);
		entity_list_title=(TextView)findViewById(R.id.entity_list_title);
		desc=(TextView)findViewById(R.id.desc);
		
		dialog_rename=(EditText)findViewById(R.id.dialog_rename);
		ll_listview=(LinearLayout)findViewById(R.id.ll_listview);
		button_gone=(LinearLayout)findViewById(R.id.button_gone);
		reject_sure= (Button) this.findViewById(R.id.reject_sure);
		reject_cancle= (Button) this.findViewById(R.id.reject_cancle);
		
		listView.setOnItemClickListener(this);
		reject_sure.setOnClickListener(this);
		reject_cancle.setOnClickListener(this);

		ArrayList<String> data=new ArrayList<String>();
		data.add("分享");
		data.add("删除");
		data.add("重命名");
		data.add("详情");
		
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(context,R.layout.dialog_video_lv ,R.id.dialog_item_tv, data);
		listView.setAdapter(adapter);

		this.setCanceledOnTouchOutside(true);// 点击对话框外部取消对话框显示
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String videoItem=(String)listView.getItemAtPosition(position);
		if(videoItem.equals("分享")){
			 Intent intent=new Intent(Intent.ACTION_SEND);   
             intent.setType("image/*");   
             intent.putExtra(Intent.EXTRA_SUBJECT, "Share");   
             intent.putExtra(Intent.EXTRA_TEXT, "I have successfully share my message through my app (分享自city丽人馆)");   
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
             context.startActivity(Intent.createChooser(intent, "分享多媒体文件"));
			 dismiss();
		}else if(videoItem.equals("删除")){
			entity_list_title.setText("您确定要删除吗");
			state=2;
			desc.setVisibility(View.VISIBLE);
			ll_listview.setVisibility(View.GONE);
			button_gone.setVisibility(View.VISIBLE);
			desc.setText("您确定删除<<"+PubParamInfo.videoInfoCache.get(mPosition).getDiaplayName()+">>歌曲?");
		}else if(videoItem.equals("重命名")){
			state=3;
			entity_list_title.setText("重命名");
			ll_listview.setVisibility(View.GONE);
			dialog_rename.setVisibility(View.VISIBLE);
			button_gone.setVisibility(View.VISIBLE);
			dialog_rename.setText(PubParamInfo.videoInfoCache.get(mPosition).getTitle());
		}else if(videoItem.equals("详情")){
			ArrayList<String> data=new ArrayList<String>();
			double l = (double)PubParamInfo.videoInfoCache.get(mPosition).getFileSize() /1024 /1024;
			String fileSize=null;
			if((l+"").length()>4){
				fileSize=(l+"").subSequence(0, 4)+ "M";
			}else
				fileSize=l+ "M";
			if(PubParamInfo.videoInfoCache.get(mPosition).getArtist().equalsIgnoreCase("<unknown>"))
				data.add("艺术家:未知");
			else
				data.add("艺术家:"+PubParamInfo.videoInfoCache.get(mPosition).getArtist());
			data.add("文件名:"+PubParamInfo.videoInfoCache.get(mPosition).getDiaplayName());
			data.add("文件大小:"+fileSize);
			data.add("时长:"+PubParamInfo.videoInfoCache.get(mPosition).getDuration().subSequence(0, 3)+"s");
			data.add("专辑:"+PubParamInfo.videoInfoCache.get(mPosition).getAlbum());
			if(PubParamInfo.videoInfoCache.get(mPosition).getYear()!=null)
				data.add("年代:"+PubParamInfo.videoInfoCache.get(mPosition).getYear());
			data.add("格式:"+PubParamInfo.videoInfoCache.get(mPosition).getMimeType());
			ArrayAdapter<String> adapter=new ArrayAdapter<String>(context,R.layout.dialog_video_lv ,R.id.dialog_item_tv, data);
			listView.setAdapter(adapter);
		}
		
	}


	@Override
	public void onClick(View v) {
		if(v.equals(reject_sure)){
			if(state==2){
				ContentResolver contentResolver=context.getContentResolver();
				Uri deleteIdUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PubParamInfo.videoInfoCache.get(mPosition).getId());
				int i=contentResolver.delete(deleteIdUri, null, null);
				if(i>0){
					Toast.makeText(context, "删除成功!", Toast.LENGTH_SHORT).show();
					videoPlayList.updateShowData();
				}
				else
					Toast.makeText(context, "删除失败!", Toast.LENGTH_SHORT).show();
			}else if(state==3){  //重命名
				String rename=dialog_rename.getText().toString();
				if(rename!=null&&rename.trim().equals("")){
					Toast.makeText(context, "请输入文件名!", Toast.LENGTH_SHORT).show();
					return ;
				}
				ContentResolver contentResolver=context.getContentResolver();
				Uri updateIdUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PubParamInfo.videoInfoCache.get(mPosition).getId());
				ContentValues updateValues = new ContentValues();
				String houzui=PubParamInfo.videoInfoCache.get(mPosition).getDiaplayName();
				houzui=houzui.substring(houzui.lastIndexOf("."), houzui.length());
				houzui=rename+houzui;
				updateValues.put(MediaStore.Video.Media.DISPLAY_NAME, houzui);
				updateValues.put(MediaStore.Video.Media.TITLE, rename);
				int i=contentResolver.update(updateIdUri, updateValues, null, null);
				if(i>0){
					Toast.makeText(context, "更新成功!", Toast.LENGTH_SHORT).show();
					PubParamInfo.videoInfoCache.get(mPosition).setTitle(rename);
					PubParamInfo.videoInfoCache.get(mPosition).setDiaplayName(houzui);
					videoPlayList.setAdpater();
				}
				else
					Toast.makeText(context, "更新失败!", Toast.LENGTH_SHORT).show();
			}
			dismiss();
		}else if(v.equals(reject_cancle)){
			dismiss();
		}
	}




}
