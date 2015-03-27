package com.wang.ui.util;

import java.io.File;
import java.io.FileFilter;
public class MyFileFilter implements FileFilter {

//	/*
//	 * 只要是多媒体文件我都要
//	 */
//	@Override
//	public boolean accept(File pathname) {
//		if (Utils.isMediaFile(pathname.getAbsolutePath())) {
//			return true;
//		} else {
//			return false;
//		}
//	}
	/*
	 * 只要文件不以点开头我都要
	 * (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File pathname) {
		if (!pathname.getName().startsWith(".")) {
			return true;
		} else {
			return false;
		}
	}

}