package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.util.Key;

import java.util.List;

public interface UserDataKeys {
    Key<String> ACTION_PLACE = Key.create("DBNavigator.ActionPlace");
    Key<Boolean> PROJECT_SETTINGS_LOADED = Key.create("DBNavigator.ProjectSettingsLoaded");
    Key<ConnectionHandlerRef> CONNECTION_HANDLER = Key.create("DBNavigator.ConnectionHandler");
    Key<DatabaseSession> DATABASE_SESSION = Key.create("DBNavigator.DatabaseSession");
    Key<SchemaId> DATABASE_SCHEMA = Key.create("DBNavigator.DatabaseSchema");
    Key<ProjectRef> PROJECT_REF = Key.create("DBNavigator.ProjectRef");
    Key<List<StatementExecutionProcessor>> STATEMENT_EXECUTION_PROCESSORS = Key.create("DBNavigator.StatementExecutionProcessors");

}
