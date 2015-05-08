package com.dci.intellij.dbn.execution.script.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

public class CmdLineInterfaceInputForm extends DBNFormImpl<CmdLineInterfaceInputDialog>{
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JLabel databaseTypeLabel;
    private JLabel nameInUseLabel;
    private JPanel executablePanel;
    private JLabel executableLabel;
    private JPanel databaseTypePanel;

    public CmdLineInterfaceInputForm(@NotNull final CmdLineInterfaceInputDialog parentComponent, @NotNull final CmdLineInterface cmdLineInterface, @NotNull final Set<String> usedNames) {
        super(parentComponent);
        DatabaseType databaseType = cmdLineInterface.getDatabaseType();
        databaseTypeLabel.setText(databaseType.name());
        databaseTypeLabel.setIcon(databaseType.getIcon());
        databaseTypePanel.setBorder(UIUtil.getTextFieldBorder());

        String executablePath = cmdLineInterface.getExecutablePath();
        executableLabel.setText(executablePath);
        executablePanel.setBorder(UIUtil.getTextFieldBorder());

        nameTextField.setText(CmdLineInterface.getDefault(databaseType).getName());
        nameInUseLabel.setIcon(Icons.COMMON_ERROR);

        cmdLineInterface.setDatabaseType(databaseType);
        cmdLineInterface.setExecutablePath(executablePath);
        nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                updateComponents(cmdLineInterface, usedNames);
            }
        });

        updateComponents(cmdLineInterface, usedNames);
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return nameTextField;
    }

    private void updateComponents(CmdLineInterface cmdLineInterface, Set<String> usedNames) {
        String name = nameTextField.getText();
        cmdLineInterface.setName(name);
        boolean isNameUsed = usedNames.contains(name);
        nameInUseLabel.setVisible(isNameUsed);

        CmdLineInterfaceInputDialog parentComponent = getParentComponent();
        parentComponent.setActionEnabled(!isNameUsed && StringUtils.isNotEmpty(nameTextField.getText()));
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
