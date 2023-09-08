package com.kruthers.datapackmanager.actions

import com.kruthers.datapackmanager.DatapackManager
import com.kruthers.datapackmanager.utils.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.InvalidRemoteException
import java.io.File
import java.util.logging.Logger

class Clone(private val sender: CommandSender, private val plugin: DatapackManager, val repo: String, val branch: String, val auth: AuthType): BukkitRunnable(), GitAction {
    private val command: CloneCommand = Git.cloneRepository()
    private val folder: File = File(plugin.config.getString("datapack_world")+"/datapacks")

    init {
        command.setDirectory(folder)
        if (branch != "") {
            command.setBranch(branch)
        }
        command.setCloneSubmodules(true)
        command.setURI(repo)
    }

    override fun run() {
        if (checkForDatapackFolder(plugin)) {
            val log: Logger = plugin.logger
            log.info("${sender.name}Is cloning the repository")
            log.info("Clearing the datapacks folder")
            sender.sendMessage(parse("<red>Starting cloning process... Clearing datapack folder"))

            //clear datapacks folder
            folder.listFiles()?.forEach {
                log.info("Removing datapack: ${it.name}")
                deleteFile(it.path,plugin,false)
            }

            //files cleared, starting clone
            log.info("Datapack folder successfully cleared starting clone")
            sender.sendMessage(parse("<green>Folder cleared, cloning from $repo"))
            Bukkit.broadcast(parse(""+plugin.config.get("messages.setup"),sender),"datapackmanager.notify")
            try {
                command.call()
            } catch (e: GitAPIException) {
                sender.sendMessage(parse("<red>Failed to clone from github, and API exception occurred, check the console for more details"))
                log.warning("Failed to complete clone, a git api exception occurred: \n${e.stackTraceToString()}\"")
                return
            } catch (e: InvalidRemoteException) {
                sender.sendMessage(parse("<red>Failed to clone from github, invalid link given, check the console for more details."))
                log.warning("Failed to complete clone, an invalid remove exception occurred: \n${e.message}")
                return
            } catch (e: NotSshRemote) {
                sender.sendMessage(parse("<red>Failed to clone from github, invalid link: The provided git remote is not a ssh clone remote. These start with git@ not https://"))
                log.warning("Failed to complete clone, the provided remote was no an ssh remove")
                return
            } catch (e: Exception) {
                sender.sendMessage(parse("<red>Failed to clone from github, a unknown Exception occurred, check the console for more details."))
                log.warning("Failed to complete clone, an unknown exception occurred: \n${e.stackTraceToString()}")
                return
            }

            log.info("Datapack folder successfully cloned, updating repo config")

            log.info("Local repo config fully set, reloading datapacks")
            sender.sendMessage(parse("<green>Datapacks cloned. Reloading"))


            DatapackManager.setup = true
            Bukkit.getServer().reloadData()


        } else {
            sender.sendMessage(parse("<red>The world folder provided does not contain the datapack folder. Please get a admin to check the config."))
        }

    }

    override fun trigger() {
        when (auth) {
            AuthType.LOGIN -> this.command.setCredentialsProvider(getUsernamePasswordAuth(plugin))
            AuthType.SSH -> this.command.setTransportConfigCallback(SshCallback(plugin))
            AuthType.TOKEN -> this.command.setCredentialsProvider(getTokenAuth(plugin))
            else -> {}
        }

        saveData(repo,branch,auth,plugin)
        this.runTaskAsynchronously(plugin)
        super.trigger()
    }

}