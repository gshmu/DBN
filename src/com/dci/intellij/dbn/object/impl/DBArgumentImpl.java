package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.properties.DBDataTypePresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBArgumentImpl extends DBObjectImpl implements DBArgument {
    private DBDataType dataType;
    private int overload;
    private int position;
    private int sequence;

    public DBArgumentImpl(@NotNull DBMethod method, ResultSet resultSet) throws SQLException {
        super(method, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        overload = resultSet.getInt("OVERLOAD");
        position = resultSet.getInt("POSITION");
        sequence = resultSet.getInt("SEQUENCE");
        String inOut = resultSet.getString("IN_OUT");
        if (inOut != null) {
            set(DBObjectProperty.INPUT, inOut.contains("IN"));
            set(DBObjectProperty.OUTPUT, inOut.contains("OUT"));

        }
        name = resultSet.getString("ARGUMENT_NAME");
        if (name == null) name = position == 0 ? "return" : "[unnamed]";

        dataType = DBDataType.get(this.getConnectionHandler(), resultSet);
        if (getParentObject() instanceof DBFunction) {
            position++;
        }
    }

    public DBDataType getDataType() {
        return dataType;
    }

    public DBMethod getMethod() {
        return (DBMethod) getParentObject();
    }

    public int getOverload() {
        return overload;
    }

    public int getPosition() {
        return position;
    }

    public int getSequence() {
        return sequence;
    }

    public boolean isInput() {
        return is(DBObjectProperty.INPUT);
    }

    public boolean isOutput() {
        return is(DBObjectProperty.OUTPUT);
    }

    public boolean isLeaf() {
        return true;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

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

    public DBObjectType getObjectType() {
        return DBObjectType.ARGUMENT;
    }

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
