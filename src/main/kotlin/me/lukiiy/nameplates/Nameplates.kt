package me.lukiiy.nameplates

import org.bukkit.plugin.java.JavaPlugin

class Nameplates : JavaPlugin() {
    lateinit var manager: NametagManager

    var viewDist: Int = 0
    var updateTicks: Long = 20
    var verticalOffset: Double = 0.0

    override fun onEnable() {
        saveDefaultConfig()
        getConfig().options().copyDefaults(true)
        saveConfig()
        reloadComms()

        manager = NametagManager()
    }

    companion object {
        val instance: Nameplates
            get() = getPlugin(Nameplates::class.java)
    }

    internal fun reloadComms() {
        viewDist = config.getInt("viewDist", 32)
        updateTicks = config.getLong("updateTicks", 20)
        verticalOffset = config.getDouble("verticalOffset", .35)
    }
}
