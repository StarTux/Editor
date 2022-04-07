package com.cavetale.editor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.editor.EditMenuAdapter;
import com.cavetale.core.editor.EditMenuItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public final class EditorCommand extends AbstractCommand<EditorPlugin> {
    protected EditorCommand(final EditorPlugin plugin) {
        super(plugin, "editor");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("test").denyTabCompletion()
            .description("Test Command")
            .playerCaller(this::test);
        rootNode.addChild("reopen").denyTabCompletion()
            .description("Reopen previous inventory")
            .playerCaller(this::reopen);
    }

    protected boolean test(Player player, String[] args) {
        Editor.open(plugin, player, new Foo(), new EditContext() { });
        return true;
    }

    protected boolean reopen(Player player, String[] args) {
        if (null == plugin.sessions.of(player).open(player)) {
            throw new CommandWarn("You do not have an open editor session");
        }
        return true;
    }

    static class Foo implements EditMenuAdapter {
        List<Integer> bars = new ArrayList<>(List.of(11, 222, 3333, 44444));
        @EditMenuItem(deletable = true)
        String foo = "foo";
        @EditMenuItem(deletable = false)
        String bar = "bar";
        @EditMenuItem(settable = false)
        int number = 1;
        double dbl = 1.23;
        String world = "home";
        int x = 12;
        int y = 65;
        int z = 13;
        double pitch = 12.3;
        double yaw = 33.3;
        boolean valid = true;
        @EditMenuItem(settable = false)
        boolean invalid = false;
        Map<String, String> map = new HashMap<>();

        @Override
        public List<Object> getPossibleValues(String fieldName, int valueIndex) {
            switch (fieldName) {
            case "dbl": return List.of(1.23, 4.56, 7.89);
            case "map":
                return valueIndex == 0
                    ? List.of("Left", "Side", "Key", "Values")
                    : List.of("Right", "Half", "Value", "Objects");
            default: return null;
            }
        }

        @Override
        public Boolean validateValue(String fieldName, Object newValue, int valueIndex) {
            switch (fieldName) {
            case "x":
            case "y":
            case "z":
                return newValue instanceof Integer integer && integer >= 0;
            default: return null;
            }
        }
    }
}
