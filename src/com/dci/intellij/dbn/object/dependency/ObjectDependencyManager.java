package com.dci.intellij.dbn.object.dependency;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.options.ConfigurationUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ui.ObjectDependencyTreeDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@State(
        name = "DBNavigator.Project.ObjectDependencyManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ObjectDependencyManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private ObjectDependencyType lastUserDependencyType = ObjectDependencyType.INCOMING;

    private ObjectDependencyManager(final Project project) {
        super(project);
    }

    public static ObjectDependencyManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ObjectDependencyManager.class);
    }

    public ObjectDependencyType getLastUserDependencyType() {
        return lastUserDependencyType;
    }

    public void setLastUserDependencyType(ObjectDependencyType lastUserDependencyType) {
        this.lastUserDependencyType = lastUserDependencyType;
    }

    public void openDependencyTree(final DBSchemaObject schemaObject) {
        new ConnectionAction("opening object dependency tree", schemaObject) {
            @Override
            protected void execute() {
                ObjectDependencyTreeDialog dependencyTreeDialog = new ObjectDependencyTreeDialog(getProject(), schemaObject);
                dependencyTreeDialog.show();
            }
        }.start();
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.ObjectDependencyManager";
    }

    public Element getState() {
        Element element = new Element("state");
        ConfigurationUtil.setEnum(element, "last-used-dependency-type", lastUserDependencyType);
        return element;
    }

    @Override
    public void loadState(final Element element) {
        lastUserDependencyType = ConfigurationUtil.getEnum(element, "last-used-dependency-type", lastUserDependencyType);
    }

}
