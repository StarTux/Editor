package com.cavetale.editor.session;

import com.cavetale.core.editor.EditMenuAdapter;
import com.cavetale.core.editor.EditMenuButton;
import com.cavetale.core.editor.EditMenuContext;
import com.cavetale.core.editor.EditMenuDelegate;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.Unicode;
import com.cavetale.editor.gui.Gui;
import com.cavetale.editor.menu.MenuException;
import com.cavetale.editor.menu.MenuItemNode;
import com.cavetale.editor.menu.MenuNode;
import com.cavetale.editor.menu.NodeType;
import com.cavetale.editor.menu.VariableType;
import com.cavetale.editor.reflect.FieldNode;
import com.cavetale.editor.reflect.ListNode;
import com.cavetale.editor.reflect.MapNode;
import com.cavetale.editor.reflect.ObjectNode;
import com.cavetale.editor.reflect.SetNode;
import com.cavetale.editor.util.Icon;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import com.cavetale.mytems.util.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Getter;
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
public final class Session implements EditMenuContext {
    private final Sessions sessions;
    private final UUID uuid;
    /**
     * @Overrides EditMenuContext
     */
    @Getter private Plugin owningPlugin;
    /**
     * @Overrides EditMenuContext
     */
    @Getter private Object rootObject;
    private EditMenuDelegate delegate;
    private final List<PathNode> path = new ArrayList<>();
    private final PathNode rootPath = new PathNode("", 0);
    protected Consumer<String> chatCallback = null;
    private final List<Integer> selection = new ArrayList<>();
    private final List<Object> clipboard = new ArrayList<>();

    public Session setup(final Plugin thePlugin, final Object theRootObject, final EditMenuDelegate theDelegate) {
        reset();
        this.owningPlugin = thePlugin;
        this.rootObject = theRootObject;
        this.delegate = theDelegate;
        return this;
    }

