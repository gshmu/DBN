package com.dci.intellij.dbn.data.export;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.processor.DataExportProcessor;
import com.dci.intellij.dbn.data.ui.table.sortable.SortableTable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class DataExportManager extends AbstractProjectComponent implements JDOMExternalizable {
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
                Messages.showInfoMessage(
                        "Content exported to clipboard.",
                        Constants.DBN_TITLE_PREFIX + "Export info");

            } else if (destination == DataExportInstructions.Destination.FILE) {
                File file = instructions.getFile();
                if (Desktop.isDesktopSupported()) {
                    //FileSystemView view = FileSystemView.getFileSystemView();
                    //Icon icon = view.getSystemIcon(file);

                    int selection = Messages.showDialog(
                            "Content exported to file " + file.getPath(),
                            Constants.DBN_TITLE_PREFIX + "Export info",
                            new String[]{"Ok", "Open File"}, 0,
                            Messages.getInformationIcon());

                    if (selection == 1) {
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException e) {
                            Messages.showErrorDialog(
                                    "Could not open file " + file.getPath() + ".\nThe file type is most probably not associated with any program." ,
                                    Constants.DBN_TITLE_PREFIX + "Open file");
                        }
                    }

                } else {
                    Messages.showInfoMessage(
                            "Content exported to file " + file.getPath(),
                            Constants.DBN_TITLE_PREFIX + "Export info");
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
    *            JDOMExternalizable         *
    *****************************************/
    public void readExternal(Element element) throws InvalidDataException {
        exportInstructions.readExternal(element.getChild("export-instructions"));
    }

    public void writeExternal(Element element) throws WriteExternalException {
        Element child = new Element("export-instructions");
        exportInstructions.writeExternal(child);
        element.addContent(child);
    }
}