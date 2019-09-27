package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.database.common.metadata.def.DBArgumentMetadata;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.properties.DBDataTypePresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBArgumentImpl extends DBObjectImpl<DBArgumentMetadata> implements DBArgument {
    private DBDataType dataType;
    private short overload;
    private short position;
    private short sequence;

    DBArgumentImpl(@NotNull DBMethod method, DBArgumentMetadata metadata) throws SQLException {
        super(method, metadata);
    }

    @Override
    protected String initObject(DBArgumentMetadata metadata) throws SQLException {
        overload = metadata.getOverload();
        position = metadata.getPosition();
        sequence = metadata.getSequence();
        String inOut = metadata.getInOut();
        if (inOut != null) {
            set(DBObjectProperty.INPUT, inOut.contains("IN"));
            set(DBObjectProperty.OUTPUT, inOut.contains("OUT"));

        }
        String name = metadata.getArgumentName();
        if (name == null) name = position == 0 ? "return" : "[unnamed]";

        dataType = DBDataType.get(getConnectionHandler(), metadata.getDataType());
        if (getParentObject() instanceof DBFunction) {
            position++;
        }
        return name;
    }

    @Override
    public DBDataType getDataType() {
        return dataType;
    }

    @Override
    public DBMethod getMethod() {
        return (DBMethod) getParentObject();
    }

    @Override
    public short getOverload() {
        return overload;
    }

    @Override
    public short getPosition() {
        return position;
    }

    @Override
    public short getSequence() {
        return sequence;
    }

    @Override
    public boolean isInput() {
        return is(DBObjectProperty.INPUT);
    }

    @Override
    public boolean isOutput() {
        return is(DBObjectProperty.OUTPUT);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.append(false, " - ", true);
        ttb.append(false, dataType.getQualifiedName(), true);
        String inOut = isInput() && isOutput() ? "IN / OUT" : isInput() ? "IN" : "OUT";
        ttb.append(true, inOut, true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return dataType.getQualifiedName();
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();
        properties.add(0, new DBDataTypePresentableProperty(dataType));
        properties.add(0, new SimplePresentableProperty("Argument type", isInput() && isOutput() ? "IN / OUT" : isInput() ? "IN" : "OUT"));
        return properties;
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        if (dataType.isDeclared()) {
            List<DBObjectNavigationList> objectNavigationLists = new ArrayList<DBObjectNavigationList>();
            objectNavigationLists.add(new DBObjectNavigationListImpl<DBType>("Type", dataType.getDeclaredType()));
            return objectNavigationLists;
        }
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return isInput() && isOutput() ? Icons.DBO_ARGUMENT_IN_OUT :
               isInput() ? Icons.DBO_ARGUMENT_IN :
               isOutput() ? Icons.DBO_ARGUMENT_OUT : Icons.DBO_ARGUMENT;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.ARGUMENT;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBArgument) {
            DBArgument argument = (DBArgument) o;
            DBMethod thisMethod = getMethod();
            DBMethod thatMethod = argument.getMethod();
            if (thisMethod.equals(thatMethod)) {
                return position - argument.getPosition();
            } else {
                return thisMethod.compareTo(thatMethod);
            }
        }
        return super.compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            DBArgument argument = (DBArgument) obj;
            return overload == argument.getOverload() &&
                    position == argument.getPosition();
        }
        return false;
    }

}
