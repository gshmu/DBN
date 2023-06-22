package com.dci.intellij.dbn.execution.method.result.ui;

import com.dci.intellij.dbn.common.action.Lookups;
import com.dci.intellij.dbn.common.action.ProjectAction;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.misc.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.dci.intellij.dbn.editor.data.options.DataEditorQualifiedEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

public class MethodExecutionLargeValueResultForm extends DBNFormBase {
    private JPanel actionsPanel;
    private JPanel mainPanel;
    private JPanel largeValuePanel;

    private final DBObjectRef<DBArgument> argumentRef;
    private EditorEx editor;
    private TextContentType contentType;

    MethodExecutionLargeValueResultForm(MethodExecutionResultForm parent, MethodExecutionResult executionResult, DBArgument argument) {
        super(parent);
        argumentRef = DBObjectRef.of(argument);

        ArgumentValue argumentValue = executionResult.getArgumentValue(argumentRef);
        LargeObjectValue value = (LargeObjectValue) argumentValue.getValue();
        String text = null;
        Project project = getProject();
        try {
            text = value.read();
        } catch (SQLException e) {
            conditionallyLog(e);
            Messages.showWarningDialog(project, "Load error", "Could not load value for argument " + argument.getName() + ". Cause: " + e.getMessage());
        }

        Document document = EditorFactory.getInstance().createDocument(text == null ? "" : Strings.removeCharacter(text, '\r'));
        contentType = TextContentType.get(project, argument.getDataType().getContentTypeName());
        if (contentType == null) contentType = TextContentType.getPlainText(project);

        editor = (EditorEx) EditorFactory.getInstance().createEditor(document, project, contentType.getFileType(), false);
        editor.getContentComponent().setFocusTraversalKeysEnabled(false);

        largeValuePanel.add(editor.getComponent(), BorderLayout.CENTER);


        largeValuePanel.setBorder(IdeBorderFactory.createBorder());

        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,
                "DBNavigator.Place.MethodExecutionResult.LobContentTypeEditor", true,
                new ContentTypeComboBoxAction());
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);


/*
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true,
                new CursorResultFetchNextRecordsAction(executionResult, resultTable),
                new CursorResultViewRecordAction(resultTable),
                ActionUtil.SEPARATOR,
                new CursorResultExportAction(resultTable, argument));

        actionsPanel.add(actionToolbar.getComponent());
*/
    }

    public void setContentType(TextContentType contentType) {
        Editors.initEditorHighlighter(editor, contentType);
    }

    public DBArgument getArgument() {
        return argumentRef.get();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public class ContentTypeComboBoxAction extends DBNComboBoxAction {

        ContentTypeComboBoxAction() {
            Presentation presentation = getTemplatePresentation();
            presentation.setText(contentType.getName());
            presentation.setIcon(contentType.getIcon());
        }

        @Override
        @NotNull
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            Project project = Lookups.getProject(button);
            DataEditorQualifiedEditorSettings qualifiedEditorSettings = DataEditorSettings.getInstance(project).getQualifiedEditorSettings();

            DefaultActionGroup actionGroup = new DefaultActionGroup();
            for (TextContentType contentType : qualifiedEditorSettings.getContentTypes()) {
                if (contentType.isSelected()) {
                    actionGroup.add(new ContentTypeSelectAction(contentType));
                }

            }
            return actionGroup;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText(contentType.getName());
            presentation.setIcon(contentType.getIcon());
        }
    }

    public class ContentTypeSelectAction extends ProjectAction {
        private final TextContentType contentType;

        ContentTypeSelectAction(TextContentType contentType) {
            super(contentType.getName(), null, contentType.getIcon());
            this.contentType = contentType;
        }

        public TextContentType getContentType() {
            return contentType;
        }

        @Override
        protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
            Editors.initEditorHighlighter(editor, contentType);
            MethodExecutionLargeValueResultForm.this.contentType = contentType;
        }
    }

    @Override
    protected void disposeInner() {
        Editors.releaseEditor(editor);
        editor = null;
    }
}
