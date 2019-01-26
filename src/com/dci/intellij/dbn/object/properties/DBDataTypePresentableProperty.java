package com.dci.intellij.dbn.object.properties;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBType;
import com.intellij.pom.Navigatable;

import javax.swing.*;

public class DBDataTypePresentableProperty extends PresentableProperty{
    private DBDataType dataType;
    private String name = "Data type";

    public DBDataTypePresentableProperty(String name, DBDataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public DBDataTypePresentableProperty(DBDataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return dataType.getQualifiedName();
    }

    @Override
    public Icon getIcon() {
        DBType declaredType = dataType.getDeclaredType();
        return declaredType == null ? null : declaredType.getIcon();
    }

    @Override
    public Navigatable getNavigatable() {
        return dataType.getDeclaredType();
    }
}
