package com.dci.intellij.dbn.connection.config.tns;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.connection.config.tns.ui.TnsNamesImportDialog;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.dci.intellij.dbn.common.component.Components.applicationService;
import static com.dci.intellij.dbn.common.options.setting.Settings.getEnum;
import static com.dci.intellij.dbn.common.options.setting.Settings.setEnum;
import static com.dci.intellij.dbn.connection.config.tns.TnsImportService.COMPONENT_NAME;

@Getter
@Setter
@State(
    name = COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class TnsImportService extends ApplicationComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Application.TnsImportService";
    private TnsImportType importType = TnsImportType.FIELDS;

    private TnsImportService() {
        super(COMPONENT_NAME);
    }

    public static TnsImportService getInstance() {
        return applicationService(TnsImportService.class);
    }


    public void importTnsNames(Project project, Consumer<TnsImportData> consumer) {
        VirtualFile[] virtualFiles = FileChooser.chooseFiles(TnsNamesParser.FILE_CHOOSER_DESCRIPTOR, project, null);
        if (virtualFiles.length != 1) return;

        File file = new File(virtualFiles[0].getPath());
        TnsNamesImportDialog dialog = new TnsNamesImportDialog(project, file);
        dialog.show();
        int exitCode = dialog.getExitCode();
        if (exitCode != DialogWrapper.OK_EXIT_CODE) return;

        consumer.accept(dialog.getImportData());
    }

    @Override
    public Element getComponentState() {
        Element stateElement = new Element("state");
        Element optionsElement = new Element("tns-import-options");
        stateElement.addContent(optionsElement);

        setEnum(optionsElement, "import-type", importType);
        return stateElement;
    }

    @Override
    public void loadComponentState(@NotNull Element stateElement) {
        Element optionsElement = stateElement.getChild("tns-import-options");
        if (optionsElement == null) return;
        importType = getEnum(optionsElement, "import-type", importType);
    }
}
