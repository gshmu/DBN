package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.content.DynamicContentElement;

public interface SearchAdapter<T> {
    int compare(T element);


    static <O extends DynamicContentElement> SearchAdapter<O> forNameAndOverload(String name, short overload) {
        String upperCaseName = name.toUpperCase();
        return object -> {
            String objectName = object.getName();
            short objectOverload = object.getOverload();

            if (objectName.equalsIgnoreCase(name)) {
                return objectOverload - overload;
            }

            int comp = objectName.toUpperCase().compareTo(upperCaseName);



/*
            // TODO underscore (_ 95) is between upper case (A 65) and lower case (b 97)
            // none of the "compare ignore case" will match the upper case sorting

            int comp0 = Strings.compare(objectName, name, true);
            int comp1 = objectName.compareToIgnoreCase(name);

            if (Integer.signum(comp) != Integer.signum(comp0)) {
                System.out.println(objectName + " " + name);
            }
*/

            return comp == 0 ? objectOverload - overload : comp;
        };
    }
}
