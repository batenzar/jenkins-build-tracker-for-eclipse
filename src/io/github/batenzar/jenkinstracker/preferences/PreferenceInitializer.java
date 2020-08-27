package io.github.batenzar.jenkinstracker.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import io.github.batenzar.jenkinstracker.Activator;

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
		store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		store.setDefault(PreferenceConstants.P_STRING,
				"Default value");
		store.setDefault(PreferenceConstants.URL01,
				"example.url.01");
		store.setDefault(PreferenceConstants.URL02,
				"example.url.02");
		store.setDefault(PreferenceConstants.URL03,
				"example.url.03");
	}

}
