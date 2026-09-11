package me.lukiiy.nameplates

import me.lukiiy.nameplates.Utils.asNMS
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.protocol.Packet
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

class NametagManager {
    private val mini: MiniMessage = MiniMessage.miniMessage()

    private val entities: MutableMap<Player, MutableList<PlateEntity>> = ConcurrentHashMap()
    private val tracking: MutableMap<Player, MutableSet<Player>> = ConcurrentHashMap()

    private val lines: MutableMap<Player, List<Component>> = ConcurrentHashMap()
    private val hidden: MutableSet<Player> = CopyOnWriteArraySet()

    fun register(player: Player) {
        entities.computeIfAbsent(player) { mutableListOf() }
        tracking.putIfAbsent(player, ConcurrentHashMap.newKeySet())

        player.scheduler.runAtFixedRate(Nameplates.instance, { _ -> refresh(player) }, null, 1, Nameplates.instance.updateTicks)
    }

    fun unregister(player: Player?) {
        val p = player ?: return

        val group = entities.remove(p)
        val viewers = tracking.remove(p)

        if (group != null && viewers != null) for (viewer in viewers) group.forEach { send(viewer, it.removePacket()) }

        lines.remove(p)
        hidden.remove(p)
    }

    fun setLines(player: Player, ordered: List<Component>?) {
        if (ordered.isNullOrEmpty()) lines.remove(player) else lines[player] = ordered

        refresh(player)
    }

    fun setHidden(player: Player, isHidden: Boolean) {
        if (isHidden) hidden.add(player) else hidden.remove(player)

        refresh(player)
    }

    fun isHidden(player: Player?): Boolean = hidden.contains(player)

    fun updateSneak(player: Player, sneaking: Boolean) {
        val group = entities[player] ?: return
        val viewers = tracking[player] ?: return

        group.forEach {
            it.sneak(sneaking)

            val packet = it.metadataPacket()

            viewers.forEach { viewer -> send(viewer, packet) }
        }
    }

    fun refreshAll() {
        for (player in entities.keys) refresh(player)
    }

    fun refresh(player: Player) {
        val group = entities.getOrPut(player) { mutableListOf() }
        val viewers = tracking.computeIfAbsent(player) { ConcurrentHashMap.newKeySet() }
        val parts = build(player)

        val totalLines = parts.size

        if (group.size != totalLines) {
            viewers.forEach { v -> group.forEach { send(v, it.removePacket()) } }
            group.clear()

            for (i in parts.indices) {
                group.add(PlateEntity(player, i, totalLines))
            }
        }

        val lastIdx = group.size - 1
        for (i in group.indices) {
            group[i].setText(parts[i])
            group[i].sneak(player.isSneaking)
        }

        if (isHidden(player)) {
            viewers.forEach { v -> group.forEach { send(v, it.removePacket()) } }
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

        val allIds = group.map { it.id }.toIntArray()
        val ownerId = player.asNMS().id

        shouldSee.forEach { viewer ->
            if (viewers.add(viewer)) { group.forEach { send(viewer, it.spawnPacket()) } }

            group.forEach { send(viewer, it.metadataPacket()) }

            send(viewer, PassengerPacketAccessor.build(ownerId, allIds))
        }

        viewers.removeAll { v ->
            if (v !in shouldSee) {
                group.forEach { send(v, it.removePacket()) }

                true // this means removed, btw
            } else {
                false
            }
        }
    }

    private fun offsetFor(index: Int, lastIndex: Int): Float = (Nameplates.instance.verticalOffset + (lastIndex - index) * Nameplates.instance.lineGap).toFloat()

    private fun build(player: Player): List<Component> = lines[player] ?: listOf(player.displayName())

    private fun send(viewer: Player, packet: Packet<*>?) {
        if (packet == null) return

        viewer.asNMS().connection.send(packet)
    }
}