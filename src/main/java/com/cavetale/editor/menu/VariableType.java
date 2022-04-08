package com.cavetale.editor.menu;

import com.cavetale.core.editor.EditMenuAdapter;
import com.cavetale.core.editor.EditMenuNode;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public final class VariableType {
    public final NodeType nodeType;
    public final Class<?> objectType;
    public final List<VariableType> genericTypes;
    private final Supplier<List<Object>> possibleValueSupplier;
    private final Function<Object, Boolean> valueValidator;
    private final Supplier<Object> newValueSupplier;

    public static VariableType of(Field field, EditMenuAdapter adapter, EditMenuNode node) {
        Class<?> objectType = field.getType();
        NodeType nodeType = NodeType.of(objectType);
        List<VariableType> genericTypes = getGenericTypes(field, adapter, node);
        return new VariableType(nodeType, objectType, genericTypes,
                                () -> adapter.getPossibleValues(node, field.getName(), 0),
                                o -> adapter.validateValue(node, field.getName(), o, 0),
                                () -> adapter.createNewValue(node, field.getName(), 0));
    }

    private static List<VariableType> getGenericTypes(Field field, EditMenuAdapter adapter, EditMenuNode node) {
        List<VariableType> result = new ArrayList<>();
        if (field.getAnnotatedType() instanceof AnnotatedParameterizedType apt) {
            int i = 0;
            for (AnnotatedType at : apt.getAnnotatedActualTypeArguments()) {
                if (at.getType() instanceof Class clazz) {
                    final int index = i++;
                    result.add(new VariableType(NodeType.of(clazz),
                                                clazz,
                                                List.of(),
                                                () -> adapter.getPossibleValues(node, field.getName(), index),
                                                o -> adapter.validateValue(node, field.getName(), o, index),
                                                () -> adapter.createNewValue(node, field.getName(), 0)));
                }
            }
        }
        return List.copyOf(result);
    }

    public static String classNameOf(Class<?> clazz) {
        Class<?> enclosing = clazz.getEnclosingClass();
        return enclosing != null
            ? classNameOf(enclosing) + "." + clazz.getSimpleName()
            : clazz.getSimpleName();
    }

    public String getClassName() {
        return classNameOf(objectType);
    }

    @Override
    public String toString() {
        return nodeType + "(" + getClassName()
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
                Constructor constructor = objectType.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
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

    public List<Object> getPossibleValues() {
        return possibleValueSupplier.get();
    }

    public boolean canHold(Object object) {
        // Adapter
        List<Object> possibleValues = possibleValueSupplier.get();
        if (possibleValues != null) return possibleValues.contains(object);
        Boolean validated = valueValidator.apply(object);
        if (validated != null) return validated;
        // Generic
        if (object == null) return false;
        if (nodeType.isPrimitive()) {
            return nodeType == NodeType.of(object.getClass());
        }
        switch (nodeType) {
        case OBJECT: return objectType.isInstance(object);
        case MAP: {
            if (!(object instanceof Map)) return false;
            @SuppressWarnings("unchecked") Map<Object, Object> map = (Map<Object, Object>) object;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if (!genericTypes.get(0).canHold(entry.getKey())) return false;
                if (!genericTypes.get(1).canHold(entry.getValue())) return false;
            }
            return true;
        }
        case LIST: {
            if (!(object instanceof List)) return false;
            @SuppressWarnings("unchecked") List<Object> list = (List<Object>) object;
            for (Object it : list) {
                if (!genericTypes.get(0).canHold(it)) return false;
            }
            return true;
        }
        default: throw new IllegalStateException("variableType=" + this);
        }
    }

    public Object createNewValue() {
        return newValueSupplier.get();
    }
}
