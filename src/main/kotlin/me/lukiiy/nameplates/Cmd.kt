package me.lukiiy.nameplates

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player


object Cmd {
    private val main = Commands.literal("nameplates").requires { it.sender.hasPermission("nameplates.cmd") }

    private val reload = Commands.literal("reload").executes {
        Nameplates.instance.apply {
            reloadConfig()
            reloadComms()
        }

        it.source.sender.sendMessage(Component.text("Nameplates reloaded!").color(NamedTextColor.GREEN))
        Command.SINGLE_SUCCESS
    }

    private val toggle = Commands.literal("toggle").then(Commands.argument("player", ArgumentTypes.player()).executes {
        val target: Player = it.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(it.getSource()).first()
        val state = !Nameplates.instance.manager.isHidden(target)

        Nameplates.instance.manager.setHidden(target, state)
        it.source.sender.sendMessage(Component.text("Toggled ${if (state) "off" else "on" } ").append(target.displayName()).append(Component.text("'s nametag.")))

        Command.SINGLE_SUCCESS
    })

    // self

    fun register(): LiteralCommandNode<CommandSourceStack> = main.then(reload).then(toggle).build()
}