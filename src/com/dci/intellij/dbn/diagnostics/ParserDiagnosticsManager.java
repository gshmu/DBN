package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.file.util.FileSearchRequest;
import com.dci.intellij.dbn.common.file.util.VirtualFiles;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticCategory;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsFilter;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dci.intellij.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.scrambler.DBLLanguageFileScrambler;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@State(
    name = ParserDiagnosticsManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ParserDiagnosticsManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.ParserDiagnosticsManager";

    private final List<ParserDiagnosticsResult> resultHistory = new ArrayList<>();
    private ParserDiagnosticsFilter resultFilter = ParserDiagnosticsFilter.EMPTY;
    private boolean running;

    private ParserDiagnosticsManager(@NotNull Project project) {
        super(project);
    }

    public static ParserDiagnosticsManager get(@NotNull Project project) {
        return Failsafe.getComponent(project, ParserDiagnosticsManager.class);
    }

    @NotNull
    public ParserDiagnosticsResult runParserDiagnostics(ProgressIndicator progress) {
        try {
            running = true;
            String[] extensions = getFileExtensions();
            FileSearchRequest searchRequest = FileSearchRequest.forExtensions(extensions);
            VirtualFile[] files = VirtualFiles.findFiles(getProject(), searchRequest);
            ParserDiagnosticsResult result = new ParserDiagnosticsResult(getProject());

            for (int i = 0, filesLength = files.length; i < filesLength; i++) {
                VirtualFile file = files[i];
                Progress.check(progress);
                String filePath = file.getPath();
                progress.setText2(filePath);
                progress.setFraction(Progress.progressOf(i, files.length));

                DBLanguagePsiFile psiFile = ensureFileParsed(file);
                Progress.check(progress);
                if (psiFile == null) {
                    result.addEntry(filePath, 1, 0);
                } else {
                    int errors = Read.call(() -> psiFile.countErrors(), 0);
                    int warnings = Read.call(() -> psiFile.countWarnings(), 0);
                    if (errors > 0 || warnings > 0) {
                        result.addEntry(filePath, errors, warnings);
                    }
                }
            }
            resultHistory.add(0, result);
            indexResults();
            return result;
        } finally {
            running = false;
        }
    }

    public void scrambleProjectFiles(ProgressIndicator progress, File rootDir) {
        String[] extensions = getFileExtensions();
        FileSearchRequest searchRequest = FileSearchRequest.forExtensions(extensions);
        VirtualFile[] files = VirtualFiles.findFiles(getProject(), searchRequest);

        DBLLanguageFileScrambler scrambler = new DBLLanguageFileScrambler();

        for (int i = 0, filesLength = files.length; i < filesLength; i++) {
            VirtualFile file = files[i];
            Progress.check(progress);
            String filePath = file.getPath();
            progress.setText2(filePath);
            progress.setFraction(Progress.progressOf(i, files.length));

            DBLanguagePsiFile psiFile = ensureFileParsed(file);
            Progress.check(progress);
            if (psiFile != null) {

                String scrambled = scrambler.scramble(psiFile);
                String newFileName = scrambler.scrambleName(file);
                File scrambledFile = new File(rootDir, newFileName);
                try {
                    FileUtils.write(scrambledFile, scrambled, file.getCharset());
                } catch (IOException e) {
                    NotificationSupport.sendWarningNotification(
                            getProject(),
                            NotificationGroup.DEVELOPER,
                            "Failed to write file" + scrambledFile.getPath() + ". " + e.getMessage());
                }
            }
        }
    }

    public String[] getFileExtensions() {
        List<String> extensions = new ArrayList<>();
        collectFileExtensions(extensions, SQLFileType.INSTANCE);
        collectFileExtensions(extensions, PSQLFileType.INSTANCE);
        return extensions.toArray(new String[0]);
    }

    private void collectFileExtensions(List<String> bucket, DBLanguageFileType fileType) {
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        List<FileNameMatcher> associations = fileTypeManager.getAssociations(fileType);
        for (FileNameMatcher association : associations) {
            if (association instanceof ExtensionFileNameMatcher) {
                ExtensionFileNameMatcher matcher = (ExtensionFileNameMatcher) association;
                bucket.add(matcher.getExtension());
            }
        }
    }

    public void openParserDiagnostics(@Nullable ParserDiagnosticsResult result) {
        Project project = getProject();
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(project);
        ParserDiagnosticsForm form = diagnosticsManager.showDiagnosticsConsole(
                DiagnosticCategory.PARSER,
                () -> new ParserDiagnosticsForm(project));

        ParserDiagnosticsResult selectedResult = form.getSelectedResult();
        form.selectResult(Commons.nvln(result, selectedResult));
    }


    @Nullable
    public ParserDiagnosticsResult getLatestResult() {
        if (resultHistory.isEmpty()) {
            return null;
        }
        return resultHistory.get(0);
    }

    @Nullable
    public ParserDiagnosticsResult getPreviousResult(ParserDiagnosticsResult result) {
        int index = resultHistory.indexOf(result);
        if (index == -1) {
            return getLatestResult();
        }
        if (index + 1 >= resultHistory.size()) {
            return null;
        }

        return resultHistory.get(index + 1);
    }

    public boolean hasDraftResults() {
        return Lists.anyMatch(resultHistory, result -> result.isDraft());
    }

    public void saveResult(@NotNull ParserDiagnosticsResult result) {
        result.markSaved();
    }

    public void deleteResult(@NotNull ParserDiagnosticsResult selectedResult) {
        resultHistory.remove(selectedResult);
        indexResults();
    }

    public void setResultFilter(ParserDiagnosticsFilter filter) {
        this.resultFilter = filter;
    }

    private void indexResults() {
        int size = resultHistory.size();
        for (int i = 0; i < size; i++) {
            ParserDiagnosticsResult result = resultHistory.get(i);
            result.setIndex(size - i);
        }
    }

    private DBLanguagePsiFile ensureFileParsed(VirtualFile file) {
        PsiFile psiFile = Read.call(() -> PsiUtil.getPsiFile(getProject(), file));
        return psiFile instanceof DBLanguagePsiFile ? (DBLanguagePsiFile) psiFile : null;
    }

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        Element historyElement = new Element("diagnostics-history");
        element.addContent(historyElement);
        for (ParserDiagnosticsResult capturedResult : resultHistory) {
            if (!capturedResult.isDraft()) {
                Element resultElement = new Element("result");
                historyElement.addContent(resultElement);
                capturedResult.writeState(resultElement);
            }
        }
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        Element historyElement = element.getChild("diagnostics-history");
        resultHistory.clear();
        if (historyElement != null) {
            List<Element> resultElements = historyElement.getChildren("result");
            for (Element resultElement : resultElements) {
                ParserDiagnosticsResult result = new ParserDiagnosticsResult(getProject(), resultElement);
                resultHistory.add(result);
            }
        }
        indexResults();
    }

    @Override
    protected void disposeInner() {
    }
}
