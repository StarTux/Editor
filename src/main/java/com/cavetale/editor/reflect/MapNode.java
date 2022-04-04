package com.cavetale.editor.reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MapNode implements MenuNode {
    protected final Map<Object, Object> map;
    protected final Class<?> keyType;
    protected final NodeType keyNodeType;
    protected final Class<?> valueType;
    protected final NodeType valueNodeType;
    private List<MapItemNode> children;

    public MapNode(final Map<Object, Object> map, final Class<?> keyType, final Class<?> valueType) {
        this.map = map;
        this.keyType = keyType;
        this.keyNodeType = NodeType.of(keyType);
        this.valueType = valueType;
        this.valueNodeType = NodeType.of(valueType);
    }

    @Override
    public Object getObject() {
        return map;
    }

    @Override
    public List<MapItemNode> getChildren() {
        if (children == null) {
            List<Object> keys = new ArrayList<>(map.keySet());
            children = new ArrayList<>(keys.size());
            for (Object key : keys) {
                children.add(new MapItemNode(this, key));
            }
        }
        return children;
    }

    private List<Object> cutCopy(List<Integer> selection, boolean doRemove) {
        List<Object> result = new ArrayList<>(selection.size());
        for (int sel : selection) {
            MapItemNode node = getChildren().get(sel);
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
        if (clipboard.size() != selection.size()) return false;
        for (Object it : clipboard) {
            if (!canHoldValue(it)) return false;
        }
        return true;
    }

    @Override
    public void paste(List<Object> clipboard, List<Integer> selection) {
        int size = clipboard.size();
        for (int i = 0; i < size; i += 1) {
            Object it = clipboard.get(i);
            MapItemNode node = getChildren().get(i);
            node.setValue(it);
        }
    }
}
