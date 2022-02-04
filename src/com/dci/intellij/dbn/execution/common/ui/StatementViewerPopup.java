package com.dci.intellij.dbn.execution.common.ui;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

public class StatementViewerPopup implements Disposable {
    private final String resultName;
    private EditorEx viewer;

    public StatementViewerPopup(ExecutionResult executionResult) {
        this.resultName = executionResult.getName();
        Project project = executionResult.getProject();

        PsiFile previewFile = Failsafe.nn(executionResult.createPreviewFile());
        Document document = Documents.ensureDocument(previewFile);
        viewer = (EditorEx) EditorFactory.getInstance().createViewer(document, project);
        viewer.setEmbeddedIntoDialogWrapper(true);
        Editors.initEditorHighlighter(viewer, SQLLanguage.INSTANCE, executionResult.getConnectionHandler());
        viewer.setBackgroundColor(Colors.getEditorCaretRowBackground());

        JScrollPane viewerScrollPane = viewer.getScrollPane();
        viewerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        viewerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        viewerScrollPane.setViewportBorder(Borders.lineBorder(Colors.getEditorCaretRowBackground(), 4));
        viewerScrollPane.setBorder(null);


        EditorSettings settings = viewer.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);

        //mainPanel.setBorder(new LineBorder(Color.BLACK, 1, false));
    }

    public void show(Component component) {
        JBPopup popup = createPopup();
        popup.showInScreenCoordinates(component,
                new Point(
                        (int) (component.getLocationOnScreen().getX() + component.getWidth() +8),
                        (int) component.getLocationOnScreen().getY()));
    }

    public void show(Component component, Point point) {
        JBPopup popup = createPopup();
        point.setLocation(
                point.getX() + component.getLocationOnScreen().getX() + 16,
                point.getY() + component.getLocationOnScreen().getY() + 16);

        popup.showInScreenCoordinates(component, point);
    }

    private JBPopup createPopup() {
        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(viewer.getComponent(), viewer.getContentComponent());
        popupBuilder.setMovable(true);
        popupBuilder.setResizable(true);
        popupBuilder.setRequestFocus(true);
        popupBuilder.setTitle("<html>" + resultName + "</html>");
        JBPopup popup = popupBuilder.createPopup();

        Dimension dimension = Editors.calculatePreferredSize(viewer);
        //Dimension dimension = ((EditorImpl) viewer).getPreferredSize();
        dimension.setSize(Math.min(dimension.getWidth() + 20, 1000), Math.min(dimension.getHeight() + 70, 800) );
        popup.setSize(dimension);

        popup.addListener(new JBPopupAdapter() {
            @Override
            public void onClosed(@NotNull LightweightWindowEvent event) {
                dispose();
            }
        });
        return popup;
    }

    @Override
    public void dispose() {
        if (viewer != null) {
            Editors.releaseEditor(viewer);
            viewer = null;
        }
    }
}
