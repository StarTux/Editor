package com.cavetale.editor;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.editor.EditMenuAdapter;
import com.cavetale.core.editor.EditMenuButton;
import java.util.ArrayList;
import java.util.List;
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

    static class Foo {
        Bar bar = new Bar();
        List<Integer> bars = new ArrayList<>(List.of(11, 222, 3333, 44444));
        String name = "foo";
        int number = 1;
        double dbl = 1.23;
        String world = "home";
        int x = 12;
        int y = 65;
        int z = 13;
        double pitch = 12.3;
        double yaw = 33.3;
        boolean valid = true;
        boolean invalid = false;
    }

    static class Bar implements EditMenuAdapter {
        String message = "Hello World";
        double volume = 99.9;
        int iter = 0;

        @Override
        public List<EditMenuButton> getEditMenuButtons() {
            return List.of();
        }
    }
}