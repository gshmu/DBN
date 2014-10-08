package com.dci.intellij.dbn.execution.compiler;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.editor.DBContentType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;

public class CompilerAction {
    public static final CompilerAction BULK_COMPILE_ACTION = new CompilerAction(Type.BULK_COMPILE);

    private Type type;
    private WeakReference<VirtualFile> virtualFile;
    private WeakReference<Editor> editor;
    private int startOffset;
    private DBContentType contentType;

    public CompilerAction(Type type) {
        this.type = type;
    }

    public CompilerAction(Type type, VirtualFile virtualFile, Editor editor) {
        this.type = type;
        this.virtualFile = new WeakReference<VirtualFile>(virtualFile);
        this.editor = new WeakReference<Editor>(editor);
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
        return virtualFile == null ? null : virtualFile.get();
    }

    @Nullable
    public Editor getEditor() {
        return editor == null ? null : editor.get();
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
