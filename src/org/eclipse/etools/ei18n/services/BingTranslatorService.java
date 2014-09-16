package org.eclipse.etools.ei18n.services;

import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.preferences.EI18NPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

import com.memetix.mst.MicrosoftTranslatorAPI;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class BingTranslatorService implements ITranslatorService {
    public BingTranslatorService() {
        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        MicrosoftTranslatorAPI.setClientId(store.getString(EI18NPreferenceConstants.BING_CLIENT_ID));
        MicrosoftTranslatorAPI.setClientSecret(store.getString(EI18NPreferenceConstants.BING_SECRET_KEY));
    }

    public String translate(String text, String fromLocale, String toLocale) throws Exception {
        for (Language lang : Language.values()) {
            if (lang.toString().equals(toLocale))
                return Translate.execute(text, lang);
        }
        return null;
    }
}
