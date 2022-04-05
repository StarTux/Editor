package com.cavetale.editor.menu;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public final class VariableType {
    public final NodeType nodeType;
    public final Class<?> objectType;
    public final List<VariableType> genericTypes;

    public static VariableType of(Field field) {
        Class<?> objectType = field.getType();
        NodeType nodeType = NodeType.of(objectType);
        List<VariableType> genericTypes = getGenericTypes(field);
        return new VariableType(nodeType, objectType, genericTypes);
    }

    public static VariableType of(Class<?> clazz) {
        return new VariableType(NodeType.of(clazz), clazz, List.of());
    }

    private static List<VariableType> getGenericTypes(Field field) {
        List<VariableType> result = new ArrayList<>();
        if (field.getAnnotatedType() instanceof AnnotatedParameterizedType apt) {
            for (AnnotatedType at : apt.getAnnotatedActualTypeArguments()) {
                if (at.getType() instanceof Class clazz) {
                    result.add(VariableType.of(clazz));
                }
            }
        }
        return List.copyOf(result);
    }

    @Override
    public String toString() {
        return nodeType + "(" + objectType.getSimpleName()
            + (genericTypes.isEmpty()
               ? ""
               : "<" + (genericTypes.stream()
                        .map(VariableType::toString)
                        .collect(Collectors.joining(", ")))
               + ">")
            + ")";
    }

    public boolean canParseValue() {
        return nodeType == NodeType.ENUM || nodeType.canParseValue();
    }

    public Object parseValue(String in) {
        switch (nodeType) {
        case ENUM: {
            @SuppressWarnings("unchecked") Class<? extends Enum> enumType = (Class<? extends Enum>) objectType;
            try {
                @SuppressWarnings("unchecked") Object o = Enum.valueOf(enumType, in);
                return o;
            } catch (IllegalArgumentException iae) { }
            for (Enum<?> en : enumType.getEnumConstants()) {
                if (en.name().equalsIgnoreCase(in)) return en;
            }
            throw new IllegalArgumentException(enumType.getSimpleName() + ": No match: " + in);
        }
        default: return nodeType.parseValue(in);
        }
    }

    public boolean canCreateNewInstance() {
        return nodeType == NodeType.OBJECT
            || nodeType == NodeType.ENUM
            || nodeType.canCreateNewInstance();
    }

    public Object createNewInstance() {
        switch (nodeType) {
        case OBJECT:
            try {
                return objectType.getConstructor().newInstance();
            } catch (NoSuchMethodException nsme) {
                throw new MenuException("No default constructor found");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        case ENUM: {
            return objectType.getEnumConstants()[0];
        }
        default: return nodeType.createNewInstance();
        }
    }
}
