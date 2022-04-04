package com.cavetale.editor.reflect;

import java.util.ArrayList;
import java.util.List;

public final class ListNode implements MenuNode {
    protected final List<Object> list;
    protected final Class<? extends Object> valueType;
    protected final NodeType valueNodeType;
    private List<ListItemNode> children;

    public ListNode(final List<Object> list, final Class<?> valueType) {
        this.list = list;
        this.valueType = valueType;
        this.valueNodeType = NodeType.of(valueType);
    }

    @Override
    public Object getObject() {
        return list;
    }

    @Override
    public List<ListItemNode> getChildren() {
        if (children == null) {
            children = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i += 1) {
                children.add(new ListItemNode(this, i));
            }
        }
        return children;
    }

    private List<Object> cutCopy(List<Integer> selection, boolean doRemove) {
        List<Object> result = new ArrayList<>(selection.size());
        for (int sel : selection) {
            ListItemNode node = getChildren().get(sel);
            result.add(node.getValue());
        }
        if (doRemove) {
            for (int i = selection.size() - 1; i >= 0; i += 1) {
                list.remove(i);
            }
            selection.clear();
        }
        return result;
    }

    @Override
    public List<Object> copy(List<Integer> selection) {
        return cutCopy(selection, false);
    }

    @Override
    public boolean canCut(List<Integer> selection) {
        return true;
    }

    @Override
    public List<Object> cut(List<Integer> selection) {
        return cutCopy(selection, true);
    }

    protected boolean canHoldValue(Object object) {
        if (object == null) return false;
        if (valueNodeType.isPrimitive()) {
            return valueNodeType == NodeType.of(object.getClass());
        }
        if (valueNodeType == NodeType.OBJECT) {
            return valueType.isInstance(object);
        }
        return false;
    }

    @Override
    public boolean canPaste(List<Object> clipboard, List<Integer> selection) {
        if (selection.size() > 1) return false;
        for (Object it : clipboard) {
            if (!canHoldValue(it)) return false;
        }
        return true;
    }

    @Override
    public void paste(List<Object> clipboard, List<Integer> selection) {
        int index = selection.isEmpty() ? list.size() : selection.get(0);
        list.addAll(index, clipboard);
    }
}
