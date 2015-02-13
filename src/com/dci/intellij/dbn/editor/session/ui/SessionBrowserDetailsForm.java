package com.dci.intellij.dbn.editor.session.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.SessionBrowserStatementVirtualFile;
import com.dci.intellij.dbn.editor.session.details.SessionDetailsTableModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBScrollPane;

public class SessionBrowserDetailsForm extends DBNFormImpl{
    private JPanel mailPanel;
    private JPanel statementViewerPanel;
    private JBScrollPane sessionDetailsTablePane;
    private Document statementDocument;
    private EditorEx statementViewer;
    private SessionBrowser sessionBrowser;
    private DBNTable sessionDetailsTable;

    public SessionBrowserDetailsForm(SessionBrowser sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
        SessionDetailsTableModel sessionDetailsTableModel = new SessionDetailsTableModel(null);
        sessionDetailsTable = new DBNTable(sessionBrowser.getProject(), sessionDetailsTableModel, false);
        sessionDetailsTablePane.setViewportView(sessionDetailsTable);
        sessionDetailsTablePane.getViewport().setBackground(sessionDetailsTable.getBackground());

        Disposer.register(this, sessionDetailsTable);
        createStatementViewer();
    }


    public void update(@Nullable SessionBrowserModelRow selectedRow) {
        SessionDetailsTableModel model = new SessionDetailsTableModel(selectedRow);
        sessionDetailsTable.setModel(model);
    }

    private void setPreviewText(final String text) {
        new WriteActionRunner() {
            @Override
            public void run() {
                statementDocument.setText(text);
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
        SessionBrowserStatementVirtualFile previewFile = new SessionBrowserStatementVirtualFile(connectionHandler, "select * from dual");
        DatabaseFileViewProvider viewProvider = new DatabaseFileViewProvider(PsiManager.getInstance(project), previewFile, true);
        PsiFile previewPsiFile = previewFile.initializePsiFile(viewProvider, SQLLanguage.INSTANCE);

        statementDocument = DocumentUtil.getDocument(previewPsiFile);


        statementViewer = (EditorEx) EditorFactory.getInstance().createViewer(statementDocument, project);
        statementViewer.setEmbeddedIntoDialogWrapper(true);
        JScrollPane viewerScrollPane = statementViewer.getScrollPane();
        SyntaxHighlighter syntaxHighlighter = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE).getSyntaxHighlighter();
        EditorColorsScheme colorsScheme = statementViewer.getColorsScheme();
        statementViewer.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
        //statementViewer.setBackgroundColor(colorsScheme.getColor(ColorKey.find("CARET_ROW_COLOR")));
        viewerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        viewerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //viewerScrollPane.setBorder(null);
        viewerScrollPane.setViewportBorder(new LineBorder(CompatibilityUtil.getEditorBackgroundColor(statementViewer), 4, false));

        EditorSettings settings = statementViewer.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);
        statementViewer.getComponent().setFocusable(false);
        statementViewerPanel.add(statementViewer.getComponent(), BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            EditorFactory.getInstance().releaseEditor(statementViewer);
            statementViewer = null;
            statementDocument = null;
        }


    }
}
