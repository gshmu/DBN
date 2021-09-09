package com.dci.intellij.dbn.menu.action.developer;

import com.dci.intellij.dbn.environment.Environment;

public class ParserDebugToggleAction extends AbstractDeveloperAction {

    @Override
    protected boolean getState() {
        return Environment.PARSER_DEBUG_MODE;
    }

    @Override
    protected void setState(boolean value) {
        Environment.PARSER_DEBUG_MODE = value;
    }
}
