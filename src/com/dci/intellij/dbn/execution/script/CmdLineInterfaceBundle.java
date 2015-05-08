package com.dci.intellij.dbn.execution.script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.connection.DatabaseType;

public class CmdLineInterfaceBundle implements com.dci.intellij.dbn.common.util.Cloneable<CmdLineInterfaceBundle>, PersistentConfiguration {
    private List<CmdLineInterface> elements = new ArrayList<CmdLineInterface>();

    public void clear() {
        elements.clear();
    }

    public void add(CmdLineInterface cmdLineInterface) {
        elements.add(cmdLineInterface);
    }

    public void add(int index, CmdLineInterface cmdLineInterface) {
        elements.add(index, cmdLineInterface);
    }

    public int size() {
        return elements.size();
    }

    public CmdLineInterface get(int index) {
        return elements.get(index);
    }

    @Nullable
    public CmdLineInterface getInterface(String id) {
        for (CmdLineInterface cmdLineInterface : elements) {
            if (cmdLineInterface.getId().equals(id)) {
                return cmdLineInterface;
            }
        }

        return null;
    }

    public CmdLineInterface remove(int index) {
        return elements.remove(index);
    }

    public List<CmdLineInterface> getInterfaces() {
        return elements;
    }

    @Override
    public void readConfiguration(Element element) {
        if (element != null) {
            List<Element> children = element.getChildren();
            for (Element child : children) {
                CmdLineInterface cmdLineInterface = new CmdLineInterface();
                cmdLineInterface.readConfiguration(child);
                elements.add(cmdLineInterface);
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        for (CmdLineInterface cmdLineInterface : elements) {
            Element child = new Element("value");
            cmdLineInterface.writeConfiguration(child);
            element.addContent(child);
        }

    }

    @Override
    public CmdLineInterfaceBundle clone() {
        CmdLineInterfaceBundle cmdLineInterfaces = new CmdLineInterfaceBundle();
        for (CmdLineInterface cmdLineInterface : elements) {
            cmdLineInterfaces.elements.add(cmdLineInterface.clone());
        }

        return cmdLineInterfaces;
    }

    public List<CmdLineInterface> getInterfaces(DatabaseType databaseType) {
        List<CmdLineInterface> interfaces = new ArrayList<CmdLineInterface>();
        for (CmdLineInterface cmdLineInterface : elements) {
            if (cmdLineInterface.getDatabaseType() == databaseType) {
                interfaces.add(cmdLineInterface);
            }
        }
        return interfaces;
    }

    public Set<String> getInterfaceNames() {
        Set<String> names = new HashSet<String>();
        for (CmdLineInterface cmdLineInterface : elements) {
            names.add(cmdLineInterface.getName());
        }
        return names;
    }
}
