package me.lukiiy.nameplates

import me.lukiiy.nameplates.Utils.asNMS
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.protocol.Packet
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

class NametagManager {
    private val mini: MiniMessage = MiniMessage.miniMessage()

    private val entities: MutableMap<Player, PlateEntity> = ConcurrentHashMap()
    private val tracking: MutableMap<Player, MutableSet<Player>> = ConcurrentHashMap()
    private val lines: MutableMap<Player, MutableList<Component>> = ConcurrentHashMap()
    private val overrides: MutableMap<Player, String> = ConcurrentHashMap()
    private val hidden: MutableSet<Player> = CopyOnWriteArraySet()

    fun register(player: Player) {
        entities.computeIfAbsent(player) { PlateEntity(it) }
        tracking.putIfAbsent(player, ConcurrentHashMap.newKeySet())

        refresh(player)
    }

    fun unregister(player: Player?) {
        val entity: PlateEntity? = entities.remove(player!!)
        val viewers = tracking.remove(player)

        if (entity != null && viewers != null) for (viewer in viewers) send(viewer, entity.removePacket())

        overrides.remove(player)
        hidden.remove(player)
    }

    fun setOverride(player: Player, raw: String?) {
        if (raw == null) overrides.remove(player) else overrides[player] = raw

        refresh(player)
    }

    fun hasOverride(player: Player?): Boolean = overrides.containsKey(player)

    fun setHidden(player: Player, isHidden: Boolean) {
        if (isHidden) hidden.add(player) else hidden.remove(player)

        refresh(player)
    }

    fun isHidden(player: Player?): Boolean = hidden.contains(player)

    fun refreshAll() {
        for (player in entities.keys) refresh(player)
    }

    fun refresh(player: Player) {
        val entity = entities[player] ?: return
        entity.setText(build(player))

        val viewers = tracking.computeIfAbsent(player) { ConcurrentHashMap.newKeySet() }

        if (isHidden(player)) {
            viewers.forEach { send(it, entity.removePacket()) }
            viewers.clear()

            return
        }

        val shouldSee = mutableSetOf<Player>()
        val viewDistSq = Nameplates.instance.viewDist.toDouble().let { it * it } // is this like, faster?
        val playerLoc = player.location


        for (target in player.world.players) {
            if (target == player || target.location.distanceSquared(playerLoc) > viewDistSq) continue

            shouldSee.add(target)
        }

        shouldSee.forEach {
            if (viewers.add(it)) {
                send(it, entity.spawnPacket())
                send(it, entity.metadataPacket())
                send(it, entity.mountPacket(player))
            } else send(it, entity.metadataPacket())
        }

        viewers.removeAll {
            if (it !in shouldSee) {
                send(it, entity.removePacket())

                true // this means removed, btw
            } else {
                false
            }
        }
    }

    private fun build(player: Player): Component {
        val override = overrides[player]
        if (override != null) return mini.deserialize(override)

        val playerLines: MutableList<Component>? = lines[player]
        if (playerLines.isNullOrEmpty()) return player.displayName()

        val components: MutableList<Component?> = ArrayList()
        for (line in playerLines) components.add(line)

        return Component.join(JoinConfiguration.newlines(), components)
    }

    private fun send(viewer: Player, packet: Packet<*>?) {
        if (packet == null) return

        viewer.asNMS().connection.send(packet)
    }
}
