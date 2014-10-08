package com.dci.intellij.dbn.execution.compiler;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.editor.DBContentType;
import com.intellij.openapi.vfs.VirtualFile;

public class CompilerAction {
    public static final CompilerAction BULK_COMPILE_ACTION = new CompilerAction(Type.BULK_COMPILE);

    private Type type;
    private VirtualFile virtualFile;
    private int startOffset;
    private DBContentType contentType;

    public CompilerAction(Type type) {
        this.type = type;
    }

    public CompilerAction(Type type, VirtualFile virtualFile) {
        this.type = type;
        this.virtualFile = virtualFile;
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
        return virtualFile;
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
