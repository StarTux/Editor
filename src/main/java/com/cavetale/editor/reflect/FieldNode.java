package com.cavetale.editor.reflect;

import com.cavetale.core.editor.EditMenuAdapter;
import com.cavetale.core.editor.EditMenuItem;
import com.cavetale.editor.menu.MenuItemNode;
import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.VariableType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class FieldNode implements MenuItemNode {
    protected final ObjectNode parentNode;
    protected final Field field;
    @Getter protected final VariableType variableType;
    private final boolean deletable;
    private final boolean canSetValue;
    @Getter private final String description;

    public FieldNode(final ObjectNode parentNode, final Field field) {
        this.parentNode = parentNode;
        this.field = field;
        EditMenuAdapter adapter = parentNode.object instanceof EditMenuAdapter theAdapter
            ? theAdapter
            : new EditMenuAdapter() { };
        this.variableType = VariableType.of(field, adapter, parentNode);
        final int modifiers = field.getModifiers();
        EditMenuItem editMenuItem = field.getAnnotation(EditMenuItem.class);
        this.deletable = editMenuItem != null
            ? editMenuItem.deletable()
            : false;
        this.canSetValue = editMenuItem != null
            ? editMenuItem.settable()
            : !Modifier.isFinal(modifiers);
        this.description = editMenuItem != null
            ? editMenuItem.description()
            : "";
    }

    @Override
    public String getKey() {
        return field.getName();
    }

    @Override
    public Object getValue() {
        field.setAccessible(true);
        try {
            return field.get(parentNode.object);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    @Override
    public boolean canSetValue() {
        return canSetValue;
    }

    @Override
    public void setValue(Object value) {
        field.setAccessible(true);
        try {
            field.set(parentNode.object, value);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }
    }

    @Override
    public boolean isDeletable() {
        return deletable;
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
        switch (variableType.nodeType) {
        case MAP: {
            Object value = getValue();
            if (!(value instanceof Map)) return null;
            @SuppressWarnings("unchecked") Map<Object, Object> map = (Map<Object, Object>) value;
            return new MapNode(parentNode.session, parentNode, map, variableType);
        }
        case LIST: {
            Object value = getValue();
            if (!(value instanceof List)) return null;
            @SuppressWarnings("unchecked") List<Object> list = (List<Object>) value;
            return new ListNode(parentNode.session, parentNode, list, variableType);
        }
        case SET: {
            Object value = getValue();
            if (!(value instanceof Set)) return null;
            @SuppressWarnings("unchecked") Set<Object> set = (Set<Object>) value;
            return new SetNode(parentNode.session, parentNode, set, variableType);
        }
        case OBJECT: {
            Object value = getValue();
            if (value == null) return null;
            return new ObjectNode(parentNode.session, parentNode, value);
        }
        default: return null;
        }
    }
}
