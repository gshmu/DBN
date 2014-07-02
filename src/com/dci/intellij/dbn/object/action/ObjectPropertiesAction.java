package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class ObjectPropertiesAction extends AnAction {
    private DBObject object;
    public ObjectPropertiesAction(DBObject object) {
        super("Properties");
        this.object = object;

    }

    public void actionPerformed(AnActionEvent event) {
        Messages.showInfoMessage("This feature is not implemented yet.", Constants.DBN_TITLE_PREFIX + "Not implemented!");
    }
}
