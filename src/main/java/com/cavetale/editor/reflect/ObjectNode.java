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

    private List<Object> cutCopy(List<Integer> selection, boolean doRemove) {
        List<FieldNode> nodes = getChildren();
        for (int sel : selection) {
            if (sel < 0 || sel >= nodes.size()) {
                throw new MenuException("Selection out of bounds!");
            }
            FieldNode node = nodes.get(sel);
            if (doRemove && !node.isDeletable()) {
                throw new MenuException("Cannot cut " + node.getKey());
            }
        }
        List<Object> result = new ArrayList<>();
        for (int sel : selection) {
            FieldNode node = nodes.get(sel);
            result.add(node.getValue());
            node.setValue(null);
        }
        return result;
    }

    @Override
    public List<Object> cut(List<Integer> selection) {
        return cutCopy(selection, true);
    }

    @Override
    public List<Object> copy(List<Integer> selection) {
        return cutCopy(selection, false);
    }

    @Override
    public int paste(List<Object> clipboard, List<Integer> selection) {
        if (clipboard.size() != selection.size()) {
            throw new MenuException("Clipboard size does not equal selection size");
        }
        int size = clipboard.size();
        for (int i = 0; i < size; i += 1) {
            Object it = clipboard.get(i);
            FieldNode node = getChildren().get(i);
            if (!node.canHold(it)) {
                throw new MenuException("Incompatible clipboard types!");
            }
        }
        return 0;
    }
}
