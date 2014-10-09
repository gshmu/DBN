package com.dci.intellij.dbn.data.export;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
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

    public static DataExportManager getInstance(Project project) {
        return project.getComponent(DataExportManager.class);
    }

    public boolean exportSortableTableContent(
            SortableTable table,
            DataExportInstructions instructions,
            ConnectionHandler connectionHandler) {
        boolean isSelection = instructions.getScope() == DataExportInstructions.Scope.SELECTION;
        DataExportModel exportModel = new SortableTableExportModel(isSelection, table);
        try {
            DataExportProcessor processor = DataExportProcessor.getExportProcessor(instructions.getFormat());
            processor.export(exportModel, instructions, connectionHandler);
            DataExportInstructions.Destination destination = instructions.getDestination();
            if (destination == DataExportInstructions.Destination.CLIPBOARD) {
                MessageUtil.showInfoDialog("Export info", "Content exported to clipboard.");

            } else if (destination == DataExportInstructions.Destination.FILE) {
                final File file = instructions.getFile();
                if (Desktop.isDesktopSupported()) {
                    //FileSystemView view = FileSystemView.getFileSystemView();
                    //Icon icon = view.getSystemIcon(file);

                    MessageUtil.showInfoDialog(
                            "Export info",
                            "Content exported to file " + file.getPath(),
                            new String[]{"OK", "Open File"}, 0,
                            new SimpleTask() {
                                @Override
                                public void execute() {
                                    if (getOption() == 1) {
                                        try {
                                            Desktop.getDesktop().open(file);
                                        } catch (IOException e) {
                                            MessageUtil.showErrorDialog("Open file",
                                                    "Could not open file " + file.getPath() + ".\n" +
                                                    "The file type is most probably not associated with any program."
                                            );
                                        }
                                    }
                                }
                            });
                } else {
                    MessageUtil.showInfoDialog("Export info", "Content exported to file " + file.getPath());
                }
            }

            return true;
        } catch (DataExportException e) {
            MessageUtil.showErrorDialog("Error performing data export.", e);
            return false;
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