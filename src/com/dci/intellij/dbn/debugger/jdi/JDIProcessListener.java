package com.dci.intellij.dbn.debugger.jdi;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.progress.ProgressIndicator;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.ListeningConnector;


public class JDIProcessListener {
    public static void start(ConnectionHandler connectionHandler) {
        new BackgroundTask(null, "Listening to JDI connection", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                ListeningConnector connector = getConnector();

                if (connector != null) {
                    Map<String, Connector.Argument> defArgs = getArguments(connector);

                    try {
                        connector.startListening(defArgs);
                        VirtualMachine virtualMachine = connector.accept(defArgs);

                        String defaultStratum = virtualMachine.getDefaultStratum();
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            connector.stopListening(defArgs);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }

            }
        }.start();

/*        new BackgroundTask(null, "Setting breakpoints", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                try {
                    ListeningConnector connector = getConnector();
                    Map<String, Connector.Argument> defArgs = getArguments(connector);
                    VirtualMachine virtualMachine = connector.accept(defArgs);
                    String defaultStratum = virtualMachine.getDefaultStratum();
                    virtualMachine.eventRequestManager().createBreakpointRequest()
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();*/
    }

    @NotNull
    private static Map<String, Connector.Argument> getArguments(ListeningConnector connector) {
        Map<String, Connector.Argument> defArgs = connector.defaultArguments();
        for (Connector.Argument argument : defArgs.values()) {
            if (argument.name().equals("port")) {
                argument.setValue("4000");
            } else if (argument.name().equals("localAddress")) {
                argument.setValue("10.7.192.247");
            }
        }
        return defArgs;
    }

    public static ListeningConnector getConnector() {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        List<ListeningConnector> connectors = vmm.listeningConnectors();
        for (ListeningConnector connector : connectors) {
            if (connector.transport().name().equals("dt_socket")) {
                return connector;
            }
        }
        return null;
    }
}
