package com.kruthers.datapackmanager.actions

import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.utils.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.lang.Exception
import java.util.logging.Logger

class Pull(private val player: Player, private val plugin: DatapackManager): GitAction, BukkitRunnable() {
    private val folder: File = File(plugin.config.getString("datapack_world")+"/datapacks/.git")

    private val builder: FileRepositoryBuilder = FileRepositoryBuilder()
    private val repo: Repository = builder.setGitDir(folder).readEnvironment().findGitDir().build()
    private var command: PullCommand = Git(repo).pull()


    override fun run() {
        if (DatapackManager.setup) {
//            repo.merge().setSquash()

            val log: Logger = plugin.logger;
            log.info("${player.name}Is updating the datapack folder")
            player.sendMessage("${ChatColor.GREEN}Starting cloning process... Clearing datapack folder")
            Bukkit.broadcast(parse(""+plugin.config.get("messages.pull"),player),"datapackmanager.notify")

            try {
                command.call()
                log.info("Successfully updated the datapacks folder")
                player.sendMessage("${ChatColor.GREEN}Successfully pulled datapacks, reloading...")
                Bukkit.getServer().reloadData()

            } catch (e: Exception) {
                log.severe("Exception occurred when pulling from git:")
                e.printStackTrace()
                player.sendMessage("${ChatColor.RED}An exception occurred while pulling the update: ${e.localizedMessage}. Check the console for more info")
            }

        } else {
            player.sendMessage("${ChatColor.RED}Failed to pull update as there is no repo yet")
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