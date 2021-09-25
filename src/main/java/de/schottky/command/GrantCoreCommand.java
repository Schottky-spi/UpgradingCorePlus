package de.schottky.command;

import com.github.schottky.zener.command.Cmd;
import com.github.schottky.zener.command.CommandBase;
import com.github.schottky.zener.localization.Language;
import de.schottky.core.CoreItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Cmd(name = "getcore", permission = "uc.get", minArgs = 1, maxArgs = 2)
public class GrantCoreCommand extends CommandBase {

    @Override
    public boolean onPlayerCommand(
            @NotNull Player player,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args)
    {
        final Language lang = Language.current();
        Optional<CoreItem> coreItemOptional = CoreItem.forName(args[0]);
        if (coreItemOptional.isPresent()) {
            ItemStack stack = coreItemOptional.get().generateItemStack();
            if (args.length == 2) {
                try {
                    int amount = Integer.parseInt(args[1]);
                    stack.setAmount(amount);
                } catch (NumberFormatException ex) {
                    player.sendMessage(ChatColor.RED + lang.translate("message.invalid_number"));
                    return true;
                }
            }
            player.getInventory().addItem(stack);
        } else {
            player.sendMessage(ChatColor.RED + lang.translateWithExtra("message.wrong_item_name",
                    "name", args[0]));
        }
        return true;
    }

    @Override
    public String tooFewArgumentsMessage(int missing) {
        return Language.current().translate("message.missing_item_name");
    }

    @Override
    protected @Nullable List<String> tabCompleteOptionsFor(
            CommandSender sender,
            Command command,
            String label,
            String @NotNull [] args)
    {
        return args.length == 1 ?
                CoreItem.all().map(CoreItem::localizeName).collect(Collectors.toList()) :
                Collections.emptyList();
    }
}
