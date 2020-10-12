package com.dci.intellij.dbn.ddl.ui;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FileListCellRenderer extends ColoredListCellRenderer<VirtualFile> {
    private final ProjectRef projectRef;

    FileListCellRenderer(Project project) {
        this.projectRef = ProjectRef.of(project);
    }

    @Override
    protected void customizeCellRenderer(@NotNull JList list, VirtualFile value, int index, boolean selected, boolean hasFocus) {
        Project project = getProject();
        Module module = ModuleUtil.findModuleForFile(value, project);
        if (module == null) {
            append(value.getPath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else {
            VirtualFile contentRoot = getModuleContentRoot(module, value);
            VirtualFile parent = contentRoot == null ? null : contentRoot.getParent();
            int relativePathIndex = parent == null ? 0 : parent.getPath().length();
            String relativePath = value.getPath().substring(relativePathIndex);
            append('[' + module.getName() + ']', SimpleTextAttributes.REGULAR_ATTRIBUTES);
            append(relativePath, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }

        setIcon(VirtualFileUtil.getIcon(value));
    }

    @NotNull
    private Project getProject() {
        return projectRef.ensure();
    }

    private static VirtualFile getModuleContentRoot(Module module, VirtualFile virtualFile) {
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] contentRoots = rootManager.getContentRoots();

        while (virtualFile != null) {
            virtualFile = virtualFile.getParent();
            for (VirtualFile contentRoot : contentRoots) {
                if (contentRoot.equals(virtualFile)) {
                    return contentRoot;
                }
            }
        }
        return null;
    }
}
