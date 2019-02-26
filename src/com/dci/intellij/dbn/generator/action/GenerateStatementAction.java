package com.dci.intellij.dbn.generator.action;

import com.dci.intellij.dbn.common.thread.Command;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Write;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public abstract class GenerateStatementAction extends DumbAwareAction implements ConnectionProvider {
    GenerateStatementAction(String text) {
        super(text);
    }

    @Override
    public final void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        ConnectionAction.invoke("generating the statement", false, this,
                (action) -> Progress.prompt(project, "Extracting select statement", true,
                        (progress) -> {
                            StatementGeneratorResult result = generateStatement(project);
                            if (result.getMessages().hasErrors()) {
                                MessageUtil.showErrorDialog(project, "Error generating statement", result.getMessages());
                            } else {
                                pasteStatement(result, project);
                            }
                        }));
    }

    private void pasteStatement(StatementGeneratorResult result, Project project) {
        Dispatch.invokeNonModal(() -> {
            Editor editor = EditorUtil.getSelectedEditor(project, SQLFileType.INSTANCE);
            if (editor != null)
                pasteToEditor(editor, result); else
                pasteToClipboard(result, project);
        });
    }

    private static void pasteToClipboard(StatementGeneratorResult result, Project project) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(result.getStatement()), null);
        MessageUtil.showInfoDialog(project, "Statement extracted", "SQL statement exported to clipboard.");
    }

    private static void pasteToEditor(final Editor editor, final StatementGeneratorResult generatorResult) {
        Command.run(
                editor.getProject(),
                "Extract statement",
                () -> Write.run(
                        () -> {
                            String statement = generatorResult.getStatement();
                            PsiUtil.moveCaretOutsideExecutable(editor);
                            int offset = EditorModificationUtil.insertStringAtCaret(editor, statement + "\n\n", false, true);
                            offset = offset - statement.length() - 2;
                        /*editor.getMarkupModel().addRangeHighlighter(offset, offset + statement.length(),
                                HighlighterLayer.SELECTION,
                                EditorColorsManager.getInstance().getGlobalScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES),
                                HighlighterTargetArea.EXACT_RANGE);*/
                            editor.getSelectionModel().setSelection(offset, offset + statement.length());
                            editor.getCaretModel().moveToOffset(offset);
                        }));
    }

    protected abstract StatementGeneratorResult generateStatement(Project project);
}
