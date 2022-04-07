package com.cavetale.editor.reflect;

import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.VariableType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public final class ListNode implements MenuNode {
    protected final List<Object> list;
    protected final VariableType variableType;
    protected final VariableType valueType;
    private List<ListItemNode> children;

    public ListNode(final List<Object> list, final VariableType variableType) {
        if (variableType.genericTypes.size() != 1) {
            throw new IllegalStateException(variableType.toString());
        }
        this.list = list;
        this.variableType = variableType;
        this.valueType = variableType.genericTypes.get(0);
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
            for (int i = selection.size() - 1; i >= 0; i -= 1) {
                int index = selection.get(i);
                list.remove(index);
            }
            selection.clear();
        }
        System.out.println("cutCopy " + list + " " + selection + " " + doRemove + " => " + result);
        return result;
    }

    @Override
    public List<Object> copy(List<Integer> selection) {
        return cutCopy(selection, false);
    }

    @Override
    public boolean canCut(List<Integer> selection) {
        for (int sel : selection) {
            if (sel >= list.size()) return false;
        }
        return true;
    }

    @Override
    public List<Object> cut(List<Integer> selection) {
        return cutCopy(selection, true);
    }

    @Override
    public boolean canPaste(List<Object> clipboard, List<Integer> selection) {
        if (selection.size() > 1) return false;
        for (Object it : clipboard) {
            if (!valueType.canHold(it)) return false;
        }
        return true;
    }

    @Override
    public void paste(List<Object> clipboard, List<Integer> selection) {
        int index = selection.isEmpty() ? list.size() : selection.get(0);
        list.addAll(index, clipboard);
    }
}
