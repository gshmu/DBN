package com.dci.intellij.dbn.execution.explain.result;

import com.dci.intellij.dbn.common.dispose.Disposed;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExplainPlanEntry extends StatefulDisposable.Base {
    private DBObjectRef<?> objectRef;
    private @Getter Integer parentId;
    private final @Getter String operation;
    private final @Getter String operationOptions;
    private final @Getter String optimizer;
    private final @Getter Integer id;
    private final @Getter BigDecimal depth;
    private final @Getter BigDecimal position;
    private final @Getter BigDecimal cost;
    private final @Getter BigDecimal cardinality;
    private final @Getter BigDecimal bytes;
    private final @Getter BigDecimal cpuCost;
    private final @Getter BigDecimal ioCost;
    private final @Getter String accessPredicates;
    private final @Getter String filterPredicates;
    private final @Getter String projection;

    private ExplainPlanEntry parent;
    private List<ExplainPlanEntry> children;

    public ExplainPlanEntry(ConnectionHandler connection, ResultSet resultSet, List<String> columnNames) throws SQLException {
        operation = resultSet.getString("OPERATION");
        operationOptions = resultSet.getString("OPTIONS");
        optimizer = resultSet.getString("OPTIMIZER");
        id = resultSet.getInt("ID");
        parentId = resultSet.getInt("PARENT_ID");
        if (resultSet.wasNull()) {
            parentId = null;
        }

        depth = columnNames.contains("DEPTH") ? resultSet.getBigDecimal("DEPTH") : null;
        position = resultSet.getBigDecimal("POSITION");
        cost = resultSet.getBigDecimal("COST");
        cpuCost = resultSet.getBigDecimal("CPU_COST");
        ioCost = resultSet.getBigDecimal("IO_COST");
        cardinality = resultSet.getBigDecimal("CARDINALITY");
        bytes = resultSet.getBigDecimal("BYTES");

        accessPredicates = resultSet.getString("ACCESS_PREDICATES");
        filterPredicates = resultSet.getString("FILTER_PREDICATES");
        projection = resultSet.getString("PROJECTION");

        String objectOwner = resultSet.getString("OBJECT_OWNER");
        String objectName = resultSet.getString("OBJECT_NAME");
        String objectTypeName = resultSet.getString("OBJECT_TYPE");
        if (Strings.isNotEmpty(objectOwner) && Strings.isNotEmpty(objectName) && Strings.isNotEmpty(objectTypeName)) {
            DBObjectType objectType = DBObjectType.ANY;
            if (objectTypeName.startsWith("TABLE")) {
                objectType = DBObjectType.TABLE;
            } else if (objectTypeName.startsWith("MAT_VIEW")) {
                objectType = DBObjectType.MATERIALIZED_VIEW;
            } else if (objectTypeName.startsWith("VIEW")) {
                objectType = DBObjectType.VIEW;
            } else if (objectTypeName.startsWith("INDEX")) {
                objectType = DBObjectType.INDEX;
            }


            DBObjectRef<?> schemaRef = new DBObjectRef<>(connection.getConnectionId(), DBObjectType.SCHEMA, objectOwner);
            objectRef = new DBObjectRef<>(schemaRef, objectType, objectName);
        }
    }

    public ExplainPlanEntry getParent() {
        return parent;
    }

    public void setParent(ExplainPlanEntry parent) {
        this.parent = parent;
    }

    public void addChild(ExplainPlanEntry child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public List<ExplainPlanEntry> getChildren() {
        return children;
    }

    public DBObjectRef<?> getObjectRef() {
        return objectRef;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void disposeInner() {
        children = SafeDisposer.replace(children, Disposed.list(), false);
        nullify();
    }
}
