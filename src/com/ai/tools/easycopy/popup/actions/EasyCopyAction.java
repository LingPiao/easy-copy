package com.ai.tools.easycopy.popup.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ai.tools.easycopy.Activator;
import com.ai.tools.easycopy.preferences.PreferenceConstants;

public class EasyCopyAction implements IObjectActionDelegate {

	private static final String JAVA_FILE_SEPARATOR = "/";

	private Shell shell;

	private List<IAdaptable> selectlist;

	private static String JAVA_EXT = ".java";

	private static String CLASS_EXT = ".class";

	// private static String GENERAL_CLASS_DIR_NAME = "classes";

	private String sourceDir = "";

	private String outputDir = "";

	private String dstDir = "";

	private String projectName = "";

	private List<String> copiedSorcesList = new ArrayList<String>();

	private List<String> copiedClassesList = new ArrayList<String>();

	IPreferenceStore store = null;

	/**
	 * Constructor for Action1.
	 */
	public EasyCopyAction() {
		super();
		store = Activator.getDefault().getPreferenceStore();
		dstDir = store.getString(PreferenceConstants.P_PATH) + JAVA_FILE_SEPARATOR;

	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		int selectedCount = 0;
		int copiedCount = 0;

		if (!getProjectInfo(selectlist)) {
			MessageDialog.openInformation(shell, "EasyCopy Plug-in", "Can not copy,Please check your settings  !");
			return;
		}

		if (selectlist != null) {

			if (store.getBoolean(PreferenceConstants.P_OVERWRITE)) {
				removeExistedDir(new File(this.dstDir + JAVA_FILE_SEPARATOR + this.projectName));
			}

			StringBuffer sb = new StringBuffer("Selected File List:\r\n");
			sb.append("================================================================\r\n");

			for (IAdaptable e : selectlist) {
				if (copyClass(getDir(e))) {
					selectedCount++;
					sb.append(processIAdaptable(e)).append("\r\n");
				}
			}
			sb.append("\r\n================================================================\r\n");
			sb.append(selectedCount + " Files was selected!\r\n\r\n");
			if (store.getBoolean(PreferenceConstants.P_GENERATE_LOG)) {
				// Need to generate the file report.
				try {
					OutputStream out = new FileOutputStream(this.dstDir + JAVA_FILE_SEPARATOR + this.projectName + ".log");
					PrintStream ps = new PrintStream(out);
					ps.print(sb.toString());

					if (store.getBoolean(PreferenceConstants.P_COPY_CLASSES)) {
						ps.print(getCopiedFileList(copiedClassesList));
						copiedCount = copiedClassesList.size();
					} else {
						ps.print(getCopiedFileList(copiedSorcesList));
						copiedCount = copiedSorcesList.size();
					}

					ps.close();
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (store.getBoolean(PreferenceConstants.P_INFORM_ME)) { 
			//Showing the inform information
			MessageDialog.openInformation(shell, "EasyCopy Plug-in", selectedCount + " Files was selected, " + copiedCount + " Files was copied!");
		}
	}

	private String getCopiedFileList(List<String> copiedList) {
		StringBuffer sb = new StringBuffer("Copied File List:\r\n");
		sb.append("================================================================\r\n");

		for (String f : copiedList) {
			sb.append(f).append("\r\n");
		}
		sb.append("\r\n================================================================\r\n");
		sb.append(copiedList.size() + " Files was Copied!\r\n\r\n");
		return sb.toString();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			selectlist = ((IStructuredSelection) selection).toList();
	}

	private boolean removeExistedDir(File path) {
		if (path.exists()) {
			File[] fs = path.listFiles();
			for (int i = 0; i < fs.length; i++) {
				if (fs[i].isDirectory()) {
					removeExistedDir(fs[i]);
				} else {
					fs[i].delete();
				}
			}
		}
		return path.delete();
	}

	private String processIAdaptable(IAdaptable element) {
		String path = "";
		Object selected = getSelected(element);

		if (selected instanceof IResource)
			path = ((IResource) selected).getFullPath().toString();
		else if (selected instanceof File)
			path = ((File) selected).getPath();
		return path;
	}

	/**
	 * 根据选择的文件进行工程信息的检测和设置 主要设置内容：1，工程名称 2，源文件目录 3，类文件目录
	 * 
	 * @param l
	 *            选择条目列表
	 * @return 检测设置成功与否
	 */
	private boolean getProjectInfo(List<IAdaptable> l) {
		if (l == null) {
			return false;
		}
		String[] srcDirs = store.getString(PreferenceConstants.P_SOURCE_DIRS).split(":");
		String[] outDirs = store.getString(PreferenceConstants.P_OUTPUT_DIRS).split(":");

		String a = "";
		IAdaptable e = l.get(0);
		a = processIAdaptable(e);
		projectName = a.substring(1, a.indexOf(JAVA_FILE_SEPARATOR, 2));
		File f = getDir(e);
		String pathTempStr = f.toString();
		String projAbsPath = pathTempStr.substring(0, pathTempStr.toLowerCase().indexOf(projectName.toLowerCase()) + projectName.length() + 1);
		// 判断源文件目录
		for (int i = 0; i < srcDirs.length; i++) {
			// 表示源文件与class同目录
			if (srcDirs[i].equals(".")) {
				sourceDir = ".";
				break;
			}
			File testDir = new File(projAbsPath + srcDirs[i]);
			if (testDir.exists()) {
				sourceDir = srcDirs[i];
				break;
			}
		}
		if (sourceDir.length() < 1) {
			MessageDialog.openInformation(shell, "EasyCopy Plug-in", "Source Dir not found! ProjectPath=" + projAbsPath);
			return false;
		}

		// 判断类输出目录
		for (int i = 0; i < outDirs.length; i++) {
			String tmp = outDirs[i];
			if ("webapp".equalsIgnoreCase(tmp) || "webroot".equalsIgnoreCase(tmp)) {
				tmp = tmp + "/WEB-INF/classes";
			}
			if (".".equals(tmp)) {
				tmp = "WEB-INF/classes";
			}
			File testDir = new File(projAbsPath + tmp);
			if (testDir.exists()) {
				outputDir = tmp;
				break;
			}
		}
		if (outputDir.length() < 1) {
			MessageDialog.openInformation(shell, "EasyCopy Plug-in", "Classes Dir not found! ProjectPath=" + projAbsPath);
			return false;
		}

		if (sourceDir.equals(".")) {
			sourceDir = outputDir;
		}

		return true;
	}

	private File getDir(IAdaptable element) {
		Object selected = getSelected(element);
		if (selected == null)
			return null;

		File directory = null;
		if (selected instanceof IResource)
			directory = new File(((IResource) selected).getLocation().toOSString());
		else {
			directory = (File) selected;
		}

		return directory;
	}

	private Object getSelected(IAdaptable element) {
		Object selected = null;
		if (element instanceof IResource)
			selected = (IResource) element;
		else
			selected = (IResource) element.getAdapter(org.eclipse.core.resources.IResource.class);
		return selected;
	}

	private boolean copyClass(File f) {
		boolean r = false;
		String absFilePath = f.toString().replaceAll("\\\\", JAVA_FILE_SEPARATOR);

		String fileName = f.getName();
		if (fileName.endsWith(JAVA_EXT) && store.getBoolean(PreferenceConstants.P_COPY_CLASSES)) { // 是java文件且只复制classes文件
			String classFileName = fileName.replace(JAVA_EXT, CLASS_EXT);
			String classPath = this.outputDir + JAVA_FILE_SEPARATOR
					+ absFilePath.substring(absFilePath.indexOf(sourceDir) + sourceDir.length() + 1, absFilePath.indexOf(fileName));
			String dstPath = this.dstDir + JAVA_FILE_SEPARATOR + this.projectName + JAVA_FILE_SEPARATOR;

			String srcClass = absFilePath.replace(this.sourceDir, this.outputDir).replace(fileName, classFileName);

			File dstClassDir = new File(dstPath + classPath);
			dstClassDir.mkdirs();
			File dstClassFile = new File(dstPath + classPath + classFileName);
			r = fileCopy(new File(srcClass), dstClassFile);

			if (r) {
				copiedClassesList.add(dstClassFile.toString());
			}

			// 如下copy匿名类,比如像:Test$1.class,Test$1UsableFeeCall.class之类的
			String pathStr = srcClass.substring(0, srcClass.indexOf(classFileName));
			File path = new File(pathStr);
			String[] list = path.list(new DirFilter(fileName.substring(0, fileName.indexOf(".")) + "\\$.*\\.class"));
			Arrays.sort(list);
			for (int i = 0; i < list.length; i++) {
				File dstInerClass = new File(dstPath + classPath + list[i]);
				r = fileCopy(new File(pathStr + list[i]), dstInerClass);
				if (r) {
					copiedClassesList.add(dstInerClass.toString());
				}
			}

		} else {
			String dstPath = this.dstDir + JAVA_FILE_SEPARATOR + this.projectName;
			String dstFileDirStr = absFilePath.substring(absFilePath.toLowerCase().indexOf(this.projectName.toLowerCase()) + this.projectName.length(),
					absFilePath.indexOf(fileName));
			File dstFileDir = new File(dstPath + dstFileDirStr);
			dstFileDir.mkdirs();
			File dstFile = new File(dstPath + dstFileDirStr + fileName);
			r = fileCopy(f, dstFile);
			if (r) {
				copiedSorcesList.add(dstFile.toString());
			}
		}

		return r;

	}

	private boolean fileCopy(File f1, File newfile) {
		boolean r = false;
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(f1);
			out = new FileOutputStream(newfile);
			byte[] buff = new byte[1023];
			int i = 0;
			while ((i = in.read(buff)) != -1) {
				out.write(buff, 0, i);
			}
			out.close();
			in.close();
			r = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return r;
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
