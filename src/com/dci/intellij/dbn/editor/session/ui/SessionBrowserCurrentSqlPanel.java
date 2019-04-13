package com.dci.intellij.dbn.editor.session.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.SessionBrowserManager;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.PsiFileRef;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.dci.intellij.dbn.vfs.file.DBSessionStatementVirtualFile;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SessionBrowserCurrentSqlPanel extends DBNFormImpl{
    private JPanel actionsPanel;
    private JPanel viewerPanel;
    private JPanel mainPanel;


    private DBSessionStatementVirtualFile virtualFile;
    private PsiFileRef<DBLanguagePsiFile> psiFileRef;
    private Document document;
    private EditorEx viewer;
    private SessionBrowser sessionBrowser;
    private Object selectedSessionId;


    SessionBrowserCurrentSqlPanel(SessionBrowser sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
        createStatementViewer();

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, new RefreshAction(), new WrapUnwrapContentAction());
        actionsPanel.add(actionToolbar.getComponent(),BorderLayout.WEST);

    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    private void setPreviewText(String text) {
        DocumentUtil.setText(document, text);
    }

    private void setSchemaId(SchemaId schemaId) {
        getVirtualFile().setSchemaId(schemaId);
    }

    @NotNull
    public DBSessionStatementVirtualFile getVirtualFile() {
        return Failsafe.nn(virtualFile);
    }

    void loadCurrentStatement() {
        SessionBrowserTable editorTable = sessionBrowser.getEditorTable();
        if (editorTable.getSelectedRowCount() == 1) {
            SessionBrowserModelRow selectedRow = editorTable.getModel().getRowAtIndex(editorTable.getSelectedRow());
            if (selectedRow != null) {
                setPreviewText("-- Loading...");
                selectedSessionId = selectedRow.getSessionId();

                Object sessionId = selectedSessionId;
                String schemaName = selectedRow.getSchema();
                Project project = sessionBrowser.getProject();

                Background.run(() -> {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    DBSchema schema = null;
                    if (StringUtil.isNotEmpty(schemaName)) {
                        schema = connectionHandler.getObjectBundle().getSchema(schemaName);
                    }

                    checkCancelled(sessionId);
                    SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(project);
                    String sql = sessionBrowserManager.loadSessionCurrentSql(connectionHandler, sessionId);

                    checkCancelled(sessionId);
                    setSchemaId(SchemaId.from(schema));
                    setPreviewText(sql.replace("\r\n", "\n"));
                });
            } else {
                setPreviewText("");
            }
        } else {
            setPreviewText("");
        }
    }

    private void checkCancelled(Object sessionId) {
        if (selectedSessionId == null || !selectedSessionId.equals(sessionId)) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    @NotNull
    private ConnectionHandler getConnectionHandler() {
        return Failsafe.nn(sessionBrowser.getConnectionHandler());
    }

    public DBLanguagePsiFile getPsiFile() {
        return psiFileRef.get();
    }

    private void createStatementViewer() {
        Project project = sessionBrowser.getProject();
        ConnectionHandler connectionHandler = getConnectionHandler();
        virtualFile = new DBSessionStatementVirtualFile(sessionBrowser, "");
        DatabaseFileViewProvider viewProvider = new DatabaseFileViewProvider(PsiManager.getInstance(project), virtualFile, true);
        DBLanguagePsiFile psiFile = (DBLanguagePsiFile) virtualFile.initializePsiFile(viewProvider, SQLLanguage.INSTANCE);
        psiFileRef = PsiFileRef.from(psiFile);
        document = DocumentUtil.getDocument(psiFile);


        viewer = (EditorEx) EditorFactory.getInstance().createViewer(document, project);
        viewer.setEmbeddedIntoDialogWrapper(true);
        JScrollPane viewerScrollPane = viewer.getScrollPane();
        SyntaxHighlighter syntaxHighlighter = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE).getSyntaxHighlighter();
        EditorColorsScheme colorsScheme = viewer.getColorsScheme();
        viewer.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
        //statementViewer.setBackgroundColor(colorsScheme.getColor(ColorKey.find("CARET_ROW_COLOR")));
        viewerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        viewerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //viewerScrollPane.setBorder(null);
        viewerScrollPane.setViewportBorder(new LineBorder(CompatibilityUtil.getEditorBackgroundColor(viewer), 4, false));

        EditorSettings settings = viewer.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);
        settings.setUseSoftWraps(true);
        viewer.getComponent().setFocusable(false);

        viewerPanel.add(viewer.getComponent(), BorderLayout.CENTER);
    }


    public class WrapUnwrapContentAction extends ToggleAction {
        WrapUnwrapContentAction() {
            super("Wrap/Unwrap", "", Icons.ACTION_WRAP_TEXT);
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return viewer != null && viewer.getSettings().isUseSoftWraps();
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            viewer.getSettings().setUseSoftWraps(state);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            boolean isWrapped = viewer != null && viewer.getSettings().isUseSoftWraps();
            e.getPresentation().setText(isWrapped ? "Unwrap Content" : "Wrap Content");

        }
    }

    public class RefreshAction extends AnAction {
        RefreshAction() {
            super("Reload", "", Icons.ACTION_REFRESH);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            loadCurrentStatement();
        }
    }

    public EditorEx getViewer() {
        return viewer;
    }

    @Override
    public void disposeInner() {
        EditorUtil.releaseEditor(viewer);
        super.disposeInner();
    }
}
