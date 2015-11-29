package com.dci.intellij.dbn.browser.options;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.options.ui.DatabaseBrowserEditorSettingsForm;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.editor.DefaultEditorOption;
import com.dci.intellij.dbn.object.common.editor.DefaultEditorType;
import com.intellij.openapi.project.Project;

public class DatabaseBrowserEditorSettings extends ProjectConfiguration<DatabaseBrowserEditorSettingsForm> {
    private List<DefaultEditorOption> options = new ArrayList<DefaultEditorOption>();

    public DatabaseBrowserEditorSettings(Project project) {
        super(project);
        options.add(new DefaultEditorOption(DBObjectType.VIEW, DefaultEditorType.SELECTION));
        options.add(new DefaultEditorOption(DBObjectType.PACKAGE, DefaultEditorType.SELECTION));
        options.add(new DefaultEditorOption(DBObjectType.TYPE, DefaultEditorType.SELECTION));
    }

    @NotNull
    @Override
    public DatabaseBrowserEditorSettingsForm createConfigurationEditor() {
        return new DatabaseBrowserEditorSettingsForm(this);
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/
    public List<DefaultEditorOption> getOptions() {
        return options;
    }

    public void setOptions(List<DefaultEditorOption> options) {
        this.options = options;
    }

    public DefaultEditorOption getOption(DBObjectType objectType) {
        for (DefaultEditorOption option : options) {
            if (option.getObjectType().matches(objectType)) {
                return option;
            }
        }
        return null;
    }

    private static boolean contains(List<DefaultEditorOption> options, DBObjectType objectType) {
        for (DefaultEditorOption option : options) {
            if (option.getObjectType() == objectType){
                return true;
            }
        }
        return false;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    public String getConfigElementName() {
        return "default-editors";
    }

    public String getDisplayName() {
        return "Database Browser";
    }

    public String getHelpTopic() {
        return "browserSettings";
    }

    public void readConfiguration(Element element) {
        List<DefaultEditorOption> newOptions = new ArrayList<DefaultEditorOption>();
        List<Element> children = element.getChildren();
        for (Element child : children) {
            String objectTypeName = child.getAttributeValue("name");
            String editorTypeName = child.getAttributeValue("editor-type");
            DBObjectType objectType = DBObjectType.getObjectType(objectTypeName);
            DefaultEditorType editorType = DefaultEditorType.valueOf(editorTypeName);

            DefaultEditorOption comparator = new DefaultEditorOption(objectType, editorType);
            newOptions.add(comparator);
        }
        for (DefaultEditorOption option : options) {
            if (!contains(newOptions, option.getObjectType())) {
                newOptions.add(option);
            }
        }
        options = newOptions;
    }

    public void writeConfiguration(Element element) {
        for (DefaultEditorOption option : options) {
            Element child = new Element("object-type");
            child.setAttribute("name", option.getObjectType().getName().toUpperCase());
            child.setAttribute("editor-type", option.getEditorType().name());
            element.addContent(child);
        }
    }
}
