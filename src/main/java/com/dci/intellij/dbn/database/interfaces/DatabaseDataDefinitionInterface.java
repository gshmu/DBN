package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.intellij.openapi.project.Project;

import java.sql.SQLException;

public interface DatabaseDataDefinitionInterface extends DatabaseInterface{

    String createDDLStatement(Project project, DatabaseObjectTypeId objectTypeId, String userName, String schemaName, String objectName, DBContentType contentType, String code, String alternativeDelimiter);

    void computeSourceCodeOffsets(SourceCodeContent content, DatabaseObjectTypeId objectTypeId, String objectName);

    boolean includesTypeAndNameInSourceContent(DatabaseObjectTypeId objectTypeId);

    /*********************************************************
     *                   CREATE statements                   *
     *********************************************************/
    void createView(String viewName, String code, DBNConnection connection) throws SQLException;

    void createMethod(MethodFactoryInput methodFactoryInput, DBNConnection connection) throws SQLException;

    void createObject(String code, DBNConnection connection) throws SQLException;

    /*********************************************************
     *                   CHANGE statements                   *
     *********************************************************/
    void updateView(String viewName, String code, DBNConnection connection) throws SQLException;

    void updateTrigger(String tableOwner, String tableName, String triggerName, String oldCode, String newCode, DBNConnection connection) throws SQLException;

    @Deprecated // TODO add objectOwner / decommission schema connection context
    void updateObject(String objectName, String objectType, String oldCode, String newCode, DBNConnection connection) throws SQLException;

   /*********************************************************
    *                   DROP statements                     *
    *********************************************************/
    void dropObject(String objectType, String objectName, DBNConnection connection) throws SQLException;

    void dropObjectBody(String objectType, String objectName, DBNConnection connection) throws SQLException;

   /*********************************************************
    *                   RENAME statements                     *
    *********************************************************/



}
