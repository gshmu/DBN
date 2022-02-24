package com.dci.intellij.dbn.data.export;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.processor.CSVDataExportProcessor;
import com.dci.intellij.dbn.data.export.processor.CustomDataExportProcessor;
import com.dci.intellij.dbn.data.export.processor.DataExportProcessor;
import com.dci.intellij.dbn.data.export.processor.ExcelDataExportProcessor;
import com.dci.intellij.dbn.data.export.processor.ExcelXDataExportProcessor;
import com.dci.intellij.dbn.data.export.processor.HTMLDataExportProcessor;
import com.dci.intellij.dbn.data.export.processor.JIRAMarkupDataExportProcessor;
import com.dci.intellij.dbn.data.export.processor.SQLDataExportProcessor;
import com.dci.intellij.dbn.data.export.processor.XMLDataExportProcessor;
import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

@State(
    name = DataExportManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DataExportManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DataExportManager";

    private DataExportInstructions exportInstructions = new DataExportInstructions();

    private DataExportManager(Project project) {
        super(project);
    }

    public static DataExportManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DataExportManager.class);
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
    *            ProjectComponent           *
    *****************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        exportInstructions.writeState(element);
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        exportInstructions.readState(element);
    }
}