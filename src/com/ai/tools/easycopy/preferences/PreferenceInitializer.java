package com.ai.tools.easycopy.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ai.tools.easycopy.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PATH, "c:\\temp");

		store.setDefault(PreferenceConstants.P_SOURCE_DIRS, "src");
		store.setDefault(PreferenceConstants.P_OUTPUT_DIRS, "bin:webapp:WebRoot:classes");

		store.setDefault(PreferenceConstants.P_COPY_CLASSES, true);
		store.setDefault(PreferenceConstants.P_OVERWRITE, true);
		store.setDefault(PreferenceConstants.P_GENERATE_LOG, true);
		store.setDefault(PreferenceConstants.P_INFORM_ME, true);

		// store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		// store.setDefault(PreferenceConstants.P_STRING, "Default value");
	}

}
