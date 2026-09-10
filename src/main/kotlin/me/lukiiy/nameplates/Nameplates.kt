package me.lukiiy.nameplates

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Nameplates : JavaPlugin() {
    lateinit var manager: NametagManager

    var viewDist: Int = 0
    var updateTicks: Long = 20
    var verticalOffset: Double = 0.0
    var lineGap: Double = .7

    override fun onEnable() {
        saveDefaultConfig()
        getConfig().options().copyDefaults(true)
        saveConfig()
        reloadComms()

        manager = NametagManager()

        server.pluginManager.registerEvents(Echo(), this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { it.registrar().apply {
            register(Cmd.register(), "Main command for Nameplates.", listOf("nameplates"))
        } }
    }

    override fun onDisable() {
        server.onlinePlayers.forEach { manager.unregister(it) }
    }

    companion object {
        val instance: Nameplates
            get() = getPlugin(Nameplates::class.java)
    }

    internal fun reloadComms() {
        viewDist = config.getInt("viewDist", 32)
        updateTicks = config.getLong("updateTicks", 20)
        verticalOffset = config.getDouble("verticalOffset", .35)
        lineGap = config.getDouble( "lineGap", .7 )
    }
}
