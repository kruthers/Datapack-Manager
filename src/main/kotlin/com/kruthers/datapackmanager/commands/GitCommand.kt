package com.kruthers.datapackmanager.commands

import cloud.commandframework.annotations.*
import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.actions.Clone
import com.kruthers.datapackmanager.actions.Pull
import com.kruthers.datapackmanager.utils.AuthType
import com.kruthers.datapackmanager.utils.MultipleAuthType
import com.kruthers.datapackmanager.utils.NoActionFound
import com.kruthers.datapackmanager.utils.parse
import org.bukkit.command.CommandSender

class GitCommand(pl: DatapackManager) {
    private val plugin: DatapackManager = pl

    @CommandMethod("git pull")
    @CommandDescription("Pull data from the github")
    @CommandPermission("datapackmanager.git")
    fun onGitPullCommand(sender: CommandSender, @Flag("force", description = "Will force from master", aliases = ["f"]) force: Boolean) {
        val command = Pull(sender, this.plugin, force)
        command.trigger()
    }

    @CommandMethod("git clone <repo> [branch]")
    @CommandDescription("Clone the data from github")
    @CommandPermission("datapackmanager.git.setup")
    fun onCloneCommand(
        sender: CommandSender,
        @Argument("repo") repo: String,
        @Argument("branch", defaultValue = "") branch: String,
        @Flag("authenticate", description = "Authenticates with username/ password (from config)", aliases = ["a","p"]) authenticate: Boolean,
        @Flag("ssh", description = "Authenticates with ssh", aliases = ["s"] ) ssh: Boolean,
        @Flag("token", description = "Authenticates with github token", aliases = ["t"]) token: Boolean,
    ) {
        if ((authenticate || ssh) && (authenticate || token) && (ssh || token)) throw MultipleAuthType()

        var authType: AuthType = AuthType.NONE
        if (authenticate) {
            authType = AuthType.LOGIN
        } else if (ssh) {
            authType = AuthType.SSH
        } else if (token) {
            authType = AuthType.TOKEN
        }

        val command = Clone(sender, plugin, repo, branch, authType)

        DatapackManager.confirmation.set(sender,command)
        sender.sendMessage(parse("<green>Git clone setup for ${repo}. Run '/git confirm' to start \n</green>"+
                "<red>Warning running this will clear your current datapack folder!"))

    }

    @CommandMethod("git confirm")
    @CommandDescription("Confirm a github action")
    @CommandPermission("datapackmanager.git")
    fun onGitConfirmCommand(sender: CommandSender) {
        if (DatapackManager.confirmation.containsKey(sender)) {
            val action = DatapackManager.confirmation[sender]
            action!!.trigger()

            DatapackManager.confirmation.remove(sender)

        } else {
            throw NoActionFound()
        }
    }
}