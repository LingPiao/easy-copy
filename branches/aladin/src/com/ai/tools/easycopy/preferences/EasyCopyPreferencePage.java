package com.ai.tools.easycopy.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import com.ai.tools.easycopy.Activator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class EasyCopyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public EasyCopyPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Gerneral settings for Easy Copy.");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, "&Directory :", getFieldEditorParent()));

		// Source and Output folder
		//addField(new StringFieldEditor(PreferenceConstants.P_SOURCE_DIRS, "General &Source folder names:", getFieldEditorParent()));
		//addField(new StringFieldEditor(PreferenceConstants.P_OUTPUT_DIRS, "General &Output folder names:", getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceConstants.P_COPY_CLASSES, "Copy &Classes,otherwise copy the sources", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_OVERWRITE, "Overwrite &Existing files", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_GENERATE_LOG, "&Generate log", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_INFORM_ME, "&Inform me", getFieldEditorParent()));
		

		// addField(new RadioGroupFieldEditor(PreferenceConstants.P_CHOICE, "An
		// example of a multiple-choice preference", 1, new String[][] {
		// { "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } },
		// getFieldEditorParent(), true));
		// addField(new StringFieldEditor(PreferenceConstants.P_STRING, "A &text
		// preference:", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}