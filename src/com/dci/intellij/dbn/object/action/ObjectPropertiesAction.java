package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ObjectPropertiesAction extends AnAction {
    private DBObject object;
    public ObjectPropertiesAction(DBObject object) {
        super("Properties");
        this.object = object;

    }

    public void actionPerformed(AnActionEvent event) {
        MessageUtil.showInfoDialog("This feature is not implemented yet.", "Not implemented!");
    }
}
