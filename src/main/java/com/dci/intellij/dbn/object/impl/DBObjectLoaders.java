package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.common.exception.ElementSkippedException;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.*;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.content.DynamicContentProperty.MASTER;
import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;
import static com.dci.intellij.dbn.object.type.DBObjectRelationType.*;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

@UtilityClass
public class DBObjectLoaders {
    public static void initLoaders() {}

    /* Loaders for root objects (children of DBObjectBundle) */
    static {
        DynamicContentLoaderImpl.<DBConsole, DBObjectMetadata>create(
                "CONSOLES", null, CONSOLE, true,
                content -> content.setElements(content.getConnection().getConsoleBundle().getConsoles()));


        DynamicContentResultSetLoader.<DBSchema, DBSchemaMetadata>create(
                "SCHEMAS", null, SCHEMA, true, true,
                (content, conn, mdi) -> mdi.loadSchemas(conn),
                (content, cache, md) -> new DBSchemaImpl(content.getConnection(), cast(md)));

        DynamicContentResultSetLoader.<DBUser, DBUserMetadata>create(
                "USERS", null, USER, true, true,
                (content, conn, mdi) -> mdi.loadUsers(conn),
                (content, cache, md) -> new DBUserImpl(content.getConnection(), md));

        DynamicContentResultSetLoader.<DBRole, DBRoleMetadata>create(
                "ROLES", null, ROLE, true, true,
                (content, conn, mdi) -> mdi.loadRoles(conn),
                (content, cache, md) -> new DBRoleImpl(content.getConnection(), cast(md)));

        DynamicContentResultSetLoader.<DBSystemPrivilege, DBPrivilegeMetadata>create(
                "SYSTEM_PRIVILEGES", null, SYSTEM_PRIVILEGE, true, true,
                (content, conn, mdi) -> mdi.loadSystemPrivileges(conn),
                (content, cache, md) -> new DBSystemPrivilegeImpl(content.getConnection(), md));

        DynamicContentResultSetLoader.<DBObjectPrivilege, DBPrivilegeMetadata>create(
                "OBJECT_PRIVILEGES", null, OBJECT_PRIVILEGE, true, true,
                (content, conn, mdi) -> mdi.loadObjectPrivileges(conn),
                (content, cache, md) -> new DBObjectPrivilegeImpl(content.getConnection(), md));

        DynamicContentResultSetLoader.<DBCharset, DBCharsetMetadata>create(
                "CHARSETS", null, CHARSET, true, true,
                (content, conn, mdi) -> mdi.loadCharsets(conn),
                (content, cache, md) -> new DBCharsetImpl(content.getConnection(), md));

        DynamicContentResultSetLoader.<DBUserRoleRelation, DBGrantedRoleMetadata>create(
                "USER_ROLES", null, USER_ROLE, true, true,
                (content, conn, mdi) -> mdi.loadAllUserRoles(conn),
                (content, cache, md) -> {
                    DBObjectBundle objects = content.ensureParentEntity();
                    DBUser user = valid(objects.getUser(md.getUserName()));
                    DBGrantedRole role = new DBGrantedRoleImpl(user, md);
                    return new DBUserRoleRelation(user, role);
                });

        DynamicContentResultSetLoader.<DBUserPrivilegeRelation, DBGrantedPrivilegeMetadata>create(
                "USER_PRIVILEGES", null, USER_PRIVILEGE, true, true,
                (content, conn, mdi) -> mdi.loadAllUserPrivileges(conn),
                (content, cache, md) -> {
                    DBObjectBundle objects = content.ensureParentEntity();
                    DBUser user = valid(objects.getUser(md.getUserName()));
                    DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(user, md);
                    return new DBUserPrivilegeRelation(user, privilege);
                });

        DynamicContentResultSetLoader.<DBRoleRoleRelation, DBGrantedRoleMetadata>create(
                "ROLE_ROLES", null, ROLE_ROLE, true, true,
                (content, conn, mdi) -> mdi.loadAllRoleRoles(conn),
                (content, cache, md) -> {
                    DBObjectBundle objects = content.ensureParentEntity();
                    DBRole role = valid(objects.getRole(md.getRoleName()));
                    DBGrantedRole grantedRole = new DBGrantedRoleImpl(role, md);
                    return new DBRoleRoleRelation(role, grantedRole);
                });

        DynamicContentResultSetLoader.<DBRolePrivilegeRelation, DBGrantedPrivilegeMetadata>create(
                "ROLE_PRIVILEGES", null, ROLE_PRIVILEGE, true, true,
                (content, conn, mdi) -> mdi.loadAllRolePrivileges(conn),
                (content, cache, md) -> {
                    DBObjectBundle objects = content.ensureParentEntity();
                    DBRole role = valid(objects.getRole(md.getRoleName()));
                    DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(role, md);
                    return new DBRolePrivilegeRelation(role, privilege);
                });
    }

