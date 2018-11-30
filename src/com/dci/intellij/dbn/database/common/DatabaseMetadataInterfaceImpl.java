package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.common.latent.ThreadLocalLatent;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.logging.ExecutionLogOutput;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public abstract class DatabaseMetadataInterfaceImpl extends DatabaseInterfaceImpl implements DatabaseMetadataInterface {
    protected static final ThreadLocalLatent<SimpleDateFormat> META_DATE_FORMAT = ThreadLocalLatent.create(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public DatabaseMetadataInterfaceImpl(String fileName, DatabaseInterfaceProvider provider) {
        super(fileName, provider);
    }
    public Cache getCache() {
        return DatabaseInterfaceProviderImpl.getMetaDataCache();
    }

    public ResultSet getDistinctValues(String ownerName, String datasetName, String columnName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "load-distinct-values", ownerName, datasetName, columnName);
    }

    public void setCurrentSchema(String schemaName, DBNConnection connection) throws SQLException {
        executeQuery(connection, "set-current-schema", schemaName);
    }

    public ResultSet loadUsers(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "users");
    }

    public ResultSet loadCharsets(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "charsets");
    }

    public ResultSet loadRoles(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "roles");
    }

    public ResultSet loadAllUserRoles(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-user-roles");
    }


    public ResultSet loadSystemPrivileges(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "system-privileges");
    }

    public ResultSet loadObjectPrivileges(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "object-privileges");
    }

    public ResultSet loadAllUserPrivileges(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-user-privileges");
    }

    public ResultSet loadAllRolePrivileges(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-role-privileges");
    }

    public ResultSet loadAllRoleRoles(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-role-roles");
    }

    public ResultSet loadSchemas(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "schemas");
    }

    public ResultSet loadClusters(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "clusters", ownerName);
    }

    public ResultSet loadTables(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "tables", ownerName);
    }

    public ResultSet loadViews(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "views", ownerName);
    }

    public ResultSet loadMaterializedViews(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "materialized-views", ownerName);
    }

    public ResultSet loadColumns(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "dataset-columns", ownerName, datasetName);
    }

    public ResultSet loadAllColumns(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-dataset-columns", ownerName);
    }

    public ResultSet loadConstraintRelations(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "column-constraint-relations", ownerName, datasetName);
    }

    public ResultSet loadAllConstraintRelations(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-column-constraint-relations", ownerName);
    }

    public ResultSet loadIndexRelations(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "column-index-relations", ownerName, tableName);
    }

    public ResultSet loadAllIndexRelations(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-column-index-relations", ownerName);
    }

    public ResultSet loadConstraints(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "constraints", ownerName, datasetName);
    }

    public ResultSet loadAllConstraints(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-constraints", ownerName);
    }

    public ResultSet loadIndexes(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "indexes", ownerName, tableName);
    }

    public ResultSet loadAllIndexes(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-indexes", ownerName);
    }

    public ResultSet loadNestedTables(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "nested-tables", ownerName, tableName);
    }

    public ResultSet loadAllNestedTables(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-nested-tables", ownerName);
    }

    @Override
    public ResultSet loadDatabaseTriggers(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "database-triggers", ownerName);
    }

    public ResultSet loadDatasetTriggers(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "dataset-triggers", ownerName, datasetName);
    }

    public ResultSet loadAllDatasetTriggers(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-dataset-triggers", ownerName);
    }

    public ResultSet loadFunctions(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "functions", ownerName);
    }

    public ResultSet loadProcedures(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "procedures", ownerName);
    }

    public ResultSet loadDimensions(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "dimensions", ownerName);
    }


   /*********************************************************
    *                        PACKAGES                       *
    *********************************************************/
    public ResultSet loadPackages(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "packages", ownerName);
    }

    public ResultSet loadPackageFunctions(String ownerName, String packageName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "package-functions", ownerName, packageName);
    }

    public ResultSet loadAllPackageFunctions(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-package-functions", ownerName);
    }

    public ResultSet loadPackageProcedures(String ownerName, String packageName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "package-procedures", ownerName, packageName);
    }

    public ResultSet loadAllPackageProcedures(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-package-procedures", ownerName);
    }

    public ResultSet loadPackageTypes(String ownerName, String packageName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "package-types", ownerName, packageName);
    }

    public ResultSet loadAllPackageTypes(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-package-types", ownerName);
    }

    /*********************************************************
     *                        TYPES                          *
     *********************************************************/
    public ResultSet loadTypes(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "types", ownerName);
    }

    public ResultSet loadTypeAttributes(String ownerName, String typeName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "type-attributes", ownerName, typeName);
    }

    public ResultSet loadAllTypeAttributes(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-type-attributes", ownerName);
    }

    public ResultSet loadProgramTypeAttributes(String ownerName, String programName, String typeName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "program-type-attributes", ownerName, programName, typeName);
    }


    public ResultSet loadTypeFunctions(String ownerName, String typeName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "type-functions", ownerName, typeName);
    }

    public ResultSet loadAllTypeFunctions(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-type-functions", ownerName);
    }

    public ResultSet loadTypeProcedures(String ownerName, String typeName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "type-procedures", ownerName, typeName);
    }

    public ResultSet loadAllTypeProcedures(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-type-procedures", ownerName);
    }

    public ResultSet loadProgramMethodArguments(String ownerName, String programName, String methodName, int overload, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "program-method-arguments", ownerName, programName, methodName, overload);
    }

    public ResultSet loadMethodArguments(String ownerName, String methodName, String methodType, int overload, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "method-arguments", ownerName, methodName, methodType, overload);
    }

    public ResultSet loadAllMethodArguments(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "all-method-arguments", ownerName);
    }


   /*********************************************************
    *                   DATABASE LINKS                      *
    *********************************************************/

    public ResultSet loadDatabaseLinks(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "database-links", ownerName);
    }

   /*********************************************************
    *                      SEQUENCES                        *
    *********************************************************/

    public ResultSet loadSequences(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "sequences", ownerName);
    }

    /*********************************************************
     *                       SYNONYMS                        *
     *********************************************************/

    public ResultSet loadSynonyms(final String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "synonyms", ownerName);
    }

    /*********************************************************
     *                      REFERENCES                       *
     *********************************************************/
    public ResultSet loadReferencedObjects(String ownerName, String objectName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "referenced-objects", ownerName, objectName);
    }

    public ResultSet loadReferencingObjects(String ownerName, String objectName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "referencing-objects", ownerName, objectName);
    }

    public ResultSet loadReferencingSchemas(String ownerName, String objectName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "referencing-schemas", ownerName, objectName);
    }


   /*********************************************************
    *                     SOURCE CODE                       *
    *********************************************************/
     public ResultSet loadViewSourceCode(String ownerName, String viewName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "view-source-code", ownerName, viewName);
    }

    public ResultSet loadMaterializedViewSourceCode(String ownerName, String viewName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "materialized-view-source-code", ownerName, viewName);
   }

    public ResultSet loadDatabaseTriggerSourceCode(String ownerName, String triggerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "database-trigger-source-code", ownerName, triggerName);
    }

    public ResultSet loadDatasetTriggerSourceCode(String tableOwner, String tableName, String ownerName, String triggerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "dataset-trigger-source-code", tableOwner, tableName, ownerName, triggerName);
    }

    public ResultSet loadObjectSourceCode(String ownerName, String objectName, String objectType, DBNConnection connection) throws SQLException {
        return loadObjectSourceCode(ownerName, objectName, objectType, 0, connection);
    }
    public ResultSet loadObjectSourceCode(String ownerName, String objectName, String objectType, int overload, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "object-source-code", ownerName, objectName, objectType, overload);
    }

   /*********************************************************
    *                   MISCELLANEOUS                       *
    *********************************************************/

    public ResultSet loadObjectChangeTimestamp(String ownerName, String objectName, String objectType, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "object-change-timestamp", ownerName, objectName, objectType);
    }

    public ResultSet loadInvalidObjects(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "invalid-objects", ownerName);
    }

    @Override
    public ResultSet loadDebugObjects(String ownerName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "debug-objects", ownerName);
    }

    @Override
    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "object-compile-errors", ownerName, objectName);
    }


    @Override
    public void compileObject(String ownerName, String objectName, String objectType, boolean debug, DBNConnection connection) throws SQLException {
        executeStatement(connection, "compile-object", ownerName, objectName, objectType, debug ? "DEBUG" : "");
    }

    @Override
    public void compileObjectBody(String ownerName, String objectName, String objectType, boolean debug, DBNConnection connection) throws SQLException {
        executeStatement(connection, "compile-object-body", ownerName, objectName, objectType, debug ? "DEBUG" : "");
    }

    @Override
    public void enableTrigger(String ownerName, String triggerName, DBNConnection connection) throws SQLException {
        executeQuery(connection, "enable-trigger", ownerName, triggerName);
    }

    @Override
    public void disableTrigger(String ownerName, String triggerName, DBNConnection connection) throws SQLException {
        executeQuery(connection, "disable-trigger", ownerName, triggerName);
    }

    @Override
    public void enableConstraint(String ownerName, String tableName, String constraintName, DBNConnection connection) throws SQLException {
        executeQuery(connection, "enable-constraint", ownerName, tableName, constraintName);
    }

    @Override
    public void disableConstraint(String ownerName, String tableName, String constraintName, DBNConnection connection) throws SQLException {
        executeQuery(connection, "disable-constraint", ownerName, tableName, constraintName);
    }

    @Override
    public ResultSet loadSessions(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "sessions");
    }

    @Override
    public ResultSet loadSessionCurrentSql(Object sessionId, DBNConnection connection) throws SQLException {
        return executeQuery(connection, "session-sql", sessionId);
    }

    @Override
    public void killSession(Object sessionId, Object serialNumber, boolean immediate, DBNConnection connection) throws SQLException {
        String loaderId = immediate ? "kill-session-immediate" : "kill-session";
        executeStatement(connection, loaderId, sessionId, serialNumber);
    }

    @Override
    public void disconnectSession(Object sessionId, Object serialNumber, boolean postTransaction, boolean immediate, DBNConnection connection) throws SQLException {
        String loaderId =
                postTransaction ? "disconnect-session-post-transaction" :
                immediate ? "disconnect-session-immediate" : "disconnect-session";
        executeStatement(connection, loaderId, sessionId, serialNumber);
    }

    @Override
    public ResultSet loadExplainPlan(DBNConnection connection) throws SQLException {
        return executeQuery(connection, "explain-plan-result");
    }

    @Override
    public void clearExplainPlanData(DBNConnection connection) throws SQLException {
        executeUpdate(connection, "clear-explain-plan-data");
    }

    @Override
    public void enableLogger(DBNConnection connection) throws SQLException {
        executeCall(connection, null, "enable-log-output");
    }

    @Override
    public void disableLogger(DBNConnection connection) throws SQLException {
        executeCall(connection, null, "disable-log-output");
    }

    @Override
    public String readLoggerOutput(DBNConnection connection) throws SQLException {
        ExecutionLogOutput outputReader = new ExecutionLogOutput();
        executeCall(connection, outputReader, "read-log-output");
        return outputReader.getLog();
    }

    public boolean isValid(DBNConnection connection) {
        ResultSet resultSet = null;
        try {
            if (connection == null || connection.isClosed()) return false;
            resultSet = executeQuery(connection, true, "validate-connection");
        } catch (SQLException e) {
            return false;
        } finally {
            ConnectionUtil.close(resultSet);
        }
        return true;
    }

    @Override
    public boolean hasPendingTransactions(@NotNull DBNConnection connection) {
        ResultSet resultSet = null;
        try {
            resultSet = executeQuery(connection, true, "count-pending-transactions");
            resultSet.next();
            int count = resultSet.getInt("COUNT");
            return count > 0;
        } catch (SQLException e) {
            return isValid(connection);
        } finally {
            ConnectionUtil.close(resultSet);
        }
    }
}
