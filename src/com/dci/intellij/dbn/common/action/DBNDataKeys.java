package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.execution.explain.ExplainPlanResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.util.Key;

public interface DBNDataKeys {
    DataKey<DatasetEditor> DATASET_EDITOR = DataKey.create("DBNavigator.DatasetEditor");
    DataKey<StatementExecutionResult> STATEMENT_EXECUTION_RESULT = DataKey.create("DBNavigator.StatementExecutionResult");
    DataKey<ExplainPlanResult> EXPLAIN_PLAN_RESULT = DataKey.create("DBNavigator.ExplainPlanResult");
    Key<String> ACTION_PLACE_KEY = Key.create("DBNavigator.ActionPlace");
    Key<Boolean> PROJECT_SETTINGS_LOADED_KEY = Key.create("DBNavigator.ProjectSettingsLoaded");
}
