package me.lukiiy.nameplates

import net.minecraft.server.level.ServerPlayer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

object Utils {
    fun Player.asNMS(): ServerPlayer = (this as CraftPlayer).handle
}