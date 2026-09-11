package me.lukiiy.nameplates

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import me.lukiiy.nameplates.Utils.asNMS
import net.kyori.adventure.text.Component as AdventureComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.Interaction
import org.bukkit.entity.Player

class PlateEntity(val owner: Player, lineIndex: Int, totalLines: Int) {
    private val handle = Interaction(EntityTypes.INTERACTION, owner.asNMS().level())

    init {
        handle.apply {
            width = 0.0f
            height = ((totalLines - 1 - lineIndex) * Nameplates.instance.lineGap).toFloat().coerceAtLeast(0.0f)

            isCustomNameVisible = true
            isInvulnerable = true
        }
    }

    val id: Int
        get() = handle.id

    fun setText(text: AdventureComponent) {
        val json = JsonParser.parseString(GsonComponentSerializer.gson().serialize(text))

        handle.customName = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow { IllegalStateException(it) }
    }

    fun sneak(isSneaking: Boolean) {
        handle.isShiftKeyDown = isSneaking
    }

    fun spawnPacket() = ClientboundAddEntityPacket(handle, 0, owner.asNMS().blockPosition())
    fun metadataPacket() = ClientboundSetEntityDataPacket(handle.id, handle.entityData.packDirty() ?: handle.entityData.packAll())
    fun removePacket() = ClientboundRemoveEntitiesPacket(handle.id)
}