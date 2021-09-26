package de.schottky.menu;

import com.github.schottky.zener.localization.Language;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import de.schottky.core.CoreItem;
import de.schottky.core.ForgingResult;
import de.schottky.core.UpgradableItem;
import de.schottky.util.MapUtil;
import de.schottky.util.Objects;
import de.schottky.util.Timers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RefactorMenu {

    private final Inventory backend;
    private final int sizeX, sizeY;
    private boolean forging;
    private final int[] forgeIndices;
    private final int coreSlot = 19;
    private final int[] coreCrossIndices;
    private final int itemSlot = 25;
    private final int[] itemCrossIndices;
    private final ItemStack[][] frontend;
    private boolean dirty = false;
    private final int anvilSlot = 49;
    private final Language lang = Language.current();

    public RefactorMenu() {
        this.sizeX = 9;
        this.sizeY = 6;
        this.backend = Bukkit.createInventory(null, sizeX * sizeY, lang.translate("menu.title"));
        this.forgeIndices = new int[] {21, 22, 23};
        this.itemCrossIndices = new int[] {16, 24, 26, 34};
        this.coreCrossIndices = new int[] {18, 10, 20, 28};
        this.frontend = new ItemStack[sizeX][sizeY];
        setCross(Material.ORANGE_STAINED_GLASS_PANE, itemCrossIndices);
        setCross(Material.ORANGE_STAINED_GLASS_PANE, coreCrossIndices);
        for (final int i: forgeIndices) {
            set(i, ItemBuilder.forType(Material.BLUE_STAINED_GLASS).setTitle(" "));
        }
        ItemBuilder anvil = ItemBuilder
                .forType(Material.ANVIL)
                .setTitle(ChatColor.RESET + ChatColor.BOLD.toString() + lang.translate("menu.anvil_title"))
                .addLine(ChatColor.RESET + lang.translate("menu.anvil_description"));
        set(anvilSlot, anvil);
    }

    /**
     * sets the cross-shaped Glass-panes to a certain material
     * @param material The material to set the cross to
     * @param cross THe indices of the cross
     */
    private void setCross(Material material, int[] cross) {
        for (int i: cross) {
            set(i, ItemBuilder.forType(material).setTitle(" "));
        }
    }

    public void set(int absolute, ItemStack stack) {
        int y = absolute / sizeX;
        int x = absolute - y * sizeX;
        this.set(x, y, stack);
    }

    public void set(int x, int y, ItemStack stack) {
        Preconditions.checkArgument(x >= 0 && x < sizeX, "index x out of bounds (" + x + ")");
        Preconditions.checkArgument(y >= 0 && y < sizeY, "index y out of bounds (" + y + ")");
        this.frontend[x][y] = stack;
        dirty = true;
    }

    public void updateLater() {
        Timers.runLater(1, this::updateNow);
    }

    public void updateNow() {
        if (!dirty) return;
        for (int x = 0; x < frontend.length; x++) {
            for (int y = 0; y < frontend[0].length; y++) {
                int absolute = getAbsolute(x, y);
                // update all cosmetic slots
                if (absolute == coreSlot || absolute == itemSlot) continue;
                backend.setItem(absolute, frontend[x][y]);
            }
        }
        dirty = false;
    }

    public void set(int absolute, @NotNull ItemBuilder builder) {
        this.set(absolute, builder.toStack());
    }

    public void open(@NotNull HumanEntity human) {
        updateLater();
        human.openInventory(this.backend);
    }

    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (forging) {
            event.setCancelled(true);
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        final int slot = event.getRawSlot();
        final InventoryView view = event.getView();
        if (slot == anvilSlot) handleAnvilClick(event.getView(), event.getWhoClicked());
        switch (event.getAction()) {
            // all of these do not need to be canceled, if the slot is correct
            // and should be canceled, if the slot is inside the inventory
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
            case DROP_ONE_CURSOR:
            case DROP_ALL_CURSOR:
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
            case SWAP_WITH_CURSOR:
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
            case CLONE_STACK:
                checkSlot(slot, event);
                break;
            case MOVE_TO_OTHER_INVENTORY:
                // from other inventory to this menu
                if (!event.getClickedInventory().equals(this.backend)) {
                    event.setCancelled(true);
                    int newSlot = CoreItem.isCoreItem(event.getCurrentItem()) ? coreSlot : itemSlot;
                    if (event.getCurrentItem() != null &&
                            (event.getCurrentItem().isSimilar(backend.getItem(newSlot)) ||
                                    backend.getItem(newSlot) == null)) {
                        moveItemLater(
                                view.getBottomInventory(), view.convertSlot(slot),
                                view.getTopInventory(), newSlot,
                                view);
                    }
                // from menu to player inventory
                } else {
                    checkSlot(slot, event);
                }
                break;
            case COLLECT_TO_CURSOR:
                if (event.getCursor() == null) return;
                this.collectToCursor(event.getCursor(), view);
                updateVisualsLater(slot, event.getView());
                event.setCancelled(true);
                break;
            default:
                event.setCancelled(true);
        }
    }

    private void checkSlot(int slot, InventoryInteractEvent event) {
        if (slot >= 54) return;
        if (slot == coreSlot || slot == itemSlot) {
            updateVisualsLater(slot, event.getView());
        } else {
            event.setCancelled(true);
        }
    }

    private void collectToCursor(@NotNull ItemStack toCollect, InventoryView view) {
        Objects.doTwiceTwiceFor(itemSlot, coreSlot, (i) -> {
            ItemStack stack = backend.getItem(i);
            collect(toCollect, stack);
            backend.setItem(i, stack);
        });
        this.updateVisualsLater(ImmutableSet.of(itemSlot, coreSlot), view);
        if (toCollect.getAmount() >= toCollect.getMaxStackSize()) return;

        Inventory inv = view.getBottomInventory();
        final Map<Integer, ? extends ItemStack> map = MapUtil.sortedValues(inv.all(toCollect.getType()),
                Comparator.comparingInt(ItemStack::getAmount));

        for (Map.Entry<Integer,? extends ItemStack> entry: map.entrySet()) {
            final ItemStack stack = entry.getValue();
            collect(toCollect, stack);
            inv.setItem(entry.getKey(), stack);
            if (toCollect.getAmount() >= toCollect.getMaxStackSize()) return;
        }
    }

    private void collect(@NotNull ItemStack collectTo, ItemStack collectFrom) {
        if (!collectTo.isSimilar(collectFrom)) return;
        int amountFrom = collectFrom.getAmount();
        int amountTo = collectTo.getAmount();
        int spareAmount = collectTo.getMaxStackSize() - amountTo;
        amountTo = Math.min(collectTo.getMaxStackSize(), amountTo + amountFrom);
        amountFrom = Math.max(0, amountFrom - spareAmount);
        collectFrom.setAmount(amountFrom);
        collectTo.setAmount(amountTo);
    }

    private void updateVisualsLater(int slot, InventoryView view) {
        Timers.runLater(1, () -> this.updateVisuals(slot, view));
    }

    private void updateVisualsLater(Set<Integer> slots, InventoryView view) {
        Timers.runLater(1, () -> {
            for (Integer slot: slots) updateVisuals(slot, view);
        });
    }

    private void updateVisuals(int slot, @NotNull InventoryView view) {
        final ItemStack stack = view.getItem(slot);
        Material material;
        if (slot == itemSlot) {
            if (stack == null) {
                material = Material.ORANGE_STAINED_GLASS_PANE;
            } else if (UpgradableItem.isUpgradable(stack.getType())) {
                material = Material.LIME_STAINED_GLASS_PANE;
            } else {
                material = Material.RED_STAINED_GLASS_PANE;
            }
            setCross(material, itemCrossIndices);
        } else if (slot == coreSlot) {
            if (stack == null) {
                material = Material.ORANGE_STAINED_GLASS_PANE;
            } else if (CoreItem.isCoreItem(stack)) {
                material = Material.LIME_STAINED_GLASS_PANE;
            } else {
                material = Material.RED_STAINED_GLASS_PANE;
            }
            setCross(material, coreCrossIndices);
        }
        updateNow();
    }

    private void moveItemLater(Inventory invFrom, int from, Inventory invTo, int to, InventoryView view) {
        Timers.runLater(1, () -> {
            final ItemStack stackFrom = invFrom.getItem(from);
            final ItemStack stackTo = invTo.getItem(to);
            if (stackFrom == null) return;
            if (stackFrom.isSimilar(stackTo)) {
                assert stackTo != null;
                collect(stackTo, stackFrom);
                invFrom.setItem(from, stackFrom);
                invTo.setItem(to, stackTo);
            } else {
                invFrom.setItem(from, stackTo);
                invTo.setItem(to, stackFrom);
            }
            updateVisuals(to, view);
        });
    }

    private void handleAnvilClick(@NotNull InventoryView inventory, HumanEntity clicker) {
        CoreItem.fromItemStack(inventory.getItem(coreSlot)).ifPresent(coreItem -> {
            ForgingResult result = coreItem.forge(inventory.getItem(itemSlot), clicker);
            Sound sound = null;
            switch (result) {
                case FAILED -> {
                    shrinkCoreSlot(inventory);
                    clicker.sendMessage(ChatColor.RED + lang.translate("message.forging_failed"));
                    sound = Sound.BLOCK_ANVIL_BREAK;
                }
                case SUCCESS -> {
                    shrinkCoreSlot(inventory);
                    sound = Sound.BLOCK_ANVIL_USE;
                    clicker.sendMessage(ChatColor.GREEN + lang.translate("message.forging_success"));
                }
                case PERMISSION_DENIED -> clicker.sendMessage(ChatColor.RED + lang.translate("message.permission_denied"));
                case MAX_LEVEL -> clicker.sendMessage(ChatColor.RED + lang.translate("message.max_level"));
                case WRONG_COMBINATION -> clicker.sendMessage(ChatColor.RED + lang.translateWithExtra("message.wrong_combination",
                        "core", coreItem.localizeName()));
                case NOT_APPLICABLE -> {
                    clicker.sendMessage(ChatColor.RED + lang.translate("message.forging_not_applicable"));
                    sound = Sound.ENTITY_VILLAGER_NO;
                }
            }
            if (sound != null && clicker instanceof Player player) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        });
    }

    private void shrinkCoreSlot(@NotNull InventoryView inventory) {
        ItemStack stack = inventory.getItem(coreSlot);
        if (stack != null) {
            stack.setAmount(stack.getAmount() - 1);
            Timers.runLater(1, () -> {
                inventory.setItem(coreSlot, stack);
                updateVisuals(coreSlot, inventory);
            });
        }
    }

    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        final Set<Integer> slots = event.getRawSlots();
        for (Integer i: slots) {
            if (i != coreSlot && i != itemSlot && i < 54) {
                event.setCancelled(true);
                return;
            }
        }
        this.updateVisualsLater(slots, event.getView());
    }

    public void onInventoryClose(@NotNull Inventory inventory, HumanEntity player) {
        Objects.doTwiceTwiceFor(inventory.getItem(itemSlot), inventory.getItem(coreSlot), item -> {
            if (item != null) player.getWorld().dropItem(player.getLocation(), item);
        });
    }

    public void close(UUID uuid) {
        final HumanEntity player = Bukkit.getPlayer(uuid);
        if (player != null) {
            onInventoryClose(this.backend, player);
            player.closeInventory();
        }
    }

    private int getAbsolute(int x, int y) { return y * sizeX + x; }

}
