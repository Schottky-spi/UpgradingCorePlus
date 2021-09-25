package de.schottky.core;

import com.github.schottky.zener.localization.Language;

public enum Tool {

    WOOD,
    STONE,
    LEATHER,
    CHAIN_MAIL,
    IRON,
    GOLD,
    DIAMOND,
    NETHERITE,
    TURTLE,
    TRIDENT,
    BOW,
    CROSSBOW;

    public String localize() {
        return Language.current().translate("tool." + this.name().toLowerCase());
    }

}
