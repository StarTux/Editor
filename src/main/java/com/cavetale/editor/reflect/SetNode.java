package com.cavetale.editor.reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SetNode implements MenuNode {
    protected final Set<Object> set;
    protected final Class<?> valueType;
    protected final NodeType valueNodeType;
    private List<SetItemNode> children;

    public SetNode(final Set<Object> set, final Class<?> valueType) {
        this.set = set;
        this.valueType = valueType;
        this.valueNodeType = NodeType.of(valueType);
    }

    @Override
    public Object getObject() {
        return set;
    }

    @Override
    public List<SetItemNode> getChildren() {
        if (children == null) {
            children = new ArrayList<>(set.size());
            for (Object it : set) {
                children.add(new SetItemNode(this, it));
            }
        }
        return children;
    }

    private List<Object> cutCopy(List<Integer> selection, boolean doRemove) {
        List<Object> result = new ArrayList<>(selection.size());
        for (int sel : selection) {
            SetItemNode node = getChildren().get(sel);
            result.add(node.getValue());
            if (doRemove) node.delete();
        }
        if (doRemove) selection.clear();
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
        if (!selection.isEmpty()) return false;
        for (Object it : clipboard) {
            if (!canHoldValue(it)) return false;
        }
        return true;
    }

    @Override
    public void paste(List<Object> clipboard, List<Integer> selection) {
        set.addAll(clipboard);
    }
}
