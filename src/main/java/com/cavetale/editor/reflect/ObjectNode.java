package com.cavetale.editor.reflect;

import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.session.Session;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public final class ObjectNode implements MenuNode {
    protected final Session session;
    protected final MenuNode parentNode;
    protected final Object object;
    private List<FieldNode> children = null;

    @Override
    public Session getContext() {
        return session;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public MenuNode getParentNode() {
        return parentNode;
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
            FieldNode fieldNode = new FieldNode(this, field);
            if (!fieldNode.isHidden()) {
                children.add(fieldNode);
            }
        }
    }

    private List<Object> cutCopy(List<Integer> selection, boolean doRemove) {
        List<Object> result = new ArrayList<>(selection.size());
        for (int sel : selection) {
            FieldNode node = getChildren().get(sel);
            result.add(node.getValue());
            if (doRemove) node.delete();
        }
        return result;
    }

    @Override
    public List<Object> copy(List<Integer> selection) {
        return cutCopy(selection, false);
    }

    @Override
    public boolean canCut(List<Integer> selection) {
        for (int it : selection) {
            if (!getChildren().get(it).isDeletable()) return false;
        }
        return true;
    }

    @Override
    public List<Object> cut(List<Integer> selection) {
        return cutCopy(selection, true);
    }

    @Override
    public boolean canPaste(List<Object> clipboard, List<Integer> selection) {
        if (clipboard.size() != selection.size()) return false;
        int size = clipboard.size();
        for (int i = 0; i < size; i += 1) {
            Object it = clipboard.get(i);
            FieldNode node = getChildren().get(selection.get(i));
            if (!node.canSetValue() || !node.variableType.canHold(it)) return false;
        }
        return true;
    }

    @Override
    public void paste(List<Object> clipboard, List<Integer> selection) {
        int size = clipboard.size();
        for (int i = 0; i < size; i += 1) {
            Object it = clipboard.get(i);
            FieldNode node = getChildren().get(selection.get(i));
            node.setValue(it);
        }
    }
}
