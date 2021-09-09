package com.dci.intellij.dbn.menu.action.developer;

import com.dci.intellij.dbn.environment.Environment;

public class SimulateSlowDatabaseAction extends AbstractDeveloperAction {
    @Override
    protected boolean getState() {
        return Environment.DATABASE_LAGGING_MODE;
    }

    @Override
    protected void setState(boolean value) {
        Environment.DATABASE_LAGGING_MODE = value;
    }
}
