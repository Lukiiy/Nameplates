package me.lukiiy.nameplates

import org.bukkit.plugin.java.JavaPlugin

class Nameplates : JavaPlugin() {
    lateinit var manager: NametagManager

    val viewDist: Int = 32

    override fun onEnable() {
        manager = NametagManager()
    }

    companion object {
        val instance: Nameplates
            get() = getPlugin(Nameplates::class.java)
    }
}
