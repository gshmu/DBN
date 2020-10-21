package com.dci.intellij.dbn.editor;

import com.dci.intellij.dbn.common.component.ApplicationComponent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

public class EditorManager implements ApplicationComponent {

    public static EditorManager getInstance() {
        return ApplicationManager.getApplication().getComponent(EditorManager.class);
    }

    public EditorManager() {
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

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.EditorManager";
    }
}
