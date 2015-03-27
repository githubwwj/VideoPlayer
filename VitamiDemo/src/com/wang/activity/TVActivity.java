package com.wang.activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.domain.VideoInfo;
import com.wang.util.BaseActivity;
import com.wang.util.HttpClientImp;
import com.wang.util.PubParamInfo;
import com.wang.util.UserPreference;
import com.wang.util.Utils;
import com.wang.video.VideoViewDemo;


public class TVActivity extends ExpandableListActivity {
	
	
	 private static final int IS_CLICK = 1;
	 private static final int LOAD_LINK = 20;
	 private static final int LOAD_LINK_FAIL = 21;
	 private List<Map<String, String>> mGroups = new ArrayList<Map<String, String>>(); 
	 
	// 将二级条目放在一个集合里，供显示时使用
	 private List<List<Map<String, String>>> mChilds=new ArrayList<List<Map<String,String>>>();
 	 private ArrayList<ArrayList<VideoInfo>>  mVideoArrayLists=new ArrayList<ArrayList<VideoInfo>>();
     private ArrayList<VideoInfo> mVideoList;
	 private boolean isCick = false;
     private TextView title_text;
     private Button titleBarLeftButton = null; 
	 private Button titleBarRightButton = null;  
	 private ArrayList<String> mArrayList=new ArrayList<String>();
	 private SimpleExpandableListAdapter mAdapter;
	 
