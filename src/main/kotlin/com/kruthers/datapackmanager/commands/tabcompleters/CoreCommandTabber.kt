package com.kruthers.datapackmanager.commands.tabcompleters

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CoreCommandTabber: TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        val options: MutableList<String> = ArrayList();
        when (args.size) {
            0 -> {
                if (sender.hasPermission("datapackmanager.reload")) {
                    options.add("reload")
                }
                options.add("version")
            }
            1 -> {
                val text: String = args[0]
                if (sender.hasPermission("datapackmanager.reload") && "reload".contains(text, ignoreCase = false)) {
                    options.add("reload")
                }
                if ("version".contains(text)) {
                    options.add("version")
                }
            }
        }

        return options;
    }
}