package com.cavetale.editor;

import com.cavetale.editor.gui.Gui;
import com.cavetale.editor.session.Sessions;
import org.bukkit.plugin.java.JavaPlugin;

public final class EditorPlugin extends JavaPlugin {
    protected static EditorPlugin instance;
    protected Sessions sessions = new Sessions(this);
    private EditorCommand editorCommand = new EditorCommand(this);
    protected Editor editor = new Editor(this);

    @Override
    public void onLoad() {
        instance = this;
        editor.register();
    }

    @Override
    public void onEnable() {
        Gui.enable(this);
        editorCommand.enable();
        sessions.enable();
    }

    @Override
    public void onDisable() {
        sessions.disable();
        Gui.disable();
        editor.unregister();
    }
}
