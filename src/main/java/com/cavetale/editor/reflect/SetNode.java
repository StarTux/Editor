package com.cavetale.editor.reflect;

import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.VariableType;
import com.cavetale.editor.session.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public final class SetNode implements MenuNode {
    protected final Session session;
    protected final MenuNode parentNode;
    protected final Set<Object> set;
    protected final VariableType variableType;
    protected final VariableType valueType;
    private List<SetItemNode> children;

    public SetNode(final Session session, final @Nullable MenuNode parentNode, final Set<Object> set, final VariableType variableType) {
        if (variableType.genericTypes.size() != 1) {
            throw new IllegalStateException(variableType.toString());
        }
        this.session = session;
        this.parentNode = parentNode;
        this.set = set;
        this.variableType = variableType;
        this.valueType = variableType.genericTypes.get(0);
    }

    @Override
    public Session getContext() {
        return session;
    }

    @Override
    public Object getObject() {
        return set;
    }

    @Override
    public MenuNode getParentNode() {
        return parentNode;
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

    @Override
    public boolean canPaste(List<Object> clipboard, List<Integer> selection) {
        if (!selection.isEmpty()) return false;
        for (Object it : clipboard) {
            if (!valueType.canHold(it)) return false;
        }
        return true;
    }

    @Override
    public void paste(List<Object> clipboard, List<Integer> selection) {
        set.addAll(clipboard);
    }
}
