package com.dci.intellij.dbn.execution.compiler;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.vfs.VirtualFile;

public class CompilerAction {
    public static final CompilerAction BULK_COMPILE_ACTION = new CompilerAction(Type.BULK_COMPILE);

    private Type type;
    private WeakReference<VirtualFile> virtualFileRef;
    private WeakReference<FileEditor> fileEditorRef;
    private int startOffset;
    private DBContentType contentType;

    public CompilerAction(Type type) {
        this.type = type;
    }

    public CompilerAction(Type type, VirtualFile virtualFile, FileEditor fileEditor) {
        this.type = type;
        this.virtualFileRef = new WeakReference<VirtualFile>(virtualFile);
        this.fileEditorRef = new WeakReference<FileEditor>(fileEditor);
    }

    public DBContentType getContentType() {
        return contentType;
    }

    public void setContentType(DBContentType contentType) {
        this.contentType = contentType;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public Type getType() {
        return type;
    }

    public boolean isDDL() {
        return type == Type.DDL;
    }

    public boolean isSave() {
        return type == Type.SAVE;
    }

    public boolean isCompile() {
        return type == Type.COMPILE;
    }

    public boolean isBulkCompile() {
        return type == Type.BULK_COMPILE;
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
            if (editor != null && editor.isDisposed()) {
                this.fileEditorRef = null;
            }
        }
        return fileEditor;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public static enum Type {
        SAVE,
        COMPILE,
        BULK_COMPILE,
        DDL
    }
}
