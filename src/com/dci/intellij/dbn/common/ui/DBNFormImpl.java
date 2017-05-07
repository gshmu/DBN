package com.dci.intellij.dbn.common.ui;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public abstract class DBNFormImpl<P extends DisposableProjectComponent> extends DisposableBase implements DBNForm {
    private boolean disposed;
    private Project project;
    private P parentComponent;

    public DBNFormImpl() {
    }

    public DBNFormImpl(@NotNull P parentComponent) {
        Disposer.register(parentComponent, this);
        this.parentComponent = parentComponent;
    }

    public DBNFormImpl(Project project) {
        this.project = project;
    }

    public EnvironmentSettings getEnvironmentSettings(Project project) {
        return GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
    }

    public P getParentComponent() {
        return parentComponent;
    }

    @NotNull
    public final Project getProject() {
        if (project != null) {
            return project;
        }

        if (parentComponent != null) {
            return parentComponent.getProject();
        }

        DataContext dataContext = DataManager.getInstance().getDataContext(getComponent());
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        return FailsafeUtil.get(project);
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        project = null;
        parentComponent = null;
    }


}
