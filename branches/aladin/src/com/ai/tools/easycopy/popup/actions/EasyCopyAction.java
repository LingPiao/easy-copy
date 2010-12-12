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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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

	// private String sourceDir = "";
	//
	// private String outputDir = "";
	//
	private String dstDir = "";

	private String projectRawLocation = "";
	private String defaultOutput = "";
	private String projectName = "";
	private Map<String, String> srcClassMap = new HashMap<String, String>();

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

		if (!getProjectInfo(selectlist)) {
			// MessageDialog.openInformation(shell, "EasyCopy Plug-in",
			// "Can not copy,Please check your settings  !");
			return;
		}
		if (selectlist == null)
			return;

		if (store.getBoolean(PreferenceConstants.P_OVERWRITE)) {
			removeExistedDir(new File(this.dstDir + JAVA_FILE_SEPARATOR + this.projectName));
		}

		StringBuffer sb = new StringBuffer("");
		selectedCount = doCopy(selectedCount, sb);
		int copiedCount = writeReport(selectedCount, sb);

		// Showing the inform information
		if (store.getBoolean(PreferenceConstants.P_INFORM_ME)) {
			MessageDialog.openInformation(shell, "EasyCopy Plug-in", selectedCount + " Files was selected, " + copiedCount + " Files was copied!");
		}

		cleanMaps();
	}

	private int doCopy(int selectedCount, StringBuffer sb) {
		sb.append("Selected File List:\r\n");
		sb.append("================================================================\r\n");
		for (IAdaptable e : selectlist) {
			if (easyCopy(e)) {
				selectedCount++;
				sb.append(processIAdaptable(e)).append("\r\n");
			}
		}
		sb.append("\r\n================================================================\r\n");
		return selectedCount;
	}

	private int writeReport(int selectedCount, StringBuffer sb) {
		int copiedCount = 0;
		sb.append(selectedCount + " Files was selected!\r\n\r\n");
		if (store.getBoolean(PreferenceConstants.P_GENERATE_LOG)) {
			// Need to generate the file report.
			try {
				OutputStream out = new FileOutputStream(this.dstDir + JAVA_FILE_SEPARATOR + this.projectName + ".log");
				PrintStream ps = new PrintStream(out);
				copiedCount = getCopiedFileList(sb);
				ps.print(sb.toString());
				ps.close();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return copiedCount;
	}

	private void cleanMaps() {
		copiedSorcesList.clear();
		copiedClassesList.clear();
	}

	private int getCopiedFileList(StringBuffer sb) {

		sb.append("Copied File List:\r\n");
		sb.append("================================================================\r\n");

		for (String f : copiedSorcesList) {
			sb.append(f).append("\r\n");
		}

		for (String f : copiedClassesList) {
			sb.append(f).append("\r\n");
		}
		sb.append("\r\n================================================================\r\n");
		int t = copiedSorcesList.size() + copiedClassesList.size();
		sb.append(t + " Files was Copied!\r\n\r\n");
		return t;
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
		IResource selected = getSelected(element);

		if (selected instanceof IResource)
			path = ((IResource) selected).getFullPath().toString();
		else if (selected instanceof File)
			path = ((File) selected).getPath();
		return path;
	}

	/**
	 * Check and set the project information: projectName,projectRawLocation and
	 * defaultOutput
	 * 
	 * @param l
	 * @return
	 */
	private boolean getProjectInfo(List<IAdaptable> l) {
		if (l == null) {
			return false;
		}

		IResource resource = getSelected(l.get(0));
		IProject project = resource.getProject();
		projectName = project.getName();
		projectRawLocation = project.getRawLocation().toString();

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null || !javaProject.exists()) {
			MessageDialog.openInformation(shell, "EasyCopy Plug-in", "It is not a Java Project!");
			return false;
		}
		try {
			defaultOutput = removeProjectName(javaProject.getOutputLocation().toString());
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}

		IClasspathEntry[] dpc = javaProject.readRawClasspath();
		for (IClasspathEntry e : dpc) {
			if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				srcClassMap.put(removeProjectName(e.getPath()), getOutputLocation(e.getOutputLocation()));
			}
		}

		if (defaultOutput.length() < 1 || projectName.length() < 1 || srcClassMap.size() < 1) {
			MessageDialog.openInformation(shell, "EasyCopy Plug-in", "Sorry, I can not do EasyCopy because of the incorrect configuration for this project!");
			return false;
		}

		return true;
	}

	private String getOutputLocation(IPath p) {
		if (p == null) {
			return defaultOutput;
		}
		return removeProjectName(p);
	}

	/**
	 * 
	 Trim the first string: /projectName/
	 * 
	 * @param p
	 * @return
	 */
	private String removeProjectName(IPath p) {
		return removeProjectName(p.toString());
	}

	private String removeProjectName(String p) {
		return p.substring(projectName.length() + 2);
	}

	private File getDir(IAdaptable element) {
		IResource selected = getSelected(element);
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

	private IResource getSelected(IAdaptable element) {
		IResource selected = null;
		if (element instanceof IResource)
			selected = (IResource) element;
		else
			selected = (IResource) element.getAdapter(org.eclipse.core.resources.IResource.class);
		return selected;
	}

	private boolean easyCopy(IAdaptable element) {

		boolean r = false;
		File sourceFile = getDir(element);

		String fileName = sourceFile.getName();
		// Java file && need to copy the class file
		if (fileName.endsWith(JAVA_EXT) && store.getBoolean(PreferenceConstants.P_COPY_CLASSES)) {
			r = copyClass(element);
		} else {
			String targetFilePath = processIAdaptable(element);
			r = copySource(sourceFile, targetFilePath);
		}

		return r;

	}

	private String getClassFilePath(String rawFile, String src, String output) {
		return rawFile.replace(src, output).replace(JAVA_EXT, CLASS_EXT);
	}

	private boolean copyClass(IAdaptable element) {

		boolean r = false;

		String rawFile = removeProjectName(processIAdaptable(element));
		String output = "";
		String src = "";
		for (String key : srcClassMap.keySet()) {
			if (rawFile.indexOf(key) >= 0) {
				output = srcClassMap.get(key);
				src = key;
			}
		}

		String classFilePath = getClassFilePath(rawFile, src, output);

		File cf = new File(projectRawLocation + JAVA_FILE_SEPARATOR + classFilePath);
		if (!cf.exists()) {
			return false;
		}

		String dstClass = dstDir + JAVA_FILE_SEPARATOR + projectName + JAVA_FILE_SEPARATOR;
		String classFilePathWithoutName = classFilePath.substring(0, classFilePath.lastIndexOf(JAVA_FILE_SEPARATOR));

		File dstClassDir = new File(dstClass + classFilePathWithoutName);
		dstClassDir.mkdirs();

		File dstClassFile = new File(dstClass + classFilePath);

		r = fileCopy(cf, dstClassFile);

		if (r) {
			copiedClassesList.add(dstClassFile.toString());
		}

		// Copy the inner classes
		String fileName = cf.getName();
		File path = new File(projectRawLocation + JAVA_FILE_SEPARATOR + classFilePathWithoutName);
		String[] list = path.list(new DirFilter(fileName.substring(0, fileName.indexOf(".")) + "\\$.*\\.class"));
		Arrays.sort(list);
		for (int i = 0; i < list.length; i++) {
			File dstInerClass = new File(dstClass + classFilePathWithoutName + JAVA_FILE_SEPARATOR + list[i]);
			r = fileCopy(new File(projectRawLocation + JAVA_FILE_SEPARATOR + classFilePathWithoutName + JAVA_FILE_SEPARATOR + list[i]), dstInerClass);
			if (r) {
				copiedClassesList.add(dstInerClass.toString());
			}
		}
		return r;
	}

	private boolean copySource(File sourceFile, String targetFilePath) {
		boolean r;
		String dstPath = this.dstDir + JAVA_FILE_SEPARATOR + targetFilePath;
		String fileName = sourceFile.getName();
		String dstFileDirStr = this.dstDir + JAVA_FILE_SEPARATOR + targetFilePath.substring(0, targetFilePath.indexOf(fileName));
		File dstFileDir = new File(dstFileDirStr);
		dstFileDir.mkdirs();
		File dstFile = new File(dstPath);
		r = fileCopy(sourceFile, dstFile);
		if (r) {
			copiedSorcesList.add(dstFile.toString());
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
