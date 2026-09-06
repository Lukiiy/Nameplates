package me.lukiiy.nameplates

import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import java.lang.reflect.Constructor
import java.lang.reflect.Field

object PassengerPacketAccessor {
    private val passengersConstructor: Constructor<ClientboundSetPassengersPacket>? = runCatching {
        ClientboundSetPassengersPacket::class.java.getDeclaredConstructor().apply { isAccessible = true }
    }.getOrNull()

    private val vehicleField: Field?
    private val passengersField: Field?

    init {
        var vField: Field? = null
        var pField: Field? = null

        for (field in ClientboundSetPassengersPacket::class.java.declaredFields) {
            field.isAccessible = true
            when (field.type) {
                Int::class.javaPrimitiveType -> vField = field
                IntArray::class.java -> pField = field
            }
        }

        vehicleField = vField
        passengersField = pField
    }

    fun build(vehicleId: Int, passengerIds: IntArray?): ClientboundSetPassengersPacket? {
        if (passengersConstructor == null || vehicleField == null || passengersField == null) return null

        return runCatching {
            passengersConstructor!!.newInstance().apply {
                vehicleField!!.set(this, vehicleId)
                passengersField!!.set(this, passengerIds)
            }
        }.getOrNull()
    }
}