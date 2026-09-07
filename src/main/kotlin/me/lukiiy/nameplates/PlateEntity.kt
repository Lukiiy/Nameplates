package me.lukiiy.nameplates

import com.google.gson.JsonParser
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
import net.minecraft.world.entity.EntityType
import org.bukkit.entity.Player
import java.lang.reflect.Method

class PlateEntity(owner: Player) {
    private val handle: Display.TextDisplay = Display.TextDisplay(EntityType.TEXT_DISPLAY, owner.asNMS().level())

    init {
        handle.apply {
            billboardConstraints = Display.BillboardConstraints.CENTER
            shadowStrength = 0f
            viewRange = 1f
            transformationInterpolationDuration = 0

            lineWidthSetter?.invoke(this, 256)
            backgroundColorSetter?.invoke(this, 0)
        }
    }

    val id: Int
        get() = handle.id

    fun setText(text: AdventureComponent) {
        val json = JsonParser.parseString(GsonComponentSerializer.gson().serialize(text))

        handle.text = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow { IllegalStateException(it) }
    }

    fun spawnPacket(): Packet<*> = ClientboundAddEntityPacket(handle, 0, handle.blockPosition())
    fun mountPacket(owner: Player): Packet<*>? = PassengerPacketAccessor.build(owner.asNMS().id, intArrayOf(handle.id))
    fun metadataPacket(): Packet<*> = ClientboundSetEntityDataPacket(handle.id, handle.entityData.packDirty() ?: handle.entityData.packAll())
    fun removePacket(): Packet<*> = ClientboundRemoveEntitiesPacket(handle.id)

    private companion object {
        val lineWidthSetter: Method? = findInt2Set("setLineWidth")
        val backgroundColorSetter: Method? = findInt2Set("setBackgroundColor")

        private fun findInt2Set(name: String): Method? = try { // reflectionsss oh boy
            Display.TextDisplay::class.java.getDeclaredMethod(name, Int::class.javaPrimitiveType).apply { isAccessible = true }
        } catch (_: NoSuchMethodException) {
            null
        }
    }
}