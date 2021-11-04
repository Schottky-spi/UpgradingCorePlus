package de.schottky.core

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFactory
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.logging.Logger
import kotlin.test.assertEquals

class UpgradingCoreItemTests {

    companion object {

        @JvmStatic
        @BeforeAll
        fun setupAll() {
            val server = mock<Server>()
            whenever(server.bukkitVersion).thenReturn("1.17")
            whenever(server.logger).thenReturn(Logger.getGlobal())
            val itemFactory = mock<ItemFactory>()
            whenever(itemFactory.getItemMeta(Material.AIR)).thenReturn(null)
            whenever(itemFactory.getItemMeta(Material.DIAMOND_SWORD)).thenReturn(mock())
            whenever(server.itemFactory).thenReturn(itemFactory)
            Bukkit.setServer(server)
        }

    }

    @Test
    fun `Forging using a core item will fail if the player does not have the required permissions`() {
        val forger = mock<Player>()
        whenever(forger.hasPermission("")).thenReturn(false)
        val config = setupMockConfig()
        val item = UpgradingCoreItem("MockItem", config)
        val stack = ItemStack(Material.DIAMOND_SWORD)
        val result = item.forge(stack, forger)
        assertEquals(ForgingResult.PERMISSION_DENIED, result)
    }

    @Test
    fun `Forging should fail when a core is used on a wrong item`() {
        val config = setupMockConfig(applicableOn = setOf(Tool.LEATHER))
        val item = UpgradingCoreItem("MockItem", config)
        val forger = mock<Player>()
        whenever(forger.hasPermission(item.upgradePermission())).thenReturn(true)
        val stack = ItemStack(Material.DIAMOND_SWORD)
        assertEquals(ForgingResult.WRONG_COMBINATION, item.forge(stack, forger))
    }

    @Test
    fun `Forging should fail when the item or the item's meta is undefined`() {
        val config = setupMockConfig()
        val item = UpgradingCoreItem("MockItem", config)
        val forger = mock<Player>()
        whenever(forger.hasPermission(item.upgradePermission())).thenReturn(true)
        val emptyStack: ItemStack? = null
        assertEquals(item.forge(emptyStack, forger), ForgingResult.NOT_APPLICABLE)
        val stackWithoutMeta = ItemStack(Material.AIR)
        assertEquals(ForgingResult.NOT_APPLICABLE, item.forge(stackWithoutMeta, forger))
    }

    private fun setupMockConfig(
        armorModifier: Double = 0.0,
        damageModifier: Double = 0.0,
        chance: Double = 0.0,
        material: Material = Material.RED_DYE,
        entities: Set<EntityType> = emptySet(),
        applicableOn: Set<Tool> = emptySet(),
        color: ChatColor = ChatColor.BLACK,
        failChance: Double = 0.0
    ): ConfigurationSection {
        val config = MemoryConfiguration()
        config.set("armorModifier", armorModifier)
        config.set("damageModifier", damageModifier)
        config.set("chance", chance)
        config.set("material", material.name)
        config.set("entities", entities.map { it.name }.toList())
        config.set("applicableOn", applicableOn.map { it.name }.toList())
        config.set("color", color.name)
        config.set("failChance", failChance)
        return config
    }
}