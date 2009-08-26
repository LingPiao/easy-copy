package com.ai.evan.easycopy.popup.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Test {

	public static void main(String[] args) {

		File a = new File("F:/runtime-EclipseApplication/asdfasd/src/com/aa/dd/a.java");
		String b = a.toString();

		String fileName = a.getName();
		String classFileName = fileName.replace(".java", ".class");
		String classPath = "bin/" + b.substring(b.indexOf("src") + 4, b.indexOf(fileName)).replace("\\", "/");
		String dstPaht = "c:/tmp/";

		String srcClass = b.replace("src", "bin").replace(fileName, classFileName);

		File dstClassDir = new File(dstPaht + classPath);
		dstClassDir.mkdirs();
		File dstClassFile = new File(dstPaht + classPath + classFileName);
		fileCopy(new File(srcClass), dstClassFile);

		System.out.println(classPath + "\n" + srcClass);

	}

	private static void fileCopy(File f1, File newfile) {
		try {
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(newfile);
			byte[] buff = new byte[1023];
			int i = 0;
			while ((i = in.read(buff)) != -1) {
				out.write(buff, 0, i);
			}
			out.close();
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
