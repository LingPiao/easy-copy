/**********************************************************************
 *         File: A.java
 *      Creator: Evan.Kong
 *         Date: Apr 7, 2009
 *  Description: 
 *               File Desc.
 *              
 *
 * MODIFICATION DESCRIPTION
 *      
 * Name                 Date                Description 
 * ============         ============        ============
 * Evan.Kong			Apr 7, 2009			Created
 * 
 * *********************************************************************/
package com.ai.tools.easycopy.test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Pattern;

public class DirList {

	public static void main(String[] args) {
		File path = new File("C:/TEMP/VIDTEST/a");
		String[] list;
		list = path.list(new DirFilter("CorbaTemplate\\$.*\\.class"));
		Arrays.sort(list);
		for (int i = 0; i < list.length; i++) {
			System.out.println(i + ": " + list[i]);
		}
	}

}

class DirFilter implements FilenameFilter {

	private Pattern pattern;

	public DirFilter(String reg) {
		pattern = Pattern.compile(reg);
	}

	public boolean accept(File dir, String name) {
		return pattern.matcher(new File(name).getName()).matches();
	}

}