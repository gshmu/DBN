package com.dci.intellij.dbn.execution.compiler;

public class CompilerAction {
    public static final CompilerAction BULK_COMPILE_ACTION = new CompilerAction(Type.BULK_COMPILE);

    private Type type;
    private Object requester;
    private int offset;

    public CompilerAction(Type type) {
        this.type = type;
    }

    public CompilerAction(Type type, Object requester) {
        this.type = type;
        this.requester = requester;
    }

    public CompilerAction(Type type, Object requester, int offset) {
        this.type = type;
        this.requester = requester;
        this.offset = offset;
    }

    public Type getType() {
        return type;
    }

    public static enum Type {
        SAVE,
        COMPILE,
        BULK_COMPILE,
        DDL
    }
}
