package com.dci.intellij.dbn.data.type;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum GenericDataType implements Presentable{
    LITERAL("Literal"),
    NUMERIC("Numeric"),
    DATE_TIME("Date/Time"),
    CLOB("Character Large Object"),
    BLOB("Byte Large Object"),
    ROWID("Row ID"),
    FILE("File"),
    BOOLEAN("Boolean"),
    CURSOR("Cursor"),
    OBJECT("Object"),
    ARRAY("Array"),
    XMLTYPE("XML Type"),
    PROPRIETARY("Proprietary"),
    ;

    private String name;

    private GenericDataType(String name) {
        this.name = name;
    }
    @NotNull
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


    public boolean is(GenericDataType... genericDataTypes) {
        for (GenericDataType genericDataType : genericDataTypes) {
            if (this == genericDataType) return true;
        }
        return false;
    }

    public boolean isLOB() {
        return is(BLOB, CLOB, XMLTYPE);
    }
}
