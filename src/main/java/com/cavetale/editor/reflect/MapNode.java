package com.cavetale.editor.reflect;

import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.VariableType;
import com.cavetale.editor.session.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public final class MapNode implements MenuNode {
    protected final Session session;
    protected final MenuNode parentNode;
    protected final Map<Object, Object> map;
    protected final VariableType variableType;
    protected final VariableType keyType;
    protected final VariableType valueType;
    private List<MapItemNode> children;

    public MapNode(final Session session, final @Nullable MenuNode parentNode, final Map<Object, Object> map, final VariableType variableType) {
        if (variableType.genericTypes.size() != 2) {
            throw new IllegalStateException(variableType.toString());
        }
        this.session = session;
        this.parentNode = parentNode;
        this.map = map;
        this.variableType = variableType;
        this.keyType = variableType.genericTypes.get(0);
        this.valueType = variableType.genericTypes.get(1);
    }

    @Override
    public Session getContext() {
        return session;
    }

    @Override
    public Object getObject() {
        return map;
    }

    @Override
    public MenuNode getParentNode() {
        return parentNode;
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

    @Override
    public boolean canPaste(List<Object> clipboard, List<Integer> selection) {
        if (clipboard.size() != selection.size()) return false;
        for (Object it : clipboard) {
            if (!valueType.canHold(it)) return false;
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
