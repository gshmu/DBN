package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public class CompilerAction {
    private final CompilerActionSource source;
    private final DBContentType contentType;
    private WeakRef<VirtualFile> virtualFileRef;
    private WeakRef<FileEditor> fileEditorRef;
    private EditorProviderId editorProviderId;
    private int sourceStartOffset;

    public CompilerAction(CompilerActionSource source, DBContentType contentType) {
        this.source = source;
        this.contentType = contentType;
    }

    public CompilerAction(CompilerActionSource source, DBContentType contentType, @Nullable VirtualFile virtualFile, @Nullable FileEditor fileEditor) {
        this.source = source;
        this.contentType = contentType;
        this.virtualFileRef = WeakRef.of(virtualFile);
        this.fileEditorRef = fileEditor == null ? null : WeakRef.of(fileEditor);
        this.editorProviderId = contentType.getEditorProviderId();
    }

    @Nullable
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    public DBContentType getContentType() {
        return contentType;
    }

    public void setSourceStartOffset(int sourceStartOffset) {
        this.sourceStartOffset = sourceStartOffset;
    }

    public CompilerActionSource getSource() {
        return source;
    }

    public boolean isDDL() {
        return source == CompilerActionSource.DDL;
    }

    public boolean isSave() {
        return source == CompilerActionSource.SAVE;
    }

    public boolean isCompile() {
        return source == CompilerActionSource.COMPILE;
    }

    public boolean isBulkCompile() {
        return source == CompilerActionSource.BULK_COMPILE;
    }

    @Nullable
    public VirtualFile getVirtualFile() {
        return virtualFileRef == null ? null : virtualFileRef.get();
    }

    @Nullable
    public FileEditor getFileEditor() {
        FileEditor fileEditor = this.fileEditorRef == null ? null : this.fileEditorRef.get();
        if (fileEditor != null) {
            Editor editor = EditorUtil.getEditor(fileEditor);
            if (editor != null) {
                this.fileEditorRef = null;
            }
        }
        return fileEditor;
    }

    public int getSourceStartOffset() {
        return sourceStartOffset;
    }
}
