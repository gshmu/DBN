package com.dci.intellij.dbn.debugger.jdwp.config.ui;

import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.debugger.jdwp.config.DBJdwpRunConfig;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.Range;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;

@Getter
public class DBJdwpDebugAttributesForm extends DBNFormBase {
    private JCheckBox compileDependenciesCheckBox;
    private JTextField tcpHostTextBox;
    private JTextField fromPortTextField;
    private JTextField toPortTextField;
    private JPanel mainPanel;

    public DBJdwpDebugAttributesForm(@Nullable Disposable parent) {
        super(parent);
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

    public void writeConfiguration(DBJdwpRunConfig configuration) throws ConfigurationException {
        configuration.setCompileDependencies(compileDependenciesCheckBox.isSelected());

        int fromPort = 0;
        int toPort = 0;
        try {
            fromPort = Integer.parseInt(fromPortTextField.getText());
            toPort = Integer.parseInt(toPortTextField.getText());
        } catch (NumberFormatException e) {
            conditionallyLog(e);
            throw new ConfigurationException("TCP Port Range inputs must me numeric");
        }
        String tcpHost = tcpHostTextBox.getText();

        try {
            if (Strings.isNotEmptyOrSpaces(tcpHost) ) InetAddress.getAllByName(tcpHost);
        }
        catch (UnknownHostException e) {
            conditionallyLog(e);
            throw new ConfigurationException("Invalid IP address: " + tcpHost);
        }

        configuration.setTcpPortRange(new Range<>(fromPort, toPort));
        configuration.setTcpHostAddress(tcpHost);

        //selectMethodAction.setConfiguration(configuration);
    }

    public void readConfiguration(DBJdwpRunConfig configuration) {
        compileDependenciesCheckBox.setSelected(configuration.isCompileDependencies());
        fromPortTextField.setText(String.valueOf(configuration.getTcpPortRange().getFrom()));
        toPortTextField.setText(String.valueOf(configuration.getTcpPortRange().getTo()));
        tcpHostTextBox.setText(configuration.getTcpHostAddress());
    }
}
