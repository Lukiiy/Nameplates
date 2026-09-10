package me.lukiiy.nameplates

import com.google.gson.JsonParser
import com.mojang.math.Transformation
import com.mojang.serialization.JsonOps
import me.lukiiy.nameplates.Utils.asNMS
import net.kyori.adventure.text.Component as AdventureComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityTypes
import org.bukkit.entity.Player
import org.joml.Quaternionf
import org.joml.Vector3f

class PlateEntity(owner: Player) {
    private val handle: Display.TextDisplay = Display.TextDisplay(EntityTypes.TEXT_DISPLAY, owner.asNMS().level())

    init {
        handle.apply {
            setPos(owner.x, owner.y, owner.z)

            billboardConstraints = Display.BillboardConstraints.CENTER
            shadowStrength = 0f
            viewRange = 1f
            transformationInterpolationDuration = 0
        }
    }

    val id: Int
        get() = handle.id

    fun setText(text: AdventureComponent) {
        val json = JsonParser.parseString(GsonComponentSerializer.gson().serialize(text))

        handle.text = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow { IllegalStateException(it) }
    }

    fun setOffset(y: Float) = handle.setTransformation(Transformation(Vector3f(0f, y, 0f), Quaternionf(), Vector3f(1f, 1f, 1f), Quaternionf()))

    fun setSneak(sneaking: Boolean) {
        handle.textOpacity = if (sneaking) 0x59 else -1 // seems accurate
    }

    fun spawnPacket(): Packet<*> = ClientboundAddEntityPacket(handle, 0, handle.blockPosition())
    fun metadataPacket(): Packet<*> = ClientboundSetEntityDataPacket(handle.id, handle.entityData.packDirty() ?: handle.entityData.packAll())
    fun removePacket(): Packet<*> = ClientboundRemoveEntitiesPacket(handle.id)
}