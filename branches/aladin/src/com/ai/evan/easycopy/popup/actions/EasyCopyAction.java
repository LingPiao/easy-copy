package com.ai.evan.easycopy.popup.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ai.evan.easycopy.Activator;
import com.ai.evan.easycopy.preferences.PreferenceConstants;

public class EasyCopyAction implements IObjectActionDelegate {

	private Shell shell;

	private List selectlist;

	/**
	 * Constructor for Action1.
	 */
	public EasyCopyAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// StringBuffer sb = new StringBuffer();
		int cnt = 0;
		if (selectlist != null) {
			// String str = "";
			// for (Iterator iter = selectlist.iterator(); iter.hasNext();
			// sb.append((new
			// StringBuilder(String.valueOf(str))).append("\n").toString())) {
			// IAdaptable element = (IAdaptable) iter.next();
			// str = processIAdaptable(element);
			// File a = getDir(element);
			// MessageDialog.openInformation(shell, "aaaaaa",
			// "New Action was executed!:===" + a.toString());
			// }

			for (Iterator it = selectlist.iterator(); it.hasNext();) {
				IAdaptable e = (IAdaptable) it.next();
				if (copyClass(getDir(e)))
					cnt++;
			}

			// java.awt.datatransfer.Clipboard clipboard =
			// java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
			// java.awt.datatransfer.StringSelection out = new
			// java.awt.datatransfer.StringSelection(sb.toString());
			// clipboard.setContents(out, null);
		}

		if (store.getBoolean(PreferenceConstants.P_INFORM_ME)) {
			MessageDialog.openInformation(shell, "EasyCopy Plug-in", cnt + " Files was copied!");
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			selectlist = ((IStructuredSelection) selection).toList();
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

	protected File getJarFile(IAdaptable adaptable) {
		JarPackageFragmentRoot jpfr = (JarPackageFragmentRoot) adaptable;
		File selected = jpfr.getPath().makeAbsolute().toFile();
		if (!selected.exists()) {
			File projectFile = new File(jpfr.getJavaProject().getProject().getLocation().toOSString());
			selected = new File((new StringBuilder(String.valueOf(projectFile.getParent()))).append(selected.toString()).toString());
		}
		return selected;
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
		String absFilePath = f.toString();

		String fileName = f.getName();
		String classFileName = fileName.replace(".java", ".class");
		String classPath = "bin/" + absFilePath.substring(absFilePath.indexOf("src") + 4, absFilePath.indexOf(fileName)).replace("\\", "/");
		String dstPaht = "c:/tmp/";

		String srcClass = absFilePath.replace("src", "bin").replace(fileName, classFileName);

		File dstClassDir = new File(dstPaht + classPath);
		dstClassDir.mkdirs();
		File dstClassFile = new File(dstPaht + classPath + classFileName);
		r = fileCopy(new File(srcClass), dstClassFile);

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
