package com.dci.intellij.dbn.editor.session.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.SessionBrowserManager;
import com.dci.intellij.dbn.editor.session.SessionBrowserStatementVirtualFile;
import com.dci.intellij.dbn.editor.session.details.SessionDetailsTable;
import com.dci.intellij.dbn.editor.session.details.SessionDetailsTableModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;

public class SessionBrowserDetailsForm extends DBNFormImpl{
    private JPanel mailPanel;
    private JPanel statementViewerPanel;
    private JBScrollPane sessionDetailsTablePane;
    private SessionBrowserStatementVirtualFile currentSqlFile;
    private Document currentSqlDocument;
    private EditorEx currentSqlViewer;
    private SessionBrowser sessionBrowser;
    private SessionDetailsTable sessionDetailsTable;

    public SessionBrowserDetailsForm(SessionBrowser sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
        sessionDetailsTable = new SessionDetailsTable(sessionBrowser.getProject());
        sessionDetailsTablePane.setViewportView(sessionDetailsTable);
        sessionDetailsTablePane.getViewport().setBackground(sessionDetailsTable.getBackground());
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mailPanel);
        JBSplitter splitter = (JBSplitter) mailPanel.getComponent(0);
        splitter.setProportion((float) 0.3);

        Disposer.register(this, sessionDetailsTable);
        createStatementViewer();
    }


    public void update(@Nullable final SessionBrowserModelRow selectedRow) {
        SessionDetailsTableModel model = new SessionDetailsTableModel(selectedRow);
        sessionDetailsTable.setModel(model);
        sessionDetailsTable.accommodateColumnsSize();
        if (selectedRow == null) {
            setPreviewText("");
        } else {
            final Object sessionId = selectedRow.getSessionId();
            final Project project = sessionBrowser.getProject();
            new BackgroundTask(project, "Loading session current SQL", true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
                    String schemaName = selectedRow.getSchema();
                    DBSchema schema = null;
                    if (StringUtil.isNotEmpty(schemaName)) {
                        schema = connectionHandler.getObjectBundle().getSchema(schemaName);
                    }


                    SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(project);
                    String sql = sessionBrowserManager.loadSessionCurrentSql(connectionHandler, sessionId);
                    if (sessionId.equals(sessionBrowser.getSelectedSessionId())) {
                        currentSqlFile.setCurrentSchema(schema);
                        setPreviewText(sql);
                    }
                }
            }.start();
        }
    }

    private void setPreviewText(final String text) {
        new WriteActionRunner() {
            @Override
            public void run() {
                currentSqlDocument.setText(text);
            }
        }.start();
    }

    @Override
    public JComponent getComponent() {
        return mailPanel;
    }

    private void createStatementViewer() {
        ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
        Project project = sessionBrowser.getProject();
        currentSqlFile = new SessionBrowserStatementVirtualFile(connectionHandler, "");
        DatabaseFileViewProvider viewProvider = new DatabaseFileViewProvider(PsiManager.getInstance(project), currentSqlFile, true);
        PsiFile previewPsiFile = currentSqlFile.initializePsiFile(viewProvider, SQLLanguage.INSTANCE);

        currentSqlDocument = DocumentUtil.getDocument(previewPsiFile);


        currentSqlViewer = (EditorEx) EditorFactory.getInstance().createViewer(currentSqlDocument, project);
        currentSqlViewer.setEmbeddedIntoDialogWrapper(true);
        JScrollPane viewerScrollPane = currentSqlViewer.getScrollPane();
        SyntaxHighlighter syntaxHighlighter = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE).getSyntaxHighlighter();
        EditorColorsScheme colorsScheme = currentSqlViewer.getColorsScheme();
        currentSqlViewer.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
        //statementViewer.setBackgroundColor(colorsScheme.getColor(ColorKey.find("CARET_ROW_COLOR")));
        viewerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        viewerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //viewerScrollPane.setBorder(null);
        viewerScrollPane.setViewportBorder(new LineBorder(CompatibilityUtil.getEditorBackgroundColor(currentSqlViewer), 4, false));

        EditorSettings settings = currentSqlViewer.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);
        currentSqlViewer.getComponent().setFocusable(false);
        statementViewerPanel.add(currentSqlViewer.getComponent(), BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            EditorFactory.getInstance().releaseEditor(currentSqlViewer);
            currentSqlViewer = null;
            currentSqlFile = null;
            currentSqlDocument = null;
        }


    }
}
