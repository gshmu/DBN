package com.dci.intellij.dbn.common.environment;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.ui.DBNColor;
import com.dci.intellij.dbn.common.util.Cloneable;
import org.jdom.Element;

import java.awt.Color;
import java.util.UUID;

public class EnvironmentType implements Cloneable, PersistentConfiguration {
    private interface EnvironmentColor {
        DBNColor DEVELOPMENT = new DBNColor(new Color(-2430209), new Color(0x445F80));
        DBNColor INTEGRATION = new DBNColor(new Color(-2621494), new Color(0x466646));
        DBNColor PRODUCTION = new DBNColor(new Color(-11574), new Color(0x634544));
        DBNColor OTHER = new DBNColor(new Color(-1576), new Color(0x5C5B41));
    }

    public static final EnvironmentType DEFAULT     = new EnvironmentType("default", "", "", null);
    public static final EnvironmentType DEVELOPMENT = new EnvironmentType("development", "Development", "Development environment", EnvironmentColor.DEVELOPMENT);
    public static final EnvironmentType INTEGRATION = new EnvironmentType("integration", "Integration", "Integration environment", EnvironmentColor.INTEGRATION);
    public static final EnvironmentType PRODUCTION  = new EnvironmentType("production", "Production", "Productive environment", EnvironmentColor.PRODUCTION);
    public static final EnvironmentType OTHER       = new EnvironmentType("other", "Other", "", EnvironmentColor.OTHER);
    public static final EnvironmentType[] DEFAULT_ENVIRONMENT_TYPES = new EnvironmentType[] {
            DEVELOPMENT,
            INTEGRATION,
            PRODUCTION,
            OTHER};

    private String id;
    private String name;
    private String description;
    private DBNColor color;

    public static EnvironmentType forName(String name) {
        for (EnvironmentType environmentType : DEFAULT_ENVIRONMENT_TYPES){
            if (environmentType.getName().equals(name)) {
                return environmentType;
            }
        }
        return null;
    }

    public EnvironmentType() {
        id = UUID.randomUUID().toString();
    }

    public EnvironmentType(String id, String name, String description, DBNColor color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DBNColor getColor() {
        return color;
    }

    public void setColor(DBNColor color) {
        this.color = color;
    }

    public EnvironmentType clone() {
        return new EnvironmentType(id, name, description, color);
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnvironmentType that = (EnvironmentType) o;

        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (!id.equals(that.id)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    @Override
    public void readConfiguration(Element element) {
        id = element.getAttributeValue("id");
        name = element.getAttributeValue("name");
        description = element.getAttributeValue("description");
        color = SettingsUtil.getColorAttribute(element, "color", color);

        EnvironmentType defaultEnvironmentType = forName(name);
        if (defaultEnvironmentType != null) {
            if (id == null) id = defaultEnvironmentType.getId();
            if (color != null && color.getRegularRgb() == color.getDarkRgb()) {
                color = new DBNColor(color.getRegularRgb(), defaultEnvironmentType.getColor().getDarkRgb());
            }
        }
        if (id == null) id = name.toLowerCase();

    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", id);
        element.setAttribute("name", name);
        element.setAttribute("description", description);
        SettingsUtil.setColorAttribute(element, "color", color);
    }
}
