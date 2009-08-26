package com.ai.evan.easycopy.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ai.evan.easycopy.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PATH, "c:\\tmp");

		store.setDefault(PreferenceConstants.P_SOURCE_DIRS, "src");
		store.setDefault(PreferenceConstants.P_OUTPUT_DIRS, "classes:bin");

		store.setDefault(PreferenceConstants.P_GENERATE_LOG, true);
		store.setDefault(PreferenceConstants.P_INFORM_ME, true);

		store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		store.setDefault(PreferenceConstants.P_STRING, "Default value");
	}
}
