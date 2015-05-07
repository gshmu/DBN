package com.dci.intellij.dbn.execution.script;

import javax.swing.Icon;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.DatabaseType;

public class CmdLineInterface extends CommonUtil implements Cloneable<CmdLineInterface>, PersistentConfiguration, Presentable {
    private DatabaseType databaseType;
    private String executablePath;
    private String name;

    public CmdLineInterface() {

    }

    public CmdLineInterface(DatabaseType databaseType, String executablePath, String name) {
        this.databaseType = databaseType;
        this.executablePath = executablePath;
        this.name = name;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return databaseType.getIcon();
    }

    @NotNull
    @Override
    public String getName() {
        return CommonUtil.nvl(name, "");
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void readConfiguration(Element element) {
        name = element.getAttributeValue("name");
        executablePath = element.getAttributeValue("executable-path");
        databaseType = DatabaseType.get(element.getAttributeValue("database-type"));
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("name", name);
        element.setAttribute("executable-path", executablePath);
        element.setAttribute("database-type", databaseType.name());
    }

    @Override
    public CmdLineInterface clone() {
        return new CmdLineInterface(databaseType, executablePath, name);
    }
}
