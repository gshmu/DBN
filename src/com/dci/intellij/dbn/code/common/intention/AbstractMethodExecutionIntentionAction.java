package com.dci.intellij.dbn.code.common.intention;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public abstract class AbstractMethodExecutionIntentionAction extends GenericIntentionAction implements HighPriorityAction {
    private WeakReference<DBMethod> lastChecked;

    @Nullable
    protected DBMethod resolveMethod(PsiFile psiFile) {
        if (psiFile != null) {
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile codeVirtualFile = (DBSourceCodeVirtualFile) virtualFile;
                DBSchemaObject object = codeVirtualFile.getObject();
                if (object != null) {
                    if (object instanceof DBMethod) {
                        DBMethod method = (DBMethod) object;
                        lastChecked = new WeakReference<DBMethod>(method);
                        return method;
                    }

                    if (object.getObjectType().isParentOf(DBObjectType.METHOD)) {

                    }
                }
            }
        }
        lastChecked = null;
        return null;
    }

    @Nullable
    protected DBMethod getMethod() {
        return lastChecked == null ? null : lastChecked.get();
    }

    @NotNull
    public String getFamilyName() {
        return "Method execution intentions";
    }
}
