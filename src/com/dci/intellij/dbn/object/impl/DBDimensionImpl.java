package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBDimension;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBDimensionImpl extends DBSchemaObjectImpl implements DBDimension {

    public DBDimensionImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, DBContentType.NONE, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("DIMENSION_NAME");
    }

    public DBObjectType getObjectType() {
        return DBObjectType.TYPE;
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }     

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    public boolean isLeafTreeElement() {
        return true;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return BrowserTreeNode.EMPTY_LIST;
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
}
