package com.dci.intellij.dbn.common.environment.options.listener;

import com.dci.intellij.dbn.vfs.DBContentVirtualFile;

public class EnvironmentManagerAdapter implements EnvironmentManagerListener {
    @Override
    public void configurationChanged() {}

    @Override
    public void editModeChanged(DBContentVirtualFile databaseContentFile) {}
}
