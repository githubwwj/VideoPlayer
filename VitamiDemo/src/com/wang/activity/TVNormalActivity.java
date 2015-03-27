package com.wang.activity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import com.wang.domain.VideoInfo;
import com.wang.util.BaseActivity;
import com.wang.util.PubParamInfo;
import com.wang.util.Utils;
import com.wang.video.VideoViewDemo;

public class TVNormalActivity extends ExpandableListActivity {
	
	 private static final int IS_CLICK = 1;
	 private static final int LOAD_LINK = 20;
	 private static final int LOAD_LINK_FAIL = 21;
	 private List<Map<String, String>> mGroups = new ArrayList<Map<String, String>>(); 
	 
	// 将二级条目放在一个集合里，供显示时使用
	 private List<List<Map<String, String>>> mChilds=new ArrayList<List<Map<String,String>>>();
 	 private ArrayList<ArrayList<VideoInfo>>  mVideoArrayLists=new ArrayList<ArrayList<VideoInfo>>();
     private ArrayList<VideoInfo> mVideoList;
	 private boolean isCick = false;
     private Button titleBarLeftButton = null; 
	 private Button titleBarRightButton = null;  
	 private SimpleExpandableListAdapter mAdapter;
	 
	 @Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState); 
			 setContentView(R.layout.tv_list_activity);
			 BaseActivity.mBaseActivity = this;
			 PubParamInfo.videoInfoCache.clear();
		 	 titleBarRightButton =(Button)findViewById(R.id.title_change_list); 
			 titleBarLeftButton =(Button)findViewById(R.id.title_search); 
		     titleBarLeftButton.setOnClickListener(listener); 
			 titleBarRightButton.setOnClickListener(listener);  
			 String jsonStr = Utils.readAssetsToString(TVNormalActivity.this, "tv.txt");
			 if(jsonStr==null){
				 Toast.makeText(this, "没有JSON数据", Toast.LENGTH_SHORT).show();
				 return ;
			 }
			 Utils.showProgress(this, null);
			 testPullRead(jsonStr); 
			 
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
	         mHandler.sendEmptyMessage(LOAD_LINK);
	 }
	 
	 private OnClickListener listener = new OnClickListener() { 
			public void onClick(View view) {
				int id = view.getId();
				switch (id) { 
				case R.id.title_search:
					TVNormalActivity.this.finish(); 
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
    		 if(Utils.isCheckNetAvailable(TVNormalActivity.this)){
 				if(Utils.getOSVersionSDKINT(TVNormalActivity.this)>=7){
 					if(!isCick){
 						 isCick = true;
 						 for(int kk=0;kk<videoinfo_array.size();kk++)
 							 PubParamInfo.videoInfoCache.add(videoinfo_array.get(kk));
 						 Intent intent = new Intent(TVNormalActivity.this,VideoViewDemo.class);
  						 intent.putExtra("CurrentPosInMediaIdList", childPosition);
  						 startActivity(intent);
  						 overridePendingTransition(R.anim.backzoomin, R.anim.backzoomout);
  						 mHandler.sendEmptyMessageDelayed(IS_CLICK,1000);
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

	    public void testPullRead(String jsonObject){  
	    	try {
				JSONObject jsonObject_1 = new JSONObject(jsonObject);
				String video = jsonObject_1.get("video").toString();
				if(video !=null){
					 JSONArray  jsonarray_video_person = jsonObject_1.getJSONArray("video");
					 for(int i=0;i < jsonarray_video_person.length();i++){
						 Map<String, String> group1 = new HashMap<String, String>();
						 List<Map<String, String>> child1 = new ArrayList<Map<String, String>>();
						 mVideoList = new ArrayList<VideoInfo>(); 
						 JSONObject jsonObject_video = (JSONObject)jsonarray_video_person.opt(i);
						 String videoNamePerson = jsonObject_video.getString("videoNamePerson");
						 group1.put("group", videoNamePerson);
						 JSONArray jsonObject_son = jsonObject_video.getJSONArray("videos");
						 for(int j= 0;j <jsonObject_son.length();j++ ){ 
							 JSONObject json = (JSONObject)jsonObject_son.opt(j); 
							 Map<String, String> group2 = new HashMap<String, String>();
							 VideoInfo videoInfo = new VideoInfo(); 
							 videoInfo.setUrl(json.getString("url"));
							 videoInfo.setTitle( json.getString("title"));
							 group2.put("child", json.getString("title")); 
							 child1.add(group2) ;
							 mVideoList.add(videoInfo);
						 }
						 mChilds.add(child1);
						 mGroups.add(group1);
						 mVideoArrayLists.add(mVideoList);
					 }
				} 
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    }  
	
	    
		Handler mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case IS_CLICK:
					isCick = false;
					break;
				case LOAD_LINK:
					if(mVideoArrayLists.size()>0){
						getExpandableListView().expandGroup(0);
					}
					Utils.dismissProgress();
					mAdapter.notifyDataSetChanged();
					break;
				case LOAD_LINK_FAIL:
					Utils.dismissProgress();
					Toast.makeText(TVNormalActivity.this, "没有数据!", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}; 
}
