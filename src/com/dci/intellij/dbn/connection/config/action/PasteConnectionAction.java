package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionListModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.jdom.Document;
import org.jdom.Element;

import javax.swing.JList;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PasteConnectionAction extends DumbAwareAction {
    protected ConnectionBundle connectionBundle;
    protected JList list;

    public PasteConnectionAction(JList list, ConnectionBundle connectionBundle) {
        super("Paste configuration from clipboard", null, Icons.CONNECTION_PASTE);
        this.list = list;
        this.connectionBundle = connectionBundle;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            String clipboardData = ClipboardUtil.getStringContent();
            if (clipboardData != null) {
                Document xmlDocument = CommonUtil.createXMLDocument(new ReaderInputStream(new StringReader(clipboardData), "UTF-8"));
                Element rootElement = xmlDocument.getRootElement();
                List<Element> configElements = rootElement.getChildren();
                ConnectionListModel model = (ConnectionListModel) list.getModel();
                int selectedIndex = list.getSelectedIndex();
                List<Integer> selectedIndexes = new ArrayList<Integer>();
                for (Element configElement : configElements) {
                    selectedIndex++;
                    ConnectionSettings clone = new ConnectionSettings(connectionBundle);
                    clone.readConfiguration(configElement);
                    clone.getDatabaseSettings().setNew(true);
                    connectionBundle.setModified(true);

                    clone.getDatabaseSettings().setNew(true);
                    String name = clone.getDatabaseSettings().getName();
                    while (model.getConnectionConfig(name) != null) {
                        name = NamingUtil.getNextNumberedName(name, true);
                    }
                    clone.getDatabaseSettings().setName(name);
                    model.add(selectedIndex, clone);
                    selectedIndexes.add(selectedIndex);
                }

                list.setSelectedIndices(ArrayUtils.toPrimitive(selectedIndexes.toArray(new Integer[selectedIndexes.size()])));
            }
        } catch (Exception e) {

        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Object clipboardData = clipboard.getData(DataFlavor.stringFlavor);
            if (clipboardData instanceof String) {
                String clipboardString = (String) clipboardData;
                presentation.setEnabled(clipboardString.contains("connection-configurations"));
            } else {
                presentation.setEnabled(false);
            }
        } catch (Exception ex) {
            presentation.setEnabled(false);
        }

    }
}
