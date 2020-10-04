package com.dci.intellij.dbn.editor.data.statusbar;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MathResult;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DatasetEditorStatusBarWidget extends AbstractProjectComponent implements CustomStatusBarWidget, FileEditorManagerListener {
    private static final String WIDGET_ID = DatasetEditorStatusBarWidget.class.getName();
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatasetEditorStatusBarWidget";

    private final JLabel textLabel;
    private final Alarm updateAlarm = new Alarm(this);
    private final JPanel component = new JPanel(new BorderLayout());

    DatasetEditorStatusBarWidget(@NotNull Project project) {
        super(project);
        textLabel = new JLabel();
        component.add(textLabel, BorderLayout.WEST);
    }

    public static DatasetEditorStatusBarWidget getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatasetEditorStatusBarWidget.class);
    }


    @NotNull
    @Override
    public String ID() {
        return WIDGET_ID;
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType type) {
        return null;
    }

    @Nullable
    private DatasetEditor getSelectedEditor() {
        Project project = getProject();
        FileEditor selectedEditor = CompatibilityUtil.getSelectedEditor(project);
        if (selectedEditor instanceof DatasetEditor) {
            return (DatasetEditor) selectedEditor;
        }
        return null;
    }

    @Override
    public void projectOpened() {}

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        update();
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        update();
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        update();
    }

    @Nullable
    private DatasetEditorTable getEditorTable() {
        DatasetEditor selectedEditor = getSelectedEditor();
        return selectedEditor == null ? null : selectedEditor.getEditorTable();
    }

    public void update() {
        updateAlarm.cancelAllRequests();
        updateAlarm.addRequest(() -> {
            DatasetEditorTable editorTable = getEditorTable();
            MathResult mathResult = Safe.call(editorTable, table -> table.getSelectionMath());

            if (mathResult == null) {
                textLabel.setText("");
                textLabel.setIcon(null);
            } else {
                textLabel.setText(" " +
                        "Sum " +  mathResult.getSum() + "   " +
                        "Count " + mathResult.getCount() + "   " +
                        "Average " + mathResult.getAverage());
                textLabel.setIcon(Icons.COMMON_DATA_GRID);
            }
            GUIUtil.repaint(getComponent());
        }, 100);
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        EventUtil.subscribe(getProject(), this, FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
    }

    @Override
    public JComponent getComponent() {
        return component;
    }
}
