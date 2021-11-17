package com.dci.intellij.dbn.common.file.util;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class FileCollector extends VirtualFileVisitor {
    private static final FileTypeManager fileTypeManager = FileTypeManager.getInstance();
    private final Map<String, VirtualFile> bucket = new HashMap<>();
    private final FileCollectorType type;
    private final String[] identifiers;

    public FileCollector(FileCollectorType type, String... identifiers) {
        this.type = type;
        this.identifiers = identifiers;
    }

    public boolean visitFile(@NotNull VirtualFile file) {
        boolean fileIgnored = fileTypeManager.isFileIgnored(file.getName());
        if (!fileIgnored) {
            if (file.isDirectory()) {
                return true;
            } else {
                boolean match = false;
                if (type == FileCollectorType.NAME) {
                    match = Arrays.stream(identifiers).anyMatch(name -> StringUtil.equalsIgnoreCase(file.getName(), name));
                } else if (type == FileCollectorType.EXTENSION) {
                    match = Arrays.stream(identifiers).anyMatch(name -> StringUtil.equalsIgnoreCase(file.getExtension(), name));
                } else if (type == FileCollectorType.PATTERN) {
                    throw new UnsupportedOperationException("Not implemented"); // TODO implement
                }

                if (match) {
                    bucket.put(file.getPath(), file);
                }
                return false;
            }
        }
        return false;
    }

    public VirtualFile[] files() {
        return bucket.values().toArray(new VirtualFile[0]);
    }
}
