package com.dci.intellij.dbn.menu.action.developer;

import com.dci.intellij.dbn.environment.Environment;

public class DatabaseDebugToggleAction extends AbstractDeveloperAction {

    @Override
    protected boolean getState() {
        return Environment.DATABASE_DEBUG_MODE;
    }

    @Override
    protected void setState(boolean value) {
        Environment.DATABASE_DEBUG_MODE = value;
    }
}
