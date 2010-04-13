/**********************************************************************
 *         File: Test.java
 *      Creator: Evan.Kong
 *         Date: Dec 7, 2008
 *  Description: 
 *               File Desc.
 *              
 *
 * MODIFICATION DESCRIPTION
 *      
 * Name                 Date                Description 
 * ============         ============        ============
 * Evan.Kong			Dec 7, 2008			Created
 * 
 * *********************************************************************/
package com.ai.tools.easycopy.test;

import java.io.File;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String a = "F:/runtime-EclipseApplication/asdfasd/src/com/aa/dd/a.java";
		
		System.out.println(a.substring(a.indexOf("asdfasd")+"asdfasd".length(),a.indexOf("a.java")));
		
		String projectName="RTM";
		String pathTempStr="D:/projects/work/dev/products/boss15/aiccs/12580/1.0/RTM/WEB-INF/classes/net/emice/rtm/shmc/cmb/RtmLogHome.java";
		
		String projAbsPath = pathTempStr.substring(0, pathTempStr.indexOf(projectName) + projectName.length() + 1);
		System.out.println(projAbsPath);
		
		File testDir = new File("D:\\projects\\work\\dev\\products\\boss15\\aiccs\\12580\\1.0\\RTM\\WEB-INF\\classes");
		System.out.println(testDir.exists());
		
		String fs = testDir.toString();
		String b = "RTM/WEB-INF/classes";
		b=b.replace("/", "\\");
		
		System.out.println(fs.indexOf(b));
		
		
	}

}
