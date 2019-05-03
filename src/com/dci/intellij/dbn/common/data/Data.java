package com.dci.intellij.dbn.common.data;

import com.dci.intellij.dbn.common.util.StringUtil;

import javax.annotation.Nullable;

public interface Data {
    static <T> T cast(@Nullable Object object, Class<T> type) {
        if (object != null) {
            if (type == String.class) {
                return (T) asString(object);
            }

            if (type == int.class) {
                return (T) ((Integer) asInt(object));
            }

            if (type == Integer.class) {
                return (T) asInteger(object);
            }

            if (type == boolean.class) {
                return (T) ((Boolean) asBool(object));
            }

            if (type == Boolean.class) {
                return (T) asBoolean(object);
            }

            throw new UnsupportedOperationException("Cast from " + object.getClass() + " to " + type + " is not implemented");
            // TODO add more cast logic if required
        }
        return null;
    }



    @Nullable
    static String asString(@Nullable Object object) {
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    @Nullable
    static Integer asInteger(@Nullable Object object) {
        if (object != null) {
            if (object instanceof Integer) {
                return (Integer) object;

            } else if (object instanceof Number) {
                Number number = (Number) object;
                return number.intValue();
            }

            return Integer.valueOf(object.toString());
        }
        return null;
    }

    static int asInt(@Nullable Object object) {
        Integer integer = asInteger(object);
        return integer == null ? 0 : integer;
    }

    @Nullable
    static Boolean asBoolean(@Nullable Object object) {
        if (object != null) {
            if (object instanceof Boolean) {
                return (Boolean) object;
            } else if (object instanceof String) {
                String string = (String) object;
                return StringUtil.isOneOfIgnoreCase(string, "Y", "YES", "TRUE", "1");
            } else if (object instanceof Number) {
                Number number = (Number) object;
                return number.intValue() != 0;
            }
        }
        return null;
    }

    static boolean asBool(@Nullable Object object) {
        Boolean bool = asBoolean(object);
        return bool != null && bool;
    }

}
