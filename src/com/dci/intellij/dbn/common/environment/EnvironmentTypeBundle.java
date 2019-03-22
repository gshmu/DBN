package com.dci.intellij.dbn.common.environment;

import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EnvironmentTypeBundle implements Iterable<EnvironmentType>, Cloneable {
    private List<EnvironmentType> environmentTypes = new ArrayList<>();
    public static EnvironmentTypeBundle DEFAULT = new EnvironmentTypeBundle();

    private EnvironmentTypeBundle() {
        List<EnvironmentType> environmentTypes = Arrays.asList(
                EnvironmentType.DEVELOPMENT,
                EnvironmentType.INTEGRATION,
                EnvironmentType.PRODUCTION,
                EnvironmentType.OTHER);
        setElements(environmentTypes);
    }
    
    public EnvironmentTypeBundle(EnvironmentTypeBundle source) {
        setElements(source.environmentTypes);
    }  
    
    private void setElements(List<EnvironmentType> environmentTypes) {
        this.environmentTypes.clear();
        CollectionUtil.cloneElements(environmentTypes, this.environmentTypes);
    }
    
    @NotNull
    public EnvironmentType getEnvironmentType(EnvironmentTypeId id) {
        for (EnvironmentType environmentType : this) {
            if (environmentType.getId() == id) {
                return environmentType;
            }
        }
        return EnvironmentType.DEFAULT;
    }

    @Override
    public Iterator<EnvironmentType> iterator() {
        return environmentTypes.iterator();
    }

    public void clear() {
        environmentTypes.clear();
    }

    public void add(EnvironmentType environmentType) {
        environmentTypes.add(environmentType);
    }
    
    public void add(int index, EnvironmentType environmentType) {
        environmentTypes.add(index, environmentType);
    }
    

    public int size() {
        return environmentTypes.size();
    }
    
    public EnvironmentType get(int index) {
        return environmentTypes.get(index);
    }

    public EnvironmentType remove(int index) {
        return environmentTypes.remove(index);
    }

    public List<EnvironmentType> getEnvironmentTypes() {
        return environmentTypes;
    }

    @Override
    public EnvironmentTypeBundle clone() {
        return new EnvironmentTypeBundle(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnvironmentTypeBundle) {
            EnvironmentTypeBundle bundle = (EnvironmentTypeBundle) obj;
            if (environmentTypes.size() != bundle.environmentTypes.size()) {
                return false;
            }
            for (int i=0; i<this.environmentTypes.size(); i++) {
                if (!environmentTypes.get(i).equals(bundle.environmentTypes.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
