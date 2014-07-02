package com.dci.intellij.dbn.common.compatibility;

import com.dci.intellij.dbn.vfs.SourceCodeFile;
import com.intellij.find.editorHeaderActions.Utils;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;

import javax.swing.JComponent;
import javax.swing.JTextField;
import java.awt.Color;

public class CompatibilityUtil {
    public static Color getEditorBackgroundColor(EditorEx editorEx) {
        return editorEx.getBackgroundColor();
    }

    public static ModuleType getModuleType(Module module) {
        //return module.getModuleType();
        return ModuleType.get(module);
    }

    public static void showSearchCompletionPopup(boolean byClickingToolbarButton, JComponent toolbarComponent, JBList list, String title, JTextField textField) {
        //Utils.showCompletionPopup(byClickingToolbarButton ? toolbarComponent : null, list, title, textField);
        Utils.showCompletionPopup(byClickingToolbarButton ? toolbarComponent : null, list, title, textField, "");
    }

    public static void setSmallerFontForChildren(JComponent component) {
        Utils.setSmallerFontForChildren(component);
    }

    public static void setSmallerFont(JComponent component) {
        Utils.setSmallerFont(component);
    }

    public static boolean isUnderGTKLookAndFeel() {
        return UIUtil.isUnderGTKLookAndFeel();
    }

    public static String getParseRootId(VirtualFile virtualFile) {
        if (virtualFile instanceof SourceCodeFile) {
            SourceCodeFile sourceCodeFile = (SourceCodeFile) virtualFile;
            return sourceCodeFile.getParseRootId();
        } else if (virtualFile instanceof LightVirtualFile) {
            LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
            VirtualFile originalFile = lightVirtualFile.getOriginalFile();
            return getParseRootId(originalFile);
        }
        return null;
    }

}
