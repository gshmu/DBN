package com.dci.intellij.dbn.code.common.completion;

import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.thread.ThreadFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.PsiFileRef;
import com.dci.intellij.dbn.language.common.element.impl.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class CodeCompletionContext {
    private @Getter @Setter boolean extended;
    private final PsiFileRef<DBLanguagePsiFile> file;
    private final ConnectionHandlerRef connectionHandler;
    private final @Getter ProjectCodeStyleSettings codeStyleSettings;
    private final @Getter CodeCompletionSettings codeCompletionSettings;
    private final @Getter CompletionParameters parameters;
    private final @Getter CompletionResultSet result;
    private final @Getter double databaseVersion;
    private final @Getter String userInput;
    private final @Getter PsiElement elementAtCaret;
    private final @Getter boolean newLine;

    private @Getter @Setter DBObject parentObject;
    private @Getter @Setter IdentifierPsiElement parentIdentifierPsiElement;
    private Map<String, LeafElementType> completionCandidates = new HashMap<>();


    private final Latent<ExecutorService> executor = Latent.basic(() ->  ThreadFactory.getCodeCompletionExecutor());


    public CodeCompletionContext(DBLanguagePsiFile file, CompletionParameters parameters, CompletionResultSet result) {
        this.file = PsiFileRef.of(file);
        this.parameters = parameters;
        this.result = result;
        this.extended = parameters.getCompletionType() == CompletionType.SMART;
        this.connectionHandler = ConnectionHandlerRef.from(file.getConnectionHandler());

        PsiElement position = parameters.getPosition();
        if (position instanceof PsiComment) {
            throw new ProcessCanceledException();
        }
        int offset = parameters.getOffset();
        userInput = calcUserInput(position, offset);

        ProjectSettings projectSettings = ProjectSettingsManager.getSettings(file.getProject());
        codeStyleSettings = projectSettings.getCodeStyleSettings();
        codeCompletionSettings = projectSettings.getCodeCompletionSettings();

        elementAtCaret = calcElementAtCaret(file, position);

        databaseVersion = file.getDatabaseVersion();
        newLine = calcNewLine(parameters, offset);
    }

    private static PsiElement calcElementAtCaret(DBLanguagePsiFile file, PsiElement position) {
        PsiElement elementAtCaret = position instanceof BasePsiElement ? (BasePsiElement) position : PsiUtil.lookupLeafAtOffset(file, position.getTextOffset());
        elementAtCaret = elementAtCaret == null ? file : elementAtCaret;
        return elementAtCaret;
    }

    private static String calcUserInput(PsiElement position, int offset) {
        if (offset > position.getTextOffset()) {
            return position.getText().substring(0, offset - position.getTextOffset());
        }
        return null;
    }

    private static boolean calcNewLine(CompletionParameters parameters, int offset) {
        Document document = parameters.getEditor().getDocument();
        int lineNumber = document.getLineNumber(offset);
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = Math.min(offset, document.getTextLength());
        if (lineStartOffset < lineEndOffset) {
            String text = document.getText(new TextRange(lineStartOffset, lineEndOffset));
            return !StringUtil.containsWhitespaces(text.trim());
        }
        return true;
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(connectionHandler);
    }

    public CodeCompletionFilterSettings getCodeCompletionFilterSettings() {
        return codeCompletionSettings.getFilterSettings().getFilterSettings(extended);
    }

    @NotNull
    public DBLanguagePsiFile getFile() {
        return file.ensure();
    }

    @NotNull
    public DBLanguage getLanguage() {
        DBLanguageDialect languageDialect = getFile().getLanguageDialect();
        return languageDialect == null ? SQLLanguage.INSTANCE : languageDialect.getBaseLanguage();
    }

    public boolean isLiveConnection() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler != null && !connectionHandler.isVirtual();
    }

    private ExecutorService getExecutor() {
        return executor.get();
    }

    public void async(Runnable runnable) {
        getExecutor().execute(runnable);
    }

    public void awaitCompletion() {
        if (this.executor.loaded()) {
            try {
                ExecutorService executor = this.executor.get();
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addCompletionCandidate(@Nullable LeafElementType leafElementType) {
        if (leafElementType != null) {
            String leafUniqueKey = getLeafUniqueKey(leafElementType);
            if (leafUniqueKey != null) {
                completionCandidates.put(leafUniqueKey, leafElementType);
            }
        }
    }

    public boolean hasCompletionCandidates() {
        return !completionCandidates.isEmpty();
    }

    public Collection<LeafElementType> getCompletionCandidates() {
        return completionCandidates.values();
    }

    private static String getLeafUniqueKey(LeafElementType leaf) {
        if (leaf instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) leaf;
            String text = tokenElementType.getText();
            String id = tokenElementType.tokenType.getId();
            return StringUtil.isEmpty(text) ? id : id + text;
        } else if (leaf instanceof IdentifierElementType){
            IdentifierElementType identifierElementType = (IdentifierElementType) leaf;
            return identifierElementType.getQualifiedObjectTypeName();
        }
        return null;
    }
}
