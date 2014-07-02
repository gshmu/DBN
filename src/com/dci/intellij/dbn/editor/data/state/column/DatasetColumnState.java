package com.dci.intellij.dbn.editor.data.state.column;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.object.DBColumn;
import org.jdom.Element;

public class DatasetColumnState implements Comparable<DatasetColumnState>{
    private String name;
    private int position = -1;
    private boolean visible = true;

    public DatasetColumnState(DBColumn column) {
        init(column);
    }

    public void init(DBColumn column) {
        if (StringUtil.isEmpty(name)) {
            // not initialized yet
            name = column.getName();
            position = column.getPosition() -1;
            visible = true;
        }
    }

    public DatasetColumnState(Element element) {
        readState(element);
    }

    public void readState(Element element) {
        name = element.getAttributeValue("name");
        position = SettingsUtil.getIntegerAttribute(element, "position", -1);
        visible = SettingsUtil.getBooleanAttribute(element, "visible", true);
    }

    public void writeState(Element element) {
        element.setAttribute("name", name);
        SettingsUtil.setIntegerAttribute(element, "position", position);
        SettingsUtil.setBooleanAttribute(element, "visible", visible);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }


    @Override
    public int compareTo(DatasetColumnState remote) {
        return position-remote.position;
    }
}
