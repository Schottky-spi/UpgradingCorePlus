package de.schottky;

import com.github.schottky.zener.api.Zener;
import com.github.schottky.zener.command.Commands;
import com.github.schottky.zener.localization.Language;
import com.github.schottky.zener.localization.LanguageFile;
import com.github.schottky.zener.messaging.Console;
import com.github.schottky.zener.config.Config;
import de.schottky.command.CommandListCores;
import de.schottky.command.CommandReload;
import de.schottky.command.GrantCoreCommand;
import de.schottky.core.CoreItem;
import de.schottky.core.UpgradableItem;
import de.schottky.core.UpgradingCoreItem;
import de.schottky.event.ArrowListener;
import de.schottky.event.InventoryClickListener;
import de.schottky.event.MobDeathListener;
import de.schottky.event.VisualChangeListener;
import de.schottky.exception.InvalidConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class UpgradingCorePlugin extends JavaPlugin {

    private InventoryClickListener inventoryClickListener;

    @Override
    public void onLoad() {
        UpgradableItem.loadItems();
    }

    @Override
    public void onEnable() {
        Zener.start(this);
        this.saveDefaultConfig();
        this.reloadConfig();
        final PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new ArrowListener(), this);
        manager.registerEvents(new MobDeathListener(), this);
        manager.registerEvents(this.inventoryClickListener = new InventoryClickListener(), this);
        manager.registerEvents(new VisualChangeListener(), this);
        Commands.registerAll(
                new GrantCoreCommand(),
                new CommandReload(this),
                new CommandListCores());
    }

    @Override
    public void onDisable() {
        this.inventoryClickListener.getActiveMenus().forEach((uuid, refactorMenu) -> refactorMenu.close(uuid));
        CoreItem.unregisterCoreItems();
        Zener.end();
    }

    @Override
    public void reloadConfig() {
        CoreItem.unregisterCoreItems();
        super.reloadConfig();
        final FileConfiguration config = this.getConfig();
        Config.loadDefaultConfig(Options.class);

        Language.setCurrent(Language.forPlugin(
                this,
                Options.locale,
                LanguageFile.StorageProvider.JSON,
                Language.Option.RESOURCE_LOC.set("lang"),
                Language.Option.FALLBACK.set("lang/en-us.lang")));

        for (String value: config.getValues(false).keySet()) {
            if (!config.isConfigurationSection(value)) continue;
            ConfigurationSection section = config.getConfigurationSection(value);
            try {
                CoreItem.registerCoreItem(value, section);
            } catch (InvalidConfiguration e) {
                Console.error(e);
            }
        }
        for (Player player: Bukkit.getOnlinePlayers()) {
            UpgradingCoreItem.updateItemsInInventory(player.getInventory());
            CoreItem.updateAllIn(player.getInventory());
        }
    }
}
