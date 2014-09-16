package org.eclipse.etools.ei18n.extensions;

import org.eclipse.etools.ei18n.services.ITranslatorService;

public class TranslatorExtension {
    private final String id;
    private final String name;
    private final String description;
    private final ITranslatorService translatorService;

    public TranslatorExtension(String id, String name, String description, ITranslatorService translatorService) {
        this.id=id;
        this.name=name;
        this.description=description;
        this.translatorService=translatorService;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ITranslatorService getTranslatorService() {
        return translatorService;
    }
}
