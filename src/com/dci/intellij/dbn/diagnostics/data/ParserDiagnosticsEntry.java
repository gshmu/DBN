package com.dci.intellij.dbn.diagnostics.data;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsUtil.computeStateTransition;

@Getter
public class ParserDiagnosticsEntry implements Comparable<ParserDiagnosticsEntry>{

    private final String filePath;
    private final int oldErrorCount;
    private final int newErrorCount;

    public ParserDiagnosticsEntry(String filePath, int oldErrorCount, int newErrorCount) {
        this.filePath = filePath;
        this.oldErrorCount = oldErrorCount;
        this.newErrorCount = newErrorCount;
    }

    @Nullable
    public VirtualFile getFile() {
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        return localFileSystem.findFileByIoFile(new File(filePath));
    }

    public StateTransition getStateTransition() {
        return computeStateTransition(oldErrorCount, newErrorCount);
    }

    @Override
    public int compareTo(@NotNull ParserDiagnosticsEntry o) {
        return filePath.compareTo(o.getFilePath());
    }
}
