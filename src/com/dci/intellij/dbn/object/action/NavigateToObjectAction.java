package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;

public class NavigateToObjectAction extends DumbAwareAction {
    private DBObjectRef<DBObject> objectRef;

    public NavigateToObjectAction(DBObject object) {
        super();
        Presentation presentation = getTemplatePresentation();
        presentation.setText(object.getName(), false);
        presentation.setIcon(object.getIcon());
        this.objectRef = DBObjectRef.of(object);
    }

    public NavigateToObjectAction(DBObject sourceObject, DBObject object) {
        super();
        this.objectRef = DBObjectRef.of(object);

        Presentation presentation = getTemplatePresentation();
        presentation.setText(
                sourceObject != object.getParentObject() ?
                        object.getQualifiedName() :
                        object.getName(), false);
        presentation.setIcon(object.getIcon());
        presentation.setDescription(object.getTypeName());
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        DBObjectRef.ensure(objectRef).navigate(true);
    }
}
