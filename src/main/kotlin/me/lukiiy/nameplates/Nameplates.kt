package me.lukiiy.nameplates

import org.bukkit.plugin.java.JavaPlugin

class Nameplates : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
    }

    companion object {
        val instance: Nameplates
            get() = getPlugin(Nameplates::class.java)
    }
}
