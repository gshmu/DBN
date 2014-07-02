package com.dci.intellij.dbn.ddl.ui;

import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.JList;

public class FileListCellRenderer extends ColoredListCellRenderer {
    private Project project;

    public FileListCellRenderer(Project project) {
        this.project = project;
    }

    protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        VirtualFile virtualFile = (VirtualFile) value;

        Module module = ModuleUtil.findModuleForFile(virtualFile, project);
        if (module == null) {
            append(virtualFile.getPath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else {
            VirtualFile contentRoot = getModuleContentRoot(module, virtualFile);
            String relativePath = virtualFile.getPath().substring(contentRoot.getParent().getPath().length());
            append("[" + module.getName() + "]", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            append(relativePath, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }

        setIcon(VirtualFileUtil.getIcon(virtualFile));
    }

    private VirtualFile getModuleContentRoot(Module module, VirtualFile virtualFile) {
        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] contentRoots = rootManager.getContentRoots();

        while (virtualFile != null) {
            virtualFile = virtualFile.getParent();
            for (VirtualFile contentRoot : contentRoots) {
                if (contentRoot == virtualFile) {
                    return contentRoot;
                }
            }
        }
        return null;
    }
}
