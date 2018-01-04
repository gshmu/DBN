package com.dci.intellij.dbn.debugger.jdbc.config.ui;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.config.ui.DBProgramRunConfigurationEditorForm;
import com.dci.intellij.dbn.debugger.jdbc.config.DBMethodJdbcRunConfig;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserDialog;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionInputForm;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;

public class DBMethodJdbcRunConfigEditorForm extends DBProgramRunConfigurationEditorForm<DBMethodJdbcRunConfig> {
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JPanel methodArgumentsPanel;
    private JCheckBox compileDependenciesCheckBox;
    private JPanel selectMethodActionPanel;
    private JPanel hintPanel;

    private MethodExecutionInputForm methodExecutionInputForm;

    public DBMethodJdbcRunConfigEditorForm(final DBMethodJdbcRunConfig configuration) {
        super(configuration.getProject());
        readConfiguration(configuration);
        if (configuration.getCategory() != DBRunConfigCategory.CUSTOM) {
            selectMethodActionPanel.setVisible(false);
            methodArgumentsPanel.setVisible(false);
            headerPanel.setVisible(false);
            hintPanel.setVisible(true);
            DBNHintForm hintForm = new DBNHintForm(DatabaseDebuggerManager.GENERIC_METHOD_RUNNER_HINT, null, true);
            hintPanel.add(hintForm.getComponent());
        } else {
            ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, new SelectMethodAction());
            selectMethodActionPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
            hintPanel.setVisible(false);
        }
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public class SelectMethodAction extends GroupPopupAction {
        public SelectMethodAction()  {
            super("Select method", "Select method", Icons.DBO_METHOD);
        }

        @Override
        protected AnAction[] getActions(AnActionEvent e) {
            return new AnAction[]{
                    new OpenMethodHistoryAction(),
                    new OpenMethodBrowserAction()
            };
        }
    }

    public class OpenMethodBrowserAction extends AnAction {
        public OpenMethodBrowserAction() {
            super("Method Browser");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            final Project project = ActionUtil.getProject(e);
            if (project != null) {
                BackgroundTask backgroundTask = new BackgroundTask(project, "Loading executable elements", false) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator progressIndicator) {
                        final MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                        final MethodBrowserSettings settings = executionManager.getBrowserSettings();
                        MethodExecutionInput executionInput = getExecutionInput();
                        DBMethod currentMethod = executionInput == null ? null : executionInput.getMethod();
                        if (currentMethod != null) {
                            settings.setConnectionHandler(currentMethod.getConnectionHandler());
                            settings.setSchema(currentMethod.getSchema());
                            settings.setMethod(currentMethod);
                        }

                        final ObjectTreeModel objectTreeModel = new ObjectTreeModel(settings.getSchema(), settings.getVisibleObjectTypes(), settings.getMethod());

                        new SimpleLaterInvocator() {
                            @Override
                            protected void execute() {
                                final MethodExecutionBrowserDialog browserDialog = new MethodExecutionBrowserDialog(project, objectTreeModel, true);
                                browserDialog.show();
                                if (browserDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                                    DBMethod method = browserDialog.getSelectedMethod();
                                    MethodExecutionInput methodExecutionInput = executionManager.getExecutionInput(method);
                                    if (methodExecutionInput != null) {
                                        setExecutionInput(methodExecutionInput, true);
                                    }
                                }
                            }
                        }.start();

                    }
                };
                backgroundTask.start();
            }
        }
    }
    public class OpenMethodHistoryAction extends AnAction {
        public OpenMethodHistoryAction() {
            super("Execution History", null, Icons.METHOD_EXECUTION_HISTORY);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.getProject(e);
            if (project != null) {
                MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                methodExecutionManager.showExecutionHistoryDialog(getExecutionInput(), false, true, new SimpleTask<MethodExecutionInput>() {
                    @Override
                    protected void execute() {
                        MethodExecutionInput executionInput = getData();
                        if (executionInput != null) {
                            setExecutionInput(executionInput, true);
                        }
                    }
                });
            }
        }
    }

    public MethodExecutionInput getExecutionInput() {
        return methodExecutionInputForm == null ? null : methodExecutionInputForm.getExecutionInput();
    }

    public void writeConfiguration(DBMethodJdbcRunConfig configuration) {
        if (methodExecutionInputForm != null) {
            methodExecutionInputForm.updateExecutionInput();
            configuration.setExecutionInput(getExecutionInput());
        }
        configuration.setCompileDependencies(compileDependenciesCheckBox.isSelected());
        //selectMethodAction.setConfiguration(configuration);
    }

    public void readConfiguration(DBMethodJdbcRunConfig configuration) {
        setExecutionInput(configuration.getExecutionInput(), false);
        compileDependenciesCheckBox.setSelected(configuration.isCompileDependencies());
    }

    public void setExecutionInput(MethodExecutionInput executionInput, boolean touchForm) {
        methodArgumentsPanel.removeAll();
        DisposerUtil.dispose(methodExecutionInputForm);
        methodExecutionInputForm = null;

        String headerTitle = "No method selected";
        Icon headerIcon = null;
        Color headerBackground = UIUtil.getPanelBackground();

        if (executionInput != null) {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            headerTitle = methodRef.getPath();
            headerIcon = methodRef.getObjectType().getIcon();
            DBMethod method = executionInput.getMethod();
            if (method != null) {
                methodExecutionInputForm = new MethodExecutionInputForm(this, executionInput, false, DBDebuggerType.JDBC);
                Disposer.register(this, methodExecutionInputForm);
                methodArgumentsPanel.add(methodExecutionInputForm.getComponent(), BorderLayout.CENTER);
                if (touchForm) methodExecutionInputForm.touch();
                headerIcon = method.getOriginalIcon();
                if (getEnvironmentSettings(method.getProject()).getVisibilitySettings().getDialogHeaders().value()) {
                    headerBackground = method.getEnvironmentType().getColor();
                }
            }
        }

        DBNHeaderForm headerForm = new DBNHeaderForm(
                headerTitle,
                headerIcon,
                headerBackground,
                this);
        headerPanel.removeAll();
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void dispose() {
        super.dispose();
        methodExecutionInputForm = null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
