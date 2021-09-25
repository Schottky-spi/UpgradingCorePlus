package de.schottky.core;

import de.schottky.Options;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import java.util.Objects;

public enum ForgingResult {
    PERMISSION_DENIED {
        @Override
        public boolean shouldFail(ItemStack stack, Permissible permissible, UpgradingCoreItem item) {
            return !permissible.hasPermission(item.upgradePermission());
        }
    },
    NOT_APPLICABLE {
        @Override
        public boolean shouldFail(ItemStack stack, Permissible permissible, UpgradingCoreItem item) {
            return stack == null || stack.getItemMeta() == null;
        }
    },
    WRONG_COMBINATION {
        @Override
        public boolean shouldFail(ItemStack stack, Permissible permissible, UpgradingCoreItem item) {
            return !item.canUpgrade(stack.getType());
        }
    },
    MAX_LEVEL {
        @Override
        public boolean shouldFail(ItemStack stack, Permissible permissible, UpgradingCoreItem item) {
            return UpgradingCoreItem.itemLevelFor(Objects.requireNonNull(stack.getItemMeta())) >= Options.maxLevel;
        }
    },
    FAILED {
        @Override
        public boolean shouldFail(ItemStack stack, Permissible permissible, UpgradingCoreItem item) {
            return item.forgingShouldFail();
        }
    },
    SUCCESS {
        @Override
        public boolean shouldFail(ItemStack stack, Permissible permissible, UpgradingCoreItem item) {
            return false;
        }
    };

    public static final ForgingResult[] ALL_VALUES = values();

    public abstract boolean shouldFail(ItemStack stack, Permissible permissible, UpgradingCoreItem item);
}
