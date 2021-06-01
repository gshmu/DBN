package com.dci.intellij.dbn.code.common.completion;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = CodeCompletionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class CodeCompletionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.CodeCompletionManager";

    public static final int BASIC_CODE_COMPLETION = 0;
    public static final int SMART_CODE_COMPLETION = 1;

    private CodeCompletionManager(Project project) {
        super(project);
    }

    public static CodeCompletionManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, CodeCompletionManager.class);
    }

    /***************************************
    *            ProjectComponent           *
    ****************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
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
    public void loadState(@NotNull Element element) {
    }
}
