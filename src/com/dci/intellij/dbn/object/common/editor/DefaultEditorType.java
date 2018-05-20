package com.dci.intellij.dbn.object.common.editor;

import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public enum DefaultEditorType implements Presentable{
    CODE("Code"),
    DATA("Data"),
    SPEC("Spec"),
    BODY("Body"),
    SELECTION("Last Selection");

    private String name;

    DefaultEditorType(String name) {
        this.name = name;
    }

    public static DefaultEditorType[] getEditorTypes(DBObjectType objectType) {
        switch (objectType){
            case VIEW: return new DefaultEditorType[]{CODE, DATA, SELECTION};
            case PACKAGE: return new DefaultEditorType[]{SPEC, BODY, SELECTION};
            case TYPE: return new DefaultEditorType[]{SPEC, BODY, SELECTION};
        }
        return new DefaultEditorType[0];
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
