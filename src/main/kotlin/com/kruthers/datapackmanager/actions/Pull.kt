package com.kruthers.datapackmanager.actions

import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.utils.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.util.logging.Logger

class Pull(private val sender: CommandSender, private val plugin: DatapackManager): GitAction, BukkitRunnable() {
    private val folder: File = File(plugin.config.getString("datapack_world")+"/datapacks/.git")

    private val builder: FileRepositoryBuilder = FileRepositoryBuilder()
    private val repo: Repository = builder.setGitDir(folder).readEnvironment().findGitDir().build()
    private var command: PullCommand = Git(repo).pull()


    override fun run() {
        if (DatapackManager.setup) {
//            repo.merge().setSquash()

            val log: Logger = plugin.logger;
            log.info("${sender.name}Is updating the datapack folder")
//            sender.sendMessage("${ChatColor.GREEN}Starting cloning process... Clearing datapack folder")
            Bukkit.broadcast(parse(""+plugin.config.get("messages.pull"),sender),"datapackmanager.notify")

            try {
                command.call()
                log.info("Successfully updated the datapacks folder")
                sender.sendMessage("${ChatColor.GREEN}Successfully pulled datapacks, reloading...")
                Bukkit.getServer().reloadData()

            } catch (e: Exception) {
                log.severe("Exception occurred when pulling from git:")
                e.printStackTrace()
                sender.sendMessage("${ChatColor.RED}An exception occurred while pulling the update: ${e.localizedMessage}. Check the console for more info")
            }

        } else {
            sender.sendMessage("${ChatColor.RED}Failed to pull update as there is no repo yet")
        }
    }

    override fun trigger() {
        val authType: AuthType = getAuthMethod(plugin)

        when (authType) {
            AuthType.LOGIN -> this.command.setCredentialsProvider(getUsernamePasswordAuth(plugin))
            AuthType.SSH -> this.command.setTransportConfigCallback(SshCallback(plugin))
            AuthType.Token -> this.command.setCredentialsProvider(getTokenAuth(plugin))
        }

        this.runTaskAsynchronously(plugin)
    }

    override fun triggerWithAuth(auth: UsernamePasswordCredentialsProvider) {
        command.setCredentialsProvider(auth)
        this.runTaskAsynchronously(plugin)

        super.triggerWithAuth(auth)
    }
}