package de.schottky.core;

import com.github.schottky.zener.localization.Language;
import com.github.schottky.zener.messaging.Console;
import com.github.schottky.zener.util.item.ItemStorage;
import com.github.schottky.zener.util.item.Lore;
import com.github.schottky.zener.util.version.Release;
import com.github.schottky.zener.util.version.Version;
import de.schottky.Options;
import de.schottky.Shared;
import de.schottky.exception.InvalidConfiguration;
import de.schottky.util.ArrayUtil;
import de.schottky.util.ConfigUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static de.schottky.util.ArrayUtil.map;

/**
 * Represents an abstract core-item
 */
public abstract class CoreItem {

    // Holds a list of core-items that could be dropped by an entity
    private static Map<EntityType,List<CoreItem>> BY_ENTITY_TYPE = new EnumMap<>(EntityType.class);
    // maps the (unlocalized) names to core-items
    private static Map<String,CoreItem> BY_NAME = new HashMap<>();

    private static final String CORE_IDENT = "core_name";

    /**
     * returns a List of applicable items for a certain entity-type
     * @param type The entity-type to get all available items for
     * @return An empty Optional, if no mappings exist (in other words, this entity
     * cannot drop the item), or an optional containing all core-items that may drop from it
     */
    public static Optional<List<CoreItem>> applicableItemsFor(EntityType type) {
        return Optional.ofNullable(BY_ENTITY_TYPE.get(type));
    }

    /**
     * returns a random item that could drop from an entity
     * @param type The type of the entity
     * @return An empty optional, if the entity cannot drop a core-item, a random
     * chosen core-item, if this is not the case
     */
    public static Optional<CoreItem> randomItem(EntityType type) {
        return applicableItemsFor(type).flatMap(ArrayUtil::randomElement);
    }

    /**
     * returns true if, and only if the given {@code ItemStack} is a core-item or not
     * @param stack The stack to test
     * @return if the stack represents a core-item or not
     */
    public static boolean isCoreItem(ItemStack stack) {return fromItemStack(stack).isPresent(); }

    /**
     * returns a core-item with a certain unlocalized name
     * @param name The name to get this core-item for
     * @return The core-item, if it can be found, null otherwise
     */
    public static Optional<CoreItem> forName(String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }

    /**
     * returns all registered core-items
     * @return A stream of all core-items that are registered
     */
    @Contract(" -> new")
    public static @NotNull Stream<CoreItem> all() {
        return BY_NAME.values().stream();
    }

    /**
     * returns a given core-item that is represented by a certain {@code ItemStack}
     * If the {@code ItemStack} does not represent a core-item, returns an empty optional
     * @param stack The stack to get the core-item for
     * @return An optional containing the core-item, or an empty optional, if the stack does
     * not represent a core-item
     */
    public static Optional<CoreItem> fromItemStack(@Nullable ItemStack stack) {
        return Optional.ofNullable(stack)
                .map(ItemStack::getItemMeta)
                .flatMap(meta -> ItemStorage.getString(meta, CORE_IDENT))
                .map(name -> BY_NAME.get(name));
    }

    /**
     * registers a certain core-item, updating the values retrievable by {@link #BY_NAME} and {@link #BY_ENTITY_TYPE}
     * as well as registering the permissions
     * @param name The name of his core-item
     * @param section The configuration-section that belongs to this core-item
     * @throws InvalidConfiguration If the config does not contain a required value
     */
    public static void registerCoreItem(String name, ConfigurationSection section) throws InvalidConfiguration {
        CoreItem item = new UpgradingCoreItem(name, section);
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.addPermission(new Permission("uc.drop." + name.toLowerCase(), PermissionDefault.TRUE));
        pluginManager.addPermission(new Permission("uc.upgrade." + name.toLowerCase(), PermissionDefault.TRUE));

        for (EntityType type: item.entityTypes) {
            BY_ENTITY_TYPE.computeIfAbsent(type, t -> new ArrayList<>()).add(item);
        }
        BY_NAME.put(name, item);
    }

    /**
     * updates the visuals all core-items in a certain collection of ItemStack's
     * @param itemStacks The item-stacks that should be checked and possibly be updated
     */
    public static void updateAllIn(@NotNull Iterable<ItemStack> itemStacks) {
        for (ItemStack stack: itemStacks) {
            CoreItem.fromItemStack(stack).ifPresent(coreItem -> coreItem.setVisuals(stack));
        }
    }

