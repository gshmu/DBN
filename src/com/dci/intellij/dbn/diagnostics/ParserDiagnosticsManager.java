package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.file.util.VirtualFileUtil;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dci.intellij.dbn.diagnostics.ui.ParserDiagnosticsDialog;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private ParserDiagnosticsManager(@NotNull Project project) {
        super(project);
    }

    public static ParserDiagnosticsManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, ParserDiagnosticsManager.class);
    }

    @NotNull
    public ParserDiagnosticsResult runParserDiagnostics(ProgressIndicator progress) {
        VirtualFile[] files = VirtualFileUtil.lookupFilesForExtensions(getProject(), "sql", "pkg");
        ParserDiagnosticsResult result = new ParserDiagnosticsResult(getProject());

        for (VirtualFile file : files) {
            Progress.check(progress);
            String filePath = file.getPath();
            progress.setText2(filePath);

            DBLanguagePsiFile psiFile = ensureFileParsed(file);
            Progress.check(progress);
            if (psiFile == null) {
                result.addEntry(filePath, 1);
            } else {
                Integer errorCount = Read.call(() -> psiFile.countErrors());
                if (errorCount != null && errorCount > 0) {
                    result.addEntry(filePath, errorCount);
                }
            }
        }
        resultHistory.add(0, result);
        indexResults();
        return result;
    }

    public void openParserDiagnostics(@Nullable ParserDiagnosticsResult result) {
        Dispatch.runConditional(() -> {
            Project project = getProject();
            ParserDiagnosticsResult selection = result;
            if (selection == null ) {
                selection = getLatestResult();
            }

            ParserDiagnosticsDialog dialog = new ParserDiagnosticsDialog(project);
            dialog.selectResult(selection);
            dialog.show();
        });
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
        return resultHistory.stream().anyMatch(result -> result.isDraft());
    }

    public void saveResult(@NotNull ParserDiagnosticsResult result) {
        result.markSaved();
    }

    public void deleteResult(@NotNull ParserDiagnosticsResult selectedResult) {
        resultHistory.remove(selectedResult);
        indexResults();
    }

    public void indexResults() {
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
