package com.wang.ui.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wang.util.Utils;

//遍历某一个盘里所有文件及文件夹及子目录下的所有文件 
public class FileList {
	static	List<File> arrayListFiles=new ArrayList<File>();
	static File[] fileList;
	
	
	public static void fileList(File file) {
		// File类型的数组fl用来存储盘里的文件
		File[] fl = file.listFiles();
		if(fl!=null){
			for (int i = 0; i < fl.length; i++) {
				if (fl[i].isFile()&&Utils.isMediaFile(fl[i].getAbsolutePath())) {
					arrayListFiles.add(fl[i]);
				}
				if (fl[i].isDirectory()) {
						fileList(fl[i]);
				}
			}
		}
	}
	
	/*
	 * 数组文件是有序排列的（只有多媒体）
	 */
	public static File[] getFileLists(){
		if(arrayListFiles.size()>0){
			Collections.sort(arrayListFiles,null);
			fileList=new File[arrayListFiles.size()];
			for(int i=0;i<arrayListFiles.size();i++)
				fileList[i]=arrayListFiles.get(i);
			arrayListFiles.clear();
		}
		return fileList;
	}
	
}