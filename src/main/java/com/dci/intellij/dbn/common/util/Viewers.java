package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@UtilityClass
public class Viewers {

    public static EditorEx createViewer(Document document, Project project, @Nullable VirtualFile file, @NotNull FileType fileType) {
        EditorFactory editorFactory = EditorFactory.getInstance();
        return  file == null ?
                cast(editorFactory.createEditor(document, project, fileType, true)) :
                cast(editorFactory.createEditor(document, project, file, true));
    }
}
