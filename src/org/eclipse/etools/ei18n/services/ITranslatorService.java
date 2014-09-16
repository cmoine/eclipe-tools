package org.eclipse.etools.ei18n.services;


public interface ITranslatorService {
    public String translate(String text, String fromLocale, String toLocale) throws Exception;
}
