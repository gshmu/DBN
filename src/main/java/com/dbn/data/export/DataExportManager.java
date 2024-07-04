package com.dbn.data.export;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.notification.NotificationGroup;
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.data.export.processor.*;
import com.dbn.data.grid.ui.table.sortable.SortableTable;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.dbn.common.component.Components.projectService;
import static com.dbn.common.options.setting.Settings.newElement;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

@Getter
@Setter
@State(
    name = DataExportManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DataExportManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DataExportManager";

    private DataExportInstructions exportInstructions = new DataExportInstructions();

    private DataExportManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static DataExportManager getInstance(@NotNull Project project) {
        return projectService(project, DataExportManager.class);
    }

    private static final DataExportProcessor[] PROCESSORS =  new DataExportProcessor[] {
            new SQLDataExportProcessor(),
            new ExcelDataExportProcessor(),
            new ExcelXDataExportProcessor(),
            new CSVDataExportProcessor(),
            new HTMLDataExportProcessor(),
            new XMLDataExportProcessor(),
            new JIRAMarkupDataExportProcessor(),
            new CustomDataExportProcessor()};

    public static DataExportProcessor getExportProcessor(DataExportFormat format) {
        for (DataExportProcessor exportProcessor : PROCESSORS) {
            if (exportProcessor.getFormat() == format) {
                return exportProcessor;
            }
        }
        return null;
    }

    public void exportTableContent(
            SortableTable table,
            DataExportInstructions instructions,
            ConnectionHandler connection,
            @NotNull Runnable successCallback) {
        Project project = getProject();
        boolean isSelection = instructions.getScope() == DataExportInstructions.Scope.SELECTION;
        DataExportModel exportModel = new SortableTableExportModel(isSelection, table);
        try {
            DataExportProcessor processor = getExportProcessor(instructions.getFormat());
            if (processor == null) return;

            processor.export(exportModel, instructions, connection);
            DataExportInstructions.Destination destination = instructions.getDestination();
            List<String> warnings = exportModel.getWarnings();

            String warningsBlock = warnings.isEmpty() ? null : String.join("\n", warnings);
            if (destination == DataExportInstructions.Destination.CLIPBOARD) {
                successCallback.run();
                if (warningsBlock == null) {
                    Messages.showInfoDialog(
                            project,
                            nls("msg.data.title.DataExported"),
                            nls("msg.data.info.DataExportedToClipboard"),
                            new String[]{nls("app.shared.button.OK")}, 0, null);
                } else {
                    Messages.showWarningDialog(
                            project,
                            nls("msg.data.title.DataExported"),
                            nls("msg.data.warning.DataExportedToClipboard", warningsBlock),
                            new String[]{nls("app.shared.button.OK")}, 0, null);

                }

            } else if (destination == DataExportInstructions.Destination.FILE) {
                File file = instructions.getFile();
                String filePath = file.getPath();
                if (Desktop.isDesktopSupported()) {
                    //FileSystemView view = FileSystemView.getFileSystemView();
                    //Icon icon = view.getSystemIcon(file);

                    if (warningsBlock == null) {
                        Messages.showInfoDialog(
                                project,
                                nls("msg.data.title.DataExported"),
                                nls("msg.data.info.DataExportedToFile", filePath),
                                new String[]{nls("app.shared.button.OK"), nls("app.shared.button.OpenFile")}, 0,
                                o -> {
                                    successCallback.run();
                                    if (o == 1) openFile(project, file);
                                });
                    }
                    else {
                        Messages.showWarningDialog(
                                project,
                                nls("msg.data.title.DataExported"),
                                nls("msg.data.warning.DataExportedToFile", filePath, warningsBlock),
                                new String[]{nls("app.shared.button.OK"), nls("app.shared.button.OpenFile")}, 0,
                                o -> {
                                    successCallback.run();
                                    if (o == 1) openFile(project, file);
                                });
                    }

                } else {
                    if (warningsBlock == null) {
                        sendInfoNotification(
                                NotificationGroup.DATA,
                                nls("ntf.data.info.DataExportedToFile", filePath));
                    } else {
                        sendWarningNotification(
                                NotificationGroup.DATA,
                                nls("ntf.data.warning.DataExportedToFile", filePath, warningsBlock));
                    }
                }
            }

        } catch (DataExportException e) {
            conditionallyLog(e);
            Messages.showErrorDialog(project, nls("msg.data.error.ExportFailure"), e);
        }
    }

    private void openFile(Project project, File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            conditionallyLog(e);
            String filePath = file.getPath();
            Messages.showErrorDialog(
                    project,
                    nls("msg.data.title.OpenFile"),
                    nls("msg.data.error.FailedToOpenFile", filePath)
            );
        }
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        Element element = newElement("state");
        exportInstructions.writeState(element);
        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        exportInstructions.readState(element);
    }
}