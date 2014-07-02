package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

import java.util.List;

public interface DBSchema extends DBObject {
    boolean isPublicSchema();
    boolean isUserSchema();
    boolean isSystemSchema();
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
    List<DBTrigger> getTriggers();
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
    DBMethod getMethod(String name, String type, int overload);
    DBMethod getMethod(String name, int overload);
    DBProcedure getProcedure(String name, int overload);
    DBFunction getFunction(String name, int overload);
    DBCluster getCluster(String name);
    DBDatabaseLink getDatabaseLink(String name);

    void refreshObjectsStatus();

    @Override
    DBObjectRef<DBSchema> getRef();
}
