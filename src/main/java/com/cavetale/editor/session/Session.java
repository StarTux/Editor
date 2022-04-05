package com.cavetale.editor.session;

import com.cavetale.core.editor.EditMenuAdapter;
import com.cavetale.core.editor.EditMenuButton;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.Unicode;
import com.cavetale.editor.EditContext;
import com.cavetale.editor.gui.Gui;
import com.cavetale.editor.menu.MenuException;
import com.cavetale.editor.menu.MenuItemNode;
import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.NodeType;
import com.cavetale.editor.menu.VariableType;
import com.cavetale.editor.reflect.ListNode;
import com.cavetale.editor.reflect.ObjectNode;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

@RequiredArgsConstructor
public final class Session {
    private final Sessions sessions;
    private final UUID uuid;
    private Plugin owningPlugin;
    private Object rootObject;
    private EditContext editContext;
    private final List<PathNode> path = new ArrayList<>();
    private final PathNode rootPath = new PathNode("", 0);
    protected Consumer<String> chatCallback = null;
    private final List<Integer> selection = new ArrayList<>();
    private final List<Object> clipboard = new ArrayList<>();

    public Session setup(final Plugin thePlugin, final Object theRootObject, final EditContext theEditContext) {
        this.owningPlugin = thePlugin;
        this.rootObject = theRootObject;
        this.editContext = theEditContext;
        this.path.clear();
        this.rootPath.page = 0;
        this.selection.clear();
        return this;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Find the MenuNode based on the current path.  This will rebuild
     * the path, stripping off path components which do not go
     * anywhere.
     */
    private MenuNode findPath() {
        MenuNode menuNode = new ObjectNode(rootObject);
        List<PathNode> pathCopy = List.copyOf(path);
        path.clear();
        for (PathNode pathIt : pathCopy) {
            MenuItemNode menuItem = menuNode.getChildNode(pathIt.name);
            if (menuItem == null || !menuItem.getNodeType().isMenu()) break;
            MenuNode newMenuNode = menuItem.getMenuNode();
            if (newMenuNode == null) break;
            menuNode = newMenuNode;
            path.add(pathIt);
        }
        return menuNode;
    }

    public String getPathString() {
        if (path.isEmpty()) return "/";
        StringBuilder sb = new StringBuilder();
        for (PathNode it : path) {
            sb.append("/").append(it.name);
        }
        return sb.toString();
    }

    private static void click(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    private static void fail(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.MASTER, 1.0f, 0.5f);
    }

    public Gui open(Player player) {
        if (rootObject == null) return null;
        MenuNode menuNode = findPath();
        final PathNode pathNode = path.isEmpty() ? rootPath : path.get(path.size() - 1);
        List<? extends MenuItemNode> children = menuNode.getChildren();
        final int rows = 5;
        final int inventorySize = rows * 9 + 9;
        final int pageSize = rows * 9;
        final int pageCount = (children.size() - 1) / pageSize + 1;
        final int pageIndex = Math.min(pageCount - 1, pathNode.page);
        pathNode.page = pageIndex;
        Component title = join(noSeparators(), new Component[] {
                text(pageCount > 1 ? "" + (pageIndex + 1) + "/" + pageCount + " " : "", GRAY),
                text("Editor ", BLUE),
                text(getPathString(), WHITE),
            });
        Gui gui = new Gui(owningPlugin).size(inventorySize);
        GuiOverlay.Builder titleBuilder = GuiOverlay.BLANK.builder(inventorySize, DARK_GRAY)
            .title(title)
            .layer(GuiOverlay.TOP_BAR, BLACK);
        if (pageIndex > 0) {
            gui.setItem(0, Mytems.ARROW_LEFT.createItemStack(), click -> {
                    pathNode.page -= 1;
                    open(player);
                    click(player);
                });
        }
        if (pageIndex < pageCount - 1) {
            gui.setItem(8, Mytems.ARROW_RIGHT.createItemStack(), click -> {
                    pathNode.page += 1;
                    open(player);
                    click(player);
                });
        }
        Iterator<Integer> iter = List.of(4, 5, 3, 6, 2, 7, 1).iterator();
        ItemStack diskItem = Mytems.FLOPPY_DISK.createItemStack();
        diskItem.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        gui.setItem(iter.next(), Items.text(diskItem, List.of(text("SAVE", GREEN))), click -> {
                if (click.isLeftClick()) {
                    try {
                        editContext.save();
                        player.sendMessage(text("Saved to disk!", GREEN));
                        click(player);
                    } catch (MenuException me) {
                        player.sendMessage(text("Error saving: " + me.getMessage(), RED));
                        fail(player);
                    }
                }
            });
        if (!selection.isEmpty()) {
            gui.setItem(iter.next(), Items.text(Mytems.MAGNET.createIcon(), List.of(text("COPY", GREEN))), click -> {
                    if (click.isLeftClick()) {
                        List<Object> newClipboard;
                        try {
                            newClipboard = menuNode.copy(selection);
                        } catch (MenuException me) {
                            player.sendMessage(text("Copy failed: " + me.getMessage(), RED));
                            fail(player);
                            return;
                        }
                        if (newClipboard != null && !newClipboard.isEmpty()) {
                            this.clipboard.clear();
                            this.clipboard.addAll(newClipboard);
                            player.sendMessage(text("Copied " + clipboard.size() + " objects", GREEN));
                            open(player);
                            click(player);
                        } else {
                            fail(player);
                        }
                    }
                });
        }
        if (!selection.isEmpty() && menuNode.canCut(selection)) {
            gui.setItem(iter.next(), Items.text(new ItemStack(Material.SHEARS), List.of(text("CUT", YELLOW))), click -> {
                    if (click.isLeftClick()) {
                        List<Object> newClipboard;
                        try {
                            newClipboard = menuNode.cut(selection);
                        } catch (MenuException me) {
                            player.sendMessage(text("Cut failed: " + me.getMessage(), RED));
                            fail(player);
                            return;
                        }
                        if (newClipboard != null && !newClipboard.isEmpty()) {
                            this.clipboard.clear();
                            this.clipboard.addAll(newClipboard);
                            player.sendMessage(text("Cut " + clipboard.size() + " objects", GREEN));
                            open(player);
                            click(player);
                        } else {
                            fail(player);
                        }
                    }
                });
        }
        if (!clipboard.isEmpty() && menuNode.canPaste(clipboard, selection)) {
            gui.setItem(iter.next(), Items.text(Mytems.WHITE_PAINTBRUSH.createItemStack(), List.of(text("PASTE", WHITE))), click -> {
                    if (click.isLeftClick()) {
                        if (clipboard.isEmpty()) {
                            player.sendMessage(text("Clipboard is empty!", RED));
                            fail(player);
                            return;
                        }
                        try {
                            menuNode.paste(clipboard, selection);
                        } catch (MenuException me) {
                            player.sendMessage(text("Paste failed: " + me.getMessage()));
                            fail(player);
                            return;
                        }
                        player.sendMessage(text("Pasted " + selection.size() + " objects"));
                        open(player);
                        click(player);
                    }
                });
        }
        if (menuNode instanceof ListNode listNode) {
            if (iter.hasNext() && selection.size() <= 1) {
                final int listIndex = selection.isEmpty() ? listNode.getList().size() : selection.get(0);
                gui.setItem(iter.next(), Items.text(Mytems.PLUS_BUTTON.createItemStack(), List.of(text("Add item", GREEN))),
                            click -> {
                                if (click.isLeftClick()) {
                                    try {
                                        fetchNewValue(player, listNode.getValueType(), null,
                                                      newValue -> {
                                                          listNode.getList().add(listIndex, newValue);
                                                          open(player);
                                                          click(player);
                                                      },
                                                      () -> {
                                                          open(player);
                                                          fail(player);
                                                      });
                                        click(player);
                                    } catch (MenuException me) {
                                        player.sendMessage(text(me.getMessage(), RED));
                                        fail(player);
                                    }
                                }
                            });
            }
        }
        if (menuNode.getObject() instanceof EditMenuAdapter adapter) {
            for (EditMenuButton button : adapter.getEditMenuButtons()) {
                if (!iter.hasNext()) break;
                gui.setItem(iter.next(), Items.text(button.getMenuIcon(), button.getTooltip()), click -> {
                        try {
                            button.onClick(player, click.getClick());
                        } catch (MenuException me) {
                            player.sendMessage(text("Error: " + me.getMessage()));
                            return;
                        }
                        open(player);
                        click(player);
                    });
            }
        }
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (click.isLeftClick()) {
                    if (!path.isEmpty()) {
                        path.remove(path.size() - 1);
                        selection.clear();
                        open(player);
                        click(player);
                    } else {
                        fail(player);
                    }
                }
            });
        for (int i = 0; i < pageSize; i += 1) {
            int childIndex = pageSize * pageIndex + i;
            if (childIndex >= children.size()) break;
            int guiIndex = 9 + i;
            if (selection.contains(childIndex)) {
                titleBuilder.highlightSlot(guiIndex, GOLD);
            }
            final MenuItemNode node = children.get(childIndex);
            final VariableType variableType = node.getVariableType();
            final Object oldValue = node.getValue();
            List<Component> tooltip = new ArrayList<>();
            tooltip.addAll(node.getTooltip());
            if (variableType.nodeType.isMenu()) {
                tooltip.add(join(separator(space()),
                                 text(Unicode.tiny("left"), GREEN),
                                 text("Open this menu", GRAY)));
            }
            final boolean canSetValue;
            if (node.canSetValue()) {
                if (variableType.nodeType == NodeType.BOOLEAN) {
                    canSetValue = true;
                    tooltip.add(join(separator(space()),
                                     text(Unicode.tiny("right"), GREEN),
                                     text("Toggle true/false", GRAY)));
                } else if (variableType.nodeType.isPrimitive()) {
                    canSetValue = true;
                    tooltip.add(join(separator(space()),
                                     text(Unicode.tiny("right"), GREEN),
                                     text("Set this value", GRAY)));
                } else if (variableType.nodeType.isMenu() && oldValue == null) {
                    canSetValue = true;
                    tooltip.add(join(separator(space()),
                                     text(Unicode.tiny("right"), GREEN),
                                     text("Create this value", GRAY)));
                } else {
                    canSetValue = false;
                }
            } else {
                canSetValue = false;
            }
            if (node.isDeletable() && oldValue != null) {
                tooltip.add(join(separator(space()),
                                 text(Unicode.tiny("drop"), GREEN),
                                 text("Delete this value", GRAY)));
            }
            tooltip.add(join(separator(space()),
                             text(Unicode.tiny("shift-left"), GREEN),
                             text("(Un)select", GRAY)));
            ItemStack icon = Items.text(node.getIcon(), tooltip);
            gui.setItem(guiIndex, icon, click -> {
                    if (click.isLeftClick() && click.isShiftClick()) {
                        if (selection.contains(childIndex)) {
                            selection.removeAll(List.of(childIndex));
                        } else {
                            selection.add(childIndex);
                            Collections.sort(selection);
                        }
                        click(player);
                        open(player);
                    } else if (click.isLeftClick()) {
                        if (variableType.nodeType.isMenu()) {
                            path.add(new PathNode(node.getKey(), 0));
                            selection.clear();
                            open(player);
                            click(player);
                        } else {
                            player.sendMessage(join(separator(newline()), node.getTooltip()));
                            click(player);
                        }
                    } else if (canSetValue && click.isRightClick()) {
                        if (variableType.nodeType == NodeType.BOOLEAN) {
                            boolean newValue = oldValue == Boolean.TRUE ? false : true;
                            node.setValue(newValue);
                            player.sendMessage(text("Updated " + node.getKey() + " to " + newValue, YELLOW));
                            click(player);
                            open(player);
                        } else {
                            try {
                                fetchNewValue(player, variableType, oldValue,
                                              newValue -> {
                                                  if (newValue != null) {
                                                      node.setValue(newValue);
                                                      player.sendMessage(text("Updated " + node.getKey() + " to " + newValue, YELLOW));
                                                  }
                                                  open(player);
                                                  click(player);
                                              },
                                              () -> {
                                                  open(player);
                                                  fail(player);
                                              });
                                click(player);
                            } catch (MenuException me) {
                                player.sendMessage(text(me.getMessage(), RED));
                                fail(player);
                            }
                        }
                    } else if (click.getClick() == ClickType.DROP) {
                        if (!node.isDeletable()) {
                            fail(player);
                        } else {
                            node.delete();
                            selection.clear();
                            click(player);
                            open(player);
                        }
                    }
                });
        }
        chatCallback = null;
        gui.title(titleBuilder.build());
        gui.open(player);
        return gui;
    }

    public void fetchNewValue(Player player, VariableType variableType, Object oldValue, Consumer<Object> valueCallback, Runnable failCallback) {
        if (variableType.canParseValue()) {
            fetchNewValueFromChat(player, variableType, oldValue, valueCallback, failCallback);
        } else if (variableType.canCreateNewInstance()) {
            valueCallback.accept(variableType.createNewInstance());
        } else {
            throw new MenuException("Cannot create new value: " + variableType);
        }
    }

    /**
     * Fetch new value from chat.
     * VariabelType#canParseValue() must be true!
     */
    public void fetchNewValueFromChat(Player player, VariableType variableType, Object oldValue, Consumer<Object> valueCallback, Runnable failCallback) {
        chatCallback = str -> {
            final Object newValue;
            try {
                newValue = variableType.parseValue(str);
            } catch (IllegalArgumentException iae) {
                player.sendMessage(text("Invalid value: " + iae.getMessage(), RED));
                failCallback.run();
                return;
            }
            valueCallback.accept(newValue);
        };
        player.sendMessage(join(separator(space()), new Component[] {
                    text("Type the new value in chat", GREEN)
                    .insertion("" + oldValue)
                    .clickEvent(suggestCommand("" + oldValue))
                    .hoverEvent(showText(text("" + oldValue, GRAY))),
                    text("[CANCEL]", RED)
                    .clickEvent(runCommand("/editor reopen"))
                    .hoverEvent(showText(text("/editor reopen", RED))),
                }));
        player.closeInventory();
    }
}
