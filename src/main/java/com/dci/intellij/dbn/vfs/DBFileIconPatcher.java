package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.CompoundIcons;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.ide.FileIconPatcher;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DBFileIconPatcher implements FileIconPatcher {
    @Override
    public Icon patchIcon(Icon baseIcon, VirtualFile file, int flags, @Nullable Project project) {
        if (file instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile objectFile = (DBEditableObjectVirtualFile) file;
            if (!objectFile.isModified()) return baseIcon;

            return CompoundIcons.addModifiedOverlay(baseIcon);
        }
        return baseIcon;
    }
}