    /**
     * unregisters all registered core-items as well as permissions for these items.
     * After a call of this method, calls to methods like {@link #forName(String)} will
     * always return an empty optional (unless new items have been registered
     */
    public static void unregisterCoreItems() {
        for (CoreItem item: BY_NAME.values()) {
            Bukkit.getPluginManager().removePermission("uc.drop." + item.name.toLowerCase());
            Bukkit.getPluginManager().removePermission("uc.upgrade." + item.name.toLowerCase());
        }
        BY_NAME = new HashMap<>();
        BY_ENTITY_TYPE = new EnumMap<>(EntityType.class);
    }

    protected final Material material;
    protected final double chance;
    protected final Set<EntityType> entityTypes;
    protected final Set<Tool> materials;
    protected final List<String> lore;
    protected final double failChance;

    @SuppressWarnings("deprecation")
    public CoreItem(final String name, final ConfigurationSection section) throws InvalidConfiguration {
        this.name = name;
        this.chance = ConfigUtil.getRequiredDouble(section, "chance");
        this.material = ConfigUtil.getRequiredMaterial(section, "material");
        this.entityTypes = ConfigUtil.getRequiredEnumSet(section, "entities", EntityType.class);
        this.materials = ConfigUtil.getRequiredEnumSet(section, "applicableOn", Tool.class);
        String colorString = ConfigUtil.getRequiredString(section, "color");
        if (Version.fromString(Bukkit.getBukkitVersion()).isOlderThan(Release.V_1_16_1)) {
            this.color = ChatColor.valueOf(colorString);
        } else {
            this.color = ChatColor.of(colorString);
        }
        this.failChance = ConfigUtil.getRequiredDouble(section, "failChance");
        this.lore = section.getStringList("lore");
    }

    protected final String name;

    public String name() {
        return name;
    }

    protected final ChatColor color;

    public ChatColor color() {
        return color;
    }

    public String permissionDropFromEntities() {
        return "uc.drop." + name.toLowerCase();
    }

    /**
     * returns whether or not an Item should drop if an entity has been killed by a certain
     * permissible
     * @param permissible the permissible to check this for
     * @return If an item should drop, or not
     */
    public boolean shouldDropOnEntityDeath(final @NotNull Permissible permissible) {
        if (!permissible.hasPermission(permissionDropFromEntities())) return false;
        final double value = Shared.random.nextDouble() * 100;
        return value <= chance;
    }

    /**@unused for future upgrades */
    @SuppressWarnings("unused")
    public abstract @NotNull Type type();

    /**
     * returns true, if this core-item can upgrade a certain material
     * @param material The material that should be checked
     * @return true, if an item represented by this material could be upgraded using
     * this core-item
     */
    boolean canUpgrade(final Material material) {
        return UpgradableItem
                .getTool(material)
                .map(this.materials::contains)
                .orElse(false);
    }

    /**
     * returns if a forging process should fail
     * @return true, if the forging-process should fail, false otherwise
     */
    public boolean forgingShouldFail() {
        return Shared.random.nextDouble() * 100 <= failChance;
    }

    public abstract ForgingResult forge(final ItemStack stack, final @NotNull Permissible permissible);

    /**
     * generate the ItemStack that represents this core-item
     * @return The ItemStack with localized title and lore
     */
    public ItemStack generateItemStack() {
        final ItemStack stack = new ItemStack(material);
        setVisuals(stack);
        return stack;
    }

    /**
     * sets the visuals of this core to a certain ItemStack
     * @param stack The builder to set the visuals for
     */
    public void setVisuals(final @NotNull ItemStack stack) {
        final ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        ItemStorage.set(meta, name, CORE_IDENT);
        meta.setDisplayName(ChatColor.RESET + color.toString() +
                Language.current().translateWithExtra("uc.title",
                        "core", ChatColor.BOLD + localizeName()));
        meta.setLore(localizeLore());
        stack.setItemMeta(meta);
    }

    // localizes the lore of this item
    private @NotNull Lore localizeLore() {
        Lore newLore = new Lore();
        for (String current : lore) {
            if (Options.localizeLore) {
                if (!Language.isValidIdentifier(current)) {
                    Console.warning("Unable to setup lore, '" + current + "' is not a valid identifier");
                    Console.warning("If you want to add the contents without localization, add the line");
                    Console.warning("'localizeLore: false' to your config");
                } else {
                    newLore.add(Language.current().translateWithExtra(current,
                            "materials", ChatColor.BOLD + applicableMaterialNames()));
                }
            } else
                newLore.add(ChatColor.translateAlternateColorCodes('&',
                        current.replace("{materials}", applicableMaterialNames())));
        }
        return newLore;
    }

    private @NotNull String applicableMaterialNames() {
        return String.join(", ", map(materials, Tool::localize));
    }

    public String localizeName() {
        return Language.current().translate("uc." + name.toLowerCase());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoreItem item = (CoreItem) o;
        return name.equals(item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public enum Type {
        UPGRADING
    }
}
