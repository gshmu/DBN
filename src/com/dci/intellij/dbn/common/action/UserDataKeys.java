package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContext;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.util.Key;

import java.util.List;

public interface UserDataKeys {
    Key<String> ACTION_PLACE = Key.create("DBNavigator.ActionPlace");
    Key<Boolean> PROJECT_SETTINGS_LOADED = Key.create("DBNavigator.ProjectSettingsLoaded");
    Key<ProjectRef> PROJECT_REF = Key.create("DBNavigator.ProjectRef");
    Key<List<StatementExecutionProcessor>> STATEMENT_EXECUTION_PROCESSORS = Key.create("DBNavigator.StatementExecutionProcessors");
    Key<FileConnectionContext> FILE_CONNECTION_MAPPING = Key.create("DBNavigator.FileConnectionMapping");
    Key<Boolean> HAS_CONNECTIVITY_CONTEXT = Key.create("DBNavigator.HasConnectivityContext");

}
