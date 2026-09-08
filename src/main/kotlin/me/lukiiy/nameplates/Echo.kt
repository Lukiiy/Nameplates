package me.lukiiy.nameplates

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.*

class Echo : Listener {
    private val plugin: Nameplates
        get() = Nameplates.instance

    @EventHandler
    fun join(e: PlayerJoinEvent) {
        plugin.manager.register(e.getPlayer())

        plugin.server.globalRegionScheduler.run(plugin) { plugin.manager.refreshAll() }
    }

    @EventHandler
    fun quit(e: PlayerQuitEvent) {
        plugin.manager.unregister(e.getPlayer())
    }

    @EventHandler
    fun tp(e: PlayerTeleportEvent) {
        plugin.server.globalRegionScheduler.run(plugin) { plugin.manager.refresh(e.getPlayer()) }
    }

    @EventHandler
    fun worldChange(e: PlayerChangedWorldEvent) {
        plugin.manager.refresh(e.getPlayer())
    }
}