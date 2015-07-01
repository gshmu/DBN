package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.common.editor.BasicTextEditorImpl;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class SourceCodeEditor extends BasicTextEditorImpl<DBSourceCodeVirtualFile>{
    private DBObjectRef<DBSchemaObject> objectRef;
    private SourceCodeOffsets offsets;

    public SourceCodeEditor(Project project, final DBSourceCodeVirtualFile sourceCodeFile, String name, EditorProviderId editorProviderId) {
        super(project, sourceCodeFile, name, editorProviderId);

        objectRef = DBObjectRef.from(sourceCodeFile.getObject());
        Document document = this.textEditor.getEditor().getDocument();
        if (document.getTextLength() > 0) {
            offsets = sourceCodeFile.getOffsets();
            if (offsets == null) {
                offsets = new SourceCodeOffsets();
            } else {
                GuardedBlockMarkers guardedBlocks = offsets.getGuardedBlocks();
                DocumentUtil.createGuardedBlocks(document, guardedBlocks, null);
            }
        } else {
            offsets = new SourceCodeOffsets();
        }
    }

    public DBSchemaObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    public int getHeaderEndOffset() {
        return offsets.getHeaderEndOffset();
    }

    public void navigateTo(DBObject object) {
        PsiFile file = PsiUtil.getPsiFile(getObject().getProject(), getVirtualFile());
        if (file instanceof PSQLFile) {
            PSQLFile psqlFile = (PSQLFile) file;
            BasePsiElement navigable = psqlFile.lookupObjectDeclaration(object.getObjectType(), object.getName());
            if (navigable == null) navigable = psqlFile.lookupObjectSpecification(object.getObjectType(), object.getName());
            if (navigable != null) navigable.navigate(true);
        }
    }

    public DBContentType getContentType() {
        return getVirtualFile().getContentType();
    }
}
