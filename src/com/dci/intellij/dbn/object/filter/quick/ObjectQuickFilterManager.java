package com.dci.intellij.dbn.object.filter.quick;

import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.quick.ui.ObjectQuickFilterDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

@State(
        name = "DBNavigator.Project.ObjectQuickFilterManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ObjectQuickFilterManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    private ObjectQuickFilterManager(Project project) {
        super(project);
    }

    public void openFilterDialog(DBObjectList objectList) {
        ObjectQuickFilterDialog dialog = new ObjectQuickFilterDialog(getProject(), objectList);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {

        }
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static ObjectQuickFilterManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ObjectQuickFilterManager.class);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.ObjectQuickFilterManager";
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element element) {

    }
}
