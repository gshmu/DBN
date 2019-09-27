package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;

import java.sql.SQLException;
import java.util.List;

public interface DBSchema extends DBObject {
    boolean isPublicSchema();
    boolean isUserSchema();
    boolean isSystemSchema();
    boolean isEmptySchema();
    List<DBDataset> getDatasets();
    List<DBTable> getTables();
    List<DBView> getViews();
    List<DBMaterializedView> getMaterializedViews();
    List<DBIndex> getIndexes();
    List<DBSynonym> getSynonyms();
    List<DBSequence> getSequences();
    List<DBProcedure> getProcedures();
    List<DBFunction> getFunctions();
    List<DBPackage> getPackages();
    List<DBDatasetTrigger> getDatasetTriggers();
    List<DBDatabaseTrigger> getDatabaseTriggers();
    List<DBType> getTypes();
    List<DBDimension> getDimensions();
    List<DBCluster> getClusters();
    List<DBDatabaseLink> getDatabaseLinks();

    DBDataset getDataset(String name);
    DBTable getTable(String name);
    DBView getView(String name);
    DBMaterializedView getMaterializedView(String name);
    DBIndex getIndex(String name);
    DBType getType(String name);
    DBPackage getPackage(String name);
    DBProgram getProgram(String name);
    DBMethod getMethod(String name, DBObjectType methodType, short overload);
    DBMethod getMethod(String name, short overload);
    DBProcedure getProcedure(String name, short overload);
    DBFunction getFunction(String name, short overload);
    DBCluster getCluster(String name);
    DBDatabaseLink getDatabaseLink(String name);

    void refreshObjectsStatus() throws SQLException;

    @Override
    DBObjectRef<DBSchema> getRef();
    SchemaId getIdentifier();
}
