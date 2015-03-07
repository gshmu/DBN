package com.dci.intellij.dbn.common.ui.dialog;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public abstract class DBNDialog extends DialogWrapper implements DisposableProjectComponent{
    private Project project;
    private boolean disposed;

    protected DBNDialog(Project project, String title, boolean canBeParent) {
        super(project, canBeParent);
        setTitle(Constants.DBN_TITLE_PREFIX + title);
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            project = null;
            super.dispose();
        }
    }

    public boolean isDisposed() {
        return disposed;
    }
}
