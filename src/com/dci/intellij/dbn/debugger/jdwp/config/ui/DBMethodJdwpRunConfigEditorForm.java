package com.dci.intellij.dbn.debugger.jdwp.config.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.config.ui.DBProgramRunConfigurationEditorForm;
import com.dci.intellij.dbn.debugger.jdwp.config.DBMethodJdwpRunConfig;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserDialog;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionInputForm;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Range;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class DBMethodJdwpRunConfigEditorForm extends DBProgramRunConfigurationEditorForm<DBMethodJdwpRunConfig>{
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JPanel methodArgumentsPanel;
    private JCheckBox compileDependenciesCheckBox;
    private JPanel selectMethodActionPanel;
    private JTextField fromPortTextField;
    private JTextField toPortTextField;
    private JPanel hintPanel;

    private MethodExecutionInputForm methodExecutionInputForm;

    public DBMethodJdwpRunConfigEditorForm(DBMethodJdwpRunConfig configuration) {
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

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    public class SelectMethodAction extends GroupPopupAction {
        SelectMethodAction()  {
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
        OpenMethodBrowserAction() {
            super("Method Browser");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.ensureProject(e);
            BackgroundTask.invoke(project,
                    instructions("Loading executable elements"),
                    (data, progress) -> {
                        MethodBrowserSettings settings = MethodExecutionManager.getInstance(project).getBrowserSettings();
                        MethodExecutionInput executionInput = getExecutionInput();
                        DBMethod currentMethod = executionInput == null ? null : executionInput.getMethod();
                        if (currentMethod != null) {
                            settings.setConnectionHandler(currentMethod.getConnectionHandler());
                            settings.setSchema(currentMethod.getSchema());
                            settings.setMethod(currentMethod);
                        }

                        DBSchema schema = settings.getSchema();
                        ObjectTreeModel objectTreeModel = DatabaseFeature.DEBUGGING.isSupported(schema) ?
                                new ObjectTreeModel(schema, settings.getVisibleObjectTypes(), settings.getMethod()) :
                                new ObjectTreeModel(null, settings.getVisibleObjectTypes(), null);

                        Dispatch.invoke(() -> {
                            MethodExecutionBrowserDialog browserDialog = new MethodExecutionBrowserDialog(project, objectTreeModel, true);
                            browserDialog.show();
                            if (browserDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                                DBMethod method = browserDialog.getSelectedMethod();
                                MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                                MethodExecutionInput methodExecutionInput = methodExecutionManager.getExecutionInput(method);
                                if (methodExecutionInput != null) {
                                    setExecutionInput(methodExecutionInput, true);
                                }
                            }
                        });

                    });
        }
    }
    public class OpenMethodHistoryAction extends AnAction {
        OpenMethodHistoryAction() {
            super("Execution History", null, Icons.METHOD_EXECUTION_HISTORY);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.ensureProject(e);
            MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
            methodExecutionManager.showExecutionHistoryDialog(getExecutionInput(), false, true,
                    SimpleTask.create((executionInput) -> {
                        if (executionInput != null) {
                            setExecutionInput(executionInput, true);
                        }
                    }));
        }
    }

    @Nullable
    public MethodExecutionInput getExecutionInput() {
        return methodExecutionInputForm == null ? null : methodExecutionInputForm.getExecutionInput();
    }

    @Override
    public void writeConfiguration(DBMethodJdwpRunConfig configuration) throws ConfigurationException {
        if (methodExecutionInputForm != null) {
            methodExecutionInputForm.updateExecutionInput();
            configuration.setExecutionInput(getExecutionInput());
        }
        configuration.setCompileDependencies(compileDependenciesCheckBox.isSelected());

        int fromPort;
        int toPort;
        try {
            fromPort = Integer.parseInt(fromPortTextField.getText());
            toPort = Integer.parseInt(toPortTextField.getText());
        } catch (NumberFormatException e) {
            throw new ConfigurationException("TCP Port Range inputs must me numeric");
        }
        configuration.setTcpPortRange(new Range<>(fromPort, toPort));
        //selectMethodAction.setConfiguration(configuration);
    }

    @Override
    public void readConfiguration(DBMethodJdwpRunConfig configuration) {
        setExecutionInput(configuration.getExecutionInput(), false);
        compileDependenciesCheckBox.setSelected(configuration.isCompileDependencies());
        fromPortTextField.setText(String.valueOf(configuration.getTcpPortRange().getFrom()));
        toPortTextField.setText(String.valueOf(configuration.getTcpPortRange().getTo()));
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
                methodExecutionInputForm = new MethodExecutionInputForm(this, executionInput, false, DBDebuggerType.JDWP);
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

        GUIUtil.repaint(mainPanel);
    }

    @Override
    public void dispose() {
        super.dispose();
        methodExecutionInputForm = null;
    }
}
