package com.dci.intellij.dbn.object.properties;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.pom.Navigatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.swing.Icon;

@Getter
@EqualsAndHashCode(callSuper = false)
public class DBObjectPresentableProperty extends PresentableProperty{
    private final DBObjectRef objectRef;
    private final boolean qualified;
    private final String name;


    public DBObjectPresentableProperty(String name, DBObject object, boolean qualified) {
        this.objectRef = object.getRef();
        this.qualified = qualified;
        this.name = name;
    }

    public DBObjectPresentableProperty(DBObject object, boolean qualified) {
        this(null, object, qualified);
    }

    public DBObjectPresentableProperty(DBObject object) {
        this(null, object, false);
    }

    @Override
    public String getName() {
        return name == null ? NamingUtil.capitalize(objectRef.getObjectType().getName()) : name;
    }

    @Override
    public String getValue() {
        return qualified ? objectRef.getPath() : objectRef.getObjectName();
    }

    @Override
    public Icon getIcon() {
        DBObject object = objectRef.get();
        return object == null ? null : object.getIcon();
    }

    @Override
    public Navigatable getNavigatable() {
        return objectRef.get();
    }
}
