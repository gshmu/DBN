package com.dci.intellij.dbn.menu.action.developer;

import com.dci.intellij.dbn.environment.Environment;

public class DatabaseResourceDebugToggleAction extends AbstractDeveloperAction {

    @Override
    protected boolean getState() {
        return Environment.DATABASE_RESOURCE_DEBUG_MODE;
    }

    @Override
    protected void setState(boolean value) {
        Environment.DATABASE_RESOURCE_DEBUG_MODE = value;
    }
}
