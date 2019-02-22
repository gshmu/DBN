package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.editor.document.OverrideReadonlyFragmentModificationHandler;
import com.dci.intellij.dbn.common.thread.ReadAction;
import com.dci.intellij.dbn.common.thread.WriteAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockMarkers;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.DocumentBulkUpdateListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import com.intellij.util.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DocumentUtil {
    private static final Key<Boolean> FOLDING_STATE_KEY = Key.create("FOLDING_STATE_KEY");
    private static final Key<Long> LAST_ANNOTATION_REFRESH_KEY = Key.create("LAST_ANNOTATION_REFRESH");

    public static void touchDocument(final Editor editor, boolean reparse) {
        final Document document = editor.getDocument();

        // restart highlighting
        Project project = editor.getProject();
        final PsiFile file = DocumentUtil.getFile(editor);
        if (project != null && !project.isDisposed() && file instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguageFile = (DBLanguagePsiFile) file;
            DBLanguage dbLanguage = dbLanguageFile.getDBLanguage();
            if (dbLanguage != null) {
                ConnectionHandler connectionHandler = dbLanguageFile.getConnectionHandler();
                DBLanguageSyntaxHighlighter syntaxHighlighter = getSyntaxHighlighter(dbLanguage, connectionHandler);

                EditorHighlighter editorHighlighter = HighlighterFactory.createHighlighter(syntaxHighlighter, editor.getColorsScheme());
                ((EditorEx) editor).setHighlighter(editorHighlighter);
            }
            if (reparse) {
                EventUtil.notify(project, DocumentBulkUpdateListener.TOPIC).updateStarted(document);
                ArrayList<VirtualFile> files = new ArrayList<>();
                files.add(file.getVirtualFile());
                FileContentUtil.reparseFiles(project, files, true);
                CodeFoldingManager codeFoldingManager = CodeFoldingManager.getInstance(project);
                codeFoldingManager.buildInitialFoldings(editor);
            }
            refreshEditorAnnotations(file);
        }
    }

    private static DBLanguageSyntaxHighlighter getSyntaxHighlighter(DBLanguage dbLanguage, ConnectionHandler connectionHandler) {
        if (connectionHandler != null) {
            DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(dbLanguage);
            if (languageDialect != null) {
                return languageDialect.getSyntaxHighlighter();
            }
        }
        return dbLanguage.getMainLanguageDialect().getSyntaxHighlighter();
    }


    public static void refreshEditorAnnotations(Editor editor) {
        refreshEditorAnnotations(DocumentUtil.getFile(editor));
    }

    public static void refreshEditorAnnotations(@Nullable final PsiFile psiFile) {
        if (psiFile != null) {
            Long lastRefresh = psiFile.getUserData(LAST_ANNOTATION_REFRESH_KEY);
            if (lastRefresh == null || TimeUtil.isOlderThan(lastRefresh, 1, TimeUnit.SECONDS)) {
                psiFile.putUserData(LAST_ANNOTATION_REFRESH_KEY, System.currentTimeMillis());
                ReadAction.invoke(false, () -> {
                    if (psiFile.isValid()) {
                        Project project = psiFile.getProject();
                        DaemonCodeAnalyzer daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project);
                        daemonCodeAnalyzer.restart(psiFile);
                    }
                    return null;
                });
            }
        }
    }

    public static Document getDocument(@NotNull PsiFile file) {
        return PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    }

    @Nullable
    public static PsiFile getFile(@Nullable Editor editor) {
        Project project = editor == null ? null : editor.getProject();
        return project == null ? null : PsiUtil.getPsiFile(project, editor.getDocument());
    }

    public static void createGuardedBlock(Document document, GuardedBlockType type, String reason, boolean highlight) {
        createGuardedBlock(document, type, 0, document.getTextLength(), reason);

        if (!highlight) {
            Editor[] editors = EditorFactory.getInstance().getEditors(document);
            for (Editor editor : editors) {
                EditorColorsScheme scheme = editor.getColorsScheme();
                scheme.setColor(EditorColors.READONLY_FRAGMENT_BACKGROUND_COLOR, scheme.getDefaultBackground());
            }
        }
    }

    public static void createGuardedBlocks(final Document document, final GuardedBlockType type, final GuardedBlockMarkers ranges, final String reason) {
        for (Range<Integer> range : ranges.getRanges()) {
            createGuardedBlock(document, type, range.getFrom(), range.getTo(), reason);
        }
    }

    public static void createGuardedBlock(final Document document, final GuardedBlockType type, final int startOffset, final int endOffset, final String reason) {
        if (startOffset != endOffset) {
            WriteAction.invoke(() -> {
                int textLength = document.getTextLength();
                if (endOffset <= textLength) {
                    RangeMarker rangeMarker = document.createGuardedBlock(startOffset, endOffset);
                    rangeMarker.setGreedyToLeft(startOffset == 0);
                    rangeMarker.setGreedyToRight(endOffset == textLength);
                    rangeMarker.putUserData(GuardedBlockType.KEY, type);
                    document.putUserData(OverrideReadonlyFragmentModificationHandler.GUARDED_BLOCK_REASON, reason);
                }
            });
        }
    }

    public static void removeGuardedBlocks(final Document document, final GuardedBlockType type) {
        if (document instanceof DocumentEx) {
            final DocumentEx documentEx = (DocumentEx) document;
            WriteAction.invoke(() -> {
                List<RangeMarker> guardedBlocks = new ArrayList<>(documentEx.getGuardedBlocks());
                for (final RangeMarker block : guardedBlocks) {
                    if (block.getUserData(GuardedBlockType.KEY) == type) {
                        document.removeGuardedBlock(block);
                    }
                }
                document.putUserData(OverrideReadonlyFragmentModificationHandler.GUARDED_BLOCK_REASON, null);
            });
        }
    }

    @Nullable
    public static VirtualFile getVirtualFile(Editor editor) {
        if (editor instanceof EditorEx) {
            EditorEx editorEx = (EditorEx) editor;
            return editorEx.getVirtualFile();
        }
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        return fileDocumentManager.getFile(editor.getDocument());
    }

    @Nullable
    public static Document getDocument(final @NotNull VirtualFile virtualFile) {
        return ReadAction.invoke(false, () -> {
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            return fileDocumentManager.getDocument(virtualFile);
        });
    }

    @Nullable
    public static PsiFile getPsiFile(Project project, VirtualFile virtualFile) {
        Document document = getDocument(virtualFile);
        if (document != null) {
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            return psiDocumentManager.getPsiFile(document);
        } else {
            return null;
        }
    }

    public static void setReadonly(Document document, Project project, boolean readonly) {
        //document.setReadOnly(readonly);
        DocumentUtil.removeGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT);
        if (readonly) {
            DocumentUtil.createGuardedBlock(document, GuardedBlockType.READONLY_DOCUMENT, null, false);
        }
    }

    public static void setText(final @NotNull Document document, final CharSequence text) {
        WriteAction.invoke(() -> {
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            VirtualFile file = fileDocumentManager.getFile(document);
            if (file != null && file.isValid()) {
                boolean isReadonly = !document.isWritable();
                try {
                    document.setReadOnly(false);
                    document.setText(text);
                } finally {
                    document.setReadOnly(isReadonly);
                }

            }
        });
    }

    public static void saveDocument(final  @NotNull Document document) {
        WriteAction.invoke(() -> {
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            fileDocumentManager.saveDocument(document);
        });
    }
}
