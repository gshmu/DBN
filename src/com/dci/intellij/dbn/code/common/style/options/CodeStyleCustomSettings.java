package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.common.options.CompositeConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public abstract class CodeStyleCustomSettings<T extends CompositeConfigurationEditorForm> extends CompositeConfiguration<T>{
    protected CodeStyleCaseSettings caseSettings;
    protected CodeStyleFormattingSettings formattingSettings;

    protected CodeStyleCustomSettings() {
        caseSettings = createCaseSettings();
        formattingSettings = createAttributeSettings();
    }

    protected abstract CodeStyleCaseSettings createCaseSettings();
    protected abstract CodeStyleFormattingSettings createAttributeSettings();

    public CodeStyleCaseSettings getCaseSettings() {
        return caseSettings;
    }

    public CodeStyleFormattingSettings getFormattingSettings() {
        return formattingSettings;
    }

    /*********************************************************
    *                     Configuration                     *
    *********************************************************/
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                caseSettings,
                formattingSettings};
    }

    protected abstract String getElementName();

    public void readConfiguration(Element element) throws InvalidDataException {
        Element child = element.getChild(getElementName());
        if (child != null) {
            readConfiguration(child, caseSettings);
            readConfiguration(child, formattingSettings);
        }
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
         Element child = new Element(getElementName());
         element.addContent(child);
         writeConfiguration(child, caseSettings);
         writeConfiguration(child, formattingSettings);
     }


}
