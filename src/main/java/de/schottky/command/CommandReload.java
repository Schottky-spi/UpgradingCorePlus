package de.schottky.command;

import com.github.schottky.zener.command.Cmd;
import com.github.schottky.zener.command.CommandBase;
import com.github.schottky.zener.localization.Language;
import de.schottky.UpgradingCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Cmd(name = "ucreload", permission = "uc.reload", maxArgs = 0)
public class CommandReload extends CommandBase {

    private final UpgradingCorePlugin plugin;

    public CommandReload(UpgradingCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onAcceptedCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args)
    {
        plugin.reloadConfig();

        sender.sendMessage(ChatColor.GREEN + Language.current().translate("message.reload_complete"));
        return true;
    }
}