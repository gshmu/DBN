package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionCursorResultForm;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.util.Key;

import java.util.List;

public interface DBNDataKeys {
    DataKey<DatasetEditor> DATASET_EDITOR = DataKey.create("DBNavigator.DatasetEditor");
    DataKey<ConnectionBundleSettingsForm> CONNECTION_BUNDLE_SETTINGS = DataKey.create("DBNavigator.ConnectionSettingsEditor");
    DataKey<SessionBrowser> SESSION_BROWSER = DataKey.create("DBNavigator.SessionBrowser");
    DataKey<StatementExecutionCursorResult> STATEMENT_EXECUTION_CURSOR_RESULT = DataKey.create("DBNavigator.StatementExecutionCursorResult");
    DataKey<MethodExecutionResult> METHOD_EXECUTION_RESULT = DataKey.create("DBNavigator.MethodExecutionResult");
    DataKey<MethodExecutionCursorResultForm> METHOD_EXECUTION_CURSOR_RESULT_FORM = DataKey.create("DBNavigator.MethodExecutionCursorResult");
    DataKey<DBArgument> METHOD_EXECUTION_ARGUMENT = DataKey.create("DBNavigator.MethodExecutionArgument");
    DataKey<ExplainPlanResult> EXPLAIN_PLAN_RESULT = DataKey.create("DBNavigator.ExplainPlanResult");
    DataKey<DatabaseLoggingResult> DATABASE_LOG_OUTPUT = DataKey.create("DBNavigator.DatabaseLogOutput");

    Key<String> ACTION_PLACE = Key.create("DBNavigator.ActionPlace");
    Key<Boolean> PROJECT_SETTINGS_LOADED = Key.create("DBNavigator.ProjectSettingsLoaded");
    Key<ConnectionHandlerRef> CONNECTION_HANDLER = Key.create("DBNavigator.ConnectionHandler");
    Key<DatabaseSession> DATABASE_SESSION = Key.create("DBNavigator.DatabaseSession");
    Key<SchemaId> DATABASE_SCHEMA = Key.create("DBNavigator.DatabaseSchema");
    Key<ProjectRef> PROJECT_REF = Key.create("DBNavigator.ProjectRef");
    Key<String> CONSOLE_TEXT = Key.create("DBNavigator.ConsoleText");
    Key<List<StatementExecutionProcessor>> STATEMENT_EXECUTION_PROCESSORS = Key.create("DBNavigator.StatementExecutionProcessors");

}
