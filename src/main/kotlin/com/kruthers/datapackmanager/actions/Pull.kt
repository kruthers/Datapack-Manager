package com.kruthers.datapackmanager.actions

import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.utils.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.SubmoduleConfig
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.util.logging.Logger

class Pull(private val sender: CommandSender, private val plugin: DatapackManager, private val force: Boolean): GitAction, BukkitRunnable() {
    private val folder: File = File(plugin.config.getString("datapack_world")+"/datapacks/.git")

    private val builder: FileRepositoryBuilder = FileRepositoryBuilder()
    private val repo: Repository = builder.setGitDir(folder).readEnvironment().findGitDir().build()
    private var command: PullCommand = Git(repo).pull()

    init {
        //configure command
        this.command.setRecurseSubmodules(SubmoduleConfig.FetchRecurseSubmodulesMode.ON_DEMAND)
        this.command.setFastForward(MergeCommand.FastForwardMode.FF)
    }


    override fun run() {
        if (DatapackManager.setup) {

            val log: Logger = plugin.logger
            log.info("${sender.name}Is updating the datapack folder")
            Bukkit.broadcast(parse(""+plugin.config.get("messages.pull"),sender),"datapackmanager.notify")

            if (force) {
                try {
                    val resetCommand = ResetCommand(this.repo)
                    resetCommand.setMode(ResetCommand.ResetType.HARD)
                    resetCommand.call()
                } catch (e: Exception) {
                    log.severe("Exception occurred when resting repo from git:")
                    e.printStackTrace()
                    sender.sendMessage(parse("An exception occurred while resetting the repo: ${e.localizedMessage}. Check the console for more info"))
                }
            }

            try {
                command.call()
                log.info("Successfully updated the datapacks folder")
                sender.sendMessage(Component.text("Successfully pulled datapacks, reloading...", NamedTextColor.GREEN))
                Bukkit.getServer().reloadData()

            } catch (e: Exception) {
                log.severe("Exception occurred when pulling from git:")
                e.printStackTrace()
                sender.sendMessage(Component.text("An exception occurred while pulling the update: ", NamedTextColor.RED)
                    .append(Component.text(e.localizedMessage, NamedTextColor.GRAY, TextDecoration.ITALIC))
                    .append(Component.text(". Check the console for more info", NamedTextColor.RED))
                )
            }

        } else {
            sender.sendMessage(parse("<red>Action not be completed, the datapack folder has not set been setup."))
        }
    }

    override fun trigger() {
        val authType: AuthType = getAuthMethod(plugin)

        when (authType) {
            AuthType.LOGIN -> this.command.setCredentialsProvider(getUsernamePasswordAuth(plugin))
            AuthType.SSH -> this.command.setTransportConfigCallback(SshCallback(plugin))
            AuthType.TOKEN -> this.command.setCredentialsProvider(getTokenAuth(plugin))
            else -> {}
        }

        this.runTaskAsynchronously(plugin)
    }

    override fun triggerWithAuth(auth: UsernamePasswordCredentialsProvider) {
        command.setCredentialsProvider(auth)
        this.runTaskAsynchronously(plugin)

        super.triggerWithAuth(auth)
    }
}