    public void reset() {
        owningPlugin = null;
        rootObject = null;
        delegate = null;
        chatCallback = null;
        path.clear();
        rootPath.page = 0;
        selection.clear();
        clipboard.clear();
        Player p = getPlayer();
        if (p != null) {
            Gui gui = Gui.of(p);
            if (gui != null) p.closeInventory();
        }
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public List<String> getCurrentPath() {
        List<String> result = new ArrayList<>(path.size());
        for (PathNode it : path) {
            result.add(it.name);
        }
        return result;
    }

    /**
     * Find the MenuNode based on the current path.  This will rebuild
     * the path, stripping off path components which do not go
     * anywhere.
     */
    private MenuNode findPath() {
        MenuNode menuNode = new ObjectNode(this, (MenuNode) null, rootObject);
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

    private static final record Click(Supplier<ItemStack> icon, Consumer<ClickType> click) { }

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
        Component title = join(separator(space()), new Component[] {
                text((pageCount > 1
                      ? "" + (pageIndex + 1) + "/" + pageCount
                      : ""), GRAY),
                text(getPathString(), WHITE),
            });
        Gui gui = new Gui(owningPlugin).size(inventorySize);
        GuiOverlay.Builder titleBuilder = GuiOverlay.BLANK.builder(inventorySize, DARK_GRAY)
            .title(title)
            .layer(GuiOverlay.TOP_BAR, BLACK);
        List<Integer> topSlots = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8));
        if (pageIndex > 0) {
            topSlots.removeAll(List.of(0));
            gui.setItem(0, Mytems.ARROW_LEFT.createItemStack(), click -> {
                    pathNode.page -= 1;
                    open(player);
                    click(player);
                });
        }
        if (pageIndex < pageCount - 1) {
            topSlots.removeAll(List.of(8));
            gui.setItem(8, Mytems.ARROW_RIGHT.createItemStack(), click -> {
                    pathNode.page += 1;
                    open(player);
                    click(player);
                });
        }
        List<Click> menuClicks = new ArrayList<>();
        Runnable saveFunction = delegate.getSaveFunction(menuNode);
        if (saveFunction != null) {
            menuClicks.add(new Click(() -> {
                        ItemStack diskItem = Mytems.FLOPPY_DISK.createItemStack();
                        diskItem.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
                        return Items.text(diskItem, List.of(text("SAVE", GREEN)));
            }, click -> {
                        if (click.isLeftClick()) {
                            try {
                                saveFunction.run();
                                player.sendMessage(text("Saved to disk!", GREEN));
                                click(player);
                            } catch (MenuException me) {
                                player.sendMessage(text("Error saving: " + me.getMessage(), RED));
                                fail(player);
                            }
                        }
            }));
        }
        if (menuNode instanceof ListNode listNode) {
            if (selection.size() <= 1) {
                final int listIndex = selection.isEmpty() ? listNode.getList().size() : selection.get(0);
                menuClicks.add(new Click(() -> {
                            return Items.text(Mytems.PLUS_BUTTON.createIcon(), List.of(text("ADD ITEM", GREEN)));
                }, click -> {
                            if (click.isLeftClick()) {
                                try {
                                    fetchNewValue(player, listNode, listNode.getValueType(), null, newValue -> {
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
                }));
            }
            if (isSelectionConsecutive()) {
                menuClicks.addAll(List.of(new Click[] {
                            new Click(() -> {
                                    return Items.text(Mytems.ARROW_DOWN.createIcon(), List.of(text("MOVE DOWN", GREEN)));
                            }, click -> {
                                    if (click.isLeftClick()) {
                                        List<Object> values = new ArrayList<>(selection.size());
                                        int index = selection.get(0);
                                        for (int i = 0; i < selection.size(); i += 1) {
                                            values.add(listNode.getList().remove(index));
                                            selection.set(i, selection.get(i) - 1);
                                        }
                                        listNode.getList().addAll(index - 1, values);
                                        open(player);
                                        click(player);
                                    }
                            }),
                            new Click(() -> {
                                    return Items.text(Mytems.ARROW_UP.createIcon(), List.of(text("MOVE UP", GREEN)));
                            }, click -> {
                                    if (click.isLeftClick()) {
                                        List<Object> values = new ArrayList<>(selection.size());
                                        int index = selection.get(0);
                                        for (int i = 0; i < selection.size(); i += 1) {
                                            values.add(listNode.getList().remove(index));
                                            selection.set(i, selection.get(i) + 1);
                                        }
                                        listNode.getList().addAll(index + 1, values);
                                        open(player);
                                        click(player);
                                    }
                            }),
                        }));
            }
        }
        if (menuNode instanceof MapNode mapNode) {
            menuClicks.add(new Click(() -> {
                        return Items.text(Mytems.PLUS_BUTTON.createIcon(), List.of(text("ADD ITEM", GREEN)));
            }, click -> {
                        if (click.isLeftClick()) {
                            try {
                                fetchNewValue(player, mapNode, mapNode.getKeyType(), null, newKey -> {
                                        try {
                                            fetchNewValue(player, mapNode, mapNode.getValueType(), null, newValue -> {
                                                    mapNode.getMap().put(newKey, newValue);
                                                    player.sendMessage(text("Added key value pair: " + newKey + ", " + newValue, GREEN));
                                                    open(player);
                                                    click(player);
                                                },
                                                () -> {
                                                    open(player);
                                                    fail(player);
                                                });
                                        } catch (MenuException me) {
                                            player.sendMessage(text(me.getMessage(), RED));
                                            fail(player);
                                            open(player);
                                        }
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
            }));
        }
        if (menuNode instanceof SetNode setNode) {
            menuClicks.add(new Click(() -> {
                        return Items.text(Mytems.PLUS_BUTTON.createIcon(), List.of(text("ADD ITEM", GREEN)));
            }, click -> {
                        if (click.isLeftClick()) {
                            try {
                                fetchNewValue(player, setNode, setNode.getValueType(), null, newValue -> {
                                        setNode.getSet().add(newValue);
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
            }));
        }
        if (!selection.isEmpty()) {
            menuClicks.add(new Click(() -> {
                        return Items.text(Mytems.MAGNET.createIcon(), List.of(text("COPY", GREEN)));
            }, click -> {
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
            }));
        }
        if (!selection.isEmpty() && menuNode.canCut(selection)) {
            menuClicks.add(new Click(() -> {
                        return Items.text(new ItemStack(Material.SHEARS), List.of(text("CUT", YELLOW)));
            }, click -> {
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
            }));
        }
        if (!clipboard.isEmpty() && menuNode.canPaste(clipboard, selection)) {
            menuClicks.add(new Click(() -> {
                        return Items.text(Mytems.WHITE_PAINTBRUSH.createItemStack(), List.of(text("PASTE", WHITE)));
            }, click -> {
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
                            player.sendMessage(text("Pasted " + clipboard.size() + " objects"));
                            open(player);
                            click(player);
                        }
            }));
        }
        if (menuNode.getObject() instanceof EditMenuAdapter adapter) {
            for (EditMenuButton button : adapter.getEditMenuButtons(menuNode)) {
                menuClicks.add(new Click(() -> {
                            return Items.text(button.getMenuIcon(), button.getTooltip());
                }, click -> {
                            try {
                                button.onClick(player, click);
                            } catch (MenuException me) {
                                player.sendMessage(text("Error: " + me.getMessage()));
                                return;
                            }
                            open(player);
                            click(player);
                }));
            }
        }
        int offset = Math.max(0, (topSlots.size() - menuClicks.size()) / 2);
        for (int i = 0; i < topSlots.size(); i += 1) {
            if (i >= menuClicks.size()) break;
            int slot = topSlots.get(i + offset);
            Click menuClick = menuClicks.get(i);
            gui.setItem(slot, menuClick.icon().get(), click -> menuClick.click().accept(click.getClick()));
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
            tooltip.addAll(Icon.tooltip(menuNode, node));
            if (node instanceof FieldNode fieldNode) {
                if (!fieldNode.getDescription().isEmpty()) {
                    tooltip.addAll(Text.wrapLore(fieldNode.getDescription()));
                }
            }
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
            ItemStack icon = Items.text(Icon.of(menuNode, oldValue), tooltip);
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
                            player.sendMessage(join(separator(newline()), Icon.tooltip(menuNode, node)));
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
                                fetchNewValue(player, menuNode, variableType, oldValue,
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

    private boolean isSelectionConsecutive() {
        if (selection.isEmpty()) return false;
        if (selection.size() == 1) return true;
        for (int i = 0; i < selection.size() - 1; i += 1) {
            if (selection.get(i) + 1 != selection.get(i + 1)) return false;
        }
        return true;
    }

    public void fetchNewValue(Player player,
                              MenuNode menuNode,
                              VariableType variableType,
                              Object oldValue,
                              Consumer<Object> valueCallback,
                              Runnable failCallback) {
        if (!variableType.nodeType.isContainer()) {
            Object newValue = variableType.createNewValue();
            if (newValue != null) {
                valueCallback.accept(newValue);
                return;
            }
            List<Object> possibleValues = variableType.getPossibleValues();
            if (possibleValues != null) {
                fetchPossibleValueFromMenu(player, menuNode, variableType, possibleValues, oldValue, valueCallback, failCallback, 0);
                return;
            }
        }
        if (variableType.nodeType == NodeType.ENUM) {
            List<Object> enumList = List.of(variableType.objectType.getEnumConstants());
            fetchPossibleValueFromMenu(player, menuNode, variableType, enumList, oldValue, valueCallback, failCallback, 0);
        } else if (variableType.canParseValue()) {
            fetchNewValueFromChat(player, menuNode, variableType, oldValue, valueCallback, failCallback);
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
    public void fetchNewValueFromChat(Player player,
                                      MenuNode menuNode,
                                      VariableType variableType,
                                      Object oldValue,
                                      Consumer<Object> valueCallback,
                                      Runnable failCallback) {
        chatCallback = str -> {
            final Object newValue;
            try {
                newValue = variableType.parseValue(str);
            } catch (IllegalArgumentException iae) {
                player.sendMessage(text("Invalid value: " + iae.getMessage(), RED));
                failCallback.run();
                return;
            }
            if (!variableType.canHold(newValue)) {
                player.sendMessage(text("Illegal value: " + newValue));
                failCallback.run();
            } else {
                valueCallback.accept(newValue);
            }
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

    public void fetchPossibleValueFromMenu(Player player,
                                           MenuNode menuNode,
                                           VariableType variableType,
                                           List<Object> possibleValues,
                                           Object oldValue,
                                           Consumer<Object> valueCallback,
                                           Runnable failCallback,
                                           int page) {
        final int rows = 5;
        final int inventorySize = rows * 9 + 9;
        final int pageSize = rows * 9;
        final int pageCount = (possibleValues.size() - 1) / pageSize + 1;
        final int pageIndex = Math.min(pageCount - 1, page);
        Component title = join(noSeparators(), new Component[] {
                text(pageCount > 1 ? "" + (pageIndex + 1) + "/" + pageCount + " " : "", GRAY),
                text("Value Picker ", GRAY),
                text(variableType.getClassName(), WHITE),
            });
        Gui gui = new Gui(owningPlugin).size(inventorySize);
        GuiOverlay.Builder titleBuilder = GuiOverlay.BLANK.builder(inventorySize, DARK_GRAY)
            .title(title)
            .layer(GuiOverlay.TOP_BAR, BLACK);
        if (pageIndex > 0) {
            gui.setItem(0, Mytems.ARROW_LEFT.createItemStack(), click -> {
                    fetchPossibleValueFromMenu(player, menuNode, variableType, possibleValues, oldValue, valueCallback, failCallback, page - 1);
                    click(player);
                });
        }
        if (pageIndex < pageCount - 1) {
            gui.setItem(8, Mytems.ARROW_RIGHT.createItemStack(), click -> {
                    fetchPossibleValueFromMenu(player, menuNode, variableType, possibleValues, oldValue, valueCallback, failCallback, page + 1);
                    click(player);
                });
        }
        for (int i = 0; i < pageSize; i += 1) {
            int enumIndex = pageSize * pageIndex + i;
            if (enumIndex >= possibleValues.size()) break;
            final Object it = possibleValues.get(enumIndex);
            int guiIndex = 9 + i;
            if (oldValue == it) {
                titleBuilder.highlightSlot(guiIndex, DARK_BLUE);
            }
            ItemStack icon = Icon.of(menuNode, it);
            List<Component> tooltip;
            tooltip = List.of(text(it.toString(), WHITE),
                              text(variableType.getClassName(), DARK_GRAY, ITALIC));
            gui.setItem(guiIndex, Items.text(icon, tooltip), click -> {
                    if (click.isLeftClick()) {
                        valueCallback.accept(it);
                        click(player);
                    }
                });
        }
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (click.isLeftClick()) {
                    failCallback.run();
                }
            });
        gui.title(titleBuilder.build());
        gui.open(player);
    }
}
