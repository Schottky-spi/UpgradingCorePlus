package de.schottky.command;

import com.github.schottky.zener.command.Cmd;
import com.github.schottky.zener.command.CommandBase;
import com.github.schottky.zener.localization.Language;
import de.schottky.core.CoreItem;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Cmd(name = "listcores", permission = "uc.list", maxArgs = 0)
public class CommandListCores extends CommandBase {

    @Override
    public boolean onAcceptedCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args)
    {
        sender.sendMessage(ChatColor.YELLOW + Language.current().translate("message.list_core"));
        CoreItem.all()
                .map(this::map)
                .forEach(baseComponents ->  sender.spigot().sendMessage(baseComponents));
        return true;
    }

    private BaseComponent[] map(@NotNull CoreItem item) {
        String name = item.name();
        return new ComponentBuilder(name)
                .color(item.color())
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/getcore " + name))
                .create();
    }
}
