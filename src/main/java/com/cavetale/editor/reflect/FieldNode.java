package com.cavetale.editor.reflect;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class FieldNode implements MenuItemNode {
    protected final Object parent;
    protected final Field field;
    @Getter protected final NodeType nodeType;

    public FieldNode(final Object parent, final Field field) {
        this.parent = parent;
        this.field = field;
        Class<?> fieldType = field.getType();
        this.nodeType = NodeType.of(fieldType);
    }

    @Override
    public String getKey() {
        return field.getName();
    }

    @Override
    public Object getValue() {
        field.setAccessible(true);
        try {
            return field.get(parent);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    @Override
    public boolean canSetValue() {
        return true;
    }

    @Override
    public void setValue(Object value) {
        field.setAccessible(true);
        try {
            field.set(parent, value);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    @Override
    public boolean isDeletable() {
        return !field.getType().isPrimitive();
    }

    @Override
    public void delete() {
        setValue(null);
    }

    private List<Class<?>> getGenericTypes() {
        List<Class<?>> result = new ArrayList<>();
        if (field.getAnnotatedType() instanceof AnnotatedParameterizedType apt) {
            for (AnnotatedType at : apt.getAnnotatedActualTypeArguments()) {
                if (at.getType() instanceof Class clazz) {
                    result.add(clazz);
                }
            }
        }
        return result;
    }

    @Override
    public MenuNode getMenuNode() {
        switch (nodeType) {
        case MAP: {
            List<Class<?>> genericTypes = getGenericTypes();
            if (genericTypes.size() != 2) {
                throw new IllegalStateException("Map<" + genericTypes.size() + ">");
            }
            Object value = getValue();
            if (!(value instanceof Map)) return null;
            @SuppressWarnings("unchecked") Map<Object, Object> map = (Map<Object, Object>) value;
            return new MapNode(map, genericTypes.get(0), genericTypes.get(1));
        }
        case LIST: {
            List<Class<?>> genericTypes = getGenericTypes();
            if (genericTypes.size() != 1) {
                throw new IllegalStateException("List<" + genericTypes.size() + ">");
            }
            Object value = getValue();
            if (!(value instanceof List)) return null;
            @SuppressWarnings("unchecked") List<Object> list = (List<Object>) value;
            return new ListNode(list, genericTypes.get(0));
        }
        case SET: {
            List<Class<?>> genericTypes = getGenericTypes();
            if (genericTypes.size() != 1) {
                throw new IllegalStateException("Set<" + genericTypes.size() + ">");
            }
            Object value = getValue();
            if (!(value instanceof Set)) return null;
            @SuppressWarnings("unchecked") Set<Object> set = (Set<Object>) value;
            return new SetNode(set, genericTypes.get(0));
        }
        case OBJECT: {
            Object value = getValue();
            if (value == null) return null;
            return new ObjectNode(value);
        }
        default: return null;
        }
    }

    @Override
    public boolean canHold(Object object) {
        if (object == null) return isDeletable();
        if (nodeType.isPrimitive()) {
            NodeType objectType = NodeType.of(object.getClass());
            return nodeType == objectType;
        }
        switch (nodeType) {
        case OBJECT: return field.getClass().isInstance(object);
        case MAP: {
            if (!(object instanceof Map)) return false;
            @SuppressWarnings("unchecked") Map<Object, Object> map = (Map<Object, Object>) object;
            List<Class<?>> genericTypes = getGenericTypes();
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if (!genericTypes.get(0).isInstance(entry.getKey())) return false;
                if (!genericTypes.get(1).isInstance(entry.getValue())) return false;
            }
            return true;
        }
        case LIST: {
            if (!(object instanceof List)) return false;
            @SuppressWarnings("unchecked") List<Object> list = (List<Object>) object;
            List<Class<?>> genericTypes = getGenericTypes();
            for (Object it : list) {
                if (!genericTypes.get(0).isInstance(it)) return false;
            }
            return true;
        }
        default: throw new IllegalStateException("nodeType=" + nodeType);
        }
    }
}