	 @Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState); 
			 setContentView(R.layout.tv_list_activity);
			 BaseActivity.mBaseActivity = this;
			 PubParamInfo.videoInfoCache.clear();
		 	 title_text = (TextView)findViewById(R.id.title_text);
		 	 titleBarRightButton =(Button)findViewById(R.id.title_change_list); 
			 titleBarLeftButton =(Button)findViewById(R.id.title_search); 
		     titleBarLeftButton.setOnClickListener(listener); 
			 titleBarRightButton.setOnClickListener(listener);  
		 	 title_text.setText("成人视频"); 
			 Utils.showProgress(this, null);
	         /**
	          * 使用SimpleExpandableListAdapter显示ExpandableListView
	          * 参数1.上下文对象Context
	          * 参数2.一级条目目录集合
	          * 参数3.一级条目对应的布局文件
	          * 参数4.fromto，就是map中的key，指定要显示的对象
	          * 参数5.与参数4对应，指定要显示在groups中的id
	          * 参数6.二级条目目录集合
	          * 参数7.二级条目对应的布局文件
	          * 参数8.fromto，就是map中的key，指定要显示的对象
	          * 参数9.与参数8对应，指定要显示在childs中的id
	          */
			 mAdapter= new SimpleExpandableListAdapter(
	                 this, mGroups, R.layout.groups, new String[] { "group" },
	                 new int[] { R.id.group }, mChilds, R.layout.childs,
	                 new String[] { "child" }, new int[] { R.id.child });
	         setListAdapter(mAdapter); 
	         new Thread(new GetNetWorkInfo()).start();
	 }
	 
	 class GetNetWorkInfo implements Runnable{
		 
		 @Override
		public void run() {
			mArrayList.clear();
			String result=null;
			try {
				result = HttpClientImp.getInstance().getForString("https://just4you-just4you.rhcloud.com");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mExitHandler.sendEmptyMessage(LOAD_LINK_FAIL);
			}
			if(TextUtils.isEmpty(result)){
				mExitHandler.sendEmptyMessage(LOAD_LINK_FAIL);
				return ;
			}
			String[] arrStr=result.split("<a href=");
			for(int k=0;k<arrStr.length;k++){
				String data=arrStr[k];
				if(data.contains("http")){
					String res=data.substring(1,data.indexOf(" "));
					res=res.substring(0, res.length()-1);
					System.out.println("=====================res="+res);
					mArrayList.add(res);
					Log.i("info",res);
				}
			}
			testPullRead();
			mExitHandler.sendEmptyMessage(LOAD_LINK);
		}
		 
	 }
	 
	 
	 private OnClickListener listener = new OnClickListener() { 
			public void onClick(View view) {
				int id = view.getId();
				switch (id) { 
				case R.id.title_search:
					TVActivity.this.finish(); 
					overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
					break;
				case R.id.title_change_list:
			
					break;
				}
			}
		};
	 /**
      * 设置哪个二级目录被默认选中
      */
     @Override
     public boolean setSelectedChild(int groupPosition, int childPosition,
             boolean shouldExpandGroup) {
             //do something
         return super.setSelectedChild(groupPosition, 0,
                 true);
     }
     /**
      * 设置哪个一级目录被默认选中
      */
     @Override
     public void setSelectedGroup(int groupPosition) {
         super.setSelectedGroup(groupPosition);
     }
     
     /**
      * 当二级条目被点击时响应
      */
     @Override
     public boolean onChildClick(ExpandableListView parent, View v,
             int groupPosition, int childPosition, long id) {
    	  System.out.println("groupPosition------"+groupPosition);
    	  System.out.println("childPosition------"+childPosition);
          Map<String, String> mapFather = (Map<String, String>)parent.getAdapter().getItem(groupPosition);//获取子父节点里面的内容
    	  Map<String, String> map = (Map<String, String>)parent.getAdapter().getItem(childPosition);//获取子节点里面的内容
    	
    	 ArrayList<VideoInfo>  videoinfo_array = null;
    	 if(groupPosition < mVideoArrayLists.size()){
    		 System.out.println("size()-----"+mVideoArrayLists.size());
    	
    		 videoinfo_array = mVideoArrayLists.get(groupPosition);
    		 System.out.println("videoinfo_array.size()-----"+videoinfo_array.size());
    		 if(Utils.isCheckNetAvailable(TVActivity.this)){
 				if(Utils.getOSVersionSDKINT(TVActivity.this)>=7){
 					if(!isCick){
 						 isCick = true;
 						 for(int kk=0;kk<videoinfo_array.size();kk++)
 							 PubParamInfo.videoInfoCache.add(videoinfo_array.get(kk));
 						 Intent intent = new Intent(TVActivity.this,VideoViewDemo.class);
  						 intent.putExtra("CurrentPosInMediaIdList", childPosition);
  						 startActivity(intent);
  						 overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
  						 mExitHandler.sendEmptyMessageDelayed(IS_CLICK,1000);
 					 }
 				}else{
 					 Utils.netNoPlayeDialog();
 				}
 				 
 				}else{
 				 Utils.netCheckDialog();
 			} 
     	 
    		 
    	 }
    	 
    	 
         return super.onChildClick(parent, v, groupPosition, childPosition, id);
      
     } 
	 @Override
	protected void onDestroy() {
		 super.onDestroy();
		 PubParamInfo.videoInfoCache.clear();
	}


	@Override
	protected void onResume() {
		BaseActivity.mBaseActivity = this;
		super.onResume();
		SharedPreferences preference = null;
		preference = PreferenceManager.getDefaultSharedPreferences(this);
	}


	public boolean onKeyDown(int keyCode, KeyEvent event) {
		    super.onKeyDown(keyCode, event);
			if (keyCode == KeyEvent.KEYCODE_BACK&& event.getRepeatCount() == 0 ) {
				finish();
				isCick = false;
				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
				return true;
			}
			return false;
		}
	
	 public void testPullRead(){  
		   Map<String, String> group1 = new HashMap<String, String>();
		   List<Map<String, String>> child1 = new ArrayList<Map<String, String>>();
		   String videoNamePerson = "AV片";
		   group1.put("group", videoNamePerson);
		   mVideoList = new ArrayList<VideoInfo>();
	        for(int i=0;i < mArrayList.size();i++){
				 Map<String, String> group2 = new HashMap<String, String>();
				 VideoInfo videoInfo = new VideoInfo(); 
				 videoInfo.setUrl(mArrayList.get(i));
				 videoInfo.setTitle( "妹妹"+(i+1)+"害人！");
				 group2.put("child", "妹妹"+(i+1)+"害人！"); 
				 child1.add(group2) ;
				 mVideoList.add(videoInfo);
			 }
	        mVideoArrayLists.add(mVideoList);
	        mChilds.add(child1);
	        mGroups.add(group1);
	    }  
	    
		Handler mExitHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case IS_CLICK:
					isCick = false;
					break;
				case LOAD_LINK:
					if(mArrayList.size()>0){
						getExpandableListView().expandGroup(0);
					}
					Utils.dismissProgress();
					mAdapter.notifyDataSetChanged();
					break;
				case LOAD_LINK_FAIL:
					Utils.dismissProgress();
					Toast.makeText(TVActivity.this, "没有数据!", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}; 
}
