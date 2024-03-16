package com.dbn.object.filter.custom.ui;

import com.dbn.common.color.Colors;
import com.dbn.common.expression.ExpressionEvaluator;
import com.dbn.common.expression.ExpressionEvaluatorContext;
import com.dbn.common.icon.Icons;
import com.dbn.common.text.TextContent;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.common.ui.form.DBNHintForm;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Commons;
import com.dbn.common.util.Documents;
import com.dbn.common.util.Editors;
import com.dbn.connection.ConnectionHandler;
import com.dbn.diagnostics.Diagnostics;
import com.dbn.language.sql.SQLFileType;
import com.dbn.language.sql.SQLLanguage;
import com.dbn.object.filter.custom.ObjectFilter;
import com.dbn.object.filter.custom.ObjectFilterAttribute;
import com.dbn.object.filter.custom.ObjectFilterDefinition;
import com.dbn.object.type.DBObjectType;
import com.dbn.vfs.DatabaseFileViewProvider;
import com.dbn.vfs.file.DBObjectFilterExpressionFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.dbn.common.util.Commons.nvl;
import static com.dbn.common.util.Lists.greatest;
import static com.dbn.common.util.Lists.toCsv;
import static com.dbn.common.util.Strings.toUpperCase;
import static org.apache.commons.lang3.StringUtils.rightPad;

public class ObjectFilterDetailsForm extends DBNFormBase {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel editorPanel;
    private JLabel errorLabel;
    private JLabel expressionLabel;
    private JPanel hintPanel;

    private final ObjectFilter<?> filter;
    private ExpressionEvaluatorContext context;
    private Document document;
    private EditorEx editor;
    private String expression;

    public ObjectFilterDetailsForm(ObjectFilterDetailsDialog parent) {
        super(parent);

        filter = parent.getFilter();
        initHeaderPanel();
        initHintPanel();
        initExpressionEditor();
        initErrorLabel(null, null);
    }

    private void initHintPanel() {
        TextContent hintText = loadHintText();
        ObjectFilterDefinition<?> definition = filter.getDefinition();
        List<ObjectFilterAttribute> attributes = definition.getAttributes();
        int longestAttr = greatest(attributes, a -> a.getName().length());

        String supportedAttributes = toCsv(attributes, "\n    ", s ->
                "<li>" +
                rightPad(s.getName(), longestAttr + 1, " ").replaceAll(" ", "&nbsp;") +
                "- " + s.getDescription() +
                " (" + s.getTypeName() + ")");
        hintText = hintText.replaceFields("SUPPORTED_ATTRIBUTES", supportedAttributes);
        hintText = hintText.replaceFields("SAMPLE_EXPRESSION", definition.getSampleExpression());
        DBNHintForm disclaimerForm = new DBNHintForm(this, hintText, null, true);
        hintPanel.add(disclaimerForm.getComponent());
    }

    @SneakyThrows
    private static TextContent loadHintText() {
        String content = Commons.readInputStream(ObjectFilterDetailsForm.class.getResourceAsStream("object_filter_expression_guide.html"));
        return TextContent.html(content);
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

    private ObjectFilterDetailsDialog getDialog() {
        return getParentComponent();
    }

    private void initErrorLabel(String error, String expression) {
        if (error == null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        } else {
            errorLabel.setVisible(Diagnostics.isDeveloperMode());
            errorLabel.setText(error);
            errorLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        }
        expressionLabel.setText(nvl(expression, "").replaceAll("\\n", " "));
    }

    private void initHeaderPanel() {
        DBObjectType objectType = filter.getObjectType();
        ConnectionHandler connection = filter.getSettings().getConnection();

        JBColor color = connection.getEnvironmentType().getColor();
        Icon icon = objectType.getIcon();
        String title = toUpperCase(objectType.getName());

        DBNHeaderForm headerForm = new DBNHeaderForm(this, title, icon, color);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
    }

    private void initExpressionEditor() {
        Project project = filter.getProject();
        DBObjectFilterExpressionFile expressionFile = new DBObjectFilterExpressionFile(filter);
        DatabaseFileViewProvider viewProvider = new DatabaseFileViewProvider(project, expressionFile, true);
        PsiFile selectStatementFile = expressionFile.initializePsiFile(viewProvider, SQLLanguage.INSTANCE);

        document = Documents.ensureDocument(selectStatementFile);
        editor = Editors.createEditor(document, project, expressionFile, SQLFileType.INSTANCE);
        Editors.initEditorHighlighter(editor, SQLLanguage.INSTANCE, filter.getConnection());

        editor.setEmbeddedIntoDialogWrapper(true);
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                expression = event.getDocument().getText();
                verifyExpression();
            }
        });

        JScrollPane editorScrollPane = editor.getScrollPane();
        editorScrollPane.setViewportBorder(Borders.lineBorder(Colors.getEditorBackground(), 4));

        EditorSettings settings = editor.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);
        settings.setUseTabCharacter(true);

        editorPanel.add(editor.getComponent(), BorderLayout.CENTER);
    }

    public String getExpression() {
        return document.getText();
    }

    private void verifyExpression() {
        Dispatch.background(getProject(),
                () -> verifyExpression(filter),
                c -> applyVerificationResult(c));
    }

    private ExpressionEvaluatorContext verifyExpression(ObjectFilter<?> filter) {
        getDialog().setActionEnabled(false);
        context = filter.createTestEvaluationContext();
        ExpressionEvaluator evaluator = filter.getSettings().getExpressionEvaluator();
        evaluator.verifyExpression(expression, context, Boolean.class);
        return context;
    }

    private void applyVerificationResult(ExpressionEvaluatorContext context) {
        if (this.context != context) return;
        boolean valid = context.isValid();
        String error = valid ? null : "Invalid or incomplete expression";
        initErrorLabel(error, context.getExpression());

        getDialog().setActionEnabled(valid);
    }

    public void disposeInner() {
        Editors.releaseEditor(editor);
        editor = null;
        document = null;
        super.disposeInner();
    }
}
