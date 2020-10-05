package com.dci.intellij.dbn.common.compatibility;

import com.intellij.find.editorHeaderActions.Utils;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CompatibilityUtil {
    private static final Key<PsiFile> HARD_REF_TO_PSI = Key.create("HARD_REFERENCE_TO_PSI");

    public static Color getEditorBackgroundColor(EditorEx editorEx) {
        return editorEx.getBackgroundColor();
    }

    public static ModuleType getModuleType(Module module) {
        //return module.getModuleType();
        return ModuleType.get(module);
    }

    public static void showSearchCompletionPopup(boolean byClickingToolbarButton, JComponent toolbarComponent, JBList list, String title, JTextField textField) {
        Utils.showCompletionPopup(byClickingToolbarButton ? toolbarComponent : null, list, title, textField, "", null);
    }


    public static void setSmallerFont(JComponent component) {
        Utils.setSmallerFont(component);
    }

    public static boolean isUnderGTKLookAndFeel() {
        return UIUtil.isUnderGTKLookAndFeel();
    }


    @Nullable
    public static FileEditor getSelectedEditor(Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        //return fileEditorManager.getSelectedEditor();

        VirtualFile[] files = fileEditorManager.getSelectedFiles();
        return files.length == 0 ? null : fileEditorManager.getSelectedEditor(files[0]);
    }
}
