package com.kruthers.datapackmanager.commands

import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.utils.parse
import com.kruthers.datapackmanager.utils.setupRepoChecks
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CoreCommand(var plugin: DatapackManager) : CommandExecutor {

    companion object {
        private var properties = DatapackManager.properties;

        //Standard return messages
        private var versionString: String = String.format("You are running Datapack Manager version %s",properties.getProperty("version"))
        private var usageString: String = String.format("%sInvalid usage given for command, correct usage: /datapackmanager <reload>",ChatColor.RED)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.size) {
            0 -> sender.sendMessage(versionString)
            1 -> {
                var argument: String = args[0]
                when (argument) {
                    "version" -> sender.sendMessage(versionString)
                    "reload" -> {
                        if (sender.hasPermission("datapackmanager.reload")) {
                            plugin.logger.info(sender.name+" Is reloading the plugin")
                            sender.sendMessage("Reloading Datapack Manager")
                            plugin.logger.info("Reloading config...")
                            plugin.reloadConfig();

                            //reset values
                            DatapackManager.setup = false;
                            DatapackManager.confirmation = HashMap();

                            //perform github checks again
                            setupRepoChecks(plugin)

                            sender.sendMessage("Reloaded Datapack Manager")
                            plugin.logger.info("Reloaded successfully")
                        } else {
                            sender.sendMessage(parse(plugin.config.getString("messages.perms")+"",sender))
                        }
                    }
                    else -> {
                        sender.sendMessage(usageString)
                    }
                }
            }
            else -> sender.sendMessage(usageString)
        }

        return true;
    }

}