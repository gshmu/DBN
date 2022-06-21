package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.action.Lookups;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Slf4j
public abstract class AbstractConfiguration<P extends Configuration, E extends ConfigurationEditorForm> implements Configuration<P, E> {

    /*****************************************************************
     *                         DOM utilities                         *
     ****************************************************************/
    protected void writeConfiguration(Element element, Configuration configuration) {
        String elementName = configuration.getConfigElementName();
        if (elementName != null) {
            Element childElement = new Element(elementName);
            element.addContent(childElement);
            configuration.writeConfiguration(childElement);
        }
    }


    protected void readConfiguration(Element element, Configuration configuration) {
        String elementName = configuration.getConfigElementName();
        if (elementName != null) {
            Element childElement = element.getChild(elementName);
            if (childElement != null) {
                configuration.readConfiguration(childElement);
            }
        }
    }

    @NotNull
    public E createConfigurationEditor() {
        throw new UnsupportedOperationException();
    };


    @Override
    public final Project resolveProject() {
        if (this instanceof ProjectSupplier) {
            ProjectSupplier projectSupplier = (ProjectSupplier) this;
            Project project = projectSupplier.getProject();
            if (project != null) {
                return project;
            }
        }

        Configuration parent = this.getParent();
        if (parent != null) {
            Project project = parent.resolveProject();
            if (project != null) {
                return project;
            }
        }

        ConfigurationEditorForm settingsEditor = this.getSettingsEditor();
        if (Failsafe.check(settingsEditor)) {
            return Lookups.getProject(settingsEditor.getComponent());
        }
        return null;
    }

}
