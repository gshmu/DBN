package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FileUtil {
    public static File createFileByRelativePath(@NotNull final File absoluteBase, @NotNull final String relativeTail) {
        // assert absoluteBase.isAbsolute() && absoluteBase.isDirectory(); : assertion seem to be too costly

        File point = absoluteBase;
        final String[] parts = relativeTail.replace('\\', '/').split("/");
        // do not validate, just apply rules
        for (String part : parts) {
            final String trimmed = part.trim();
            if (trimmed.length() == 0) continue;
            if (".".equals(trimmed)) continue;
            if ("..".equals(trimmed)) {
                point = point.getParentFile();
                if (point == null) return null;
                continue;
            }
            point = new File(point, trimmed);
        }
        return point;
    }

    public static String convertToRelativePath(Project project, String path) {
        if (!StringUtil.isEmptyOrSpaces(path)) {
            VirtualFile baseDir = project.getBaseDir();
            if (baseDir != null) {
                File projectDir = new File(baseDir.getPath());
                String relativePath = com.intellij.openapi.util.io.FileUtil.getRelativePath(projectDir, new File(path));
                if (relativePath != null) {
                    if (relativePath.lastIndexOf(".." + File.separatorChar) < 1) {
                        return relativePath;
                    }
                }
            }
        }
        return path;
    }

    public static String convertToAbsolutePath(Project project, String path) {
        if (!StringUtil.isEmptyOrSpaces(path)) {
            VirtualFile baseDir = project.getBaseDir();
            if (baseDir != null) {
                File projectDir = new File(baseDir.getPath());
                if (new File(path).isAbsolute()) {
                    return path;
                } else {
                    File file = FileUtil.createFileByRelativePath(projectDir, path);
                    return file == null ? null : file.getPath();
                }
            }
        }
        return path;
    }

    public static boolean isValidFileUrl(String fileUrl, Project project) {
        DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

        if (databaseFileSystem.isDatabaseUrl(fileUrl)) {
            if (!databaseFileSystem.isValidPath(fileUrl, project)) {
                return false;
            }
        } else if (fileUrl.startsWith("file://")) {
            VirtualFile virtualFile = localFileSystem.findFileByPath(fileUrl.substring(7));
            if (virtualFile == null) {
                return false;
            }
        }
        return true;
    }
}
