package ir.syrent.velocityvanish.spigot.bridge

import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.velocity.bridge.Bridge
import ir.syrent.velocityvanish.velocity.vruom.utils.GsonUtils
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
class BukkitBridgeManager(
    val bridge: Bridge,
    private val plugin: VelocityVanishSpigot
) {

    fun updateVanishedPlayersRequest(sender: Player, vanished: Boolean) {
        if(plugin.config.getBoolean("disable-velocity-messaging")){
            return;
        }
        val messageJson = JsonObject()
        messageJson.addProperty("type", "Vanish")
        messageJson.addProperty("name", sender.name)
        messageJson.addProperty("vanished", vanished)

        sendPluginMessage(sender, messageJson)
    }

    private fun sendPluginMessage(sender: Player, messageJson: JsonObject) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(sender, byteArrayInputStream.toByteArray())
    }

    fun handleMessage(messageJson: JsonObject) {
        when (val type = messageJson["type"].asString) {
            "Vanish" -> {
                val vanishedPlayers = messageJson["vanished_players"].asJsonArray
                val vanishedPlayersOnline = messageJson["vanished_players_online"].asJsonArray

                plugin.vanishedNames.clear()
                plugin.vanishedNamesOnline.clear()

                plugin.vanishedNames.addAll(vanishedPlayers.map { it.asString })
                plugin.vanishedNamesOnline.addAll(vanishedPlayersOnline.map { it.asString })
            }
            "Players" -> {
                val servers = messageJson["servers"].asJsonObject
                plugin.proxyPlayers.clear()
                for (server in servers.keySet()) {
                    plugin.proxyPlayers[server] = servers[server].asJsonArray.map { it.asString }.toList()
                }
            }
            else -> {
                Ruom.warn("Unsupported plugin message received from internal channel: $type")
            }
        }
    }

}