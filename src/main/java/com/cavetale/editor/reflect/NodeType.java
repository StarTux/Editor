package com.cavetale.editor.reflect;

import com.cavetale.mytems.Mytems;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NodeType {
    STRING(Category.PRIMITIVE, Mytems.DATA_STRING),
    INTEGER(Category.PRIMITIVE, Mytems.DATA_INTEGER),
    LONG(Category.PRIMITIVE, Mytems.DATA_INTEGER),
    SHORT(Category.PRIMITIVE, Mytems.DATA_INTEGER),
    DOUBLE(Category.PRIMITIVE, Mytems.DATA_FLOAT),
    FLOAT(Category.PRIMITIVE, Mytems.DATA_FLOAT),
    BOOLEAN(Category.PRIMITIVE, Mytems.LETTER_B),
    MAP(Category.MENU, Mytems.FOLDER),
    LIST(Category.MENU, Mytems.FOLDER),
    SET(Category.MENU, Mytems.FOLDER),
    OBJECT(Category.MENU, Mytems.FOLDER);

    public final Category category;
    public final Mytems mytems;

    public enum Category {
        PRIMITIVE,
        MENU;
    }

    public boolean isMenu() {
        return category == Category.MENU;
    }

    public boolean isPrimitive() {
        return category == Category.PRIMITIVE;
    }

    public static NodeType of(Class<?> type) {
        if (Map.class.isAssignableFrom(type)) {
            return MAP;
        } else if (List.class.isAssignableFrom(type)) {
            return LIST;
        } else if (Set.class.isAssignableFrom(type)) {
            return SET;
        } else if (String.class == type) {
            return STRING;
        } else if (Integer.class == type || int.class == type) {
            return INTEGER;
        } else if (Long.class == type || long.class == type) {
            return LONG;
        } else if (Short.class == type || short.class == type) {
            return SHORT;
        } else if (Double.class == type || double.class == type) {
            return DOUBLE;
        } else if (Float.class == type || float.class == type) {
            return FLOAT;
        } else if (Boolean.class == type || boolean.class == type) {
            return BOOLEAN;
        } else {
            return OBJECT;
        }
    }

    public Object parseValue(String in) throws IllegalArgumentException {
        switch (this) {
        case STRING: return in;
        case INTEGER: return Integer.parseInt(in);
        case LONG: return Long.parseLong(in);
        case SHORT: return Short.parseShort(in);
        case DOUBLE: return Double.parseDouble(in);
        case FLOAT: return Float.parseFloat(in);
        case BOOLEAN: return Boolean.parseBoolean(in);
        default:
            throw new IllegalArgumentException(this + " cannot be parsed!");
        }
    }
}
