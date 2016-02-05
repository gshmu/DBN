package com.dci.intellij.dbn.data.export;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.processor.DataExportProcessor;
import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;

@State(
    name = "DBNavigator.Project.DataExportManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class DataExportManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private DataExportInstructions exportInstructions = new DataExportInstructions();

    private DataExportManager(Project project) {
        super(project);
    }

    public static DataExportManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DataExportManager.class);
    }

    public void exportSortableTableContent(
            SortableTable table,
            DataExportInstructions instructions,
            ConnectionHandler connectionHandler,
            final SimpleTask successCallback) {
        final Project project = getProject();
        boolean isSelection = instructions.getScope() == DataExportInstructions.Scope.SELECTION;
        DataExportModel exportModel = new SortableTableExportModel(isSelection, table);
        try {
            DataExportProcessor processor = DataExportProcessor.getExportProcessor(instructions.getFormat());
            if (processor != null) {
                processor.export(exportModel, instructions, connectionHandler);
                DataExportInstructions.Destination destination = instructions.getDestination();
                if (destination == DataExportInstructions.Destination.CLIPBOARD) {
                    successCallback.start();
                    NotificationUtil.sendInfoNotification(project, Constants.DBN_TITLE_PREFIX + "Data Export", "Data content exported to clipboard.");
                } else if (destination == DataExportInstructions.Destination.FILE) {
                    final File file = instructions.getFile();
                    if (Desktop.isDesktopSupported()) {
                        //FileSystemView view = FileSystemView.getFileSystemView();
                        //Icon icon = view.getSystemIcon(file);

                        SimpleTask<Integer> openFileTask = new SimpleTask<Integer>() {
                            @Override
                            protected void execute() {
                                successCallback.start();
                                if (getOption() == 1) {
                                    try {
                                        Desktop.getDesktop().open(file);
                                    } catch (IOException e) {
                                        MessageUtil.showErrorDialog(
                                                project,
                                                "Open file",
                                                "Could not open file " + file.getPath() + ".\n" +
                                                        "The file type is most probably not associated with any program."
                                        );
                                    }
                                }
                            }
                        };
                        MessageUtil.showInfoDialog(
                                project,
                                "Export info",
                                "Content exported to file " + file.getPath(),
                                new String[]{"OK", "Open File"}, 0,
                                openFileTask);
                    } else {
                        NotificationUtil.sendInfoNotification(project, Constants.DBN_TITLE_PREFIX + "Data Export", "Content exported to file " + file.getPath());
                    }
                }
            }

        } catch (DataExportException e) {
            MessageUtil.showErrorDialog(project, "Error performing data export.", e);
        } catch (InterruptedException ignore) {

        }
    }

    public DataExportInstructions getExportInstructions() {
        try {
            return exportInstructions.clone();
        } catch (CloneNotSupportedException e) {
            //should not happen
            e.printStackTrace();
            return null;
        }
    }

    public void setExportInstructions(DataExportInstructions exportInstructions) {
        this.exportInstructions = exportInstructions;
    }

    /****************************************
    *            ProjectComponent           *
    *****************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DataExportManager";
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
    public void loadState(Element element) {
        exportInstructions.readState(element);
    }
}