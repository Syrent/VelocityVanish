package ir.syrent.velocityvanish.spigot.command.vanish

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.command.library.SubCommand
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.utils.sendMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OffSubcommand(
    private val plugin: VelocityVanishSpigot
) : SubCommand("off", "velocityvanish.command.vanish", true) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        val player = sender as? Player ?: return

        plugin.vanishManager.unVanish(player, false)
        if (plugin.vanishedNames.contains(player.name)) {
            player.sendMessage(Message.VANISH_USE_UNVANISH)
        }
    }
}