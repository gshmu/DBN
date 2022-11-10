package com.dci.intellij.dbn.editor;

import com.dci.intellij.dbn.common.component.ApplicationComponentBase;

import static com.dci.intellij.dbn.common.component.Components.applicationService;

public class EditorManager extends ApplicationComponentBase {

    public static EditorManager getInstance() {
        return applicationService(EditorManager.class);
    }

    public EditorManager() {
        super("DBNavigator.EditorManager");
    }

/*    @Override
    public void initComponent() {
        IntentionManager intentionManager = IntentionManager.getInstance();
        intentionManager.addAction(new ExecuteScriptIntentionAction());
        intentionManager.addAction(new ExecuteStatementIntentionAction());
        intentionManager.addAction(new DebugStatementIntentionAction());
        intentionManager.addAction(new RunMethodIntentionAction());
        intentionManager.addAction(new DebugMethodIntentionAction());
        intentionManager.addAction(new ExplainPlanIntentionAction());
        intentionManager.addAction(new DatabaseConnectIntentionAction());
        intentionManager.addAction(new JumpToExecutionResultIntentionAction());
        intentionManager.addAction(new SelectConnectionIntentionAction());
        intentionManager.addAction(new SelectSchemaIntentionAction());
        intentionManager.addAction(new SelectSessionIntentionAction());
        intentionManager.addAction(new ToggleDatabaseLoggingIntentionAction());
        //intentionManager.addAction(new SetupCodeCompletionIntentionAction());
    }*/
}
