package com.dci.intellij.dbn.code.common.completion.options.sorting;

import com.dci.intellij.dbn.code.common.completion.options.sorting.ui.CodeCompletionSortingSettingsForm;
import com.dci.intellij.dbn.code.common.lookup.AliasLookupItemFactory;
import com.dci.intellij.dbn.code.common.lookup.DBObjectLookupItemFactory;
import com.dci.intellij.dbn.code.common.lookup.LookupItemFactory;
import com.dci.intellij.dbn.code.common.lookup.TokenLookupItemFactory;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class CodeCompletionSortingSettings extends Configuration<CodeCompletionSortingSettingsForm> {
    private boolean enabled = true;
    private List<CodeCompletionSortingItem> sortingItems = new ArrayList<CodeCompletionSortingItem>();

    public int getSortingIndexFor(LookupItemFactory lookupItemFactory) {
        if (lookupItemFactory instanceof AliasLookupItemFactory) {
            return -1;
        }
        if (lookupItemFactory instanceof DBObjectLookupItemFactory) {
            DBObjectLookupItemFactory objectLookupItemFactory = (DBObjectLookupItemFactory) lookupItemFactory;
            DBObjectType objectType = objectLookupItemFactory.getObject().getObjectType();
            return getSortingIndexFor(objectType);
        }

        if (lookupItemFactory instanceof TokenLookupItemFactory) {
            TokenLookupItemFactory tokenLookupItemFactory = (TokenLookupItemFactory) lookupItemFactory;
            TokenTypeCategory tokenTypeCategory = tokenLookupItemFactory.getTokenTypeCategory();
            return getSortingIndexFor(tokenTypeCategory);
        }
        return 0;
    }

    public int getSortingIndexFor(DBObjectType objectType) {
        for (int i=0; i<sortingItems.size(); i++) {
            if (sortingItems.get(i).getObjectType() == objectType) {
                return sortingItems.size() - i;
            }
        }
        return 0;
    }

    public int getSortingIndexFor(TokenTypeCategory tokenTypeCategory) {
        for (int i=0; i<sortingItems.size(); i++) {
            if (sortingItems.get(i).getTokenTypeCategory() == tokenTypeCategory) {
                return sortingItems.size() - i;
            }
        }
        return 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }


    public List<CodeCompletionSortingItem> getSortingItems() {
        return sortingItems;
    }

    public String getDisplayName() {
        return "Code completion sorting";
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public CodeCompletionSortingSettingsForm createConfigurationEditor() {
        return new CodeCompletionSortingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "sorting";
    }

    public void readConfiguration(Element element) {
        enabled = SettingsUtil.getBooleanAttribute(element, "enabled", enabled);
        for (Object child : element.getChildren()) {
            Element childElement = (Element) child;
            CodeCompletionSortingItem sortingItem = new CodeCompletionSortingItem();
            sortingItem.readConfiguration(childElement);
            if (sortingItems.contains(sortingItem)) {
                sortingItems.remove(sortingItem);
            }
            sortingItems.add(sortingItem);
        }
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setBooleanAttribute(element, "enabled", enabled);
        for (CodeCompletionSortingItem sortingItem : sortingItems) {
            writeConfiguration(element, sortingItem);
        }
    }

}
