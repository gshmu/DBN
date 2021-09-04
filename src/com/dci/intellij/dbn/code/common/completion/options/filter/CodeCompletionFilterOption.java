package com.dci.intellij.dbn.code.common.completion.options.filter;

import com.dci.intellij.dbn.code.common.completion.options.filter.ui.CheckedTreeNodeProvider;
import com.dci.intellij.dbn.code.common.completion.options.filter.ui.CodeCompletionFilterTreeNode;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.ui.CheckedTreeNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import javax.swing.Icon;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBooleanAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBooleanAttribute;

@Getter
@Setter
@EqualsAndHashCode
public class CodeCompletionFilterOption implements CheckedTreeNodeProvider, PersistentConfiguration{
    @EqualsAndHashCode.Exclude
    private final CodeCompletionFilterSettings filterSettings;

    private TokenTypeCategory tokenTypeCategory = TokenTypeCategory.UNKNOWN;
    private DBObjectType objectType;
    private boolean selected;

    CodeCompletionFilterOption(CodeCompletionFilterSettings filterSettings) {
        this.filterSettings = filterSettings;
    }

    public String getName() {
        return objectType == null ?
                tokenTypeCategory.getName() :
                objectType.getName().toUpperCase();
    }

    public Icon getIcon() {
        return objectType == null ? null : objectType.getIcon();
    }

    @Override
    public void readConfiguration(Element element) {
        if (element != null) {
            String filterElementType = element.getAttributeValue("type");
            if (filterElementType.equals("OBJECT")) {
                String objectTypeName = element.getAttributeValue("id");
                objectType = DBObjectType.get(objectTypeName);
            } else {
                String tokenTypeName = element.getAttributeValue("id");
                tokenTypeCategory = TokenTypeCategory.getCategory(tokenTypeName);
            }
            selected = getBooleanAttribute(element, "selected", selected);
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

        setBooleanAttribute(element, "selected", selected);
    }

    @Override
    public CheckedTreeNode createCheckedTreeNode() {
        return new CodeCompletionFilterTreeNode(this, selected);
    }
}
