package me.lukiiy.nameplates

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket

object PassengerPacketAccessor {
    private val constructor = ClientboundSetPassengersPacket::class.java.getDeclaredConstructor(FriendlyByteBuf::class.java).apply { isAccessible = true }

    fun build(vehicleId: Int, passengerIds: IntArray): ClientboundSetPassengersPacket {
        val byteBuf = FriendlyByteBuf(Unpooled.buffer())

        byteBuf.writeVarInt(vehicleId)
        byteBuf.writeVarIntArray(passengerIds)

        return constructor.newInstance(byteBuf)
    }
}