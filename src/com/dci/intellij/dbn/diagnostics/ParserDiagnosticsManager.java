package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.file.util.VirtualFileUtil;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.MessageUtil;
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

    public void runParserDiagnostics() {
        Progress.prompt(getProject(), "Running Parser Diagnostics", true, progress -> {
            ParserDiagnosticsResult result = runParserDiagnostics(progress);
            openParserDiagnostics(result);
        });
    }

    @NotNull
    public ParserDiagnosticsResult runParserDiagnostics(ProgressIndicator progress) {
        VirtualFile[] files = VirtualFileUtil.lookupFilesForExtensions(getProject(), "sql", "pkg");
        ParserDiagnosticsResult result = new ParserDiagnosticsResult();

        for (VirtualFile file : files) {
            String filePath = file.getPath();
            progress.setText2(filePath);

            DBLanguagePsiFile psiFile = ensureFileParsed(file);
            if (psiFile == null) {
                result.addEntry(filePath, 1);
            } else {
                int errorCount = Read.call(() -> psiFile.countErrors());
                if (errorCount > 0) {
                    result.addEntry(filePath, errorCount);
                }
            }
        }
        return result;
    }

    public void openParserDiagnostics(@Nullable ParserDiagnosticsResult result) {
        if (result == null ) {
            result = getLatestResult();
        }

        if (result == null) {
            MessageUtil.showInfoDialog(getProject(), "No parser diagnostics", "No parser diagnostics captured so far");
        } else {
            ParserDiagnosticsResult diagnosticsResult = result;
            Dispatch.run(() -> {
                ParserDiagnosticsDialog dialog = new ParserDiagnosticsDialog(getProject());
                dialog.initResult(diagnosticsResult);
                Dispatch.run(() -> dialog.show());
            });
        }
    }

    public ParserDiagnosticsResult getLatestResult() {
        if (resultHistory.isEmpty()) {
            return null;
        }
        return resultHistory.get(0);
    }

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

    public void saveResult(ParserDiagnosticsResult result) {
        resultHistory.add(0, result);
        result.markSaved();
    }

    public void deleteResult(ParserDiagnosticsResult selectedResult) {
        resultHistory.remove(selectedResult);
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
            Element resultElement = new Element("result");
            historyElement.addContent(resultElement);
            capturedResult.writeState(resultElement);
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
                ParserDiagnosticsResult result = new ParserDiagnosticsResult(resultElement);
                resultHistory.add(result);
            }
        }
    }

    @Override
    protected void disposeInner() {
    }
}
