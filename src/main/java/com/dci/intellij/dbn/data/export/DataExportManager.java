package com.dci.intellij.dbn.data.export;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.processor.*;
import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.dci.intellij.dbn.common.component.Components.projectService;

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

    public void exportSortableTableContent(
            SortableTable table,
            DataExportInstructions instructions,
            ConnectionHandler connection,
            @NotNull Runnable successCallback) {
        Project project = getProject();
        boolean isSelection = instructions.getScope() == DataExportInstructions.Scope.SELECTION;
        DataExportModel exportModel = new SortableTableExportModel(isSelection, table);
        try {
            DataExportProcessor processor = getExportProcessor(instructions.getFormat());
            if (processor != null) {
                processor.export(exportModel, instructions, connection);
                DataExportInstructions.Destination destination = instructions.getDestination();
                if (destination == DataExportInstructions.Destination.CLIPBOARD) {
                    successCallback.run();
                    Messages.showInfoDialog(
                            project,
                            "Export info",
                            "Content exported to clipboard",
                            new String[]{"OK"}, 0, null);

/*
                            sendInfoNotification(
                            NotificationGroup.DATA,
                            "Data content exported to clipboard.");
*/

                } else if (destination == DataExportInstructions.Destination.FILE) {
                    final File file = instructions.getFile();
                    if (Desktop.isDesktopSupported()) {
                        //FileSystemView view = FileSystemView.getFileSystemView();
                        //Icon icon = view.getSystemIcon(file);

                        Messages.showInfoDialog(
                                project,
                                "Export info",
                                "Content exported to file " + file.getPath(),
                                new String[]{"OK", "Open File"}, 0,
                                (option) -> {
                                    successCallback.run();
                                    if (option == 1) {
                                        try {
                                            Desktop.getDesktop().open(file);
                                        } catch (IOException e) {
                                            Messages.showErrorDialog(
                                                    project,
                                                    "Open file",
                                                    "Could not open file " + file.getPath() + ".\n" +
                                                            "The file type is most probably not associated with any program."
                                            );
                                        }
                                    }
                                });
                    } else {
                        sendInfoNotification(
                                NotificationGroup.DATA,
                                "Content exported to file: {0}", file.getPath());
                    }
                }
            }

        } catch (DataExportException e) {
            Messages.showErrorDialog(project, "Error performing data export.", e);
        }
    }

    public DataExportInstructions getExportInstructions() {
        return exportInstructions.clone();
    }

    public void setExportInstructions(DataExportInstructions exportInstructions) {
        this.exportInstructions = exportInstructions;
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        exportInstructions.writeState(element);
        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        exportInstructions.readState(element);
    }
}