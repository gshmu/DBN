package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.properties.DBDataTypePresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBTypeAttributeImpl extends DBObjectImpl implements DBTypeAttribute {
    private DBDataType dataType;
    private int position;

    DBTypeAttributeImpl(DBType parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("ATTRIBUTE_NAME");
        position = resultSet.getInt("POSITION");
        dataType = DBDataType.get(this.getConnectionHandler(), resultSet);
        return name;
    }


    @Override
    public int getPosition() {
        return position;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.TYPE_ATTRIBUTE;
    }

    @Override
    public DBType getType() {
        return (DBType) getParentObject();        
    }

    @Override
    public DBDataType getDataType() {
        return dataType;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, "type attribute", true);
        ttb.append(false, " - ", true);
        ttb.append(false, dataType.getQualifiedName(), true);

        ttb.createEmptyRow();
        super.buildToolTip(ttb);            
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();
        properties.add(0, new DBDataTypePresentableProperty(dataType));
        return properties;
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return dataType.getQualifiedName();
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = new ArrayList<DBObjectNavigationList>();

        if (dataType.isDeclared()) {
            objectNavigationLists.add(new DBObjectNavigationListImpl("Type", dataType.getDeclaredType()));
        }

        return objectNavigationLists;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBTypeAttribute) {
            DBTypeAttribute typeAttribute = (DBTypeAttribute) o;
            if (getType().equals(typeAttribute.getType())) {
                return position - typeAttribute.getPosition();
            }
        }
        return super.compareTo(o);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }


}
