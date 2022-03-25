package com.cavetale.editor.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ObjectNode implements MenuNode {
    private final Object object;
    private List<FieldNode> children = null;

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public List<FieldNode> getChildren() {
        if (children == null) computeChildren();
        return children;
    }

    public void computeChildren() {
        children = new ArrayList<>();
        computeChildrenRecursive(object.getClass());
    }

    private void computeChildrenRecursive(Class<?> clazz) {
        Class<?> class2 = clazz.getSuperclass();
        if (class2 != null) computeChildrenRecursive(class2);
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            List<String> modifierNames = new ArrayList<>();
            if (Modifier.isStatic(modifiers)) continue;
            if (Modifier.isTransient(modifiers)) continue;
            if (Modifier.isFinal(modifiers)) continue;
            children.add(new FieldNode(object, field));
        }
    }
}