    /* Loaders for acl objects (DBUser / DBRole / DBPrivilege) */
    static {
        DynamicContentLoaderImpl.<DBUser, DBObjectMetadata>create(
                "PRIVILEGE_USERS", PRIVILEGE, USER, true,
                content -> {
                    DBPrivilege privilege = content.ensureParentEntity();
                    List<DBUser> users = nd(privilege.getObjectBundle().getUsers());

                    List<DBUser> grantees = new ArrayList<>();
                    for (DBUser user : users) {
                        if (user.hasPrivilege(privilege)) {
                            grantees.add(user);
                        }
                    }
                    content.setElements(grantees);
                    content.set(MASTER, false);
                });

        DynamicContentLoaderImpl.<DBRole, DBObjectMetadata>create(
                "PRIVILEGE_ROLES", PRIVILEGE, ROLE, true, content -> {
                    DBPrivilege privilege = content.ensureParentEntity();
                    List<DBRole> roles = nd(privilege.getObjectBundle().getRoles());

                    List<DBRole> grantees = new ArrayList<>();
                    for (DBRole role : roles) {
                        if (role.hasPrivilege(privilege)) grantees.add(role);
                    }
                    content.setElements(grantees);
                    content.set(MASTER, false);
                });
    }

    /* Loaders for schema objects (children of DBSchema) */
    static {
        DynamicContentResultSetLoader.<DBTable, DBTableMetadata>create(
                "TABLES", SCHEMA, TABLE, true, true,
                (content, conn, mdi) -> mdi.loadTables(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBTableImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBView, DBViewMetadata>create(
                "VIEWS", SCHEMA, VIEW, true, true,
                (content, conn, mdi) -> mdi.loadViews(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBViewImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBMaterializedView, DBMaterializedViewMetadata>create(
                "MATERIALIZED_VIEWS", SCHEMA, MATERIALIZED_VIEW, true, true,
                (content, conn, mdi) -> mdi.loadMaterializedViews(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBMaterializedViewImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBSynonym, DBSynonymMetadata>create(
                "SYNONYMS", SCHEMA, SYNONYM, true, true,
                (content, conn, mdi) -> mdi.loadSynonyms(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBSynonymImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBSequence, DBSequenceMetadata>create(
                "SEQUENCES", SCHEMA, SEQUENCE, true, true,
                (content, conn, mdi) -> mdi.loadSequences(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBSequenceImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBProcedure, DBProcedureMetadata>create(
                "PROCEDURES", SCHEMA, PROCEDURE, true, true,
                (content, conn, mdi) -> mdi.loadProcedures(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBProcedureImpl((DBSchema) content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBFunction, DBFunctionMetadata>create(
                "FUNCTIONS", SCHEMA, FUNCTION, true, true,
                (content, conn, mdi) -> mdi.loadFunctions(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBFunctionImpl((DBSchema) content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBPackage, DBPackageMetadata>create(
                "PACKAGES", SCHEMA, PACKAGE, true, true,
                (content, conn, mdi) -> mdi.loadPackages(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBPackageImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBType, DBTypeMetadata>create(
                "TYPES", SCHEMA, TYPE, true, true,
                (content, conn, mdi) -> mdi.loadTypes(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBTypeImpl((DBSchema) content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBDatabaseTrigger, DBTriggerMetadata>create(
                "DATABASE_TRIGGERS", SCHEMA, DATABASE_TRIGGER, true, true,
                (content, conn, mdi) -> mdi.loadDatabaseTriggers(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBDatabaseTriggerImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBDimension, DBDimensionMetadata>create(
                "DIMENSIONS", SCHEMA, DIMENSION, true, true,
                (content, conn, mdi) -> mdi.loadDimensions(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBDimensionImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBCluster, DBClusterMetadata>create(
                "CLUSTERS", SCHEMA, CLUSTER, true, true,
                (content, conn, mdi) -> mdi.loadClusters(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBClusterImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBDatabaseLink, DBDatabaseLinkMetadata>create(
                "DBLINKS", SCHEMA, DBLINK, true, true,
                (content, conn, mdi) -> mdi.loadDatabaseLinks(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> new DBDatabaseLinkImpl(content.getParentEntity(), md));

        DynamicContentResultSetLoader.<DBColumn, DBColumnMetadata>create(
                "ALL_COLUMNS", SCHEMA, COLUMN, true, true,
                (content, conn, mdi) -> mdi.loadAllColumns(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String datasetName = md.getDatasetName();
                    DBDataset dataset = valid(cache.get(datasetName, () -> ((DBSchema) content.ensureParentEntity()).getDataset(datasetName)));
                    return new DBColumnImpl(dataset, md);
                });

        DynamicContentResultSetLoader.<DBConstraint, DBConstraintMetadata>create(
                "ALL_CONSTRAINTS", SCHEMA, CONSTRAINT, true, true,
                (content, conn, mdi) -> mdi.loadAllConstraints(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String datasetName = md.getDatasetName();
                    DBDataset dataset = valid(cache.get(datasetName, () -> ((DBSchema) content.ensureParentEntity()).getDataset(datasetName)));
                    return new DBConstraintImpl(dataset, md);
                });

        DynamicContentResultSetLoader.<DBIndex, DBIndexMetadata>create(
                "ALL_INDEXES", SCHEMA, INDEX, true, true,
                (content, conn, mdi) -> mdi.loadAllIndexes(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String datasetName = md.getTableName();
                    DBDataset dataset = valid(cache.get(datasetName, () -> ((DBSchema) content.ensureParentEntity()).getDataset(datasetName)));
                    return new DBIndexImpl(dataset, md);
                });

        DynamicContentResultSetLoader.<DBDatasetTrigger, DBTriggerMetadata>create(
                "ALL_DATASET_TRIGGERS", SCHEMA, DATASET_TRIGGER, true, true,
                (content, conn, mdi) -> mdi.loadAllDatasetTriggers(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String datasetName = md.getDatasetName();
                    DBDataset dataset = valid(cache.get(datasetName, () -> ((DBSchema) content.ensureParentEntity()).getDataset(datasetName)));
                    return new DBDatasetTriggerImpl(dataset, md);
                });

        DynamicContentResultSetLoader.<DBNestedTable, DBNestedTableMetadata>create(
                "ALL_NESTED_TABLES", SCHEMA, NESTED_TABLE, true, true,
                (content, conn, mdi) -> mdi.loadAllNestedTables(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String tableName = md.getTableName();
                    DBTable table = valid(cache.get(tableName, () -> ((DBSchema) content.ensureParentEntity()).getTable(tableName)));
                    return new DBNestedTableImpl(table, md);
                });

        DynamicContentResultSetLoader.<DBPackageFunction, DBFunctionMetadata>create(
                "ALL_PACKAGE_FUNCTIONS", SCHEMA, PACKAGE_FUNCTION, true, true,
                (content, conn, mdi) -> mdi.loadAllPackageFunctions(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String programName = md.getPackageName();
                    DBPackage program = valid(cache.get(programName, () -> ((DBSchema) content.ensureParentEntity()).getPackage(programName)));
                    return new DBPackageFunctionImpl(program, md);
                });

        DynamicContentResultSetLoader.<DBPackageProcedure, DBProcedureMetadata>create(
                "ALL_PACKAGE_PROCEDURES", SCHEMA, PACKAGE_PROCEDURE, true, true,
                (content, conn, mdi) -> mdi.loadAllPackageProcedures(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String programName = md.getPackageName();
                    DBPackage program = valid(cache.get(programName, () -> ((DBSchema) content.ensureParentEntity()).getPackage(programName)));
                    return new DBPackageProcedureImpl(program, md);
                });

        DynamicContentResultSetLoader.<DBPackageType, DBTypeMetadata>create(
                "ALL_PACKAGE_TYPES", SCHEMA, PACKAGE_TYPE, true, true,
                (content, conn, mdi) -> mdi.loadAllPackageTypes(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String programName = md.getPackageName();
                    DBPackage program = valid(cache.get(programName, () -> ((DBSchema) content.ensureParentEntity()).getPackage(programName)));
                    return new DBPackageTypeImpl(program, md);
                });

        DynamicContentResultSetLoader.<DBTypeAttribute, DBTypeAttributeMetadata>create(
                "ALL_TYPE_ATTRIBUTES", SCHEMA, TYPE_ATTRIBUTE, true, true,
                (content, conn, mdi) -> mdi.loadAllTypeAttributes(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String typeName = md.getTypeName();
                    DBType type = valid(cache.get(typeName, () -> ((DBSchema) content.ensureParentEntity()).getType(typeName)));
                    return new DBTypeAttributeImpl(type, md);
                });

        DynamicContentResultSetLoader.<DBTypeFunction, DBFunctionMetadata>create(
                "ALL_TYPE_FUNCTIONS", SCHEMA, TYPE_FUNCTION, true, true,
                (content, conn, mdi) -> mdi.loadAllTypeFunctions(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String typeName = md.getTypeName();
                    DBType type = valid(cache.get(typeName, () -> ((DBSchema) content.ensureParentEntity()).getType(typeName)));
                    return new DBTypeFunctionImpl(type, md);
                });

        DynamicContentResultSetLoader.<DBTypeProcedure, DBProcedureMetadata>create(
                "ALL_TYPE_PROCEDURES", SCHEMA, TYPE_PROCEDURE, true, true,
                (content, conn, mdi) -> mdi.loadAllTypeProcedures(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String typeName = md.getTypeName();
                    DBType type = valid(cache.get(typeName, () -> ((DBSchema) content.ensureParentEntity()).getType(typeName)));
                    return new DBTypeProcedureImpl(type, md);
                });

        DynamicContentResultSetLoader.<DBArgument, DBArgumentMetadata>create(
                "ALL_METHOD_ARGUMENTS", SCHEMA, ARGUMENT, true, true,
                (content, conn, mdi) -> mdi.loadAllMethodArguments(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String programName = md.getProgramName();
                    String methodName = md.getMethodName();
                    String methodType = md.getMethodType();
                    short overload = md.getOverload();
                    DBSchema schema = content.ensureParentEntity();
                    DBProgram program = programName == null ? null : schema.getProgram(programName);

                    String key = methodName + methodType + overload;
                    DBMethod method = cache.get(key);
                    DBObjectType objectType = get(methodType);

                    if (method == null || method.getProgram() != program || method.getOverload() != overload) {
                        method = programName == null ?
                                schema.getMethod(methodName, objectType, overload):
                                program == null ? null : program.getMethod(methodName, overload);
                        cache.set(key, method);
                    }
                    return new DBArgumentImpl(valid(method), md);
                });

        DynamicContentResultSetLoader.<DBConstraintColumnRelation, DBConstraintColumnMetadata>create(
                "ALL_CONSTRAINT_COLUMNS", SCHEMA, CONSTRAINT_COLUMN, true, false,
                (content, conn, mdi) -> mdi.loadAllConstraintRelations(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String datasetName = md.getDatasetName();
                    DBDataset dataset = valid(cache.get(datasetName, () -> ((DBSchema) content.ensureParentEntity()).getDataset(datasetName)));
                    DBColumn column = valid(dataset.getColumn(md.getColumnName()));
                    DBConstraint constraint = valid(dataset.getConstraint(md.getConstraintName()));
                    return new DBConstraintColumnRelation(constraint, column, md.getPosition());
                });

        DynamicContentResultSetLoader.<DBIndexColumnRelation, DBIndexColumnMetadata>create(
                "ALL_INDEX_COLUMNS", SCHEMA, INDEX_COLUMN, true, false,
                (content, conn, mdi) -> mdi.loadAllIndexRelations(content.ensureParentEntity().getName(), conn),
                (content, cache, md) -> {
                    String tableName = md.getTableName();
                    DBDataset dataset = valid(cache.get(tableName, () -> ((DBSchema) content.ensureParentEntity()).getDataset(tableName)));
                    DBColumn column = valid(dataset.getColumn(md.getColumnName()));
                    DBIndex index = valid(dataset.getIndex(md.getIndexName()));
                    return new DBIndexColumnRelation(index, column);
                });

    }

    /* Loaders for table child objects (children of DBDataset) */
    static {
        DynamicSubcontentLoader.create("DATASET_COLUMNS", DATASET, COLUMN,
                DynamicContentResultSetLoader.<DBColumn, DBColumnMetadata>create(
                        "DATASET_COLUMNS", DATASET, COLUMN, false, true,
                        (content, conn, mdi) -> mdi.loadColumns(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBColumnImpl(valid(content.getParentEntity()), md)));

        DynamicSubcontentLoader.create("DATASET_CONSTRAINTS", DATASET, CONSTRAINT,
                DynamicContentResultSetLoader.<DBConstraint, DBConstraintMetadata>create(
                        "DATASET_CONSTRAINTS", DATASET, CONSTRAINT, false, true,
                        (content, conn, mdi) -> mdi.loadConstraints(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBConstraintImpl(valid(content.getParentEntity()), md)));

        DynamicSubcontentLoader.create("DATASET_TRIGGERS", DATASET, DATASET_TRIGGER,
                DynamicContentResultSetLoader.<DBDatasetTrigger, DBTriggerMetadata>create(
                        "DATASET_TRIGGERS", DATASET, DATASET_TRIGGER, false, true,
                        (content, conn, mdi) -> mdi.loadDatasetTriggers(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBDatasetTriggerImpl(valid(content.getParentEntity()), md)));

        DynamicSubcontentLoader.create("DATASET_INDEXES", DATASET, INDEX,
                DynamicContentResultSetLoader.<DBIndex, DBIndexMetadata>create(
                        "DATASET_INDEXES", DATASET, INDEX, false, true,
                        (content, conn, mdi) -> mdi.loadIndexes(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBIndexImpl(valid(content.getParentEntity()), md)));

        DynamicSubcontentLoader.create("DATASET_INDEX_COLUMNS", DATASET, INDEX_COLUMN,
                DynamicContentResultSetLoader.<DBIndexColumnRelation, DBIndexColumnMetadata>create(
                        "DATASET_INDEX_COLUMNS", DATASET, INDEX_COLUMN, false, false,
                        (content, conn, mdi) -> mdi.loadIndexRelations(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> {
                            DBDataset dataset = valid(content.getParentEntity());
                            DBIndex index = valid(dataset.getIndex(md.getIndexName()));
                            DBColumn column = valid(dataset.getColumn(md.getColumnName()));
                            return new DBIndexColumnRelation(index, column);
                        }));

        DynamicSubcontentLoader.create("DATASET_CONSTRAINT_COLUMNS", DATASET, CONSTRAINT_COLUMN,
                DynamicContentResultSetLoader.<DBConstraintColumnRelation, DBConstraintColumnMetadata>create(
                        "DATASET_CONSTRAINT_COLUMNS", DATASET, CONSTRAINT_COLUMN, false, false,
                        (content, conn, mdi) -> mdi.loadConstraintRelations(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> {
                            DBDataset dataset = valid(content.getParentEntity());
                            DBColumn column = valid(dataset.getColumn(md.getColumnName()));
                            DBConstraint constraint = valid(dataset.getConstraint(md.getConstraintName()));
                            return new DBConstraintColumnRelation(constraint, column, md.getPosition());
                        }));

        DynamicSubcontentLoader.create("NESTED_TABLES", TABLE, NESTED_TABLE,
                DynamicContentResultSetLoader.<DBNestedTable, DBNestedTableMetadata>create(
                        "NESTED_TABLES", TABLE, NESTED_TABLE, false, true,
                        (content, conn, mdi) -> mdi.loadNestedTables(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBNestedTableImpl(valid(content.getParentEntity()), md)));
    }

    /* Loaders for program child objects (children of DBProgram) */
    static {
        DynamicSubcontentLoader.create("ALL_PACKAGE_FUNCTIONS", PACKAGE, PACKAGE_FUNCTION,
                DynamicContentResultSetLoader.<DBPackageFunction, DBFunctionMetadata>create(
                        "PACKAGE_FUNCTIONS", PACKAGE, PACKAGE_FUNCTION, false, true,
                        (content, conn, mdi) -> mdi.loadPackageFunctions(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBPackageFunctionImpl(valid(content.getParentEntity()), md)));

        DynamicSubcontentLoader.create("ALL_PACKAGE_PROCEDURES", PACKAGE, PACKAGE_PROCEDURE,
                DynamicContentResultSetLoader.<DBPackageProcedure, DBProcedureMetadata>create(
                        "PACKAGE_PROCEDURES", PACKAGE, PACKAGE_PROCEDURE, false, true,
                        (content, conn, mdi) -> mdi.loadPackageProcedures(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBPackageProcedureImpl(valid(content.getParentEntity()), md)));

        DynamicSubcontentLoader.create("ALL_PACKAGE_TYPES", PACKAGE, PACKAGE_TYPE,
                DynamicContentResultSetLoader.<DBPackageType, DBTypeMetadata>create(
                        "PACKAGE_TYPES", PACKAGE, PACKAGE_TYPE, false, true,
                        (content, conn, mdi) -> mdi.loadPackageTypes(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBPackageTypeImpl(valid(content.getParentEntity()), md)));


        DynamicContentResultSetLoader.<DBTypeAttribute, DBTypeAttributeMetadata>create(
                "PACKAGE_TYPE_ATTRIBUTES", PACKAGE_TYPE, TYPE_ATTRIBUTE, true, true,
                (content, conn, mdi) -> {
                    DBPackageType type = valid(content.getParentEntity());
                    return mdi.loadProgramTypeAttributes(
                            type.getSchema().getName(),
                            type.getPackage().getName(),
                            type.getName(), conn);
                    },
                (content, cache, md) -> new DBTypeAttributeImpl(valid(content.getParentEntity()), md));

        DynamicSubcontentLoader.create("TYPE_TYPE_ATTRIBUTES", TYPE, TYPE_ATTRIBUTE,
                DynamicContentResultSetLoader.<DBTypeAttribute, DBTypeAttributeMetadata>create(
                        "TYPE_TYPE_ATTRIBUTES", TYPE, TYPE_ATTRIBUTE, false, true,
                        (content, conn, mdi) -> mdi.loadTypeAttributes(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBTypeAttributeImpl(valid(content.getParentEntity()), md)));

        DynamicSubcontentLoader.create("TYPE_TYPE_FUNCTIONS", TYPE, TYPE_FUNCTION,
                DynamicContentResultSetLoader.<DBTypeFunction, DBFunctionMetadata>create(
                        "TYPE_TYPE_FUNCTIONS", TYPE, TYPE_FUNCTION, false, true,
                        (content, conn, mdi) -> mdi.loadTypeFunctions(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBTypeFunctionImpl(valid(content.getParentEntity()), md)));

        DynamicSubcontentLoader.create("TYPE_TYPE_PROCEDURES", TYPE, TYPE_PROCEDURE,
                DynamicContentResultSetLoader.<DBTypeProcedure, DBProcedureMetadata>create(
                        "TYPE_TYPE_PROCEDURES", TYPE, TYPE_PROCEDURE, false, true,
                        (content, conn, mdi) -> mdi.loadTypeProcedures(content.getParentSchemaName(), content.getParentObjectName(), conn),
                        (content, cache, md) -> new DBTypeProcedureImpl(valid(content.getParentEntity()), md)));


        DynamicSubcontentLoader.create("TYPE_TYPES", TYPE, TYPE, null/*TODO*/);

        DynamicSubcontentLoader.create("METHOD_ARGUMENTS", METHOD, ARGUMENT,
                DynamicContentResultSetLoader.<DBArgument, DBArgumentMetadata>create(
                        "METHOD_ARGUMENTS", METHOD, ARGUMENT, false, true,
                        (content, conn, mdi) -> {
                            DBMethod method = content.ensureParentEntity();
                            String ownerName = method.getSchemaName();
                            short overload = method.getOverload();
                            DBProgram program = method.getProgram();
                            return program == null ?
                                    mdi.loadMethodArguments(ownerName, method.getName(), method.getMethodType().id(), overload, conn) :
                                    mdi.loadProgramMethodArguments(ownerName, program.getName(), method.getName(), overload, conn);
                        },
                        (content, cache, md) -> new DBArgumentImpl(valid(content.getParentEntity()), md)));
    }

    /* Loaders for object dependencies */
    static {
        DynamicContentResultSetLoader.<DBObject, DBObjectDependencyMetadata>create(
                "INCOMING_DEPENDENCIES", null, INCOMING_DEPENDENCY, true, false,
                (content, conn, mdi) ->  mdi.loadReferencedObjects(content.getParentSchemaName(), content.getParentObjectName(), conn),
                (content, cache, md) -> {
                    String objectOwner = md.getObjectOwner();
                    String objectName = md.getObjectName();
                    String objectTypeName = md.getObjectType();
                    DBObjectType objectType = get(objectTypeName);
                    if (objectType == PACKAGE_BODY) objectType = PACKAGE;
                    if (objectType == TYPE_BODY) objectType = TYPE;

                    DBSchema schema = valid(cache.get(objectOwner, () -> content.ensureParentEntity().getObjectBundle().getSchema(objectOwner)));
                    return schema.getChildObject(objectType, objectName, (short) 0, true);
                });

        DynamicContentResultSetLoader.<DBObject, DBObjectDependencyMetadata>create(
                "OUTGOING_DEPENDENCIES", null, OUTGOING_DEPENDENCY, true, false,
                (content, conn, mdi) ->  mdi.loadReferencingObjects(content.getParentSchemaName(), content.getParentObjectName(), conn),
                (content, cache, md) -> {
                    String objectOwner = md.getObjectOwner();
                    String objectName = md.getObjectName();
                    String objectTypeName = md.getObjectType();
                    DBObjectType objectType = get(objectTypeName);
                    if (objectType == PACKAGE_BODY) objectType = PACKAGE;
                    if (objectType == TYPE_BODY) objectType = TYPE;

                    DBSchema schema = valid(cache.get(objectOwner, () -> content.ensureParentEntity().getObjectBundle().getSchema(objectOwner)));
                    return schema.getChildObject(objectType, objectName, (short) 0, true);
                });
    }

    /* Loaders for sub-contents from relation lists */
    static {
        DBObjectListFromRelationListLoader.create("COLUMN_CONSTRAINTS", COLUMN, CONSTRAINT);
        DBObjectListFromRelationListLoader.create("COLUMN_INDEXES", COLUMN, INDEX);
        DBObjectListFromRelationListLoader.create("CONSTRAINT_COLUMNS", CONSTRAINT, COLUMN);
        DBObjectListFromRelationListLoader.create("INDEX_COLUMNS", INDEX, COLUMN);
        DBObjectListFromRelationListLoader.create("ROLE_PRIVILEGES", ROLE, GRANTED_PRIVILEGE);
        DBObjectListFromRelationListLoader.create("ROLE_ROLES", ROLE, GRANTED_ROLE);
        DBObjectListFromRelationListLoader.create("USER_ROLES", USER, GRANTED_ROLE);
        DBObjectListFromRelationListLoader.create("USER_PRIVILEGES", USER, GRANTED_PRIVILEGE);
    }


    private static <T> T valid(T element) {
        if (element == null || isNotValid(element)) throw ElementSkippedException.INSTANCE;
        return element;
    }

}
