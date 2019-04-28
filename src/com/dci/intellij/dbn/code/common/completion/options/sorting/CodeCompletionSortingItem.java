package com.dci.intellij.dbn.code.common.completion.options.sorting;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CodeCompletionSortingItem extends BasicConfiguration<CodeCompletionSortingSettings, ConfigurationEditorForm> {
    private DBObjectType objectType;
    private TokenTypeCategory tokenTypeCategory = TokenTypeCategory.UNKNOWN;

    CodeCompletionSortingItem(CodeCompletionSortingSettings parent) {
        super(parent);
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public TokenTypeCategory getTokenTypeCategory() {
        return tokenTypeCategory;
    }

    public String getTokenTypeName() {
        return tokenTypeCategory.getName();
    }

    public boolean equals(Object obj) {
        if (obj instanceof CodeCompletionSortingItem) {
            CodeCompletionSortingItem remote = (CodeCompletionSortingItem) obj;
            return
                remote.objectType == objectType &&
                remote.tokenTypeCategory == tokenTypeCategory;
        }
        return false;
    }

    public String toString() {
        return objectType == null ? tokenTypeCategory.getName() : objectType.getName();
    }

    @Override
    @Nls
    public String getDisplayName() {
        return null;
    }

    /*********************************************************
     *                      Configuration                    *
     *********************************************************/
    @Override
    @NotNull
    public ConfigurationEditorForm createConfigurationEditor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConfigElementName() {
        return "sorting-element";
    }

    @Override
    public void readConfiguration(Element element) {
        String sortingItemType = element.getAttributeValue("type");
        if (sortingItemType.equals("OBJECT")) {
            String objectTypeName = element.getAttributeValue("id");
            objectType = DBObjectType.get(objectTypeName);
        } else {
            String tokenTypeName = element.getAttributeValue("id");
            tokenTypeCategory = TokenTypeCategory.getCategory(tokenTypeName);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        if (objectType != null) {
            element.setAttribute("type", "OBJECT");
            element.setAttribute("id", objectType.getName());
        } else {
            element.setAttribute("type", "RESERVED_WORD");
            element.setAttribute("id", tokenTypeCategory.getName());
        }
    }
}
