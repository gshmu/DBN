package com.dci.intellij.dbn.execution.compiler.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class CompileActionGroup extends DefaultActionGroup {

    public CompileActionGroup(DBSchemaObject object) {
        super("Compile", true);
        boolean debugSupported = DatabaseFeature.DEBUGGING.isSupported(object);
        getTemplatePresentation().setIcon(Icons.OBEJCT_COMPILE);
        if (object.getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
            add(new CompileObjectAction(object, DBContentType.CODE_SPEC_AND_BODY, CompileTypeOption.NORMAL));
            if (debugSupported) {
                add(new CompileObjectAction(object, DBContentType.CODE_SPEC_AND_BODY, CompileTypeOption.DEBUG));
            }
        } else {
            add(new CompileObjectAction(object, DBContentType.CODE, CompileTypeOption.NORMAL));
            if (debugSupported) {
                add(new CompileObjectAction(object, DBContentType.CODE, CompileTypeOption.DEBUG));
            }
        }

        addSeparator();
        add(new CompileInvalidObjectsAction(object.getSchema()));
    }
}