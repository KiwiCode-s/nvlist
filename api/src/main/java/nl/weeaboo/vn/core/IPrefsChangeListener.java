package nl.weeaboo.vn.core;

import nl.weeaboo.prefsstore.IPreferenceStore;

/**
 * Event listener for {@link IPreferenceStore}.
 */
public interface IPrefsChangeListener {

    /**
     * Called when the user preferences have changed.
     *
     * @param config An object containing the user preferences.
     */
    void onPrefsChanged(IPreferenceStore config);

}
