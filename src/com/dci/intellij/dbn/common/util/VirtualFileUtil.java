package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.io.ReadOnlyAttributeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualFileUtil {

    public static Icon getIcon(VirtualFile virtualFile) {
        if (virtualFile instanceof DBVirtualFileImpl) {
            DBVirtualFileImpl file = (DBVirtualFileImpl) virtualFile;
            return file.getIcon();
        }
        return virtualFile.getFileType().getIcon();
    }

    public static boolean isDatabaseFileSystem(@NotNull VirtualFile file) {
        return file.getFileSystem() instanceof DatabaseFileSystem;
    }

    public static boolean isLocalFileSystem(@NotNull VirtualFile file) {
        return file.isInLocalFileSystem();
    }

    public static boolean isVirtualFileSystem(@NotNull VirtualFile file) {
        return !isDatabaseFileSystem(file) && !isLocalFileSystem(file);
    }    

    public static VirtualFile ioFileToVirtualFile(File file) {
        return LocalFileSystem.getInstance().findFileByIoFile(file);
    }

    public static void setReadOnlyAttribute(VirtualFile file, boolean readonly) {
        try {
            ReadOnlyAttributeUtil.setReadOnlyAttribute(file, readonly);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setReadOnlyAttribute(String path, boolean readonly) {
        try {
            ReadOnlyAttributeUtil.setReadOnlyAttribute(path, readonly);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static VirtualFile[] lookupFilesForName(Project project, String name) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] contentRoots = rootManager.getContentRoots();
        return lookupFilesForName(contentRoots, name);
    }

    public static VirtualFile[] lookupFilesForName(Module module, String name) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(module.getProject());
        VirtualFile[] contentRoots = rootManager.getContentRoots();
        return lookupFilesForName(contentRoots, name);
    }

    public static VirtualFile[] lookupFilesForName(VirtualFile[] roots, String name) {
        List<VirtualFile> bucket = new ArrayList<VirtualFile>();
        for (VirtualFile root: roots) {
            collectFilesForName(root, name, bucket);
        }
        return bucket.toArray(new VirtualFile[0]);
    }

    private static void collectFilesForName(VirtualFile root, String name, List<VirtualFile> bucket) {
        for (VirtualFile virtualFile: root.getChildren()) {
            boolean fileIgnored = FileTypeManager.getInstance().isFileIgnored(virtualFile.getName());
            if (!fileIgnored) {
                if (virtualFile.isDirectory() ) {
                    collectFilesForName(virtualFile, name, bucket);
                } else {
                    if (StringUtil.equalsIgnoreCase(virtualFile.getName(), name)) {
                        bucket.add(virtualFile);
                    }
                }
            }
        }
    }

    public static String ensureFilePath(String fileUrlOrPath) {
        if (fileUrlOrPath != null && fileUrlOrPath.startsWith(StandardFileSystems.FILE_PROTOCOL_PREFIX)) {
            return fileUrlOrPath.substring(StandardFileSystems.FILE_PROTOCOL_PREFIX.length());
        }
        return fileUrlOrPath;
    }

    public static String ensureFileUrl(String fileUrlOrPath) {
        if (fileUrlOrPath != null && !fileUrlOrPath.startsWith(StandardFileSystems.FILE_PROTOCOL_PREFIX)) {
            return StandardFileSystems.FILE_PROTOCOL_PREFIX + fileUrlOrPath;
        }
        return fileUrlOrPath;
    }

    @Nullable
    public static VirtualFile getOriginalFile(VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile) {
            LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
            return lightVirtualFile.getOriginalFile();
        }
        return null;
    }

}

