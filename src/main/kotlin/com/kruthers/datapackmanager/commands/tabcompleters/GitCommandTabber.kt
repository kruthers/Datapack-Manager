package com.kruthers.datapackmanager.commands.tabcompleters

import com.kruthers.datapackmanager.DatapackManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class GitCommandTabber: TabCompleter {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        val options: MutableList<String> = ArrayList();
        when (args.size) {
            0 -> {
                if (sender.hasPermission("datapackmanager.git.setup")) {
                    options.add("clone")
                }
                if (DatapackManager.confirmation.containsKey(sender)) {
                    options.add("confirm")
                }

                options.add("pull")
            }
            1 -> {
                val text: String = args[0]
                if (sender.hasPermission("datapackmanager.git.setup") && "clone".contains(text, ignoreCase = false)) {
                    options.add("clone")
                }
                if ("pull".contains(text)) {
                    options.add("pull")
                }
                if (DatapackManager.confirmation.containsKey(sender) && "confirm".contains(text)) {
                    options.add("confirm")
                }
            }
        }

        return options;
    }
}