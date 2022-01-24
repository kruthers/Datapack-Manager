package com.kruthers.datapackmanager.commands

import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.actions.Clone
import com.kruthers.datapackmanager.actions.Pull
import com.kruthers.datapackmanager.utils.AuthType
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Git(var plugin: DatapackManager): CommandExecutor {
    private val branchArgs:MutableSet<String> = mutableSetOf("branch","b")
    private val authArgs:MutableSet<String> = mutableSetOf("a","as","at");


    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            if (DatapackManager.setup) {
                sender.sendMessage("The Datapack repo is setup")
            } else {
                sender.sendMessage("That datapack repo is not yet setup, setup with /github clone <repo> <args>")
            }
        } else {
            var action: String = args[0]
            when(action) {
                "pull" -> {
                    if (args.size > 1) {
                        sender.sendMessage("${ChatColor.RED}Invalid usage, a repository is required, /git pull")
                        return true;
                    }

                    val player: Player = sender as Player;
                    val command = Pull(player,plugin)
                    command.trigger()

                }
                "clone" -> {
                    if (args.size < 2) {
                        sender.sendMessage("${ChatColor.RED}Invalid usage, a repository is required, /git clone <repo> <args...>\n"+
                                "- Valid args: \n"+
                                " | -a Authenticate with a username and password from config.yml"+
                                " | -as Authenticate with ssh"+
                                " | -at Authenticate with a token"+
                                " | -b <branch> Set a branch other then master"
                        )
                        return true;
                    }
                    if (!sender.hasPermission("datapackmanager.git.setup")) {
                        sender.sendMessage("${ChatColor.RED}Sorry you dont have permission todo this")
                        return true;
                    }

                    val player: Player = sender as Player;
                    val repo: String = args[1]
                    var branch: String = "";
                    var auth: AuthType = AuthType.NONE;

                    var i: Int = 2;
                    while (i < args.size) {
                        var arg = args[i].toLowerCase()
                        if (arg.startsWith("-")) {
                            arg = arg.removePrefix("-")
                            if (branchArgs.contains(arg)) {
                                if (i + 1 >= args.size) {
                                    sender.sendMessage("${ChatColor.RED}Branch argument given but no branch supplied")
                                    return true;
                                }
                                branch = args[i + 1]
                                i++ // skip foward one

                            } else if (authArgs.contains(arg)) {
                                auth = when(arg) {
                                    "as" -> AuthType.SSH
                                    "at" -> AuthType.Token
                                    else -> AuthType.LOGIN
                                }
                            }
                        } else {
                            sender.sendMessage("${ChatColor.RED}Unknown argument given $arg")
                            return true;
                        }


                        i++ //step
                    }

                    val command = Clone(player, plugin, repo, branch, auth)

                    DatapackManager.confirmation.set(sender,command)
                    player.sendMessage("${ChatColor.GREEN}Gitclone setup for ${repo}. Run '/git confirm' to start \n"+
                            "${ChatColor.RED}Warning running this will clear your current datapack folder!")


                }
                "confirm" -> {
                    if (DatapackManager.confirmation.containsKey(sender)) {
                        val action = DatapackManager.confirmation.get(sender)
                        action!!.trigger()

                        DatapackManager.confirmation.remove(sender);

                    } else {
                        sender.sendMessage(""+ChatColor.RED+"You have no actions await confirmation")
                    }
                }
                else -> {
                    sender.sendMessage(""+ ChatColor.RED+"Invalid usage: /git <action> ...")
                }
            }
        }

        return true;
    }

}